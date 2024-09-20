package dev.carbonshow.matchmaking;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

/**
 * 匹配单元数据结构定义，可能包含多个不同的玩家
 *
 * @param matchUnitId            匹配单元 ID
 * @param enterTimestamp         匹配单元进入匹配池的时间戳，是以秒计算的 Epoch Time
 * @param userIds                匹配单元内包含的用户 ID 列表
 * @param rank                   当前匹配单元综合的星数
 * @param positions              本单元所有玩家擅长的位置集合，用 BitSet 表示，第 i 位如果为 1 表示玩家擅长第 i 个位置
 * @param skill                  基于整数衡量的当前匹配单元的竞技实力
 * @param expectedWinProbability 预期胜率
 * @param relayLatency           当前匹配单元到不同帧同步服务器的延迟，key 是帧同步服务器分组 id，value 是 udp 通信延迟，单位是 ms
 */
public record MatchUnit(
        long matchUnitId,
        long enterTimestamp,
        ArrayList<Long> userIds,
        int rank,
        BitSet positions,
        double skill,
        double expectedWinProbability,
        Map<Integer, Integer> relayLatency
) {

    /**
     * 获取当前匹配单元所含用户数
     */
    public int userCount() {
        return userIds.size();
    }
}
