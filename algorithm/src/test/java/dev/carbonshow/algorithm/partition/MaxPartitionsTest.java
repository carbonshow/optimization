package dev.carbonshow.algorithm.partition;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MaxPartitionsTest {
    final private Map<Long, Long> ADDENDS = Map.of(1L, 100L, 2L, 40L, 5L, 10L);
    final private long PARTITIONED = 10L;
    final private ArrayList<Map<Long, Long>> PARTITION_PLANS = new ArrayList<>(Arrays.asList(Map.of(1L, 6L, 2L, 2L),
            Map.of(1L, 1L, 2L, 2L, 5L, 1L),
            Map.of(5L,2L),
            Map.of(1L,5L,5L,1L)
            ));

    @Tag("TwoPhase")
    @Test
    void solveTwoPhase() {
        DefaultMaxPartitions solver = new DefaultMaxPartitions();
        var partitions = solver.solve(ADDENDS, PARTITIONED);
        validate(partitions);
    }

    @Tag("TwoPhase")
    @Test
    void solveWithPartitionPlanTwoPhase() {
        DefaultMaxPartitions solver = new DefaultMaxPartitions();
        var partitions = solver.solveWithPartitionPlan(ADDENDS, PARTITIONED, PARTITION_PLANS);
        assertNotNull(partitions);
        for (var data : partitions) {
            System.out.printf("%s\n", data);
        }
    }

    @Tag("IntegerProgramming")
    @Test
    void solveIntegerProgramming() {
        IntegerProgrammingMaxPartitions solver = new IntegerProgrammingMaxPartitions();
        var partitions = solver.solve(ADDENDS, PARTITIONED);
        validate(partitions);
    }

    // 划分结果的校验逻辑
    private void validate(ArrayList<PartitionData> partitions){
        assertNotNull(partitions);
        for (var data : partitions) {
            System.out.printf("%s\n", data);
        }

        // 校验方案总数
        var totalPartitionCount = PartitionData.totalPartitionsCount(partitions);
        assertEquals(totalPartitionCount, 23);

        // 校验加数使用数量限制
        var addendUseCounts = PartitionData.addendsUsedCount(partitions);
        for (var entry : addendUseCounts.entrySet()) {
            assertTrue(entry.getValue() <= ADDENDS.get(entry.getKey()));
        }
    }
}
