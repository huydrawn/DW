package core;

import lombok.Data;
import utility.CronTaskSchedulerWithCronUtils;
import utility.LogUtil;

@Data
public abstract class TaskCronAbstract implements TaskCron {
	private String cron;

	public void run() {
//		4.2.1 call method scheduleCronTask From CronTaskScheduleerWithCronUtils
//		with cron and a runnable contain the code Task executed as parameters
		CronTaskSchedulerWithCronUtils.scheduleCronTask(cron, () -> {
			long end = 0;
			long start = System.currentTimeMillis();
			try {
				System.out.println(jobDescription() + " is starting ..................");
				execute();
				System.out.println(jobDescription() + " is ending ..................");
				end = System.currentTimeMillis() - start;
//				4.2.3 log success
				LogUtil.logSuccess("Handle Success at " + jobDescription(), jobDescription(), end, "INF");
			} catch (Exception e) {

				end = System.currentTimeMillis() - start;
//				4.2.4 log error
				LogUtil.logError("Error at " + jobDescription() + ": " + e.getMessage(), jobDescription(), end,
						"WARNING");
			}

		});
	}
}
