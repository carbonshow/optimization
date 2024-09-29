package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.ArrayList;

/**
 * 寻找可用组队的求解器，用于将不同的匹配单元组织起来得到一个可用队伍
 */
public interface FeasibleTeamFinder {
    /**
     * 根据指定的匹配单元数组，获得满足要求的所有队伍
     *
     * @param units 匹配单元数组
     * @param currentTimestamp 当前时间戳，单位业务自定义保持一致即可
     * @return 返回可用队伍
     */
    ArrayList<FeasibleTeam> solve(MatchUnit[] units, long currentTimestamp);
}
