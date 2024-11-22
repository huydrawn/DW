package core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import config.ConfigManager;
import config.PropertiesConfig;
import model.DataConfig;

public class Engine {

	private List<TaskCron> prepareTasks() {
//		3.1 Get Data Config Class
		DataConfig dataConfig = ConfigManager.getInstance().getDataConfig();
//		3.2 Create List to track all tasks
		List<TaskCron> tasks = new ArrayList<>();
//		3.2.1.1  create LinkedHashMap with key is a Task class and value a boolean to track the order of substack
		Map<Task, Boolean> subTaskOfFetching = new LinkedHashMap<>();
//		3.2.1.2  put all subtask in to Map
		subTaskOfFetching.put(new CrawlTask(), false);
		subTaskOfFetching.put(new ExtractTask(), false);
//		3.2.1 create fetching task by Map as parameter contain subtasks in it
		TaskCron fetchingJob = new FetchingTask(subTaskOfFetching, dataConfig.getDataFetchFrequency());
//		3.2.2 create processing task by cron as parameter get from processingFrequent in DataConfig
		TaskCron processingJob = new ProcessingTask(dataConfig.getDataProcessingFrequency());
//		3.2.3 add all taskcron in to List
		tasks.add(fetchingJob);
		tasks.add(processingJob);
//		3.2.4 return that List
		return tasks;

	}

	private void runTasks(List<TaskCron> tasks) {
//		4.1. loop all tasks
		for (var task : tasks) {
//			4.2 call run method
			task.run();
		}
	}

	public void run() throws Exception {
		// 1 load file properties config
		PropertiesConfig.getInstance().loadProperties();

		// 2 load fileconfig from db_controller
		var manager = ConfigManager.getInstance();
		manager.loadConfig();

		// 3 prepare tasks for running
		var tasks = prepareTasks();

		// 4 run tasks
		runTasks(tasks);
	}

	public static void main(String[] args) throws Exception {
		new Engine().run();
	}
}
