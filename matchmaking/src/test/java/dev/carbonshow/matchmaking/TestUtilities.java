package dev.carbonshow.matchmaking;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.MatchUnitTimeVaryingParameters;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

public class TestUtilities {
    public static final AtomicLong COUNTER = new AtomicLong(0L);
    public static final AtomicLong ID = new AtomicLong(0L);
    public static final MatchMakingCriteria CRITERIA = new MatchMakingCriteria(2, 5, 10, 5, 2);
    public static final Random RANDOM = new Random(12306);

    public static MatchUnit createMatchUnit() {
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

        MatchUnitTimeVaryingParameters timeVaryingParameters = new MatchUnitTimeVaryingParameters(enterTimestamp,
                RANDOM.nextInt(CRITERIA.maxRank() / 4) + 1,
                RANDOM.nextDouble(0.0, 1.0) + 3, positions);

        return new MatchUnit(COUNTER.addAndGet(1L), members,
                RANDOM.nextDouble(0.0, 1.0),
                relayLatencies,
                timeVaryingParameters
        );
    }

    public static ArrayList<MatchUnit> getFeasibleMatchUnit() {
        long currentTimestamp = Instant.now().getEpochSecond();
        BitSet positions = new BitSet(CRITERIA.maxPositions());
        for (int i = 0; i < CRITERIA.maxPositions(); i++) {
            positions.set(i);
        }

        MatchUnitTimeVaryingParameters timeVaryingParameters = new MatchUnitTimeVaryingParameters(currentTimestamp, 2, 1.0, positions);

        Map<Integer, Integer> relayLatencies = new HashMap<>();
        for (int i = 0; i < CRITERIA.maxRelayGroups(); i++) {
            relayLatencies.put(i, RANDOM.nextInt(999));
        }

        var members1 = new ArrayList<Long>(List.of(1L));
        var members2 = new ArrayList<Long>(List.of(2L, 3L));
        var members3 = new ArrayList<Long>(List.of(4L, 5L, 6L));
        var members4 = new ArrayList<Long>(List.of(7L, 8L, 9L, 10L));
        var members5 = new ArrayList<Long>(List.of(11L, 12L, 13L, 14L, 15L));
        return new ArrayList<MatchUnit>(List.of(
                new MatchUnit(11L, members1, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(12L, members1, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(13L, members1, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(14L, members1, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(15L, members1, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(16L, members1, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(17L, members1, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(18L, members1, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(19L, members1, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(10L, members1, 0.5, relayLatencies, timeVaryingParameters),

                new MatchUnit(21L, members2, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(22L, members2, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(23L, members2, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(24L, members2, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(25L, members2, 0.5, relayLatencies, timeVaryingParameters),

                new MatchUnit(31L, members3, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(32L, members3, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(33L, members3, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(34L, members3, 0.5, relayLatencies, timeVaryingParameters),

                new MatchUnit(41L, members4, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(42L, members4, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(43L, members4, 0.5, relayLatencies, timeVaryingParameters),

                new MatchUnit(51L, members5, 0.5, relayLatencies, timeVaryingParameters),
                new MatchUnit(52L, members5, 0.5, relayLatencies, timeVaryingParameters)
                ));
    }
}
