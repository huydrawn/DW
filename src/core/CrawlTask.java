package core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
//		4.1.1.3.3.1.1 get DataConfig class
        DataConfig config = ConfigManager.getInstance().getDataConfig();
//		4.1.1.3.3.1.2 get all crawl tool path from DataConfig class
        String crawlToolPaths[] = config.getCrawlToolPath().split(",");

        // 4.1.1.3.3.1.3 create tool thread for all crawl tools
        // Tạo thread pool
        ExecutorService executor = Executors.newFixedThreadPool(crawlToolPaths.length);

        // 4.1.1.3.3.1.4 Create a reference to store exception
        AtomicReference<Exception> exceptionReference = new AtomicReference<>();

        //		4.1.1.3.3.1.5 loop all crawl tool
        for (var tool : crawlToolPaths) {
            // 4.1.1.3.3.1.6 create a task for each crawl tool
            executor.submit(() -> {
                try {
                    System.out.println("Crawl tool: " + tool);
//			4.1.1.3.3.1.7
//			create path file to save when crawl
//			by tmp folder and datetime get data
                    String threadName = Thread.currentThread().getName();
                    String pathFile = config.getTempDir() + "/" + createFileName(Integer.parseInt(threadName.substring(threadName.lastIndexOf("-") + 1)));
                    System.out.println("Path file: " + pathFile);
//			4.1.1.3.3.1.8
//			run crawl tool by pass pathFile,file seperator and fileFormat as parameters
                    ProcessBuilder processBuilder = new ProcessBuilder("python", tool, pathFile, config.getFileItemSeparator(),
                            config.getFileFormat());

                    Map<String, String> env = processBuilder.environment();
                    env.put("PYTHONIOENCODING", "utf-8");
                    processBuilder.redirectErrorStream(true);

                    // Chạy tiến trình
                    Process process = processBuilder.start();
                    // Đọc stdout
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"))) {
                        String line;
                        while ((line = reader.readLine()) != null) {

                        }
                    }
                    // Đợi cho đến khi tiến trình hoàn tất
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
//				4.1.1.3.3.1.9
//				Get jdbi control database 
                        var jdbi = DatabaseUtil.getJdbiConnectionToConfig();
//				4.1.1.3.3.1.10
//				insert into file_processing_table
//				pathFile and statis is PENDING 
                        jdbi.withHandle(handle -> handle
                                .createUpdate("INSERT INTO file_processing_status (fileName, sourcePath, status, retryCount, "
                                        + "createdTimestamp) "
                                        + "VALUES (:fileName, :sourcePath, :status, :retryCount, :createdTimestamp)")
                                .bind("fileName", pathFile).bind("sourcePath", config.getTempDir()).bind("status", "PENDING")
                                .bind("retryCount", 0).bind("createdTimestamp", LocalDateTime.now()).execute());
                    } else {
                        throw new Exception("Đã có lỗi xảy ra khi run crawl tool at path " + tool + ": Mã lỗi " + exitCode);
                    }
                } catch (Exception e) {
                    //	4.1.1.3.3.1.9.1
                    // set exception to reference
                    exceptionReference.set(e);
                }
            });

        }
        // 4.1.1.3.3.1.11 wait for all thread finish
        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            throw new Exception("Thread pool không thể hoàn thành đúng hạn.");
        }
        // 4.1.1.3.3.1.12 throw exception if have
        if (exceptionReference.get() != null) {
            throw exceptionReference.get();
        }
    }

    @Override
    public String jobDescription() {
        // TODO Auto-generated method stub
        return "CrawlTask";
    }

}
