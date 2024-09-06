package dev.carbonshow.matchmaking;

import java.util.ArrayList;

/**
 * 匹配单元数据结构定义，可能包含多个不同的玩家
 *
 * @param matchUnitId 匹配单元 ID
 * @param userIds 匹配单元内包含的用户 ID 列表
 * @param rank 当前匹配单元综合的星数
 * @param tier 当前匹配单元综合的段位
 * @param trueSkillMu 当前匹配单元综合的 true skill 正态分布的期望
 * @param trueSkillSigma 当前匹配单元综合的 true skill 正态分布的标准差
 * @param overallNBA 当前匹配单元综合的 NBA 模式 overall
 * @param overallStreet 当前匹配单元综合的 Street 模式 overall
 */
public record MatchUnit(
  long matchUnitId,
  ArrayList<Long> userIds,
  int rank,
  int tier,
  int trueSkillMu,
  int trueSkillSigma,
  int overallNBA,
  int overallStreet){
}
