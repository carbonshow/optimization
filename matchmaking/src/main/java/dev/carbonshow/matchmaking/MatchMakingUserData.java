package dev.carbonshow.matchmaking;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 匹配单元中单个玩家的基本数据，一个匹配单元可能包含多个玩家。同一个单元内的所有玩家不可分隔，一定在同一场比赛的同一边出现。
 * 该数据通过 matchUnitId 聚合后，可用于确定最终的匹配单元。
 * <br>
 * 该数据会从外部加载，使用 jackson 进行标注和管理。其中的 json 标记有两点需要注意：
 * <ul>
 *   <li>只提取部分指定字段，其他忽略</li>
 *   <li>为字段设置别名</li>
 * </ul>
 *
 * @param matchUnitId 匹配单元的唯一 ID
 * @param userId 匹配单元当前包含的用户 ID
 * @param rank 玩家当前排位赛星数
 * @param tier 玩家当前排位赛段位
 * @param trueSkillMu true skill 的 μ 值
 * @param trueSkillSigma true skill 的 σ 值
 * @param overallNBA NBA 模式的 overall
 * @param overallStreet Street 模式的 overall
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchMakingUserData(
  @JsonProperty("matchunitid")
  long matchUnitId,
  @JsonProperty("userid")
  long userId,
  @JsonProperty("rank")
  int rank,
  @JsonProperty("tier")
  int tier,
  @JsonProperty("tsmu")
  int trueSkillMu,
  @JsonProperty("tssigma")
  int trueSkillSigma,
  @JsonProperty("rotationoverall")
  int overallNBA,
  @JsonProperty("rotationoverallstreet")
  int overallStreet
  ) {
}
