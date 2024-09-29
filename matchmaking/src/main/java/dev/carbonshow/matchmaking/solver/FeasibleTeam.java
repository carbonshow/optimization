package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.config.MatchUnitTimeVaryingParameters;

import java.util.BitSet;

/**
 * 可用 Team 的基本数据定义。人数一定符合赛事要求，内部所含各单元一定相互契合
 *
 * @param unitMembers 该 Team 内部所含的匹配单元的索引集合，使用 BitSet 表示，第 i 位是 1，则包含第 i 个单元
 * @param timeVaryingParameters 将各个单元合并到同一个队伍后总的评估参数
 */
public record FeasibleTeam(BitSet unitMembers, MatchUnitTimeVaryingParameters timeVaryingParameters) {
}
