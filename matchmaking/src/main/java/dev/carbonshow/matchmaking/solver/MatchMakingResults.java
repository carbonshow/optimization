package dev.carbonshow.matchmaking.solver;


import java.util.List;

/**
 * 匹配结果
 *
 * @param results 每个元素代表一场可用单局，元素内部是一个二维数组，一维表示单局内不同队伍，二维表示队伍内的匹配单元 id
 */
public record MatchMakingResults(List<List<List<Long>>> results) {
}
