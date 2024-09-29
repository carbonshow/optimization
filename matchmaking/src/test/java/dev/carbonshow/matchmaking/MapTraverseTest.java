package dev.carbonshow.matchmaking;

import org.apache.commons.math3.util.Combinations;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.IntStream;

public class MapTraverseTest {

    @Test
    void testLinkedHashMapOrder() {
        var linkedMap = new LinkedHashMap<Integer, String>();
        linkedMap.put(1, "one");
        linkedMap.put(2, "two");
        linkedMap.put(3, "three");
        linkedMap.put(4, "four");
        linkedMap.put(5, "five");
        linkedMap.put(6, "six");

        System.out.println(linkedMap.keySet());
        System.out.println(linkedMap.values());
    }

    @Test
    void testHashMapOrder() {
        var map = new HashMap<Integer, String>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");
        map.put(4, "four");
        map.put(5, "five");
        map.put(6, "six");

        System.out.println(map.keySet());
        System.out.println(map.values());
    }

    @Test
    void testCombination() {
        for (int[] ints : new Combinations(10, 3)) {
            System.out.println(Arrays.toString(ints));
        }
    }
}