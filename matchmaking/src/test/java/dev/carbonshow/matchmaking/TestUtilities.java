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

        MatchUnitTimeVaryingParameters timeVaryingParameters = new MatchUnitTimeVaryingParameters(enterTimestamp, RANDOM.nextInt(CRITERIA.maxRank() / 2) + 1, RANDOM.nextDouble(0.0, 5.0));

        return new MatchUnit(COUNTER.addAndGet(1L), members,
                positions,
                RANDOM.nextDouble(0.0, 1.0),
                relayLatencies,
                timeVaryingParameters
        );
    }
}
