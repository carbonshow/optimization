package dev.carbonshow.matchmaking.solver;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import dev.carbonshow.algorithm.partition.DPIntegerPartition;
import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.config.TimeVaryingConfig;
import dev.carbonshow.matchmaking.pool.MatchMakingPool;
import dev.carbonshow.matchmaking.MatchMakingResults;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

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
public class MatchMakingCPSolver implements MatchMakingSolver {
    private final MatchMakingCriteria criteria;
    private final String name;
    private final TimeVaryingConfig timeVaryingConfig;

    // 存储不同的划分方案，每个方案使用 Map 表示，key 表示 Unit人数，value 表示需要的该人数对应小队的数量
    private List<Map<Long, Long>> unitMemberCountPartitions;

    public MatchMakingCPSolver(MatchMakingCriteria criteria, String name, TimeVaryingConfig timeVaryingConfig) {
        this.criteria = criteria;
        this.name = name;
        this.timeVaryingConfig = timeVaryingConfig;
        Loader.loadNativeLibraries();

        // 更新用户数量划分方案，后续不必重复计算
        updateUserCountPartitions();
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
     * @param pool       匹配池，包含所有匹配单元的基础数据
     * @param parameters 求解器配置参数
     * @param currentTimestamp 当前时间戳，单位是秒
     * @return 符合要求的匹配结果
     */
    @Override
    public MatchMakingResults solve(MatchMakingPool pool, SolverParameters parameters, long currentTimestamp) {
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
            // 更新当前 Unit 的时变参数
            units[i].timeVaryingParameters().update(currentTimestamp, timeVaryingConfig);

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
                //gameUserCount.add(teamUserCount);
                model.addEquality(teamUserCount, userCountPerTeam);
            }
            //model.addEquality(gameUserCount, userCountPerGame);
        }

        // 每个匹配单元最多只能被分配到一个单局的一个队伍中
        for (int i = 0; i < matchUnitCount; i++) {
            ArrayList<Literal> unitsLiteral = new ArrayList<>();
            for (int j = 0; j < maxGameCount; j++) {
                unitsLiteral.addAll(Arrays.asList(assignment[i][j]).subList(0, teamCountPerGame));
            }
            model.addAtMostOne(unitsLiteral);
        }

        // 计算互斥关系
//        var mutableExclusiveUnits = pool.getMutableExclusiveMatchUnits(units);
//        for (int j = 0; j < maxGameCount; j++) {
//            for (int k = 0; k < teamCountPerGame; k++) {
//
//            }
//        }

        // 求解
        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(parameters.maxSolveTimeInSeconds());
        solver.getParameters().setEnumerateAllSolutions(true);
        MatchMakingSolutionWithLimit cb = new MatchMakingSolutionWithLimit(units, assignment, parameters.maxGameCount());
        solver.solve(model, cb);

        return cb.getSolutions();
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 找到可用解后，分析各变量数值，转化为业务层可用格式
     */
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
            if (solutionLimit > 0 && solutionLimit <= solutions.size()) {
                stopSearch();
            }
        }

        /**
         * 将求解器的解转化为符合匹配要求的格式
         */
        public MatchMakingResults getSolutions() {
            var results = solutions.values().stream()
                    .map(game -> game.values().stream()
                            .map(teams -> teams.stream().toList())
                            .toList()
                    )
                    .toList();
            return new MatchMakingResults(results);
        }

        // 池子内的匹配单元
        private final MatchUnit[] units;

        // 求解器决策变量
        private final Literal[][][] assignment;

        // 求解数量上线
        private final int solutionLimit;

        // 记录可用分配方案
        // - Key 是 game index
        // - value 表示局内各个队伍的分配情况：
        //   - key 表示单局内队伍 index
        //   - value 是该队伍内各个 unit id 列表
        private final HashMap<Integer, HashMap<Integer, ArrayList<Long>>> solutions = new HashMap<>();
    }

    /**
     * 对人数进行划分，只要 criteria 确定就是固定不变的，不必每次都重复计算。比如单局内一个队伍必须有 N 个人，每个 MatchUnit 的人数可能是
     * [1, N]，作为加数。问题转化为将 N 用[1, N]的加数进行拆解，获得不同的拆解方案。这里使用默认的基于动态规划的求解器进行处理，效率很高。
     */
    private void updateUserCountPartitions() {
        var countPartitionSolver = new DPIntegerPartition();
        var partitions = countPartitionSolver.solveWithPartitions(LongStream.range(1, criteria.userCountPerTeam()).toArray(), criteria.userCountPerTeam());
        unitMemberCountPartitions = partitions.stream().map(partition ->
                        partition.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 根据 units 数组，找到可行的单局解，注意是：在独立的单局视角下可作为后选最终匹配结果；而非全局视角，不同的可行解可能有冲突。
     * <ul>
     *     <li>单局内各队伍符合人数要求</li>
     *     <li>单局内个匹配单元不能互斥</li>
     *     <li>单个单局，如果不考虑最优化，可以进入比赛；多不保证全局最优，且同一个 unit 可能出现在多个单局中</li>
     * </ul>
     * 换句话，如果不考虑最优化，这些结果均可进入单局
     *
     * @param units 匹配单元数组
     */
    private void findFeasibleGames(MatchUnit[] units) {
    }

}