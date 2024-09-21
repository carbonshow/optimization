package dev.carbonshow.matchmaking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchMakingPoolInMemDBTest {
    private final AtomicLong COUNTER = new AtomicLong(1L);
    private final AtomicLong ID = new AtomicLong(1L);
    private final MatchMakingCriteria CRITERIA = new MatchMakingCriteria(2, 5, 90, 5, 2);
    private final Random RANDOM = new Random();

    @Test
    void addMatchUnit() {
        Assertions.assertDoesNotThrow(() -> {
            var poolInMemDB = new MatchMakingPoolInMemDB(CRITERIA, "test");

            for (int i = 0; i < 50; i++) {
                assertTrue(poolInMemDB.addMatchUnit(createMatchUnit()));
            }

            var solver = new MatchMakingCPSolver();
            var solutions = solver.solve(poolInMemDB);
            int i = 1;
            for (var game: solutions.results()) {
                System.out.println("\nGame " + i);
                int j = 1;
                for (var team: game) {
                    System.out.println("  Team " + j);
                    for (var unitId: team) {
                        var unit = poolInMemDB.getMatchUnit(unitId);
                        assertNotNull(unit);
                        System.out.println("    Unit " + unit);
                    }
                    j++;
                }
                i++;
            }
        });
    }

    private MatchUnit createMatchUnit() {
        final int timeWindow = 10 * 60;
        final long timeBase = Instant.now().getEpochSecond() - timeWindow;
        final long enterTimestamp = timeBase + RANDOM.nextInt(timeWindow);

        final int memberCount = RANDOM.nextInt(CRITERIA.userCountPerTeam()) + 1;
        final var members = new ArrayList<>( LongStream.rangeClosed(1, memberCount).map(i -> ID.addAndGet(1L)).boxed().toList());

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

        return new MatchUnit(COUNTER.addAndGet(1L), enterTimestamp, members,
                RANDOM.nextInt(CRITERIA.maxRank()+1), positions,
                RANDOM.nextDouble(0.0, 100.0),
                RANDOM.nextDouble(0.0, 1.0),
                relayLatencies
                );
    }
}
