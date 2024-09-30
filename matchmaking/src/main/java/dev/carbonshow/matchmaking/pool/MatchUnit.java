package dev.carbonshow.matchmaking.pool;

import dev.carbonshow.matchmaking.config.MatchUnitTimeVaryingParameters;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * 匹配单元数据结构定义，可能包含多个不同的玩家
 *
 * @param matchUnitId           匹配单元 ID
 * @param userIds               匹配单元内包含的用户 ID 列表
 * @param positions             本单元所有玩家擅长的位置集合，用 BitSet 表示，第 i 位如果为 1 表示玩家擅长第 i 个位置
 * @param relayLatency          当前匹配单元到不同帧同步服务器的延迟，key 是帧同步服务器分组 id，value 是 udp 通信延迟，单位是 ms
 * @param timeVaryingParameters 时变参数，比如接纳的等级差异等
 */
public record MatchUnit(
        long matchUnitId,
        List<Long> userIds,
        BitSet positions,
        double expectedWinProbability,
        Map<Integer, Integer> relayLatency,
        MatchUnitTimeVaryingParameters timeVaryingParameters
) {
    /**
     * 获取当前匹配单元所含用户数
     */
    public int userCount() {
        return userIds.size();
    }

    public long positionAsLong() {
        long longValue = 0;
        for (int bit = 0; bit < positions.length(); bit++) {
            if (positions.get(bit)) {
                longValue |= (1L << bit);
            }
        }
        return longValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof MatchUnit other) {
            return this.matchUnitId == other.matchUnitId;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        var timeVaryingParameters = this.timeVaryingParameters;
        var rankRange = timeVaryingParameters.getMatchedRankRange();
        var skillRange = timeVaryingParameters.getMatchedSkillRange();
        builder.append(matchUnitId)
                .append("[userCnt=").append(userCount()).append(",")
                .append("rank=").append(rankRange.getMinimum()).append("-")
                .append(timeVaryingParameters.getRank()).append("-").append(rankRange.getMaximum()).append(",")
                .append("skill=").append(skillRange.getMinimum().intValue()).append("-")
                .append((int)timeVaryingParameters.getSkill()).append("-").append(skillRange.getMaximum().intValue()).append(",")
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(matchUnitId);
    }
}