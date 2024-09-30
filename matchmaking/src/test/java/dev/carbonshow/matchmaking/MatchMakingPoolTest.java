package dev.carbonshow.matchmaking;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.MatchUnitTimeVaryingParameters;
import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.config.TimeVaryingConfig;
import dev.carbonshow.matchmaking.pool.MatchMakingPoolGraph;
import dev.carbonshow.matchmaking.pool.MatchUnit;
import dev.carbonshow.matchmaking.solver.MatchMakingCPSolver;
import dev.carbonshow.matchmaking.solver.MatchMakingDecomposeSolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

class MatchMakingPoolTest {
    private final AtomicLong COUNTER = new AtomicLong(0L);
    private final AtomicLong ID = new AtomicLong(0L);
    private final MatchMakingCriteria CRITERIA = new MatchMakingCriteria(2, 5, 10, 5, 2);
    private final Random RANDOM = new Random(12306);
    private final SolverParameters SOLVER_PARAMETERS_LIMIT = new SolverParameters(1, 50, 30);
    private final SolverParameters SOLVER_PARAMETERS = new SolverParameters(-1, -1, 999999);

    @Tag("100Units")
    @Test
    void matchMakingWithTimeLimit() {
        Assertions.assertDoesNotThrow(() -> {
            var pool = new MatchMakingPoolGraph(CRITERIA, "test", TimeVaryingConfig.defaultVal());

            System.out.println("[Create Units]");
            for (int i = 0; i < 100; i++) {
                var unit = createMatchUnit();
                System.out.println(unit);
                assertTrue(pool.addMatchUnit(unit));
            }

            long start = System.currentTimeMillis();
            var solver = new MatchMakingCPSolver(CRITERIA, "test", TimeVaryingConfig.defaultVal());
            var solutions = solver.solve(pool, SOLVER_PARAMETERS_LIMIT, Instant.now().getEpochSecond());
            System.out.println("\nElapsed time of solving: " + (System.currentTimeMillis() - start) + "ms");
            int i = 1;
            ArrayList<Long> unitIds = new ArrayList<>();

            System.out.println("\n[Solutions]");
            for (var game : solutions.results()) {
                System.out.println("\nGame " + i);
                int j = 1;
                for (var team : game) {
                    System.out.println("  Team " + j);
                    int userCountPerTeam = 0;
                    for (var unitId : team) {
                        var unit = pool.getMatchUnit(unitId);
                        assertNotNull(unit);
                        userCountPerTeam += unit.userCount();
                        unitIds.add(unitId);
                        System.out.println("    Unit " + unit);
                    }
                    j++;

                    assertEquals(userCountPerTeam, CRITERIA.userCountPerTeam(), "user count per team should be correct");
                }
                i++;

                assertEquals(game.size(), CRITERIA.teamCountPerGame(), "team count per game should be correct");
            }

            // match unit should be unique
            assertEquals(new HashSet<>(unitIds).size(), unitIds.size(), "match unit should be allocated in one game once");
        });
    }

    @Test
    void testDecomposeSolver() {
        Assertions.assertDoesNotThrow(() -> {
            var pool = new MatchMakingPoolGraph(CRITERIA, "test", TimeVaryingConfig.defaultVal());
            System.out.println("[Create Units]");
            for (int i = 0; i < 100; i++) {
                var unit = createMatchUnit();
                assertTrue(pool.addMatchUnit(unit));
                System.out.println("Unit :" + unit);
            }

            long start = System.currentTimeMillis();
            var solver = new MatchMakingDecomposeSolver(CRITERIA, "test", TimeVaryingConfig.defaultVal());
            var solutions = solver.solve(pool, SOLVER_PARAMETERS, Instant.now().getEpochSecond());
            assertNull(solutions);

            var end = System.currentTimeMillis();
            System.out.println("\nElapsed time of solving: " + (end - start) + "ms");
        });
    }

    private MatchUnit createMatchUnit() {
        final int timeWindow = 10 * 60;
        final long timeBase = Instant.now().getEpochSecond() - timeWindow;
        final long enterTimestamp = timeBase + RANDOM.nextInt(timeWindow);

        final int memberCount = RANDOM.nextInt(CRITERIA.userCountPerTeam()) + 1;
        final var members = new ArrayList<>(LongStream.rangeClosed(1, memberCount).map(i -> ID.addAndGet(1L)).boxed().toList());

        BitSet positions = new BitSet(CRITERIA.maxPositions());
        for (int i = 0; i < CRITERIA.maxPositions(); i++) {
            if (RANDOM.nextBoolean()) {
                positions.set(i);
            }
        }

        Map<Integer, Integer> relayLatencies = new HashMap<>();
        for (int i = 0; i < CRITERIA.maxRelayGroups(); i++) {
            relayLatencies.put(i, RANDOM.nextInt(999));
        }

        MatchUnitTimeVaryingParameters timeVaryingParameters = new MatchUnitTimeVaryingParameters(enterTimestamp, RANDOM.nextInt(CRITERIA.maxRank()/2) + 1, RANDOM.nextDouble(0.0, 5.0));

        return new MatchUnit(COUNTER.addAndGet(1L), members,
                positions,
                RANDOM.nextDouble(0.0, 1.0),
                relayLatencies,
                timeVaryingParameters
        );
    }
}
