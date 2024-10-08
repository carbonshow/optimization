package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.pool.MatchMakingPool;

public interface MatchMakingSolver {
    /**
     * 针对指定的匹配标准和配置，以及目标匹配池中的所有匹配单元，进行匹配优化计算，并获取可用的匹配结果
     *
     * @param pool 匹配池，包含所有匹配单元的基础数据
     * @param parameters 求解器配置参数
     * @param currentTimestamp 当前时间戳，单位是秒
     */
    MatchMakingResults solve(MatchMakingPool pool, SolverParameters parameters, long currentTimestamp);

    /**
     * 给当前求解器起一个名字
     */
    String getName();
}
