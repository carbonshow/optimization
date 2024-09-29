package dev.carbonshow.matchmaking.config;

/**
 * 求解过程的配置参数
 * @param maxGameCount 单次求解可以生成的单局可行解数量上限
 * @param maxTeamCount 单次求解可以生成的队伍可行解数量上限
 * @param maxSolveTimeInSeconds 单词求解耗时上限，单位是秒
 */
public record SolverParameters(int maxGameCount, int maxTeamCount, double maxSolveTimeInSeconds) {
}
