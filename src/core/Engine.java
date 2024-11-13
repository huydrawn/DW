package core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import config.ConfigManager;
import model.DataConfig;

public class Engine {

	public void run() {
		var manager = ConfigManager.getInstance();
		manager.loadConfig();
		DataConfig dataConfig = manager.getDataConfig();

		Map<Task, Boolean> subTaskOfFetching = new LinkedHashMap<>();
		subTaskOfFetching.put(new CrawlTask(), false);
		subTaskOfFetching.put(new ExtractTask(), false);
		TaskCron fetchingJob = new FetchingTask(subTaskOfFetching, dataConfig.getDataFetchFrequency());
		fetchingJob.run();
		TaskCron processingJob = new ProcessingTask(dataConfig.getDataProcessingFrequency());
		processingJob.run();
	}

	public static void main(String[] args) throws Exception {
		new Engine().run();
	}
}
