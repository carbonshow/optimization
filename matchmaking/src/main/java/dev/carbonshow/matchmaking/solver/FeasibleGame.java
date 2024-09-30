package dev.carbonshow.matchmaking.solver;

import java.util.List;

/**
 * 一个可用的单局，一定满足要求的各队伍人数符合要求，各匹配单元相互亲和
 * @param teams 局内包含的各个队伍，每个队伍皆可用
 */
public record FeasibleGame(List<FeasibleTeam> teams) {
}
