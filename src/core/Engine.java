package core;

import config.ConfigManager;
import model.DataConfig;

public class Engine {

	public static void main(String[] args) throws Exception {
		var manager = ConfigManager.getInstance();
		manager.loadConfig();
		DataConfig dataConfig = manager.getDataConfig();
		ExtractTask a = new ExtractTask();
		a.execute();
//		Map<Task, Boolean> subTaskOfFetching = new HashMap<>();
//		subTaskOfFetching.put(new CrawlTask(), false);
//		TaskCronAbstract job = new FetchingTask(subTaskOfFetching, dataConfig.getDataFetchFrequency());
//		job.run();

	}
}
