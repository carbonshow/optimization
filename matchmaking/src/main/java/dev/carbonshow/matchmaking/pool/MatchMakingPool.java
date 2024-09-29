package dev.carbonshow.matchmaking.pool;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;

import java.util.ArrayList;

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
     * 返回互斥的匹配单元列表，这里只返回索引，而非 MatchUnit ID，返回的索引和 `units`
     * 数组保持一致
     * <ul> 注意事项
     *     <li>使用组合方式避免重复，即 1 和 2 如果出现过，就不会出现 2 和 1；</li>
     * </ul>
     * @param units 数组形式的匹配单元列表
     * @return 返回二维数组，一维表示第 i 个单元，二维表示和第 i 个单元互斥的其它单元的索引
     */
    ArrayList<ArrayList<Integer>> getMutableExclusiveMatchUnits(MatchUnit[] units);

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
