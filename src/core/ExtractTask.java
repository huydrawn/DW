package core;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.jdbi.v3.core.statement.Batch;

import com.opencsv.CSVReader;

import config.ConfigManager;
import model.FileInfProcessing;
import utility.DatabaseUtil;

public class ExtractTask extends TaskAbstract {

	@Override
	public void execute() throws Exception {
		var config = ConfigManager.getInstance().getDataConfig();
		var jdbiConfig = DatabaseUtil.getJdbiConnectionToConfig();
		var jdbiWareHouse = DatabaseUtil.getJdbiConnectionToWareHouse();
		var filesProcessing = jdbiConfig.withHandle(handle -> handle.createQuery(
				"SELECT fileName as pathFile, status FROM file_processing_status WHERE status IN (:pending, :success)")
				.bind("pending", "PENDING").bind("success", "SUCCESS").mapToBean(FileInfProcessing.class).list());

		for (var fileProcessing : filesProcessing) {
			try (CSVReader csvReader = new CSVReader(new FileReader(fileProcessing.getPathFile()))) {
				List<String[]> records = csvReader.readAll();
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

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String jobDescription() {
		// TODO Auto-generated method stub
		return "ExtractTask";
	}

}
