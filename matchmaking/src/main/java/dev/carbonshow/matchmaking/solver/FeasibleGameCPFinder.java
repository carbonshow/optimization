package dev.carbonshow.matchmaking.solver;

import com.google.ortools.sat.*;
import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Constraint-Programming ，根据给定的可用队伍寻找可用 Game
 */
public class FeasibleGameCPFinder implements FeasibleGameFinder {

    private final MatchMakingCriteria criteria;
    private final MatchUnitOperator operator;

    public FeasibleGameCPFinder(MatchMakingCriteria matchMakingCriteria, MatchUnitOperator matchUnitOperator) {
        criteria = matchMakingCriteria;
        operator = matchUnitOperator;
    }

    @Override
    public List<FeasibleGame> solve(MatchUnit[] units, List<FeasibleTeam> teams, SolverParameters parameters, long currentTimestamp) {
        CpModel model = new CpModel();

        // 决策变量是 teams 可以分配到同一个 Game 中
        final int maxGameCount = teams.size() / criteria.teamCountPerGame();
        Literal[] assignment = new Literal[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            assignment[i] = model.newBoolVar("t" + i);
        }

        // 添加约束条件：每局有效单局数量符合要求
        LinearExprBuilder teamCounter = LinearExpr.newBuilder();
        for (int i = 0; i < teams.size(); i++) {
            teamCounter.add(assignment[i]);
        }
        model.addEquality(teamCounter, criteria.teamCountPerGame());

        // 添加约束条件，同一个匹配单元最多出现一次
        for (int idx = 0; idx <= units.length; idx++) {
            var sameUnits = new ArrayList<Literal>();
            var teamIdx = 0;
            for (var team: teams) {
                if (team.unitMembers().get(idx)) {
                    sameUnits.add(assignment[teamIdx]);
                }
                teamIdx += 1;
            }
            model.addAtMostOne(sameUnits);
        }

        // 求解
        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(parameters.maxSolveTimeInSeconds());
        solver.getParameters().setEnumerateAllSolutions(true);
        GameSolutionCollector cb = new GameSolutionCollector(teams, assignment, parameters.maxGameCount(), operator, currentTimestamp);
        solver.solve(model, cb);

        return cb.getSolutions();
    }


    /**
     * 使用 Constraint-Programming 方式收集可行解时的记录器，将有效的决策变量转换为可用的 Game 列表
     */
    private static class GameSolutionCollector extends CpSolverSolutionCallback {

        public GameSolutionCollector(List<FeasibleTeam> teams, Literal[] assignment, int limit, MatchUnitOperator operator, long currentTimestamp) {
            this.teams = teams;
            this.assignment = assignment;
            solutionLimit = limit;
            this.operator = operator;
            this.currentTimestamp = currentTimestamp;
        }

        @Override
        public void onSolutionCallback() {
            final int teamCount = assignment.length;

            ArrayList<FeasibleTeam> teamsInGame = new ArrayList<>();
            int teamIdx = 0;
            for (var team: teams) {
                if (booleanValue(assignment[teamIdx])) {
                    if (teamsInGame.stream().allMatch(oldTeam -> operator.isFitOneGame(oldTeam, team))) {
                        teamsInGame.add(team);
                    } else {
                        return;
                    }
                }
                teamIdx += 1;
            }

            solutions.add(new FeasibleGame(teamsInGame, currentTimestamp));

            // 如果已经满足搜索要求，那么终止
            if (solutionLimit > 0 && solutionLimit <= solutions.size()) {
                stopSearch();
            }
        }

        /**
         * 将求解器的解转化为符合匹配要求的格式
         */
        public ArrayList<FeasibleGame> getSolutions() {
            return solutions;
        }

        // 可用队伍列表
        private final List<FeasibleTeam> teams;

        // 求解器决策变量
        private final Literal[] assignment;

        private final MatchUnitOperator operator;

        // 求解数量上线
        private final int solutionLimit;

        private final long currentTimestamp;

        // 记录可用分配方案
        private final ArrayList<FeasibleGame> solutions = new ArrayList<>();
    }
}
