package dev.carbonshow.matchmaking.solver;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.config.TimeVaryingConfig;
import dev.carbonshow.matchmaking.pool.MatchMakingPool;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.*;

/**
 * 基于 Constraint-Programming Satisfy 的匹配优化器。求解过程分为多个阶段：
 * <ol>
 *     <li>基于人数，确定单局内人数达标前提下，每个比赛方可以由不同小队组合的方式。比如 5v5，一方 5 人，可以由两个 1 人小队和一个 3 人小队组成</li>
 *     <li>针对组合方式，找到当前匹配池中的可行解，即一个有效单局，具体由哪几个小队组成，能满足各种约束条件</li>
 *     <li>针对所有可行解，结合优化目标进行最优化选择，挑出最终的最佳匹配结果</li>
 * </ol>
 * 为什么要分为多个步骤呢？不能使用 CP 一次性全部解决吗？
 * <ul>
 *     <li>CP 的约束条件种类有限，主要面向线性运算、逻辑运算等，不能表达所有匹配约束</li>
 *     <li>问题规模越大，使用 CP 的开销越高，比如 100 个匹配单元在只考虑人数约束情况下，大概需要 28s。为此考虑：
 *         <ul>
 *             <li>不要寻找最优解，而是次优解。通过启发式算法处理，比如 Greedy 或者 遗传算法等</li>
 *             <li>问题拆解，将大问题拆分为小的子问题。这是本方案采取的方式</li>
 *         </ul>
 *     </li>
 * </ul>
 */
public class MatchMakingDecomposeSolver implements MatchMakingSolver {
    private final String name;

    private final FeasibleTeamFinder teamFinder;
    private final FeasibleGameFinder gameFinder;
    private final TimeVaryingConfig timeVaryingConfig;

    public MatchMakingDecomposeSolver(MatchMakingCriteria criteria, String name, TimeVaryingConfig timeVaryingConfig) {
        this.name = name;
        this.timeVaryingConfig = timeVaryingConfig;
        var operator = new DefaultMatchUnitOperator(criteria, timeVaryingConfig);
        teamFinder = new FeasibleTeamDPFinder(criteria, operator);
        gameFinder = new FeasibleGameBacktraceFinder(criteria, operator);
        //gameFinder = new FeasibleGameCPFinder(this.criteria, operator);

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
     * @param pool             匹配池，包含所有匹配单元的基础数据
     * @param parameters       求解器配置参数
     * @param currentTimestamp 当前时间戳，单位是秒
     * @return 符合要求的匹配结果
     */
    @Override
    public MatchMakingResults solve(MatchMakingPool pool, SolverParameters parameters, long currentTimestamp) {
        // 获取匹配单元数组，并按照成员数量升序排列
        final MatchUnit[] units = pool.matchUnits();
        for (MatchUnit unit : units) {
            unit.timeVaryingParameters().update(currentTimestamp, timeVaryingConfig);
        }
        Arrays.sort(units, Comparator.comparingInt(MatchUnit::userCount));

        // 先找到可行队伍解
        long start = System.currentTimeMillis();
        var feasibleTeams = teamFinder.solve(units, currentTimestamp);
        System.out.println("[Feasible Teams] " + feasibleTeams.size() + ", Time: " + (System.currentTimeMillis() - start));

        // 基于可行队伍找到可行单局
        start = System.currentTimeMillis();
        var feasibleGames = gameFinder.solve(units, feasibleTeams, parameters, currentTimestamp);
        System.out.println("[Feasible Games] " + feasibleGames.size() + ", Time: " + (System.currentTimeMillis() - start));

        // 基于可行单局寻找最终解
        start = System.currentTimeMillis();
        var result = getFinalResult(units, feasibleGames);
        if (result != null) {
            System.out.println("[Results] " + result.results().size() + ", Time: " + (System.currentTimeMillis() - start));
        } else {
            System.out.println("No solution");
        }

        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    private MatchMakingResults getFinalResult(MatchUnit[] units, List<FeasibleGame> games) {
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return null;
        }

        // 确定哪个方案最终被选择
        final var assigns = solver.makeBoolVarArray(games.size(), "valid");

        // 约束条件是每个匹配单元最多只出现一次
        MPConstraint[] constraints = new MPConstraint[units.length];
        for (int i = 0; i < units.length; i++) {
            constraints[i] = solver.makeConstraint(0, 1);
        }
        int j = 0;
        for (var game : games) {
            final var assign = assigns[j];
            game.getMembers().stream().forEach(idx -> constraints[idx].setCoefficient(assign, 1));
            j++;
        }

        // 优化目标是可用单局越多越好
        MPObjective objective = solver.objective();
        for (var assign: assigns) {
            objective.setCoefficient(assign, 1);
        }
        objective.setMaximization();

        // 求解
        final MPSolver.ResultStatus status = solver.solve();
        List<List<List<Long>>> results = new ArrayList<>();
        if (status == MPSolver.ResultStatus.OPTIMAL) {
            j = 0;
            for (var game: games) {
                if (assigns[j].solutionValue() > 0.0) {
                    results.add(game.toRaw(units));
                }
                j++;
            }
            return new MatchMakingResults(results);
        } else {
            System.err.println("The problem does not have an optimal solution!");
            return null;
        }
    }
}
