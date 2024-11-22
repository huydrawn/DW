package core;

import java.util.Map;

import config.ConfigManager;
import model.DataConfig;

public class FetchingTask extends TaskCronAbstract {
	private Map<Task, Boolean> subtasks;

	public FetchingTask() {
		// TODO Auto-generated constructor stub
	}

	public FetchingTask(Map<Task, Boolean> subtasks, String cron) {
		this.subtasks = subtasks;
		super.setCron(cron);
	}

	@Override
	public void execute() throws Exception {
//		4.1.1.1 get DataConfig class
		DataConfig config = ConfigManager.getInstance().getDataConfig();
//		4.1.1.2 set all task by false representing as that task isn't processing 
		subtasks.replaceAll((task, value) -> false);
//		4.1.1.3  int i =0 
//		if i < maxRetries is condition
		for (int i = 0; i < config.getMaxRetries(); i++) {
			try {
//				4.1.1.3.1 loop all subtasks
				for (var x : subtasks.entrySet()) {
//					4.1.1.3.2 take task if value is false 
					if (!x.getValue()) {
//						4.1.1.3.3 get task  run it
						x.getKey().run();
//						4.1.1.3.4 set task value is true
						x.setValue(true);
					}
				}
//				4.1.1.4 break the loop
				break;
			} catch (Exception e) {
				if (i == config.getMaxRetries() - 1) {
//					4.1.1.5 throw error
					throw new Exception(e.getMessage());
				}
			}
		}
	}

	@Override
	public String jobDescription() {
		return "Fetching Data";
	}

}
