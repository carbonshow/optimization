package dev.carbonshow.algorithm;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class IntegerLinearProgramming {
  public static void main(String[] args) {
    Loader.loadNativeLibraries();

    // 基于 SCIP 实现的混合型整数规划优化器，同时支持线性和非线性规划
    MPSolver solver = MPSolver.createSolver("SCIP");
    if (solver == null) {
      System.out.println("Could not create solver SCIP");
      return;
    }

    // 约束条件：
    // x + 7y <= 17.5
    // 0 <= x <= 3.5
    // 0 <= y
    // x, y 均为整数
    double infinity = java.lang.Double.POSITIVE_INFINITY;
    // x and y are integer non-negative variables.
    MPVariable x = solver.makeIntVar(0.0, infinity, "x");
    MPVariable y = solver.makeIntVar(0.0, infinity, "y");

    System.out.println("Number of variables = " + solver.numVariables());

    // x + 7 * y <= 17.5.
    MPConstraint c0 = solver.makeConstraint(-infinity, 17.5, "c0");
    c0.setCoefficient(x, 1);
    c0.setCoefficient(y, 7);

    // x <= 3.5.
    MPConstraint c1 = solver.makeConstraint(-infinity, 3.5, "c1");
    c1.setCoefficient(x, 1);
    c1.setCoefficient(y, 0);

    System.out.println("Number of constraints = " + solver.numConstraints());

    // 待优化的目标函数 x + 10y ，最大化
    MPObjective objective = solver.objective();
    objective.setCoefficient(x, 1);
    objective.setCoefficient(y, 10);
    objective.setMaximization();

    // 定义规划器
    final MPSolver.ResultStatus resultStatus = solver.solve();
    if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
      System.out.println("Solution:");
      System.out.println("Objective value = " + objective.value());
      System.out.println("x = " + x.solutionValue());
      System.out.println("y = " + y.solutionValue());
    } else {
      System.err.println("The problem does not have an optimal solution!");
    }
  }
}
