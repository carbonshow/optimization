package dev.carbonshow.matchmaking;

import java.sql.*;

import org.apache.commons.lang3.Range;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

/**
 * 测试 H2 Database 在 In-Memory 模式下的访问性能。结论：
 * <ul>
 *     <li>JDBC 比 JDBI，性能更好。插入 5w 条数据，JDBI 耗时 2213ms，JDBC 耗时 246ms</li>
 *     <li>在 5w 级别数据量下，添加索引的影响。具体要看查询和添加、删除的比例
 *         <ul>
 *             <li>单次查询，时间开销减少 1ms，密集查询还是很有必要添加的</li>
 *             <li>插入 5w 条数据的累计时间开销，从 246ms 增加到 347ms</li>
 *         </ul>
 *     带来的查询性能提升影响不大，只有 1ms 优势，但是对插入影响性能较大。
 *     </li>
 *     <li>In-Memory 数据库和内存 collection 遍历比较，</li>
 *         <ul>
 *             <li>在 5k 数量级下，查询 5k 次。H2 检索时间开销 2104 ms；内存使用 collection 遍历，需要 549ms </li>
 *             <li>在 5w 数量级下，查询 5w 次。H2 检索时间开销 202208 ms；内存使用 collection 遍历，需要 58476ms </li>
 *         </ul>
 * </ul>
 */
public class InMemDBTest {

    private static final int RECORD_COUNT = 50000;
    private static final int QUERY_COUNT = RECORD_COUNT;

