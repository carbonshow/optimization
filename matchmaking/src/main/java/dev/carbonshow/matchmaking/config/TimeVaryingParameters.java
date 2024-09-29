package dev.carbonshow.matchmaking.config;

/**
 * 记录时变参数，即相关属性随时间变化
 */
public interface TimeVaryingParameters {
    /**
     * 起始时间戳，单位业务自定义
     */
    long startTimestamp();

    /**
     * 上次有效更新的时间戳，单位业务自定义
     */
    long lastUpdateTimestamp();

    /**
     * 基于起始时间戳和当前时间戳，更新属性
     * @param currentTimestamp 当前时间戳，单位业务自定义
     * @param config 时变配置参数
     */
    void update(long currentTimestamp, TimeVaryingConfig config);
}
