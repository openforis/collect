package org.openforis.collect.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.exception.RecordFileException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.model.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 * 
 * It needs to have scope "session" because it manages files to be associated
 * to the current edited record (one per session)
 *
 */
public class SessionRecordFileManager implements Serializable {
	
	private static final long serialVersionUID = 1L;

	protected static final Logger LOG = LogManager.getLogger(SessionRecordFileManager.class);

	@Autowired
	private RecordFileManager recordFileManager;
	
	private Map<Integer, TempFileInfo> nodeIdToTempFilePath = new HashMap<Integer, TempFileInfo>();
	private Map<Integer, String> filesToDelete = new HashMap<Integer, String>();

	protected java.io.File tempRootDir;

	public void init() {
		resetTempInfo();
	}
	
	public void destroy() {
		deleteAllTempFiles();
		resetTempInfo();
	}

	public void resetTempInfo() {
		nodeIdToTempFilePath.clear();
		filesToDelete.clear();
	}
	
	public java.io.File saveToTempFile(byte[] data, String originalFileName, CollectRecord record, int nodeId) throws RecordFileException {
		return saveToTempFile(new ByteArrayInputStream(data), originalFileName, record, nodeId);
	}
	
	public java.io.File saveToTempFile(InputStream is, String originalFileName, CollectRecord record, int nodeId) throws RecordFileException {
		try {
			prepareDeleteFile(record, nodeId);
			
			String extension = FilenameUtils.getExtension(originalFileName);
			java.io.File tempFile = java.io.File.createTempFile("collect_record_file_upload", "." + extension);
			FileUtils.copyInputStreamToFile(is, tempFile);
			
			indexTempFile(nodeId, tempFile, originalFileName);
			return tempFile;
		} catch (IOException e) {
			throw new RecordFileException(e);
		}
	}

	public void indexTempFile(int nodeId, java.io.File tempFile, String originalFileName) {
		String filePath = tempFile.getAbsolutePath();
		nodeIdToTempFilePath.put(nodeId, new TempFileInfo(filePath, originalFileName));
	}
	
	protected boolean moveTempFilesToRepository(CollectRecord record) throws RecordFileException {
		try {
			boolean recordChanged = false;
			for (Entry<Integer, TempFileInfo> entry : nodeIdToTempFilePath.entrySet()) {
				int nodeId = entry.getKey();
				TempFileInfo fileInfo = entry.getValue();
				java.io.File tempFile = new java.io.File(fileInfo.getTempFilePath());
				boolean currentRecordChanged = recordFileManager.moveFileIntoRepository(record, nodeId, tempFile, fileInfo.getOriginalFileName());
				recordChanged = recordChanged || currentRecordChanged;
			}
			nodeIdToTempFilePath.clear();
			return recordChanged;
		} catch (IOException e) {
			throw new RecordFileException(e);
		}
	}
	
	public void deleteAllTempFiles() {
		for (Entry<Integer, TempFileInfo> entry : nodeIdToTempFilePath.entrySet()) {
			TempFileInfo fileInfo = entry.getValue();
			java.io.File tempFile = new java.io.File(fileInfo.getTempFilePath());
			tempFile.delete();
		}
		nodeIdToTempFilePath.clear();
	}
	
	public void prepareDeleteAllFiles(CollectRecord record) {
		for (FileAttribute fileAttribute : record.getFileAttributes()) {
			prepareDeleteFile(record, fileAttribute.getInternalId());
		}
	}
	
	public void prepareDeleteFile(CollectRecord record, int nodeId) {
		TempFileInfo tempFileInfo = nodeIdToTempFilePath.get(nodeId);
		if ( tempFileInfo == null ) {
			//prepare repository file delete
			java.io.File repositoryFile = recordFileManager.getRepositoryFile(record, nodeId);
			if ( repositoryFile != null ) {
				String fileName = repositoryFile.getName();
				filesToDelete.put(nodeId, fileName);
			}
		} else {
			deleteTempFile(record, nodeId);
		}
	}
	
	public void deleteTempFile(CollectRecord record, int nodeId) {
		TempFileInfo tempFileInfo = nodeIdToTempFilePath.get(nodeId);
		if (tempFileInfo != null ) {
			java.io.File tempFile = new java.io.File(tempFileInfo.getTempFilePath());
			tempFile.delete();
			nodeIdToTempFilePath.remove(nodeId);
		}
	}
	
	/**
	 * Apply the changes from the temp directory to the final repository directory.
	 * Returns true if the passed record have been changed during the saving process
	 * (for example fileName attribute changed)
	 * 
	 * @param record
	 * @return
	 * @throws RecordFileException
	 */
	public boolean commitChanges(CollectRecord record) throws RecordFileException {
		performFilesDelete(record);
		boolean recordChanged = moveTempFilesToRepository(record);
		return recordChanged;
	}
	
	public java.io.File getFile(FileAttribute fileAttribute) {
		java.io.File file;
		TempFileInfo tempFileInfo = nodeIdToTempFilePath.get(fileAttribute.getInternalId());
		if ( tempFileInfo == null ) {
			file = recordFileManager.getRepositoryFile(fileAttribute);
		} else {
			file = new java.io.File(tempFileInfo.getTempFilePath());
		}
		return file;
	}
	
	protected boolean performFilesDelete(CollectRecord record) {
		boolean result = false;
		Set<Entry<Integer,String>> entrySet = filesToDelete.entrySet();
		for (Entry<Integer, String> entry : entrySet) {
			int nodeId = entry.getKey();
			String fileName = entry.getValue();
			FileAttribute fileAttr = (FileAttribute) record.getNodeByInternalId(nodeId);
			FileAttributeDefinition defn = fileAttr.getDefinition();
			java.io.File repositoryFile = recordFileManager.getRepositoryFile(defn, fileName);
			repositoryFile.delete();
			result = true;
		}
		filesToDelete.clear();
		return result;
	}
	
	public void setRecordFileManager(RecordFileManager recordFileManager) {
		this.recordFileManager = recordFileManager;
	}
	
	private class TempFileInfo {
		
		private String tempFilePath;
		private String originalFileName;
		
		public TempFileInfo(String tempFilePath, String originalFileName) {
			super();
			this.tempFilePath = tempFilePath;
			this.originalFileName = originalFileName;
		}
		
		public String getTempFilePath() {
			return tempFilePath;
		}
		
		public String getOriginalFileName() {
			return originalFileName;
		}
	}
}
