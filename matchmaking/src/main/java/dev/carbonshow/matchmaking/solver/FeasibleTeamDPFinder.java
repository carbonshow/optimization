package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Stack;

/**
 * 基于动态规划，从给定的匹配池中，寻找所有 Team 可行解，即所以的可用 Team。
 */
public class FeasibleTeamDPFinder implements FeasibleTeamFinder {

    private final MatchMakingCriteria matchMakingCriteria;
    private final MatchUnitOperator matchUnitOperator;

    public FeasibleTeamDPFinder(MatchMakingCriteria criteria, MatchUnitOperator operator) {
        matchMakingCriteria = criteria;
        matchUnitOperator = operator;
    }

    /**
     * 动态规划(Dynamic Programming)解决方案，用于计算可能的 unit 组合来形成一个有效的 team，即人数满足要求，匹配单元相互间满足亲和性要求
     * 从匹配单元队首开始向队尾遍历，首先考虑人数符合要求，然后考虑亲和性。如果人数不符合要求则不必继续后续的遍历。具体是：
     * <ul>
     *     <li>将匹配单元池按人数递增排列</li>
     *     <li>已选单元和新单元的人数总数，不能超出单个队伍上限，若超出则终止当前组合分支</li>
     *     <li>已选单元和新单元，必须满足匹配亲和性要求，比如 rank/skill 差距不能过大 等 </li>
     * </ul>
     * 由于要记录所有可用解，且避免栈溢出，所以使用循环方式
     * <b>返回结果，一定按 units index 从小到达的方式组织的</b>
     * @param units            匹配单元列表，需要按照成员人数升序排列，用于尽快收敛解空间
     * @param currentTimestamp 当前时间戳，单位是秒
     */
    @Override
    public ArrayList<FeasibleTeam> solve(MatchUnit[] units, long currentTimestamp) {
        // 记录可用解
        ArrayList<FeasibleTeam> solutions = new ArrayList<>();

        // 用 Stack 模拟调用栈，记录不同分支的状态
        Stack<TeamSearchState> states = new Stack<>();
        states.add(new TeamSearchState(0, matchMakingCriteria.userCountPerTeam(), new BitSet(units.length)));

        while (!states.isEmpty()) {
            var state = states.pop();

            if (state.exploredCount() < units.length) {
                // 当前考虑的匹配单元一定是已遍历元素的下一个元素
                var curUnitIndex = state.exploredCount;
                var curUnit = units[curUnitIndex];

                // 由于是升序的，如果当前单元的成员数已经超过划分约束，那没必要继续了
                if (state.partitioned < curUnit.userCount()) {
                    continue;
                }

                // 将包含当前可用最大数和不包含两种情况分别添加到 states 中，不论哪种，已探索的元素数量都递增 1
                if (isMatchable(units, state.unitIndices, curUnitIndex)) {
                    // 包含当前可用最大 unit，两两之间要符合亲和性要求
                    var includeSolution = (BitSet) state.unitIndices.clone();
                    includeSolution.set(curUnitIndex);
                    int newPartitioned = state.partitioned - curUnit.userCount();
                    if (newPartitioned > 0) {
                        // 添加新的状态分支，用于进一步的人数划分
                        states.add(new TeamSearchState(curUnitIndex + 1, newPartitioned, includeSolution));
                    } else if (newPartitioned == 0) {
                        // 划分完成
                        solutions.add(matchUnitOperator.mergeUnitsToTeam(units, includeSolution, currentTimestamp));
                    }
                }

                // 不含当前可用最大 unit，但仅在还有其他元素的情况下才有必要
                var excludeSolution = (BitSet) state.unitIndices.clone();
                states.add(new TeamSearchState(curUnitIndex + 1, state.partitioned, excludeSolution));
            }
        }
        return solutions;
    }

    /**
     * 表示组队过程中的搜索状态
     *
     * @param exploredCount units按照升序排列，从首端开始表示已经探索过的元素数量
     * @param partitioned   等待被划分的数值，也就是凑齐队伍还差多少人
     * @param unitIndices   当前成功组队的 unit 的索引集合
     */
    private record TeamSearchState(int exploredCount, int partitioned, BitSet unitIndices) {
    }

    /**
     * 判断待添加的新 unit 是否和已有的 unit 可匹配
     *
     * @param units              匹配池所有匹配单元数组
     * @param matchedUnitIndices 已经撮合到同一个小组的匹配单元索引列表，以 BitSet 形式存储
     * @param newUnitIndex       新的匹配单元索引
     * @return true 表示新旧可匹配；false 只要存在一个不匹配则认为不可匹配
     */
    private boolean isMatchable(MatchUnit[] units, BitSet matchedUnitIndices, int newUnitIndex) {
        var newUnit = units[newUnitIndex];
        for (int i = matchedUnitIndices.nextSetBit(0); i >= 0; i = matchedUnitIndices.nextSetBit(i + 1)) {
            if (!matchUnitOperator.isFitOneTeam(newUnit, units[i])) {
                return false;
            }
        }
        return true;
    }

}
