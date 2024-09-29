package dev.carbonshow.matchmaking.pool;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;

import java.util.HashMap;

/**
 * 提供匹配池常用的公共基础逻辑，比如配置信息，人数维护，名称等等
 */
public abstract class MatchMakingPoolBasic implements MatchMakingPool {
    // 匹配评估准则和参数配置
    final protected MatchMakingCriteria criteria;

    // 匹配池名称
    final private String name;

    // 匹配单元，用 HashMap 保存，便于通过 ID 索引
    final protected HashMap<Long, MatchUnit> units = new HashMap<>();

    // 玩家总数，在匹配池增减单元时更新
    private int userCount = 0;

    MatchMakingPoolBasic(MatchMakingCriteria criteria, String name) {
        this.criteria = criteria;
        this.name = name;
    }

    @Override
    public boolean addMatchUnit(MatchUnit matchUnit) {
        if (!units.containsKey(matchUnit.matchUnitId())) {
            units.put(matchUnit.matchUnitId(), matchUnit);
            userCount += matchUnit.userCount();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeMatchUnit(long matchUnitId) {
        var unit = units.get(matchUnitId);
        if (unit != null) {
            userCount -= unit.userCount();
            units.remove(matchUnitId);
            return true;
        }
        return false;
    }

    @Override
    public MatchUnit getMatchUnit(long matchUnitId) {
        return units.get(matchUnitId);
    }

    @Override
    public MatchUnit[] matchUnits() {
        return units.values().toArray(MatchUnit[]::new);
    }

    @Override
    public int matchUnitCount() {
        return units.size();
    }

    @Override
    public int userCount() {
        return userCount;
    }

    @Override
    public int maxGameCount() {
        return userCount() / criteria.userCountPerGame();
    }

    @Override
    public MatchMakingCriteria getCriteria() {
        return criteria;
    }

    @Override
    public String poolName() {
        return name;
    }
}
