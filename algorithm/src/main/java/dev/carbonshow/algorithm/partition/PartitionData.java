package dev.carbonshow.algorithm.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 划分数据
 * @param addends 划分方案，由加数Map表示，key 是加数，value 是加数出现的次数
 * @param count 当前划分方案在在实际划分结果中出现的次数
 */
public record PartitionData(Map<Long, Long> addends, long count) {
  /**
   * 加数在当前划分方案中出现的次数，比如：凑 10，加数有 1，2，5，一种划分方案是：(1+1+1) + 2 + 5。
   * 那么 1 在当前划分方案中的出现次数为 3.
   * @param addend 加数
   * @return 返回加数在当前划分方案中出现次数，如果加数不存在返回 0
   */
  public Long usedCountOnePartition(Long addend) {
    return addends.getOrDefault(addend, 0L);
  }

  /**
   * 加数在划分方案的所有实施实例中，出现的总次数，比如：凑 10，加数有 1，2，5，一种划分方案是：(1+1+1) + 2 + 5。
   * 该划分方案在实际划分结果中出现了 4 次，那么 1 的总的出现次数是 3*4 = 12。
   * @param addend 加数
   * @return 加数在划分方案的所有实施实例中出现的总次数，加数不存在则返回 0
   */
  public Long usedCountTotal(Long addend) {
    return usedCountOnePartition(addend) * count;
  }

  /**
   * 统计指定的划分结果中，所有出现过的加数及其出现的总次数
   *
   * @param partitions 划分数据列表，每个元素表示一个不同的划分数据，包含划分方案及其实例化数量
   * @return Map 形式的统计结果，key 是出现的加数，value 是该加数在各个划分方案所有实施实例中出现的次数之和
   */
  public static Map<Long,Long> addendsUsedCount(ArrayList<PartitionData> partitions) {
    HashMap<Long, Long> addendUseCounts = new HashMap<>();
    for (var partition: partitions) {
      for (var addendData: partition.addends().entrySet()) {
        var addend = addendData.getKey();
        var addendCount = addendData.getValue();
        var sum = addendUseCounts.getOrDefault(addend, 0L) + addendCount*partition.count();
        addendUseCounts.put(addend, sum);
      }
    }

    return addendUseCounts;
  }

  /**
   * 返回各划分方案所有实施实例的总数
   * @param partitions 划分数据列表，每个元素表示一个不同的划分数据，包含划分方案及其实例化数量
   * @return 划分结果中个划分实例的总数
   */
  public static long totalPartitionsCount(ArrayList<PartitionData> partitions) {
    return partitions.stream().map(PartitionData::count).mapToLong(Long::longValue).sum();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("Partition: [");
    for (var addend : addends.keySet()) {
      builder.append("(").append(addend).append(",").append(addends.get(addend)).append("), ");
    }
    builder.append("], Count: ").append(count);
    return builder.toString();
  }
}
