package dao;

import java.time.LocalDateTime;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import model.DataConfig;

public interface DataConfigDAO {
	@SqlQuery("SELECT * FROM dataconfig ORDER BY dataConfigId DESC LIMIT 1")
	@RegisterBeanMapper(DataConfig.class)
	DataConfig getLastConfig();

	@SqlUpdate("INSERT INTO fileprocessingstatus (file_name, source_path, status, retry_count, created_timestamp, last_attempt_timestamp, staging_timestamp, error_message, processing_time) "
			+ "VALUES (:fileName, :sourcePath, :status, :retryCount, :createdTimestamp, :lastAttemptTimestamp, :stagingTimestamp, :errorMessage, :processingTime)")
	void insertFileProcessingStatus(@Bind("fileName") String fileName, @Bind("sourcePath") String sourcePath,
			@Bind("status") String status, @Bind("retryCount") int retryCount,
			@Bind("createdTimestamp") LocalDateTime createdTimestamp,
			@Bind("lastAttemptTimestamp") LocalDateTime lastAttemptTimestamp,
			@Bind("stagingTimestamp") LocalDateTime stagingTimestamp, @Bind("errorMessage") String errorMessage,
			@Bind("processingTime") Integer processingTime);
}