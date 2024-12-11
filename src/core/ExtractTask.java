package core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import com.opencsv.CSVReader;

import config.ConfigManager;
import model.FileInfProcessing;
import utility.DatabaseUtil;
import utility.LogUtil;

public class ExtractTask extends TaskAbstract {

	@Override
	public void execute() throws Exception {
//		4.1.1.3.3.2.1 get DataConfig class
		var config = ConfigManager.getInstance().getDataConfig();
//		4.1.1.3.3.2.2 get jdbi to database control
		var jdbiConfig = DatabaseUtil.getJdbiConnectionToConfig();
//		4.1.1.3.3.2.3 get jdbi to database warehouse
		var jdbiWareHouse = DatabaseUtil.getJdbiConnectionToWareHouse();
//		4.1.1.3.3.2.4 get all filesProcessing have status is PENDING in database control and map to FileInProcessing Class
		var filesProcessing = jdbiConfig.withHandle(handle -> handle.createQuery(
				"SELECT fileId , fileName as pathFile, status FROM file_processing_status WHERE status IN (:pending)")
				.bind("pending", "PENDING").mapToBean(FileInfProcessing.class).list());
//		4.1.1.3.3.2.5 loop all the filesProcessing
		for (var fileProcessing : filesProcessing) {
//			4.1.1.3.3.2.6 read that file csv
			try (CSVReader csvReader = new CSVReader(new FileReader(fileProcessing.getPathFile()))) {
				List<String[]> records = csvReader.readAll();
//				4.1.1.3.3.2.7  update that status of this file in database control is ERROR continue the loop
				if (records.size() == 1) {
					jdbiConfig.useHandle(handle -> {
						handle.createUpdate("Update file_processing_status set status=':status' where fileId=:fileId")
								.bind("status", "ERROR").bind("fileId", fileProcessing.getFileId());
					});
					continue;
				}
//				4.1.1.3.3.2.8 insert all record expect first row into staging table in database warehouse
				StringBuilder sql = new StringBuilder(
						"INSERT INTO liquidation_staging (symbolName, liquidationPrice,liquidationAmount, liquidationSide,  timeLiquidation, exchangeName, expiredAt) values  ");
				for (var row : records) {
					if (!row[0].contains("BTC"))
						continue;

					sql.append(String.format("('%s','%s','%s','%s','%s','%s','%s'),", row[0], row[1], row[2],
							row[3].toLowerCase(), row[4], row[5],
							(Timestamp.from(Instant.ofEpochMilli(
									System.currentTimeMillis() + Integer.valueOf(config.getRetentionStagingPeriod()))))
									+ ""));
				}
				sql.deleteCharAt(sql.length() - 1);

				jdbiWareHouse.useHandle(handle -> {
					handle.execute(sql);
				});
//				4.1.1.3.3.2.9 update status is SUCCESS for that file in table file_processing_status
				jdbiConfig.useHandle(handle -> {
					handle.createUpdate("Update file_processing_status set status='SUCCESS' where fileId=:fileId")
							.bind("fileId", fileProcessing.getFileId()).execute();
				});
			} catch (IOException e) {
				System.out.println(e);
				// 4.1.1.3.3.2.10.1 update status is ERROR for that file in table file_processing_status
				if(e instanceof FileNotFoundException) {
					jdbiConfig.useHandle(handle -> {
						handle.createUpdate("Update file_processing_status set status='ERROR' where fileId=:fileId")
								.bind("fileId", fileProcessing.getFileId()).execute();
					});
				}
//				4.1.1.3.3.2.10 throw exception
				throw e;
			}
		}
	}

	@Override
	public String jobDescription() {
		// TODO Auto-generated method stub
		return "ExtractTask";
	}

}
