package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.List;

/**
 * 寻找可用单局的求解器，用于将不同的队伍组织起来得到一个可用单局
 */
public interface FeasibleGameFinder {
    /**
     * 根据指定的小队，获得满足要求的所有可用单局
     *
     * @param units 匹配单元数组
     * @param teams 所有可用 team，同一个匹配单元可能出现在多个不同的 teams 中
     * @param parameters 求解器的配置参数
     * @param currentTimestamp 当前时间戳，单位业务自定义保持一致即可
     * @return 返回可用单局，这些单局可以同时开启，即任何一个匹配单元最多只会出现在一个单局中
     */
    List<FeasibleGame> solve(MatchUnit[] units, List<FeasibleTeam> teams, SolverParameters parameters, long currentTimestamp);
}
