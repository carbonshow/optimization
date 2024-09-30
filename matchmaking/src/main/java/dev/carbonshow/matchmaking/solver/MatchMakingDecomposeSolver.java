package dev.carbonshow.matchmaking.solver;

import com.google.ortools.Loader;
import dev.carbonshow.matchmaking.MatchMakingResults;
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
    private final MatchMakingCriteria criteria;
    private final String name;

    private final FeasibleTeamFinder teamFinder;
    private final FeasibleGameFinder gameFinder;
    private final TimeVaryingConfig timeVaryingConfig;

    public MatchMakingDecomposeSolver(MatchMakingCriteria criteria, String name, TimeVaryingConfig timeVaryingConfig) {
        this.criteria = criteria;
        this.name = name;
        this.timeVaryingConfig = timeVaryingConfig;
        var operator = new DefaultMatchUnitOperator(this.criteria, timeVaryingConfig);
        teamFinder = new FeasibleTeamDPFinder(this.criteria, operator);
        gameFinder = new FeasibleGameCPFinder(this.criteria, operator);
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
        var start = System.currentTimeMillis();
        var feasibleTeams = teamFinder.solve(units, currentTimestamp);
        System.out.println("Find Feasible Teams: " + feasibleTeams.size() + ", Elapsed Time: " + (System.currentTimeMillis() - start));

        // 基于可行队伍找到可行单局
        start = System.currentTimeMillis();
        var feasibleGames = gameFinder.solve(units, feasibleTeams, parameters, currentTimestamp);
        System.out.println("Find Feasible Games: " + feasibleGames.size() + ", Elapsed Time: " + (System.currentTimeMillis() - start));

        // 从可行队伍解中，找到可行单局解
//        System.out.println("[Find Feasible Games]");
//        start = System.currentTimeMillis();
//        var feasibleGames = getFeasibleGames(units, feasibleTeams);
//        System.out.println("Feasible Game Count: " + feasibleGames.size() + ", Elapsed Time: " + (System.currentTimeMillis() - start));
//        int gameIndex = 1;
//        for (var game : feasibleGames) {
//            System.out.println("Game: " + gameIndex);
//            int teamIndex = 1;
//            for (var team: game) {
//                System.out.println("Team " + teamIndex);
//                for (int i = team.nextSetBit(0); i != -1; i = team.nextSetBit(i + 1)) {
//                    System.out.println(units[i]);
//                }
//                teamIndex += 1;
//            }
//            gameIndex += 1;
//        }

        return null;
    }


    @Override
    public String getName() {
        return name;
    }

    private ArrayList<ArrayList<BitSet>> getFeasibleGames(MatchUnit[] units, ArrayList<BitSet> teams) {
        ArrayList<ArrayList<BitSet>> games = new ArrayList<>();
        backtrack(games, new ArrayList<>(), 1, teams);
        return games;
    }

    private void backtrack(ArrayList<ArrayList<BitSet>> games, ArrayList<BitSet> tmpTeams, int start, ArrayList<BitSet> teams) {
        if (tmpTeams.size() == criteria.teamCountPerGame()) {
            games.add(new ArrayList<>(tmpTeams));
            return;
        }

        for (int i = start; i < teams.size(); i++) {
            var newTeam = teams.get(i);
            if (tmpTeams.stream().noneMatch(team -> team.intersects(newTeam))) {
                tmpTeams.add(teams.get(i));
                backtrack(games, tmpTeams, i + 1, teams);
                tmpTeams.removeLast();
            }
        }
    }
}
