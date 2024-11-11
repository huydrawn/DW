package model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileProcessingStatus {
	private int fileId;
	private String fileName;
	private String sourcePath;
	private String status;
	private int retryCount;
	private LocalDateTime createdTimestamp;
	private LocalDateTime lastAttemptTimestamp;
	private LocalDateTime stagingTimestamp;
	private String errorMessage;
	private Integer processingTime;
}