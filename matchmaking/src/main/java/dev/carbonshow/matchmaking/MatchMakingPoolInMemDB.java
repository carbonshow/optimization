package dev.carbonshow.matchmaking;

import java.sql.*;
import java.util.HashMap;

/**
 * 匹配池中多个匹配单元，存在频繁的更新、删除，以及复杂的过滤、排序。
 * 在这种情况下基于 In-Memory Database Engine 进行匹配单元的管理。为了充分发挥 collections 和 database 的有优势会使用下面的方式：
 * <ul>
 *     <li>内存保存 MatchUnit 的完整数据</li>
 *     <li>数据库维护需要做复杂过滤、排序的部分字段</li>
 * </ul>
 */
public class MatchMakingPoolInMemDB implements MatchMakingPool {
    final private MatchMakingCriteria criteria;
    final private Connection connection;
    final private String name;
    final private String dbTableName;

    final private HashMap<Long, MatchUnit> units = new HashMap<>();
    private int userCount = 0;

    /**
     * 设置匹配配置参数和指标信息，并创建内存数据库用于管理、查询匹配单元
     *
     * @param criteria 匹配参数和指标
     * @param name     当前匹配池的名称
     * @throws SQLException 创建内存数据库可能出错
     */
    MatchMakingPoolInMemDB(MatchMakingCriteria criteria, String name) throws SQLException {
        this.criteria = criteria;
        this.name = name;
        dbTableName = "mmpool_" + name.toLowerCase();
        connection = DriverManager.getConnection("jdbc:h2:mem:matchmaking", "user", "password");

        initializeDatabase();
    }

    @Override
    public boolean addMatchUnit(MatchUnit matchUnit) {
        final String addMatchUnit = "INSERT INTO " + dbTableName + " VALUES(?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(addMatchUnit);
            statement.setLong(1, matchUnit.matchUnitId());
            statement.setLong(2, matchUnit.enterTimestamp());
            statement.setInt(3, matchUnit.userCount());
            statement.setInt(4, matchUnit.rank());
            statement.setLong(5, matchUnit.positionAsLong());
            statement.setDouble(6, matchUnit.skill());
            statement.setDouble(7, matchUnit.expectedWinProbability());
            int count = statement.executeUpdate();
            if (count > 0) {
                units.put(matchUnit.matchUnitId(), matchUnit);
                userCount += matchUnit.userCount();
                return true;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public int removeMatchUnit(long matchUnitId) {
        final String removeMatchUnit = "DELETE FROM " + dbTableName + " WHERE ID = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(removeMatchUnit);
            statement.setLong(1, matchUnitId);
            int count = statement.executeUpdate();
            if (count > 0) {
                var unit = units.get(matchUnitId);
                userCount -= unit.userCount();
                units.remove(matchUnitId);
            }
            return count;
        } catch (SQLException e) {
            return 0;
        }
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

    /**
     * 数据库初始化：建表、建索引
     *
     * @throws SQLException 创建过程可能出现异常
     */
    private void initializeDatabase() throws SQLException {
        var statement = connection.createStatement();

        // 创建数据库
        final String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS " + dbTableName +
                " (id BIGINT PRIMARY KEY , enter_timestamp BIGINT, usercnt INT, rank INT, positions BIGINT, skill NUMERIC, ewp NUMERIC)";
        statement.addBatch(sqlCreateTbl);

        // 建索引
        final String sqlIndexRank = "CREATE INDEX index_rank ON " + dbTableName + " (rank)";
        statement.addBatch(sqlIndexRank);
        final String sqlIndexSkill = "CREATE INDEX index_skill ON " + dbTableName + " (skill)";
        statement.addBatch(sqlIndexSkill);
        final String sqlIndexEwp = "CREATE INDEX index_ewp ON " + dbTableName + " (ewp)";
        statement.addBatch(sqlIndexEwp);
        final String sqlIndexUserCount = "CREATE INDEX index_user_cnt ON " + dbTableName + " (usercnt)";
        statement.addBatch(sqlIndexUserCount);
        final String sqlIndexEnterTime = "CREATE INDEX index_enter_time ON " + dbTableName + " (enter_timestamp)";
        statement.addBatch(sqlIndexEnterTime);
        statement.executeBatch();
    }
}
