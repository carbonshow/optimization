package dev.carbonshow.algorithm;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TwoPhaseMaxPartitionTest {
  final private Map<Integer, Integer> ADDENDS = Map.of(1,100, 2, 40, 5,10);
  final private int PARTITIONED = 10;

  @Test
  void solveWithPartitions() {
    TwoPhaseMaxPartition programmingMaxPartition = new TwoPhaseMaxPartition();
    var partitions = programmingMaxPartition.solveWithPartitions(ADDENDS, PARTITIONED);
    assertNotNull(partitions);
    for (var data: partitions) {
      System.out.printf("%s\n", data);
    }

    // 校验方案总数
    int totalPartitionCount = PartitionData.totalPartitionsCount(partitions);
    assertEquals(totalPartitionCount, 23);

    // 校验加数使用数量限制
    var addendUseCounts = PartitionData.addendsUsedCount(partitions);
    for (Map.Entry<Integer, Long> entry: addendUseCounts.entrySet()) {
      assertTrue(entry.getValue() <= ADDENDS.get(entry.getKey()));
    }

  }
}
