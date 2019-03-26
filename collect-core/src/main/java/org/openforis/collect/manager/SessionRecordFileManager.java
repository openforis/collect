package org.openforis.collect.manager;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

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
import org.openforis.collect.manager.RecordFileManager.RecordFileHandle;
import org.openforis.collect.manager.exception.RecordFileException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.model.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 * 
 *         It needs to have scope "session" because it manages files to be
 *         associated to the current edited record (one per session)
 *
 */
@Transactional(readOnly=true, propagation=SUPPORTS)
public class SessionRecordFileManager implements Serializable {

	private static final long serialVersionUID = 1L;

	protected static final Logger LOG = LogManager.getLogger(SessionRecordFileManager.class);

	@Autowired
	private RecordFileManager recordFileManager;

	private Map<Integer, TempFileInfo> nodeIdToTempFilePath = new HashMap<Integer, TempFileInfo>();
	private Map<Integer, RecordFileHandle> filesToDelete = new HashMap<Integer, RecordFileHandle>();

	protected java.io.File tempRootDir;

	public void init() {
		resetTempInfo();
	}

	public void resetTempInfo() {
		nodeIdToTempFilePath.clear();
		filesToDelete.clear();
	}

	public java.io.File saveToTempFile(byte[] data, String originalFileName, CollectRecord record, int nodeId)
			throws RecordFileException {
		return saveToTempFile(new ByteArrayInputStream(data), originalFileName, record, nodeId);
	}

	public java.io.File saveToTempFile(InputStream is, String originalFileName, CollectRecord record, int nodeId)
			throws RecordFileException {
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
				boolean currentRecordChanged = recordFileManager.moveFileIntoRepository(record, nodeId, tempFile,
						fileInfo.getOriginalFileName());
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
		if (tempFileInfo == null) {
			// prepare repository file delete
			RecordFileHandle handle = recordFileManager.getRepositoryFileHandle(record, nodeId);
			if (handle != null) {
				filesToDelete.put(nodeId, handle);
			}
		} else {
			// remove temp file
			java.io.File tempFile = new java.io.File(tempFileInfo.getTempFilePath());
			tempFile.delete();
			nodeIdToTempFilePath.remove(nodeId);
		}
	}

	/**
	 * Apply the changes from the temp directory to the final repository directory.
	 * Returns true if the passed record have been changed during the saving process
	 * (for example fileName attribute changed)
	 */
	@Transactional(readOnly=false, propagation=REQUIRED)
	public boolean commitChanges(CollectRecord record) throws RecordFileException {
		performFilesDelete(record);
		return moveTempFilesToRepository(record);
	}

	public java.io.File getFile(CollectRecord record, int nodeId) {
		TempFileInfo tempFileInfo = nodeIdToTempFilePath.get(nodeId);
		java.io.File file = tempFileInfo == null 
			? recordFileManager.getRepositoryFile(record, nodeId)
			: new java.io.File(tempFileInfo.getTempFilePath())
		;
		return file;
	}

	protected boolean performFilesDelete(CollectRecord record) {
		Set<Entry<Integer, RecordFileHandle>> entrySet = filesToDelete.entrySet();
		for (Entry<Integer, RecordFileHandle> entry : entrySet) {
			RecordFileHandle handle = entry.getValue();
			recordFileManager.deleteRepositoryFile(handle);
		}
		filesToDelete.clear();
		return !entrySet.isEmpty();
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
