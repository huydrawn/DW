package config;

import javax.mail.MessagingException;

import org.jdbi.v3.core.Jdbi;

import dao.DataConfigDAO;
import model.DataConfig;
import utility.DatabaseUtil;
import utility.EmailUtil;
import utility.LogUtil;

public class ConfigManager {
	private static ConfigManager instance; // Instance duy nhất của Singleton
	private final Jdbi jdbi;
	private DataConfig dataConfig;

	// Constructor private để ngăn tạo instance từ bên ngoài
	private ConfigManager(Jdbi jdbi) {
		this.jdbi = jdbi;
		this.dataConfig = new DataConfig();
	}

//	2.1 get ConfigManager Instance
	public static ConfigManager getInstance() {
		if (instance == null) {
			synchronized (ConfigManager.class) {
				if (instance == null) {
					try {
//						2.1.1 create ConfigManager Instance
//						2.1.1.1 call constructor with jdbi of dataware house as parameter ConfigManager 
						instance = new ConfigManager(DatabaseUtil.getJdbiConnectionToConfig());
					} catch (Exception e) {
//						2.1.1.2 log error
						LogUtil.logError("Error at get data from config " + e.getMessage(), "Load Config Table", 0,
								"DANGER");
					}
				}
			}
		}
//		2.1.2 get ConfigManager Instance
		return instance;
	}

//	2.2 load table confing in database controller
	public void loadConfig() {
		try {
			this.dataConfig = jdbi.withExtension(DataConfigDAO.class, DataConfigDAO::getLastConfig);
		} catch (Exception e) {
			String email = PropertiesConfig.getInstance().getPropertie("email.notification");
			try {
				EmailUtil.sendEmail(email, "Error At loadConfig", "Load failure " + e.getMessage());
			} catch (MessagingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw e;
		}
	}

	// Truy xuất cấu hình đã tải
	public DataConfig getDataConfig() {
		return dataConfig;
	}
}