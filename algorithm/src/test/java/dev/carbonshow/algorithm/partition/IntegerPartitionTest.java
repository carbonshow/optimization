package dev.carbonshow.algorithm.partition;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntegerPartitionTest {
  // 组队问题
  static int[] group3 = new int[]{1, 2, 3};
  static int[] group5 = new int[]{1, 2, 3, 4, 5};
  static int[] group10 = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

  // 硬币问题
  static int[] coin1 = new int[]{2, 3, 4};
  static int[] coin2 = new int[]{2, 5, 8, 9, 10};
  static int[] coin3 = new int[]{8, 9, 10};

  // 基于动态规划的求解器
  DPIntegerPartition tableFillResolver = new DPIntegerPartition(DPIntegerPartition.DPImplementMethod.TABLE_FILL);

  // 基于循环递归方式的求解器
  DPIntegerPartition recursiveResolver = new DPIntegerPartition();

  @Tag("partition")
  @Tag("dynamic-programming")
  @Test
  public void testPartitionUseDP() {
    assertEquals(tableFillResolver.solve(group3, 3), 3);
    assertEquals(tableFillResolver.solve(group5, 5), 7);
    assertEquals(tableFillResolver.solve(group10, 10), 42);
  }

  @Tag("partition")
  @Tag("recursive")
  @Test
  public void testPartitionUseRecursive() {
    assertEquals(recursiveResolver.solve(group3, 3), 3);
    assertEquals(recursiveResolver.solve(group5, 5), 7);
    assertEquals(recursiveResolver.solve(group10, 10), 42);
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
    var paths = recursiveResolver.solveWithPartitions(coin2, 10);

    ArrayList<ArrayList<Integer>> realPaths = new ArrayList<>();
    realPaths.add(new ArrayList<>(List.of(10)));
    realPaths.add(new ArrayList<>(List.of(8, 2)));
    realPaths.add(new ArrayList<>(List.of(5, 5)));
    realPaths.add(new ArrayList<>(List.of(2, 2, 2, 2, 2)));

    assertEquals(paths, realPaths);
  }

  @Tag("partition")
  @Tag("paths")
  @Tag("recursive")
  @Test
  public void testPartitionUseRecursiveWithPaths() {
    var paths = recursiveResolver.solveWithPartitions(group5, 5);

    ArrayList<ArrayList<Integer>> realPaths = new ArrayList<>();
    realPaths.add(new ArrayList<>(List.of(5)));
    realPaths.add(new ArrayList<>(List.of(4, 1)));
    realPaths.add(new ArrayList<>(List.of(3, 2)));
    realPaths.add(new ArrayList<>(List.of(3, 1, 1)));
    realPaths.add(new ArrayList<>(List.of(2, 2, 1)));
    realPaths.add(new ArrayList<>(List.of(2, 1, 1, 1)));
    realPaths.add(new ArrayList<>(List.of(1, 1, 1, 1, 1)));

    assertEquals(paths, realPaths);
  }

}
