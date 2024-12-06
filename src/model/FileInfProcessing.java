package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public class FileInfProcessing {
		private int fileId;
		private String pathFile;
		private String status;
	}