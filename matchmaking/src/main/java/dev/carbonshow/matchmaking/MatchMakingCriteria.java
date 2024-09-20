package dev.carbonshow.matchmaking;

/**
 * 匹配评价标准，也包含匹配参数等内容
 * @param teamCountPerGame 一场对局中包含的队伍数量，一般只有两方
 * @param userCountPerTeam 一场对局中各个队伍中所应包含的用户数量
 * @param maxRank 最大段位
 * @param maxPositions 不同位置的最多数量
 */
public record MatchMakingCriteria(int teamCountPerGame, int userCountPerTeam, int maxRank, int maxPositions) {

    public int getUserCountPerGame() {
        return teamCountPerGame * userCountPerTeam;
    }
}
