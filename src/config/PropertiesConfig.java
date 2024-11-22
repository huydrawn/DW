package config;

import java.io.FileInputStream;
import java.util.Properties;

import lombok.Getter;

@Getter
public class PropertiesConfig {
	public static PropertiesConfig propertiesConfig;
	private Properties properties;

	private PropertiesConfig() {
		properties = new Properties();
	}

	public String getPropertie(String key) {
		return properties.getProperty(key);
	}

	public static PropertiesConfig getInstance() {
		if (propertiesConfig == null)
			propertiesConfig = new PropertiesConfig();
		return propertiesConfig;
	}

	public void loadProperties() throws Exception {

		//	1.1 create FileInputStream  from path  'src/config.properties'
		FileInputStream fis = new FileInputStream("src/config.properties");
		//	1.2 load properties in FileInputStream in to Properties
		properties.load(fis);

	}
}
