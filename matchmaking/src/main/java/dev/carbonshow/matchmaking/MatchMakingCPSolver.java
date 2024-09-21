package dev.carbonshow.matchmaking;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;

import java.util.*;

/**
 * 基于 Constraint-Programming Satisfy 的匹配优化器
 */
public class MatchMakingCPSolver implements MatchMakingSolver {

    MatchMakingCPSolver() {
        Loader.loadNativeLibraries();
    }

    /**
     * 使用 google OR-tools 中的 cp-sat 模型，来解决匹配优化问题
     * <ul>
     *    <li>决策变量
     *       <ul>
     *           <li>x[i][j][k]，第 i 个匹配单元在第 j 个单局第 k 个队伍中的分配标记，1 表示分配，0 表示未分配</li>
     *           <li>y[j]，第 j 个单局的分配是否有效</li>
     *       </ul>
     *    </li>
     *    <li>约束条件
     *       <ul>
     *           <li>单局内各队伍人数，符合要求</li>
     *           <li>每个匹配单元最多只能出现在一个单局中</li>
     *           <li>满足匹配的其它约束条件，比如段位、最近对手回避等</li>
     *       </ul>
     *    </li>
     *    <li>优化目标
     *       <ul>
     *           <li>匹配质量更高，包含多种因素，比如：网络延迟低、职业覆盖全等等</li>
     *       </ul>
     *    </li>
     * </ul>
     *
     * @param pool 匹配池，包含所有匹配单元的基础数据
     * @return 符合要求的匹配结果
     */
    @Override
    public MatchMakingResults solve(MatchMakingPool pool) {
        // 定义模型
        CpModel model = new CpModel();

        final MatchUnit[] units = pool.matchUnits();
        final int matchUnitCount = pool.matchUnitCount();
        final int maxGameCount = pool.maxGameCount();
        final int teamCountPerGame = pool.getCriteria().teamCountPerGame();
        final int userCountPerTeam = pool.getCriteria().userCountPerTeam();
        final int userCountPerGame = pool.getCriteria().userCountPerGame();

        // 决策变量：匹配单元分配到具体某个单局的某个队伍中
        Literal[][][] assignment = new Literal[matchUnitCount][maxGameCount][teamCountPerGame];
        for (int i = 0; i < matchUnitCount; i++) {
            for (int j = 0; j < maxGameCount; j++) {
                for (int k = 0; k < teamCountPerGame; k++) {
                    assignment[i][j][k] = model.newBoolVar("assignment" + units[i].matchUnitId() + "g" + j + "t" + k);
                }
            }
        }

        // 如果一个单局有效，那么内部所有队伍人数必须符合要求
        for (int j = 0; j < maxGameCount; j++) {
            LinearExprBuilder gameUserCount = LinearExpr.newBuilder();
            for (int k = 0; k < teamCountPerGame; k++) {
                LinearExprBuilder teamUserCount = LinearExpr.newBuilder();
                for (int i = 0; i < matchUnitCount; i++) {
                    teamUserCount.addTerm(assignment[i][j][k], units[i].userCount());
                }
                gameUserCount.add(teamUserCount);
                model.addEquality(teamUserCount, userCountPerTeam);
            }
            model.addEquality(gameUserCount, userCountPerGame);
        }

        // 每个匹配单元最多只能被分配到一个单局的一个队伍中
        for (int i = 0; i < matchUnitCount; i++) {
            ArrayList<Literal> unitsLiteral = new ArrayList<>();
            for (int j = 0; j < maxGameCount; j++) {
                unitsLiteral.addAll(Arrays.asList(assignment[i][j]).subList(0, teamCountPerGame));
            }
            model.addAtMostOne(unitsLiteral);
        }

        // 求解
        CpSolver solver = new CpSolver();
        solver.getParameters().setLinearizationLevel(0);
        solver.getParameters().setEnumerateAllSolutions(true);
        MatchMakingSolutionWithLimit cb = new MatchMakingSolutionWithLimit(units, assignment, 5);
        solver.solve(model, cb);

        return cb.getSolutions();
    }

    private static class MatchMakingSolutionWithLimit extends CpSolverSolutionCallback {

        public MatchMakingSolutionWithLimit(MatchUnit[] units, Literal[][][] assignment, int limit) {
            this.units = units;
            this.assignment = assignment;
            solutionLimit = limit;
        }

        @Override
        public void onSolutionCallback() {
            final int userCount = assignment.length;
            final int gameCount = assignment[0].length;
            final int teamCount = assignment[0][0].length;
            for (int i = 0; i < userCount; i++) {
                for (int j = 0; j < gameCount; j++) {
                    for (int k = 0; k < teamCount; k++) {
                        if (booleanValue(assignment[i][j][k])) {
                            if (solutions.containsKey(j)) {
                                if (solutions.get(j).containsKey(k)) {
                                    solutions.get(j).get(k).add(units[i].matchUnitId());
                                } else {
                                    solutions.get(j).put(k, new ArrayList<>(List.of(units[i].matchUnitId())));
                                }
                                solutions.get(j);
                            } else {
                                HashMap<Integer, ArrayList<Long>> teamAssignment = new HashMap<>();
                                teamAssignment.put(k, new ArrayList<>(List.of(units[i].matchUnitId())));
                                solutions.put(j, teamAssignment);
                            }
                        }
                    }
                }
            }

            // 如果已经满足搜索要求，那么终止
            if (solutionLimit <= solutions.size()) {
                stopSearch();
            }
        }

        public MatchMakingResults getSolutions() {
            var results = solutions.values().stream()
                    .map(game -> game.values().stream()
                            .map(teams -> teams.stream().toList())
                            .toList()
                    )
                    .toList();
            return new MatchMakingResults(results);
        }

        private final MatchUnit[] units;
        private final Literal[][][] assignment;
        private final int solutionLimit;
        // 记录可用分配方案
        // - Key 是 game index
        // - value 表示局内各个队伍的分配情况：
        //   - key 表示单局内队伍 index
        //   - value 是该队伍内各个 unit id 列表
        private final HashMap<Integer, HashMap<Integer, ArrayList<Long>>> solutions = new HashMap<>();
    }
}
