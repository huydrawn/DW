package utility;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;

import config.ConfigManager;
import config.PropertiesConfig;
import model.DataConfig;

public class LogUtil {

	public static String getEmailLog() {
		String emailLog = null;
		try {
			DataConfig dataConfig = ConfigManager.getInstance().getDataConfig();
			emailLog = dataConfig.getAlertEmail();
		} catch (Exception e) {
			emailLog = PropertiesConfig.getInstance().getPropertie("email.notification");
		}
		return emailLog;
	}

	public static void logError(String msg, String taskName, long processingTime, String loglevel) {
		String emailLog = getEmailLog();
		try {
			EmailUtil.sendEmail(emailLog, "DataWare House Software Error", msg);
			logToDb(msg, taskName, processingTime, loglevel);
		} catch (Exception e) {
			EmailUtil.sendEmail(emailLog, "Error when logError to DB", "ERROR");
		}
	}

	public static void logSuccess(String msg, String taskName, long processingTime, String loglevel) {
		String emailLog = getEmailLog();
		try {
			logToDb(msg, taskName, processingTime, loglevel);
		} catch (Exception e) {
			EmailUtil.sendEmail(emailLog, "Error when log to DB", "ERROR");
		}
	}

	private static void logToDb(String msg, String taskName, long processingTime, String loglevel) throws Exception {
		Jdbi jdbi = null;
		jdbi = DatabaseUtil.getJdbiConnectionToConfig();
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