    // 创建 jdbi 连接到 in-mem 模式的 H2
    static Jdbi createJdbi() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;CACHE_SIZE=65536;LOCK_MODE=0");
        dataSource.setUser("none");
        dataSource.setPassword("");
        return Jdbi.create(dataSource);
    }

    public record MatchUnitMinimal(int id, int memberCount, int rank, double skill) {
    }

    interface MatchUnitDaoJdbi extends SqlObject {
        @SqlUpdate("CREATE TABLE units (id INT PRIMARY KEY, member_count INT, rank INT, skill DOUBLE)")
        void createTable();

        @SqlUpdate("INSERT INTO units (id, member_count, rank, skill) VALUES (:id, :memberCount, :rank, :skill)")
        void addUnit(@BindMethods MatchUnitMinimal unit);

        @SqlQuery("SELECT * FROM units WHERE id = :id")
        @RegisterConstructorMapper(MatchUnitMinimal.class)
        List<MatchUnitMinimal> getUnitById(@Bind("id") int id);
    }


    public static class MatchUnitUnitDaoJdbc {
        private static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;CACHE_SIZE=65536;LOCK_MODE=0";
        private static final String USER = "sa";
        private static final String PASSWORD = "";

        private static final String INSERT_SQL = "INSERT INTO units (id, member_count, rank, skill) VALUES (?, ?, ?, ?)";
        private static final String QUERY_BY_ID_SQL = "SELECT * FROM units WHERE id = ?";
        private static final String QUERY_FEASIBLE_SQL =
                "SELECT id FROM units WHERE member_count <= ? AND rank BETWEEN ? AND ? AND skill BETWEEN ? AND ? "
                        .concat("ORDER BY ABS(rank - ?) ASC, ABS(skill - ?) ASC, id ASC LIMIT ?");

        private Connection connection;

        public MatchUnitUnitDaoJdbc() throws SQLException {
            connect();
            createTable();
        }

        private void connect() throws SQLException {
            connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        }

        private void createTable() throws SQLException {
            String createTableSQL = """
                        CREATE TABLE IF NOT EXISTS units (
                            id INT PRIMARY KEY,
                            member_count INT,
                            rank INT,
                            skill DOUBLE);
                        -- CREATE INDEX idx_feasible ON units(member_count, rank, skill);
                    """;
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
            }
        }

        public void close() throws SQLException {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }

        public boolean add(MatchUnitMinimal unit) {
            try (PreparedStatement pstmt = connection.prepareStatement(INSERT_SQL)) {
                pstmt.setInt(1, unit.id);
                pstmt.setInt(2, unit.memberCount);
                pstmt.setInt(3, unit.rank);
                pstmt.setDouble(4, unit.skill);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                return false;
            }
        }

        public MatchUnitMinimal getUnitById(int id) {
            try (PreparedStatement pstmt = connection.prepareStatement(QUERY_BY_ID_SQL)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new MatchUnitMinimal(rs.getInt("id"), rs.getInt("member_count"), rs.getInt("rank"), rs.getDouble("skill"));
                }
            } catch (SQLException e) {
                return null;
            }

            return null;
        }

        public List<Integer> getFeasibleUnits(int maxMemberCount, int selfRank, Range<Integer> rankRange, double selfSkill, Range<Double> skillRange, int limit) {
            try (PreparedStatement pstmt = connection.prepareStatement(QUERY_FEASIBLE_SQL)) {
                pstmt.setInt(1, maxMemberCount);
                pstmt.setInt(2, rankRange.getMinimum());
                pstmt.setInt(3, rankRange.getMaximum());
                pstmt.setDouble(4, skillRange.getMinimum());
                pstmt.setDouble(5, skillRange.getMaximum());
                pstmt.setInt(6, selfRank);
                pstmt.setDouble(7, selfSkill);
                pstmt.setInt(8, limit);
                ResultSet rs = pstmt.executeQuery();
                List<Integer> units = new ArrayList<>();
                while (rs.next()) {
                    units.add(rs.getInt("id"));
                }
                return units;
            } catch (SQLException e) {
                return null;
            }
        }
    }

    /**
     * 使用 JDBI 访问 in-memory H2，插入 5w 条数据耗时约：2213 ms
     */
    @Test
    void testJdbi() {
        Jdbi jdbi = createJdbi();
        jdbi.installPlugin(new SqlObjectPlugin());

        try {
            MatchUnitDaoJdbi matchUnitDao = jdbi.onDemand(MatchUnitDaoJdbi.class);
            matchUnitDao.createTable();

            ArrayList<MatchUnitMinimal> units = createUnits(RECORD_COUNT);

            var start = System.currentTimeMillis();
            units.forEach(matchUnitDao::addUnit);
            System.out.println("insert time(ms): " + (System.currentTimeMillis() - start));

            var unit = matchUnitDao.getUnitById(100);
            System.out.println("Retrieved unit: " + unit);
        } finally {
            System.out.println("jdbi closed");
        }
    }

    /**
     * 使用 JDBC 访问 in-memory H2，插入 5w 条数据耗时约：246ms，比 JDBI 快 9 倍
     */
    @Test
    void testJdbc() {
        Assertions.assertDoesNotThrow(() -> {
            var jdbcDao = new MatchUnitUnitDaoJdbc();

            ArrayList<MatchUnitMinimal> units = createUnits(RECORD_COUNT);

            var start = System.currentTimeMillis();
            units.forEach(jdbcDao::add);
            System.out.println("insert time(ms): " + (System.currentTimeMillis() - start));

            var unit = jdbcDao.getUnitById(100);
            System.out.println("Retrieved unit: " + unit);

            start = System.currentTimeMillis();
            IntStream.rangeClosed(1, QUERY_COUNT).forEach(i -> {
                var count = TestUtilities.RANDOM_GENERATOR.nextInt(5) + 1;
                var rankDelta = TestUtilities.RANDOM_GENERATOR.nextInt(3);
                var skillDelta = TestUtilities.RANDOM_GENERATOR.nextDouble() * 10;
                var feasible = jdbcDao.getFeasibleUnits(count, 3, Range.of(3 - rankDelta, 3 + rankDelta), 50.0, Range.of(50.0 - skillDelta, 50.0 + skillDelta), 20);
//                System.out.println("Query: " + count + ", " + rankDelta + ", " + skillDelta);
//                feasible.forEach(id -> System.out.println(units.get(id - 1)));
            });
            System.out.println("query time(ms): " + (System.currentTimeMillis() - start));
        });
    }

    @Test
    void testDataStructure() {
        var units = createUnits(RECORD_COUNT);

        var start = System.currentTimeMillis();
        IntStream.rangeClosed(1, QUERY_COUNT).forEach(i -> {
            var count = TestUtilities.RANDOM_GENERATOR.nextInt(5) + 1;
            var rankDelta = TestUtilities.RANDOM_GENERATOR.nextInt(3);
            var skillDelta = TestUtilities.RANDOM_GENERATOR.nextDouble() * 40;
            var feasible = units.stream().filter(unit -> {
                return unit.memberCount <= count && Range.of(3 - rankDelta, 3 + rankDelta).contains(unit.rank)
                        && Range.of(50.0 - skillDelta, 50.0 + skillDelta).contains(unit.skill);
            }).sorted(new Comparator<>() {
                @Override
                public int compare(MatchUnitMinimal o1, MatchUnitMinimal o2) {
                    var rankGap1 = abs(o1.rank - 3);
                    var rankGap2 = abs(o2.rank - 3);
                    if (rankGap1 == rankGap2) {
                        var skillGap1 = abs(o1.skill - 50.0);
                        var skillGap2 = abs(o2.skill - 50.0);
                        return Double.compare(skillGap1, skillGap2);
                    }
                    return rankGap1 - rankGap2;
                }
            }).limit(20).toList();
//            System.out.println("Query: " + count + ", " + rankDelta + ", " + skillDelta);
//            feasible.forEach(System.out::println);
        });
        System.out.println("query time(ms): " + (System.currentTimeMillis() - start));
    }

    static ArrayList<MatchUnitMinimal> createUnits(int count) {
        long start = System.currentTimeMillis();
        ArrayList<MatchUnitMinimal> units = new ArrayList<>();
        for (int i = 1; i < count; i++) {
            units.add(new MatchUnitMinimal(i, TestUtilities.RANDOM_GENERATOR.nextInt(5) + 1,
                    TestUtilities.RANDOM_GENERATOR.nextInt(10), TestUtilities.RANDOM_GENERATOR.nextDouble() * 100));
        }
        System.out.println("create time(ms): " + (System.currentTimeMillis() - start));
        return units;
    }
}
