package dev.carbonshow.matchmaking;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.MatchUnitTimeVaryingParameters;
import dev.carbonshow.matchmaking.pool.MatchUnit;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class TestUtilities {
    public static final AtomicLong COUNTER = new AtomicLong(0L);
    public static final AtomicLong ID = new AtomicLong(0L);
    public static final MatchMakingCriteria CRITERIA = new MatchMakingCriteria(2, 5, 10, 5, 2);
    public static final double MAX_SKILL = 100.0;
    public static final RandomGenerator RANDOM_GENERATOR = new JDKRandomGenerator(42);
    public static final NormalDistribution RANK_DISTRIBUTION = new NormalDistribution(RANDOM_GENERATOR, CRITERIA.maxRank() / 2.0, CRITERIA.maxRank() / 6.0);
    public static final NormalDistribution SKILL_DISTRIBUTION = new NormalDistribution(RANDOM_GENERATOR, MAX_SKILL / 2, MAX_SKILL / 6);

    public static MatchUnit createMatchUnit() {
        final int timeWindow = 5 * 60;
        final long timeBase = Instant.now().getEpochSecond() - timeWindow;
        final long enterTimestamp = timeBase + RANDOM_GENERATOR.nextInt(timeWindow);

        final int memberCount = RANDOM_GENERATOR.nextInt(CRITERIA.userCountPerTeam()) + 1;
        final var members = new ArrayList<>(LongStream.rangeClosed(1, memberCount).map(i -> ID.addAndGet(1L)).boxed().toList());

        BitSet positions = new BitSet(CRITERIA.maxPositions());
        for (int i = 0; i < CRITERIA.maxPositions(); i++) {
            if (RANDOM_GENERATOR.nextBoolean()) {
                positions.set(i);
            }
        }

        Map<Integer, Integer> relayLatencies = new HashMap<>();
        for (int i = 0; i < CRITERIA.maxRelayGroups(); i++) {
            relayLatencies.put(i, RANDOM_GENERATOR.nextInt(999));
        }

        MatchUnitTimeVaryingParameters timeVaryingParameters = new MatchUnitTimeVaryingParameters(enterTimestamp,
                genRank(), genSkill(), positions);

        return new MatchUnit(COUNTER.addAndGet(1L), members,
                RANDOM_GENERATOR.nextDouble(),
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
            relayLatencies.put(i, RANDOM_GENERATOR.nextInt(999));
        }

        var members1 = new ArrayList<>(List.of(1L));
        var members2 = new ArrayList<>(List.of(2L, 3L));
        var members3 = new ArrayList<>(List.of(4L, 5L, 6L));
        var members4 = new ArrayList<>(List.of(7L, 8L, 9L, 10L));
        var members5 = new ArrayList<>(List.of(11L, 12L, 13L, 14L, 15L));

        var unitsWithMember1 = IntStream.rangeClosed(1, 10).mapToObj(i -> new MatchUnit(10 + i - 1L, members1, 0.5, relayLatencies, timeVaryingParameters));
        var unitsWithMember2 = IntStream.rangeClosed(1, 5).mapToObj(i -> new MatchUnit(20 + i - 1L, members2, 0.5, relayLatencies, timeVaryingParameters));
        var unitsWithMember3 = IntStream.rangeClosed(1, 4).mapToObj(i -> new MatchUnit(30 + i - 1L, members3, 0.5, relayLatencies, timeVaryingParameters));
        var unitsWithMember4 = IntStream.rangeClosed(1, 3).mapToObj(i -> new MatchUnit(40 + i - 1L, members4, 0.5, relayLatencies, timeVaryingParameters));
        var unitsWithMember5 = IntStream.rangeClosed(1, 2).mapToObj(i -> new MatchUnit(50 + i - 1L, members5, 0.5, relayLatencies, timeVaryingParameters));

        return new ArrayList<>(Stream.of(unitsWithMember1, unitsWithMember2, unitsWithMember3, unitsWithMember4, unitsWithMember5).flatMap(i -> i).toList());
    }

    static int genRank() {
        var value = RANK_DISTRIBUTION.sample();
        if (value < 0) {
            return 1;
        } else if (value > CRITERIA.maxRank()) {
            return CRITERIA.maxRank();
        } else {
            return (int) value;
        }
    }

    static double genSkill() {
        var value = SKILL_DISTRIBUTION.sample();
        if (value < 0) {
            return 1.0;
        } else return Math.min(value, MAX_SKILL);
    }
}
