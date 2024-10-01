package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.List;

/**
 * 最优对局求解器
 */
public interface OptimalGameFinder {

    /**
     * 从候选可用 Game 中挑选若干单局，既保证 team 只会出现一次，有保证优化目标符合要求
     * @param units 匹配池中的所有匹配单元
     * @param games 单局可行解区间，不同成员可能包含相同的 match unit
     * @param parameters 求解参数
     * @param currentTimestamp 当前时间戳，单位是秒
     */
    void solve(MatchUnit[] units, List<FeasibleGame> games, SolverParameters parameters, long currentTimestamp);
}
