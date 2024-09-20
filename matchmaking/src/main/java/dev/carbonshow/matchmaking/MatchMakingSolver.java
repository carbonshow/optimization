package dev.carbonshow.matchmaking;

interface MatchMakingSolver {
    /**
     * 针对指定的匹配标准和配置，以及目标匹配池中的所有匹配单元，进行匹配优化计算，并获取可用的匹配结果
     *
     * @param pool 匹配池，包含所有匹配单元的基础数据
     *
     */
    MatchMakingResults solve(MatchMakingPool pool);
}
