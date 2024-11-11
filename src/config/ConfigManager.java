package config;

import org.jdbi.v3.core.Jdbi;

import dao.DataConfigDAO;
import model.DataConfig;
import utility.DatabaseUtil;

public class ConfigManager {
	private static ConfigManager instance; // Instance duy nhất của Singleton
	private final Jdbi jdbi;
	private DataConfig dataConfig;

	// Constructor private để ngăn tạo instance từ bên ngoài
	private ConfigManager(Jdbi jdbi) {
		this.jdbi = jdbi;
		this.dataConfig = new DataConfig();
	}

	// Phương thức truy cập Singleton
	public static ConfigManager getInstance() {
		if (instance == null) {
			synchronized (ConfigManager.class) {
				if (instance == null) {
					instance = new ConfigManager(DatabaseUtil.getJdbiConnectionToConfig());
				}
			}
		}
		return instance;
	}

	// Tải cấu hình từ cơ sở dữ liệu vào bộ nhớ
	public void loadConfig() {
		this.dataConfig = jdbi.withExtension(DataConfigDAO.class, DataConfigDAO::getLastConfig);
	}

	// Truy xuất cấu hình đã tải
	public DataConfig getDataConfig() {
		return dataConfig;
	}
}