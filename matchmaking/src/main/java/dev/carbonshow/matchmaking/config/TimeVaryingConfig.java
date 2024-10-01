package dev.carbonshow.matchmaking.config;

/**
 * 时变参数的配置
 * @param rankWindow rank 变化窗口，单位是 s，每过该时长，rank 上下界扩大
 * @param deltaRank 每个 rankWindow 之后，rank 上下界扩大值
 * @param skillWindow skill 变化窗口，单位是 s，每过该时长，skill 上下界扩大
 * @param deltaSkillRatio 每个 skillWindow 之后，skill 在基础值上上下界扩大该比例
 */
public record TimeVaryingConfig(int rankWindow, int deltaRank, int skillWindow, double deltaSkillRatio) {

    public static TimeVaryingConfig defaultVal() {
        return new TimeVaryingConfig(60, 1, 60, 0.2);
    }
}
