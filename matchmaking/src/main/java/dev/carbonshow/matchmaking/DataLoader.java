package dev.carbonshow.matchmaking;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.FileReader;

/**
 * 基于 jackson 读取 csv 文件，并提取到 MatchUnitData 中
 */
public class DataLoader {
  private static final CsvMapper csvMapper = new CsvMapper();
  private static final CsvSchema csvSchema = csvMapper.typedSchemaFor(MatchMakingUserData.class).withHeader().withColumnReordering(true);

  // 保存读取到的，提取出来的 csv 数据
  private final MappingIterator<MatchMakingUserData> mappingIterator;

  /**
   * 从指定的 csv 文件中读取数据
   * @param dataFileName csv 数据文件
   * @throws java.io.IOException 文件不存在，或者 csv 解析失败时抛出
   */
  public DataLoader(String dataFileName) throws java.io.IOException {
    var csvFileReader = new FileReader(dataFileName);
    mappingIterator = csvMapper.readerFor(MatchMakingUserData.class).with(csvSchema).readValues(csvFileReader);
  }

  public MappingIterator<MatchMakingUserData> getMappingIterator() {
    return mappingIterator;
  }
}
