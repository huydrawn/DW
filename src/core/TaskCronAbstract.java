package core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utility.CronTaskSchedulerWithCronUtils;
import utility.LogUtil;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class TaskCronAbstract implements TaskCron {
	private String cron;

	public void run() {
		CronTaskSchedulerWithCronUtils.scheduleCronTask(cron, () -> {
			long end = 0;
			long start = System.currentTimeMillis();
			try {
				execute();
				end = System.currentTimeMillis() - start;
				LogUtil.logSuccess("Handle Success at " + jobDescription(), jobDescription(), end, "INF");
			} catch (Exception e) {
				end = System.currentTimeMillis() - start;
				LogUtil.logError("Error at " + jobDescription() + ": " + e.getMessage(), jobDescription(), end,
						"WARNING");
			}

		});
	}
}
