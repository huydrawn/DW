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
		DataConfig config = ConfigManager.getInstance().getDataConfig();
		
		subtasks.replaceAll((task, value) -> false);
		for (int i = 0; i < config.getMaxRetries(); i++) {
			try {
				for (var x : subtasks.entrySet()) {
					if (!x.getValue()) {
						x.getKey().execute();
					}
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				if (i == config.getMaxRetries() - 1) {
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
