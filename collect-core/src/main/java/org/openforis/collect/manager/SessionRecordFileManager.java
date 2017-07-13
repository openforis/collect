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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	protected static Log LOG = LogFactory.getLog(SessionRecordFileManager.class);

	@Autowired
	private RecordFileManager recordFileManager;
	
	private Map<Integer, String> nodeIdToTempFilePath;
	private Map<Integer, String> filesToDelete;

	protected java.io.File tempRootDir;

	public void init() {
		resetTempInfo();
	}

	public void resetTempInfo() {
		nodeIdToTempFilePath = new HashMap<Integer, String>();
		filesToDelete = new HashMap<Integer, String>();
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
			
			indexTempFile(tempFile, nodeId);
			return tempFile;
		} catch (IOException e) {
			throw new RecordFileException(e);
		}
	}

	public void indexTempFile(java.io.File tempFile, int nodeId) {
		String filePath = tempFile.getAbsolutePath();
		nodeIdToTempFilePath.put(nodeId, filePath);
	}
	
	protected boolean moveTempFilesToRepository(CollectRecord record) throws RecordFileException {
		try {
			boolean recordChanged = false;
			for (Entry<Integer, String> entry : nodeIdToTempFilePath.entrySet()) {
				int nodeId = entry.getKey();
				String fileName = entry.getValue();
				java.io.File tempFile = new java.io.File(fileName);
				boolean currentRecordChanged = recordFileManager.moveFileIntoRepository(record, nodeId, tempFile);
				recordChanged = recordChanged || currentRecordChanged;
			}
			nodeIdToTempFilePath.clear();
			return recordChanged;
		} catch (IOException e) {
			throw new RecordFileException(e);
		}
	}
	
	public void deleteAllTempFiles() {
		Set<Entry<Integer,String>> entrySet = nodeIdToTempFilePath.entrySet();
		for (Entry<Integer, String> entry : entrySet) {
			String filePath = entry.getValue();
			java.io.File tempFile = new java.io.File(filePath);
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
		String tempFilePath = nodeIdToTempFilePath.get(nodeId);
		if ( tempFilePath == null ) {
			//prepare repository file delete
			java.io.File repositoryFile = recordFileManager.getRepositoryFile(record, nodeId);
			if ( repositoryFile != null ) {
				String fileName = repositoryFile.getName();
				filesToDelete.put(nodeId, fileName);
			}
		} else {
			//remove temp file
			java.io.File tempFile = new java.io.File(tempFilePath);
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
	
	public java.io.File getFile(CollectRecord record, int nodeId) {
		java.io.File file;
		String tempFilePath = nodeIdToTempFilePath.get(nodeId);
		if ( tempFilePath == null ) {
			file = recordFileManager.getRepositoryFile(record, nodeId);
		} else {
			file = new java.io.File(tempFilePath);
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
	
}
