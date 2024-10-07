package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.pool.MatchUnit;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 一个可用的单局，一定满足要求的各队伍人数符合要求，各匹配单元相互亲和
 */
public class FeasibleGame {
    private final List<FeasibleTeam> teams;
    private double score = 0.0;
    private final BitSet members = new BitSet();

    public FeasibleGame(List<FeasibleTeam> teams, long currentTimestamp) {
        this.teams = teams;
        score = calculateScore(currentTimestamp);
    }

    /**
     * 评分计算，会综合考虑进入时间，技能和段位等因素
     */
    private double calculateScore(long currentTimestamp) {
        int minRank = Integer.MAX_VALUE;
        int maxRank = Integer.MIN_VALUE;
        double minSkill = Double.MAX_VALUE;
        double maxSkill = Double.MIN_VALUE;
        long enterTimeStamp = Long.MAX_VALUE;

        for (var team : teams) {
            members.or(team.unitMembers());
            minRank = NumberUtils.min(minRank, team.timeVaryingParameters().getRank());
            maxRank = NumberUtils.max(maxRank, team.timeVaryingParameters().getRank());
            minSkill = NumberUtils.min(minSkill, team.timeVaryingParameters().getSkill());
            maxSkill = NumberUtils.max(maxSkill, team.timeVaryingParameters().getSkill());
            enterTimeStamp = NumberUtils.min(enterTimeStamp, team.timeVaryingParameters().startTimestamp());
        }

        return (currentTimestamp - enterTimeStamp) / 60.0 - (maxRank - minRank) - (maxSkill - minSkill);
    }

    public double getScore() {
        return score;
    }

    public boolean contains(int unitIndex) {
        return members.get(unitIndex);
    }

    public BitSet getMembers() {
        return members;
    }

    public List<List<Long>> toRaw(MatchUnit[] units) {
        ArrayList<List<Long>> result = new ArrayList<>();

        for (var team : teams) {
            result.add(team.unitMembers().stream().mapToLong(idx -> units[idx].matchUnitId()).boxed().collect(Collectors.toList()));
        }

        return result;
    }
}
