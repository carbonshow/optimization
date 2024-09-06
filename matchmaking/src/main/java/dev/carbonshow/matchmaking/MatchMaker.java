package dev.carbonshow.matchmaking;

import com.fasterxml.jackson.databind.MappingIterator;

import java.io.IOException;

public class MatchMaker {
  public static void main(String[] args) {
    MappingIterator<MatchMakingUserData> csvDataIterator;
    try {
      System.out.println("current working directory: " + System.getProperty("user.dir"));
      csvDataIterator = new DataLoader("matchmaking/data/matchmaking-nba.csv").getMappingIterator();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    System.out.println(csvDataIterator.next().toString());
  }
}
