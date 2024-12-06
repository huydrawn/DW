package core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import config.ConfigManager;
import model.DataConfig;
import utility.DatabaseUtil;

public class CrawlTask extends TaskAbstract {

	private String createFileName(int index) {
		// Định dạng ngày giờ theo mẫu yyyyMMdd_HHmmss
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		String formattedDateTime = LocalDateTime.now().format(formatter);

		// Tạo tên file
		String fileName = "data_source" + "_" + index + "_" + formattedDateTime + ".csv";
		return fileName;
	}

	@Override
	public void execute() throws Exception {
		// Lấy cấu hình
		DataConfig config = ConfigManager.getInstance().getDataConfig();
		String[] crawlToolPaths = config.getCrawlToolPath().split(",");
		System.out.println("CrawlToolPaths: " + Arrays.toString(crawlToolPaths));

		// Tạo thread pool
		ExecutorService executor = Executors.newFixedThreadPool(crawlToolPaths.length);

		for (String tool : crawlToolPaths) {
			// Tạo một tác vụ riêng cho mỗi công cụ crawl
			executor.submit(() -> {
				try {
					runCrawlTool(tool, config);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		// Đóng executor sau khi tất cả tác vụ hoàn tất
		executor.shutdown();
		while (!executor.isTerminated()) {
			Thread.sleep(100); // Chờ tất cả các luồng hoàn thành
		}
	}

	private void runCrawlTool(String tool, DataConfig config) throws Exception {
		String threadName = Thread.currentThread().getName();
		// Tạo tên file
		// Ví dụ: data_source_1_20210820_123456.csv : data_source_{source_index}_{yyyyMMdd_HHmmss}.csv
		String pathFile = config.getTempDir() + "/" + createFileName(Integer.parseInt(threadName.substring(threadName.lastIndexOf("-") + 1)));

		System.out.println("[START] Tool: " + tool + " | Time: " + LocalDateTime.now());
		System.out.println("Path file: " + pathFile);

		ProcessBuilder processBuilder = new ProcessBuilder("python", tool, pathFile, config.getFileItemSeparator(),
				config.getFileFormat());

		Map<String, String> env = processBuilder.environment();
		env.put("PYTHONIOENCODING", "utf-8");
		processBuilder.redirectErrorStream(true);

		// Chạy tiến trình
		Process process = processBuilder.start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Log thông tin nếu cần
			}
		}

		// Đợi tiến trình hoàn tất
		int exitCode = process.waitFor();

		if (exitCode == 0) {
			System.out.println("[SUCCESS] Tool: " + tool + " | Time: " + LocalDateTime.now());
			// Kết nối tới database và cập nhật trạng thái file
			var jdbi = DatabaseUtil.getJdbiConnectionToConfig();
			jdbi.withHandle(handle -> handle
					.createUpdate("INSERT INTO file_processing_status (fileName, sourcePath, status, retryCount, createdTimestamp) "
							+ "VALUES (:fileName, :sourcePath, :status, :retryCount, :createdTimestamp)")
					.bind("fileName", pathFile)
					.bind("sourcePath", config.getTempDir())
					.bind("status", "PENDING")
					.bind("retryCount", 0)
					.bind("createdTimestamp", LocalDateTime.now())
					.execute());
		} else {
			System.out.println("[ERROR] Tool: " + tool + " | Time: " + LocalDateTime.now());
			throw new Exception("Đã có lỗi xảy ra khi run crawl tool tại đường dẫn " + tool + ": Mã lỗi " + exitCode);
		}
	}

	@Override
	public String jobDescription() {
		// TODO Auto-generated method stub
		return "CrawlTask";
	}

}
