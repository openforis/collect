package org.openforis.collect.io.data;

import java.util.zip.ZipEntry;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.io.exception.DataParsingExeption;
import org.openforis.collect.model.CollectRecord.Step;

/**
 * @author S. Ricci
 *
 */
@Deprecated
public class RecordEntry {
	
	private Step step;
	private int recordId;
	private String namePrefix;
	
	public RecordEntry(Step step, int recordId) {
		this(step, recordId, "");
	}
	
	public RecordEntry(Step step, int recordId, String namePrefix) {
		this.step = step;
		this.recordId = recordId;
		this.namePrefix = namePrefix;
	}
	
	public static boolean isValidRecordEntry(ZipEntry zipEntry) {
		String name = zipEntry.getName();
		return isValidRecordEntry(name);
	}

	public static boolean isValidRecordEntry(String name) {
		return name.endsWith(".xml") && ! (XMLDataExportProcess.IDML_FILE_NAME.equals(name) || 
				name.startsWith(XMLDataExportProcess.RECORD_FILE_DIRECTORY_NAME));
	}
	
	public static RecordEntry parse(String zipEntryName) throws DataParsingExeption {
		//for backward compatibility with previous generated backup files
		String zipEntryNameFixed = zipEntryName.replace("\\", XMLDataExportProcess.ZIP_DIRECTORY_SEPARATOR);
		String[] entryNameSplitted = zipEntryNameFixed.split(XMLDataExportProcess.ZIP_DIRECTORY_SEPARATOR);
		if (entryNameSplitted.length != 2) {
			throw new DataParsingExeption("Packaged file format exception: wrong zip entry name: " + zipEntryName);
		}
		//step
		String stepNumStr = entryNameSplitted[0];
		int stepNumber = Integer.parseInt(stepNumStr);
		Step step = Step.valueOf(stepNumber);
		//file name
		String fileName = entryNameSplitted[1];
		String baseName = FilenameUtils.getBaseName(fileName);
		int recordId = Integer.parseInt(baseName);
		RecordEntry result = new RecordEntry(step, recordId);
		return result;
	}

	public String getName() {
		return namePrefix + 
				step.getStepNumber() + 
				XMLDataExportProcess.ZIP_DIRECTORY_SEPARATOR + 
				recordId + 
				".xml";
	}
	
	public int getRecordId() {
		return recordId;
	}
	
	public Step getStep() {
		return step;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + recordId;
		result = prime * result + ((step == null) ? 0 : step.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecordEntry other = (RecordEntry) obj;
		if (recordId != other.recordId)
			return false;
		if (step != other.step)
			return false;
		return true;
	}
	
}