package dev.carbonshow.matchmaking;

import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RangeIntersectionTest {

    private static final int MAX_RANK = 100;
    private static final Random random = new Random();


    @Test
    void cliqueFinder100() {
        final int rangeCount = 100;
        var ranges = initRange(rangeCount);
        ranges.forEach(range -> System.out.println(range.toString()));

//        ArrayList<ArrayList<Range<Integer>>> cliques = new ArrayList<>();
//        for (int i = 0; i < rangeCount - 1; i++) {
//            for (int j = i + 1; j < rangeCount; j++) {
//
//            }
//        }
    }

    @Test
    void cliqueFinder500() {
        final int rangeCount = 500;
        var ranges = initRange(rangeCount);
        assertEquals(ranges.size(), rangeCount);
    }

    private ArrayList<Range<Integer>> initRange(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(i -> generateRange()).collect(Collectors.toCollection(ArrayList::new));
    }

    private static Range<Integer> generateRange() {
        var start = random.nextInt(MAX_RANK / 2);
        var end = start + random.nextInt(MAX_RANK / 2);
        return Range.of(start, end);
    }
}
