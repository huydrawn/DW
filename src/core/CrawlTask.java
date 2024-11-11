package core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import config.ConfigManager;
import model.DataConfig;
import utility.DatabaseUtil;

public class CrawlTask extends TaskAbstract {

	private String createFileName() {
		// Định dạng ngày giờ theo mẫu yyyyMMdd_HHmmss
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String formattedDateTime = LocalDateTime.now().format(formatter);

		// Tạo tên file
		String fileName = formattedDateTime + ".csv";
		return fileName;
	}

	@Override
	public void execute() throws Exception {
		DataConfig config = ConfigManager.getInstance().getDataConfig();

		String crawlToolPaths[] = config.getCrawlToolPath().split(",");

		for (var tool : crawlToolPaths) {
			// Tạo tiến trình để chạy file Python
			String pathFile = config.getTempDir() + "/" + createFileName();

			ProcessBuilder processBuilder = new ProcessBuilder("python", tool, pathFile, config.getFileItemSeparator(),
					config.getFileFormat());

			Map<String, String> env = processBuilder.environment();
			env.put("PYTHONIOENCODING", "utf-8");
			processBuilder.redirectErrorStream(true);

			// Chạy tiến trình
			Process process = processBuilder.start();

			// Đợi cho đến khi tiến trình hoàn tất
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				var jdbi = DatabaseUtil.getJdbiConnectionToConfig();
				jdbi.withHandle(handle -> handle
						.createUpdate("INSERT INTO file_processing_status (fileName, sourcePath, status, retryCount, "
								+ "createdTimestamp) "
								+ "VALUES (:fileName, :sourcePath, :status, :retryCount, :createdTimestamp)")
						.bind("fileName", pathFile).bind("sourcePath", config.getTempDir()).bind("status", "PENDING")
						.bind("retryCount", 0).bind("createdTimestamp", LocalDateTime.now()).execute());
			} else {
				throw new Exception("Đã có lỗi xảy ra khi run crawl tool at path " + tool + ": Mã lỗi " + exitCode);
			}

		}
	}

	@Override
	public String jobDescription() {
		// TODO Auto-generated method stub
		return "CrawlTask";
	}

}
