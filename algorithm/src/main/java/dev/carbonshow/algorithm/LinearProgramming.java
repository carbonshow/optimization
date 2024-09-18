package dev.carbonshow.algorithm;

import org.apache.commons.math4.legacy.optim.PointValuePair;
import org.apache.commons.math4.legacy.optim.linear.*;
import org.apache.commons.math4.legacy.optim.nonlinear.scalar.GoalType;

import java.util.ArrayList;
import java.util.Arrays;

public class LinearProgramming {
    public static void main(String[] args) {

        // 定义目标函数，有两个决定变量x1 和 x2，最小化z = 0.6x1 + 0.35x2
        LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(new double[]{0.6, 0.35}, 0.0);

        // 定义约束条件
        var constraints = new ArrayList<LinearConstraint>();

        // 关于 x1 和 x2 的约束
        constraints.add(new LinearConstraint(new double[]{5,7}, Relationship.GEQ, 8));
        constraints.add(new LinearConstraint(new double[]{4,2}, Relationship.GEQ, 15));
        constraints.add(new LinearConstraint(new double[]{3,1}, Relationship.GEQ, 3));

        // 决定变量自身的约束，均不能小于 0
        constraints.add(new LinearConstraint(new double[]{1,0}, Relationship.GEQ, 0));
        constraints.add(new LinearConstraint(new double[]{0,1}, Relationship.GEQ, 0));

        // 创建求解器
        PointValuePair solution = new SimplexSolver().optimize(objectiveFunction, new LinearConstraintSet(constraints), GoalType.MINIMIZE);
        if (solution != null) {
            double minValue = solution.getValue();
            System.out.println("points: " + Arrays.toString(solution.getPoint()));
            System.out.println("the optimization value: " + minValue);
        }
    }
}
