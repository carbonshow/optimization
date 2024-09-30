package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.List;

/**
 * 使用回溯方法，寻找可用的单局求解器，用于将不同的队伍组织起来得到一个可用单局
 */
public class FeasibleGameBacktraceFinder implements FeasibleGameFinder {

    private final MatchMakingCriteria criteria;
    private final MatchUnitOperator operator;

    public FeasibleGameBacktraceFinder(MatchMakingCriteria matchMakingCriteria, MatchUnitOperator matchUnitOperator) {
        criteria = matchMakingCriteria;
        operator = matchUnitOperator;
    }

    /**
     * 根据指定的匹配单元数组，获得满足要求的所有队伍
     *
     * @param units            匹配单元数组
     * @param teams            所有可用 team，同一个匹配单元可能出现在多个不同的 teams 中
     * @param currentTimestamp 当前时间戳，单位业务自定义保持一致即可
     * @return 返回可用单局，这些单局可以同时开启，即任何一个匹配单元最多只会出现在一个单局中
     */
    public List<FeasibleGame> solve(MatchUnit[] units, List<FeasibleTeam> teams, SolverParameters parameters, long currentTimestamp) {
        return null;
    }
}
