package dev.carbonshow.matchmaking.solver;

import com.google.ortools.sat.*;
import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.stream.Collectors;

/**
 * 基于 Constraint-Programming 从匹配池的所有单元中，寻找可用队伍的各种组合。处理方式符合一般的运筹学模型：
 * <ul>
 *     <li>确定决策变量，队伍中是否包含某个匹配单元</li>
 *     <li>确定约束条件：队伍总人数符合要求，队伍中同一个匹配单元最多只出现一次</li>
 *     <li>特别的地方在于，这里没有优化目标，只是找出可行解</li>
 * </ul>
 * <b>注意：计算开销较高</b>
 */
public class FeasibleTeamCPFinder implements FeasibleTeamFinder {
    private final MatchMakingCriteria matchMakingCriteria;
    private final MatchUnitOperator matchUnitOperator;
    private final SolverParameters solverParameters;

    public FeasibleTeamCPFinder(MatchMakingCriteria criteria, MatchUnitOperator operator, SolverParameters parameters) {
        matchMakingCriteria = criteria;
        matchUnitOperator = operator;
        solverParameters = parameters;
    }

    /**
     * 使用 Constraint-Programming 的方式找到有效的队伍。根据 units 数组，找到可行的队伍解，注意是：非全局视角，不同的可行解可能有冲突。
     * <ul>
     *     <li>队伍符合人数要求</li>
     *     <li>队伍内个匹配单元不能互斥</li>
     * </ul>
     *
     * @param units            匹配池中所有成员数组
     * @param currentTimestamp 当前时间戳，单位是秒
     * @return 返回可用队伍列表，队伍中所含单元在 units 中的索引集合，通过 BitSet 表示
     */
    @Override
    public ArrayList<FeasibleTeam> solve(MatchUnit[] units, long currentTimestamp) {
        // 定义模型
        CpModel model = new CpModel();

        final int matchUnitCount = units.length;
        final int userCountPerTeam = matchMakingCriteria.userCountPerTeam();

        // 决策变量：匹配单元分配到具体某个单局的某个队伍中
        Literal[] assignment = new Literal[matchUnitCount];
        for (int i = 0; i < matchUnitCount; i++) {
            assignment[i] = model.newBoolVar("mu" + units[i].matchUnitId());
        }

        // 如果一个队伍有效，那么内部所有队伍人数必须符合要求
        LinearExprBuilder userCount = LinearExpr.newBuilder();
        for (int i = 0; i < matchUnitCount; i++) {
            userCount.addTerm(assignment[i], units[i].userCount());
        }
        model.addEquality(userCount, userCountPerTeam);

        // 添加互斥条件
        for (int i = 0; i < matchUnitCount - 1; i++) {
            for (int j = i + 1; j < matchUnitCount; j++) {
                if (!matchUnitOperator.isFitOneTeam(units[i], units[j])) {
                    model.addAtMostOne(new Literal[]{assignment[i], assignment[j]});
                }
            }
        }

        // 求解
        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(solverParameters.maxSolveTimeInSeconds());
        solver.getParameters().setEnumerateAllSolutions(true);
        var cb = new TeamSolutionWithLimit(units, assignment, solverParameters.maxTeamCount());
        solver.solve(model, cb);

        return cb.getSolutions().stream()
                .map(unitMembers -> matchUnitOperator.mergeUnitsToTeam(units, unitMembers, currentTimestamp))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 使用 Constraint-Programming 方式收集可行解时的记录器，将有效的决策变量转换为可用的 Team 列表
     */
    private static class TeamSolutionWithLimit extends CpSolverSolutionCallback {

        public TeamSolutionWithLimit(MatchUnit[] units, Literal[] assignment, int limit) {
            this.units = units;
            this.assignment = assignment;
            solutionLimit = limit;
        }

        @Override
        public void onSolutionCallback() {
            final int userCount = assignment.length;

            BitSet solution = new BitSet(units.length);
            for (int i = 0; i < userCount; i++) {
                if (booleanValue(assignment[i])) {
                    solution.set(i);
                }
            }

            solutions.add(solution);

            // 如果已经满足搜索要求，那么终止
            if (solutionLimit > 0 && solutionLimit <= solutions.size()) {
                stopSearch();
            }
        }

        /**
         * 将求解器的解转化为符合匹配要求的格式
         */
        public ArrayList<BitSet> getSolutions() {
            return solutions;
        }

        // 池子内的匹配单元
        private final MatchUnit[] units;

        // 求解器决策变量
        private final Literal[] assignment;

        // 求解数量上线
        private final int solutionLimit;

        // 记录可用分配方案
        // - 一维是不同方案
        // - 二维是不同方案中所含 unit index，index 必然是从 0 开始计数，是稠密的，可以用 BitSet 表示
        private final ArrayList<BitSet> solutions = new ArrayList<>();
    }

}
