package dev.carbonshow.algorithm.partition;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMaxPartitionsTest {
    final private Map<Integer, Integer> ADDENDS = Map.of(1, 100, 2, 40, 5, 10);
    final private int PARTITIONED = 10;
    final private ArrayList<Map<Integer, Long>> PARTITION_PLANS = new ArrayList<>(Arrays.asList(Map.of(1, 6L, 2, 2L),
            Map.of(1, 1L, 2, 2L, 5, 1L),
            Map.of(5,2L),
            Map.of(1,5L,5,1L)
            ));

    @Test
    void solve() {
        DefaultMaxPartitions programmingMaxPartition = new DefaultMaxPartitions();
        var partitions = programmingMaxPartition.solve(ADDENDS, PARTITIONED);
        assertNotNull(partitions);
        for (var data : partitions) {
            System.out.printf("%s\n", data);
        }

        // 校验方案总数
        int totalPartitionCount = PartitionData.totalPartitionsCount(partitions);
        assertEquals(totalPartitionCount, 23);

        // 校验加数使用数量限制
        var addendUseCounts = PartitionData.addendsUsedCount(partitions);
        for (Map.Entry<Integer, Long> entry : addendUseCounts.entrySet()) {
            assertTrue(entry.getValue() <= ADDENDS.get(entry.getKey()));
        }
    }

    @Test
    void solveWithPartitionPlan() {
        DefaultMaxPartitions programmingMaxPartition = new DefaultMaxPartitions();
        var partitions = programmingMaxPartition.solveWithPartitionPlan(ADDENDS, PARTITIONED, PARTITION_PLANS);
        assertNotNull(partitions);
        for (var data : partitions) {
            System.out.printf("%s\n", data);
        }
    }
}
