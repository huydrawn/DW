package utility;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

public class CronTaskSchedulerWithCronUtils {

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

	public static void main(String[] args) {
		String cronExpression = "*/5 * * * * ?"; // Cron expression: every 5 minutes
		scheduleCronTask(cronExpression, new Runnable() {

			@Override
			public void run() {
				System.out.println("run1");

			}
		});
		scheduleCronTask(cronExpression, new Runnable() {

			@Override
			public void run() {
				System.out.println("run2");

			}
		});
	}

	public static void scheduleCronTask(String cronExpression, Runnable execute) {
		// Sử dụng cron-utils để phân tích cron expression
		CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
		Cron cron = parser.parse(cronExpression);
		ExecutionTime executionTime = ExecutionTime.forCron(cron);

		// Lấy thời gian hiện tại và chuyển đổi sang ZonedDateTime
		ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());

		// Tính thời gian tiếp theo khi cron chạy
		executionTime.timeToNextExecution(now).ifPresent(duration -> {
			long delayInSeconds = duration.getSeconds() + 1;

			// Lên lịch công việc với delay tính từ cron
			scheduler.scheduleAtFixedRate(execute, delayInSeconds, delayInSeconds, TimeUnit.SECONDS);
		});
	}

}