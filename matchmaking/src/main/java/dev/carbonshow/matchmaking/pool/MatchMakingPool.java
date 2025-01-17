package dev.carbonshow.matchmaking.pool;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;

/**
 * 匹配池，用于高效管理内部存储单元
 */
public interface MatchMakingPool {

    /**
     * 向池子中添加新的匹配单元
     *
     * @param matchUnit 待添加的匹配单元
     * @return true 表示添加成功；false 表示添加失败，原因在于匹配单元已经存在
     */
    boolean addMatchUnit(MatchUnit matchUnit);

    /**
     * 根据匹配单元的唯一 ID，移除对应单元
     *
     * @param matchUnitId 匹配单元的唯一 ID
     * @return true 表示实际删除了单元；false 表示未删除，比如单元不存在
     */
    boolean removeMatchUnit(long matchUnitId);

    /**
     * 匹配单元亲和性相关参数是时变的，在该接口中实现对应逻辑
     * @param currentTimestamp 当前时间戳，单位自定义，内部保持一直即可
     */
    default void update(long currentTimestamp) {}

    /**
     * 根据匹配单元 ID 获取对应数据
     * @param matchUnitId 匹配单元唯一 ID
     */
    MatchUnit getMatchUnit(long matchUnitId);

    /**
     * 返回所有匹配单元的数组
     * @return 数组形式的匹配单元
     */
    MatchUnit[] matchUnits();

    /**
     * 获取匹配单元总数
     * @return 匹配单元总数
     */
    int matchUnitCount();

    /**
     * 获取所有匹配单元所含玩家总数
     * @return 玩家总数
     */
    int userCount();

    /**
     * 当前匹配池理论上可促成的单局的最大数量
     * @return 返回可以生成的最大单局数量
     */
    int maxGameCount();

    /**
     * 获取匹配参数和衡量标准
     */
    MatchMakingCriteria getCriteria();

    /**
     * 返回当前匹配池的名称
     */
    String poolName();
}
