package model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataConfig {
	private int dataConfigId;
	private String crawlToolPath;
	private Integer timeout;
	private String tempDir;
	private String fileFormat;
	private String fileItemSeparator;
	private String retentionStagingPeriod;
	private String dataFetchFrequency;
	private String dataProcessingFrequency;
	private Integer maxRetries;
	private Integer retryDelay;
	private String logFile;
	private String alertEmail;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	private String status;
	private String schemaDatabaseWarehouseUrl;
	private String username;
	private String password;

}