package utility;

import org.jdbi.v3.core.statement.Update;

import config.ConfigManager;
import model.DataConfig;

public class LogUtil {
	public static void logError(String msg, String taskName, long processingTime, String loglevel) {
		DataConfig dataConfig = ConfigManager.getInstance().getDataConfig();
		EmailUtil.sendEmail(dataConfig.getAlertEmail(), "DataWare House Software Error", msg);
		logToDb(msg, taskName, processingTime, loglevel);
	}

	public static void logSuccess(String msg, String taskName, long processingTime, String loglevel) {
		logToDb(msg, taskName, processingTime, loglevel);
	}

	private static void logToDb(String msg, String taskName, long processingTime, String loglevel) {
		var jdbi = DatabaseUtil.getJdbiConnectionToConfig();
		jdbi.useHandle(handle -> {
			Update update = handle
					.createUpdate("INSERT INTO log (logLevel, logMessage, taskName, processingTime, status) \r\n"
							+ "VALUES (:logLevel, :logMessage,   :taskName, :processingTime, :status)");

			// Set parameters
			update.bind("logMessage", msg);
			update.bind("taskName", taskName);
			update.bind("processingTime", processingTime);
			update.bind("logLevel", loglevel);
			update.bind("status", "PENDIND");

			// Execute the insert
			update.execute();
		});
	}
}
