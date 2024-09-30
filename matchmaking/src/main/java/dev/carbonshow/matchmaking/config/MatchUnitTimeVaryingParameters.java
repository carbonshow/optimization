package dev.carbonshow.matchmaking.config;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 记录匹配单元随时间变化的匹配参数
 * 这里的时间参数单位皆为秒
 */
public class MatchUnitTimeVaryingParameters implements TimeVaryingParameters {
    // 匹配单元进入匹配池的时间戳，是以秒计算的 Epoch Time
    private long enterTimestamp;
    private long lastUpdateTimestamp;

    // 当前匹配单元综合的星数
    private int rank;

    // 基于整数衡量的当前匹配单元的经济实力
    private double skill;

    // 在等待一段时间后，一个匹配单元可以接纳的其他匹配单元的属性区间
    private Range<Integer> matchedRankRange;
    private Range<Double> matchedSkillRange;

    public MatchUnitTimeVaryingParameters(long enterTimestamp, int rank, double skill) {
        this.enterTimestamp = enterTimestamp;
        this.lastUpdateTimestamp = 0;
        this.rank = rank;
        this.skill = skill;
        matchedRankRange = Range.of(rank, rank);
        matchedSkillRange = Range.of(skill, skill);
    }

    /**
     * 匹配单元进入匹配池的时间戳，单位是秒
     */
    @Override
    public long startTimestamp() {
        return enterTimestamp;
    }

    /**
     * 上次有效更新的时间戳，单位是秒
     */
    @Override
    public long lastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * 基于最新的时间戳，更新可变属性，主要是当前匹配单元可以接受的 rank，skill
     * 等数值范围。
     *
     * @param currentTimestamp 当前时间戳，单位是秒
     * @param config 时变配置参数
     */
    @Override
    public void update(long currentTimestamp, TimeVaryingConfig config) {
        // 更新的最小时间窗口：60s ，即 1 分钟内不变化
        long minUpdateTimeWindow = 60;

        if (currentTimestamp - lastUpdateTimestamp < minUpdateTimeWindow) {
            return;
        }

        lastUpdateTimestamp = currentTimestamp;

        // 按分钟计算，每过一分钟，rank 放宽 1
        int elapsedTime = (int) (currentTimestamp - enterTimestamp);
        int deltaRank = elapsedTime / config.rankWindow() * config.deltaRank();

        // 每过 一分钟，skill 差异放宽 5%
        var deltaSkill = skill * config.deltaSkillRatio() * elapsedTime / config.rankWindow();

        matchedRankRange = Range.of(rank - deltaRank, rank + deltaRank);
        matchedSkillRange = Range.of(skill - deltaSkill, skill + deltaSkill);
    }

    public int getRank() {
        return rank;
    }

    public double getSkill() {
        return skill;
    }

    public Range<Integer> getMatchedRankRange() {
        return matchedRankRange;
    }

    public Range<Double> getMatchedSkillRange() {
        return matchedSkillRange;
    }

    public void merge(MatchUnitTimeVaryingParameters other) {
        if (other == null) {
            return;
        }

        enterTimestamp = NumberUtils.max(enterTimestamp, other.enterTimestamp);
        lastUpdateTimestamp = NumberUtils.min(lastUpdateTimestamp, other.lastUpdateTimestamp);
        rank = NumberUtils.max(rank, other.rank);
        skill = NumberUtils.max(skill, other.skill);
        matchedRankRange = Range.of(rank, rank);
        matchedSkillRange = Range.of(skill, skill);
    }
}
