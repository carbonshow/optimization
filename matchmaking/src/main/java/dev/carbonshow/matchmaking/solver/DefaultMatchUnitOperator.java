package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.MatchUnitTimeVaryingParameters;
import dev.carbonshow.matchmaking.config.TimeVaryingConfig;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.security.InvalidParameterException;
import java.util.BitSet;

/**
 * 默认的匹配单元计算器
 */
public class DefaultMatchUnitOperator implements MatchUnitOperator {
    private final MatchMakingCriteria matchMakingCriteria;
    private final TimeVaryingConfig timeVaryingConfig;

    public DefaultMatchUnitOperator(MatchMakingCriteria criteria, TimeVaryingConfig timeVaryingConfig) {
        matchMakingCriteria = criteria;
        this.timeVaryingConfig = timeVaryingConfig;
    }

    /**
     * 判断两个匹配单元是否能进入同一单局的同一个队伍。每个匹配单元可视为一个多面体，维度有可以接受的 rank，skill，人数等信息。
     * 这个多面体大小随时间变化，两个多面体有交集则可以进入同一单局
     *
     * @param unit1 匹配单元 1
     * @param unit2 匹配单元 2
     * @return true 说明两个匹配单元能进入同一单局；false 说明互斥
     */
    @Override
    public boolean isFitOneTeam(MatchUnit unit1, MatchUnit unit2) {
        if (unit1.userCount() + unit2.userCount() > matchMakingCriteria.userCountPerTeam()) {
            return false;
        }

        return checkTimeVaryingParameters(unit1.timeVaryingParameters(), unit2.timeVaryingParameters());
    }

    /**
     * 判断给定的两个匹配单元是否可以进入同一单局的不同队伍内
     * @param unit1 队伍 1，自身一定是可用的
     * @param unit2 队伍 2，自身一定是可用的
     * @return true 表示这些队伍可以进入同一单局；否则表示不能进入同一单局
     */
    @Override
    public boolean isFitOneGame(MatchUnit unit1, MatchUnit unit2){
        return checkTimeVaryingParameters(unit1.timeVaryingParameters(), unit2.timeVaryingParameters());
    }

    /**
     * 判断给定的几个 team 是否可以进入同一单局内
     *
     * @param team1 队伍 1，自身一定是可用的
     * @param team2 队伍 2，自身一定是可用的
     * @return true 表示这些队伍可以进入同一单局；否则表示不能进入同一单局
     */
    @Override
    public boolean isFitOneGame(FeasibleTeam team1, FeasibleTeam team2) {
        // 成员不能重叠
        if (team1.unitMembers().intersects(team2.unitMembers())) {
            return false;
        }

        // 时变参数符合要求
        return checkTimeVaryingParameters(team1.timeVaryingParameters(), team2.timeVaryingParameters());
    }

    @Override
    public FeasibleTeam mergeUnitsToTeam(MatchUnit[] units, BitSet unitMembers, long currentTimestamp) throws InvalidParameterException {
        MatchUnitTimeVaryingParameters parameters = new MatchUnitTimeVaryingParameters(0L, 0, 0.0, new BitSet(matchMakingCriteria.maxPositions()));
        unitMembers.stream().forEach(idx -> {
            var unit = units[idx];
            parameters.merge(unit.timeVaryingParameters());
        });
        parameters.update(currentTimestamp, timeVaryingConfig);
        return new FeasibleTeam(unitMembers, parameters);
    }

    @Override
    public double getWinProbability(MatchUnit unit1, MatchUnit unit2) {
        return 0;
    }

    /**
     * 判断两个匹配单元的时变参数是否符合要求。skill 和 rank 的容忍度都是随时间变化的，相互有交集即可用
     *
     * @param param1 时变参数 1
     * @param param2 时变参数 2
     * @return true 说明两个参数有交集可用；负责不可用
     */
    public boolean checkTimeVaryingParameters(MatchUnitTimeVaryingParameters param1, MatchUnitTimeVaryingParameters param2) {
        return param1.getMatchedRankRange().contains(param2.getRank()) && param2.getMatchedRankRange().contains(param1.getRank())
                &&
                param1.getMatchedSkillRange().contains(param2.getSkill()) && param2.getMatchedSkillRange().contains(param1.getSkill());
    }
}
