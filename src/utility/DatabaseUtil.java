package utility;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import config.ConfigManager;
import config.PropertiesConfig;
import model.DataConfig;

public class DatabaseUtil {

	public static Jdbi getJdbiConnectionToConfig() throws Exception {
		try {
			var properties = PropertiesConfig.getInstance().getProperties();
			var dbUrl = properties.get("db.url").toString();
			var dbUsername = properties.get("db.username").toString();
			var dbPassword = properties.get("db.password").toString();
			var jdbi = Jdbi.create(dbUrl, dbUsername, dbPassword);
			jdbi.installPlugin(new SqlObjectPlugin());
			return jdbi;
		} catch (Exception e) {
			throw e;
		}
	}

	public static Jdbi getJdbiConnectionToWareHouse() throws Exception {
		try {
			DataConfig config = ConfigManager.getInstance().getDataConfig();
			Jdbi jdbi = Jdbi.create(config.getSchemaDatabaseWarehouseUrl(), config.getUsername(), config.getPassword());
			jdbi.installPlugin(new SqlObjectPlugin());
			return jdbi;
		} catch (Exception e) {
			throw e;
		}
	}
}