package dev.carbonshow.matchmaking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

class MatchMakingPoolInMemDBTest {
    private final AtomicLong COUNTER = new AtomicLong();
    private final MatchMakingCriteria CRITERIA = new MatchMakingCriteria(2, 5, 90, 5);
    private final int TIME_WINDOW = 10 * 60;
    private final Random RANDOM = new Random();

    @Test
    void addMatchUnit() {
        Assertions.assertDoesNotThrow(() -> {
            var poolInMemDB = new MatchMakingPoolInMemDB(CRITERIA, "test");
            poolInMemDB.addMatchUnit(createMatchUnit());
        });
    }

    private MatchUnit createMatchUnit() {
        final long timeBase = Instant.now().getEpochSecond() - TIME_WINDOW;
        final long enterTimestamp = timeBase + RANDOM.nextInt(TIME_WINDOW);

        final int memberCount = RANDOM.nextInt(CRITERIA.userCountPerTeam()) + 1;
        final var members = new ArrayList<Long>(LongStream.rangeClosed(1, memberCount).boxed().toList());

        BitSet positions = new BitSet(CRITERIA.maxPositions());
        for (int i = 0; i < CRITERIA.maxPositions(); i++) {
            if (RANDOM.nextBoolean()) {
                positions.set(i);
            }
        }

        Map<Integer, Integer> relayLatencies = new HashMap<Integer, Integer>();
        for (int i = 0; i < CRITERIA.maxPositions(); i++) {
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