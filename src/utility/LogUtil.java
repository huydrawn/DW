package utility;

import javax.mail.MessagingException;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;

import config.ConfigManager;
import config.PropertiesConfig;
import model.DataConfig;

public class LogUtil {
	public static void logForSendEmailFailure(Exception e) {
		try {
			logToDb(e.getMessage(), "Send Email", 0, "DANGER");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

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
			if (e instanceof MessagingException) {
				logForSendEmailFailure(e);
			} else {
				try {
					EmailUtil.sendEmail(emailLog, "Error when logError to DB", "ERROR");
				} catch (MessagingException e1) {
					// TODO Auto-generated catch block
					logForSendEmailFailure(e1);
				}
			}
		}
	}

	public static void logSuccess(String msg, String taskName, long processingTime, String loglevel) {
		try {
			logToDb(msg, taskName, processingTime, loglevel);
		} catch (Exception e) {
			String emailLog = getEmailLog();
			try {
				EmailUtil.sendEmail(emailLog, "Error when logs to DB", "ERROR");
			} catch (MessagingException e1) {
				logForSendEmailFailure(e1);
			}
		}
	}

	public static void logToDb(String msg, String taskName, long processingTime, String loglevel) throws Exception {
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
