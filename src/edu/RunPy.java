package edu;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.script.ScriptException;

public class RunPy {
	public static void main(String[] args) throws FileNotFoundException, ScriptException {
		try {
			// Tạo tiến trình để chạy file Python
			ProcessBuilder processBuilder = new ProcessBuilder("python", "D:/r.py", "parameter1", "parameter2");
			Map<String, String> env = processBuilder.environment();
			env.put("PYTHONIOENCODING", "utf-8");
			processBuilder.redirectErrorStream(true);

			// Chạy tiến trình
			Process process = processBuilder.start();

			// Đọc và in output từ file Python
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			// Đợi cho đến khi tiến trình hoàn tất
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				System.out.println("Thành công");
			} else {
				System.out.println("Có lỗi xảy ra. Mã lỗi: " + exitCode);
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
