package utility;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import config.ConfigManager;
import model.DataConfig;

public class DatabaseUtil {

	public static Jdbi getJdbiConnectionToConfig() {
		var jdbi = Jdbi.create("jdbc:mysql://localhost:3308/data_control", "root", "20102003");
		jdbi.installPlugin(new SqlObjectPlugin());
		return jdbi;
	}

	public static Jdbi getJdbiConnectionToWareHouse() {
		DataConfig config = ConfigManager.getInstance().getDataConfig();
		Jdbi jdbi = Jdbi.create(config.getSchemaDatabaseWarehouseUrl(), config.getUsername(), config.getPassword());
		jdbi.installPlugin(new SqlObjectPlugin());
		return jdbi;
	}
}