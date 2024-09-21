package dev.carbonshow.algorithm.partition;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

class IntegerPartitionTest {
  // 组队问题
  static long[] group3 = new long[]{1, 2, 3};
  static long[] group5 = LongStream.rangeClosed(1, 5).toArray();
  static long[] group10 = LongStream.rangeClosed(1, 10).toArray();
  static long[] group100 = LongStream.rangeClosed(1, 100).toArray();

  // 硬币问题
  static long[] coin1 = new long[]{2, 3, 4};
  static long[] coin2 = new long[]{2, 5, 8, 9, 10};
  static long[] coin3 = new long[]{8, 9, 10};

  // 基于自底向上动态规划的求解器
  DPIntegerPartition tableFillResolver = new DPIntegerPartition(DPIntegerPartition.DPImplementMethod.TABLE_FILL);

  // 基于自顶向下动态规划的求解器
  DPIntegerPartition recursiveResolver = new DPIntegerPartition();

  // 基于 CP-SAT 的求解器
  CPIntegerPartition cpResolver = new CPIntegerPartition();

  @Tag("partition")
  @Tag("dynamic-programming")
  @Test
  public void testPartitionUseDP() {
    assertEquals(tableFillResolver.solve(group3, 3), 3);
    assertEquals(tableFillResolver.solve(group5, 5), 7);
    assertEquals(tableFillResolver.solve(group10, 10), 42);
    System.out.println(tableFillResolver.solve(group100, 100));
  }

  @Tag("partition")
  @Tag("recursive")
  @Test
  public void testPartitionUseRecursive() {
    assertEquals(recursiveResolver.solve(group3, 3), 3);
    assertEquals(recursiveResolver.solve(group5, 5), 7);
    assertEquals(recursiveResolver.solve(group10, 10), 42);

    var result = recursiveResolver.solveWithPartitions(group100, 100);
    System.out.println("partitions: " + result.size());
  }


  @Tag("partition")
  @Tag("cp-sat")
  @Test
  public void testPartitionUseCP() {
    var result = cpResolver.solveWithPartitions(group10, 10);
    System.out.println("partitions: " + result.size());
    for (var partition: result) {
      System.out.println(partition);
    }
  }

  @Tag("partition100")
  @Tag("cp-sat")
  @Test
  public void testPartitionUseCP100() {
    var result = cpResolver.solve(group100, 100);
    System.out.println("result size: " + result);
  }

  @Tag("coins")
  @Tag("dynamic-programming")
  @Test
  public void testCoinsUseDP() {
    assertEquals(tableFillResolver.solve(coin1, 6), 3);
    assertEquals(tableFillResolver.solve(coin2, 10), 4);
    assertEquals(tableFillResolver.solve(coin3, 10), 1);
  }

  @Tag("coins")
  @Tag("recursive")
  @Test
  public void testCoinsUseRecursive() {
    assertEquals(recursiveResolver.solve(coin1, 6), 3);
    assertEquals(recursiveResolver.solve(coin2, 10), 4);
    assertEquals(recursiveResolver.solve(coin3, 10), 1);
  }

  @Tag("coins")
  @Tag("paths")
  @Tag("recursive")
  @Test
  public void testCoinsUseRecursiveWithPaths() {
    var paths = recursiveResolver.solveWithPartitions(coin2, 10L);

    ArrayList<ArrayList<Long>> realPaths = new ArrayList<>();
    realPaths.add(new ArrayList<>(List.of(10L)));
    realPaths.add(new ArrayList<>(List.of(8L, 2L)));
    realPaths.add(new ArrayList<>(List.of(5L, 5L)));
    realPaths.add(new ArrayList<>(List.of(2L, 2L, 2L, 2L, 2L)));

    assertEquals(paths, realPaths);
  }

  @Tag("partition")
  @Tag("paths")
  @Tag("recursive")
  @Test
  public void testPartitionUseRecursiveWithPaths() {
    var paths = recursiveResolver.solveWithPartitions(group5, 5L);

    ArrayList<ArrayList<Long>> realPaths = new ArrayList<>();
    realPaths.add(new ArrayList<>(List.of(5L)));
    realPaths.add(new ArrayList<>(List.of(4L, 1L)));
    realPaths.add(new ArrayList<>(List.of(3L, 2L)));
    realPaths.add(new ArrayList<>(List.of(3L, 1L, 1L)));
    realPaths.add(new ArrayList<>(List.of(2L, 2L, 1L)));
    realPaths.add(new ArrayList<>(List.of(2L, 1L, 1L, 1L)));
    realPaths.add(new ArrayList<>(List.of(1L, 1L, 1L, 1L, 1L)));

    assertEquals(paths, realPaths);

    paths = cpResolver.solveWithPartitions(group5, 5);
    assertEquals(paths.size(), realPaths.size());
    for (var path: paths) {
      System.out.printf("Partition: %s\n", path.toString());
    }

    paths = cpResolver.solveWithPartitions(group10, 10);
    assertEquals(paths.size(), 42);
    for (var path: paths) {
      System.out.printf("Partition: %s\n", path.toString());
    }
  }

}
