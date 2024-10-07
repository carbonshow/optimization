package dev.carbonshow.matchmaking.solver;

import dev.carbonshow.matchmaking.config.MatchMakingCriteria;
import dev.carbonshow.matchmaking.config.SolverParameters;
import dev.carbonshow.matchmaking.pool.MatchUnit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 使用回溯方法，寻找可用的单局求解器，用于将不同的队伍组织起来得到一个可用单局
 */
public class FeasibleGameBacktraceFinder implements FeasibleGameFinder {

    private final MatchMakingCriteria criteria;
    private final MatchUnitOperator operator;

    public FeasibleGameBacktraceFinder(MatchMakingCriteria matchMakingCriteria, MatchUnitOperator matchUnitOperator) {
        criteria = matchMakingCriteria;
        operator = matchUnitOperator;
    }

    /**
     * 根据指定的匹配单元数组，获得满足要求的所有队伍
     *
     * @param units            匹配单元数组
     * @param teams            所有可用 team，同一个匹配单元可能出现在多个不同的 teams 中
     * @param currentTimestamp 当前时间戳，单位业务自定义保持一致即可
     * @return 返回可用单局，这些单局可以同时开启，即任何一个匹配单元最多只会出现在一个单局中
     */
    public List<FeasibleGame> solve(MatchUnit[] units, List<FeasibleTeam> teams, SolverParameters parameters, long currentTimestamp) {
        PriorityQueue<FeasibleGame> games = new PriorityQueue<>(parameters.maxGameCount(), Comparator.comparingDouble(FeasibleGame::getScore));
        Stack<GameFindState> states = new Stack<>();
        states.push(new GameFindState(0, criteria.teamCountPerGame(), new BitSet(teams.size())));

        while (!states.empty()) {
            var state = states.pop();

            if (state.exploredTeamCount >= teams.size()) {
                continue;
            }

            var currentTeam = teams.get(state.exploredTeamCount);
            if (state.foundTeams.stream().allMatch(foundTeam -> operator.isFitOneGame(currentTeam, teams.get(foundTeam)))) {
                // 包含当前 Team
                var newFoundTeams = (BitSet) state.foundTeams.clone();
                newFoundTeams.set(state.exploredTeamCount);
                int newLeftTeamCount = state.leftTeamCount - 1;
                if (newLeftTeamCount <= 0) {
                    var newFeasibleGame = new FeasibleGame(newFoundTeams.stream().mapToObj(teams::get).collect(Collectors.toCollection(ArrayList::new)), currentTimestamp);
                    keepTopNGames(games, newFeasibleGame, parameters.maxGameCount());
                } else {
                    var newState = new GameFindState(state.exploredTeamCount + 1, state.leftTeamCount - 1, newFoundTeams);
                    states.push(newState);
                }
            }

            // 不包含当前 team
            var newState = new GameFindState(state.exploredTeamCount + 1, state.leftTeamCount, state.foundTeams);
            states.push(newState);
        }

        return games.stream().toList();
    }

    private record GameFindState(int exploredTeamCount, int leftTeamCount, BitSet foundTeams) {
    }

    private void keepTopNGames(PriorityQueue<FeasibleGame> games, FeasibleGame newGame, int N) {
        if (games.size() < N) {
            games.offer(newGame);
        } else {
            if (newGame.getScore() > games.peek().getScore()) {
                games.poll();
                games.offer(newGame);
            }
        }
    }
}
