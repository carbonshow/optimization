package dev.carbonshow.matchmaking.pool;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;

import java.sql.*;

/**
 * 匹配池中多个匹配单元，存在频繁的更新、删除，以及复杂的过滤、排序。
 * 在这种情况下基于 In-Memory Database Engine 进行匹配单元的管理。为了充分发挥 collections 和 database 的有优势会使用下面的方式：
 * <ul>
 *     <li>内存保存 MatchUnit 的完整数据</li>
 *     <li>数据库维护需要做复杂过滤、排序的部分字段</li>
 * </ul>
 */
public class MatchMakingPoolInMemDB extends MatchMakingPoolBasic {
    final private Connection connection;
    final private String dbTableName;

    /**
     * 设置匹配配置参数和指标信息，并创建内存数据库用于管理、查询匹配单元
     *
     * @param criteria 匹配参数和指标
     * @param name     当前匹配池的名称
     * @throws SQLException 创建内存数据库可能出错
     */
    public MatchMakingPoolInMemDB(MatchMakingCriteria criteria, String name) throws SQLException {
        super(criteria, name);
        dbTableName = "mmpool_" + name.toLowerCase();
        connection = DriverManager.getConnection("jdbc:h2:mem:matchmaking", "user", "password");

        initializeDatabase();
    }

    /**
     * 添加成功则向数据库更新
     *
     * @param matchUnit 待添加的匹配单元
     */
    @Override
    public boolean addMatchUnit(MatchUnit matchUnit) {
        final String addMatchUnit = "INSERT INTO " + dbTableName + " VALUES(?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(addMatchUnit);
            statement.setLong(1, matchUnit.matchUnitId());
            statement.setLong(2, matchUnit.timeVaryingParameters().startTimestamp());
            statement.setInt(3, matchUnit.userCount());
            statement.setInt(4, matchUnit.timeVaryingParameters().getRank());
            statement.setLong(5, matchUnit.positionAsLong());
            statement.setDouble(6, matchUnit.timeVaryingParameters().getSkill());
            statement.setDouble(7, matchUnit.expectedWinProbability());
            int count = statement.executeUpdate();
            if (count > 0) {
                // 数据库添加成功在进行本地添加
                super.addMatchUnit(matchUnit);
            }
            return count > 0;
        } catch (SQLException e) {
            // 数据库添加失败，那么回滚
            super.removeMatchUnit(matchUnit.matchUnitId());
            return false;
        }
    }

    @Override
    public boolean removeMatchUnit(long matchUnitId) {
        final String removeMatchUnit = "DELETE FROM " + dbTableName + " WHERE ID = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(removeMatchUnit);
            statement.setLong(1, matchUnitId);
            int count = statement.executeUpdate();
            if (count > 0) {
                super.removeMatchUnit(matchUnitId);
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
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
