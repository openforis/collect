package org.openforis.collect.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.exception.RecordFileException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.File;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Record;

/**
 * 
 * @author S. Ricci
 * 
 * It needs to have scope "session" because it manages files to be associated
 * to the current edited record (one per session)
 *
 */
public class RecordFileManager extends BaseStorageManager {
	
	private static final long serialVersionUID = 1L;

	protected static Log LOG = LogFactory.getLog(RecordFileManager.class);

	private static final String UPLOAD_PATH_CONFIGURATION_KEY = "upload_path";
	
	private static final String TEMP_RECORD_FILES_SUBFOLDER = "collect_upload";
	private static final String DEFAULT_RECORD_FILES_SUBFOLDER = "collect_upload";
	
	private Map<Integer, String> tempFiles;
	private Map<Integer, String> filesToDelete;

	protected java.io.File tempRootDir;

	public void init() {
		initTempDir();
		initStorageDirectory();
		resetTempInfo();
	}

	public void resetTempInfo() {
		tempFiles = new HashMap<Integer, String>();
		filesToDelete = new HashMap<Integer, String>();
	}

	protected void initStorageDirectory() {
		super.initStorageDirectory(UPLOAD_PATH_CONFIGURATION_KEY, DEFAULT_RECORD_FILES_SUBFOLDER);
		if ( storageDirectory == null ) {
			String message = "Upload directory not configured properly";
			LOG.error(message);
			throw new IllegalStateException(message);
		} else if ( LOG.isInfoEnabled() ) {
			LOG.info("Using storage directory: " + storageDirectory.getAbsolutePath());
		}
	}
	
	protected void initTempDir() {
		java.io.File systemTempRootDir = getTempFolder();
		if ( systemTempRootDir == null ) {
			systemTempRootDir = getCatalinaBaseTempFolder();
		}
		if ( systemTempRootDir == null ) {
			throw new IllegalStateException("Cannot init temp folder");
		} else {
			tempRootDir = new java.io.File(systemTempRootDir, TEMP_RECORD_FILES_SUBFOLDER);
			if ( (tempRootDir.exists() || tempRootDir.mkdirs()) && tempRootDir.canWrite() ) {
				if ( LOG.isInfoEnabled() ) {
					LOG.info("Using temp directory: " + tempRootDir.getAbsolutePath());
				}
			} else {
				throw new IllegalStateException("Cannot access temp folder: " + tempRootDir.getAbsolutePath());
			}
		}
	}
	
	public File saveToTempFolder(byte[] data, String originalFileName, String sessionId, CollectRecord record, int nodeId) throws RecordFileException {
		File file = saveToTempFolder(new ByteArrayInputStream(data), originalFileName, sessionId, record, nodeId);
		return file;
	}
	
	public File saveToTempFolder(InputStream is, String originalFileName, String sessionId, CollectRecord record, int nodeId) throws RecordFileException {
		try {
			prepareDeleteFile(sessionId, record, nodeId);
			
			java.io.File tempDestinationFolder = getTempDestDir(sessionId, nodeId);
			if (tempDestinationFolder.exists() || tempDestinationFolder.mkdirs()) {
				String fileName = generateUniqueFilename(tempDestinationFolder, originalFileName);
				java.io.File tempDestinationFile = new java.io.File(tempDestinationFolder, fileName);
				if ( ! tempDestinationFile.exists() && tempDestinationFile.createNewFile() ) {
					FileUtils.copyInputStreamToFile(is, tempDestinationFile);
					long size = tempDestinationFile.length();
					File result = new File(fileName, size);
					tempFiles.put(nodeId, fileName);
					return result;
				} else {
					throw new RecordFileException("Cannot write temp file: " + tempDestinationFile.getAbsolutePath());
				}
			} else {
				throw new RecordFileException("Cannot write into destination folder: " + tempDestinationFolder.getAbsolutePath());
			}
		} catch (IOException e) {
			throw new RecordFileException(e);
		}
	}
	
	protected boolean moveTempFilesToRepository(String sessionId, CollectRecord record) throws RecordFileException {
		try {
			Set<Entry<Integer,String>> entrySet = tempFiles.entrySet();
			boolean result = false;
			for (Entry<Integer, String> entry : entrySet) {
				int nodeId = entry.getKey();
				boolean recordChanged = moveTempFileToRepository(sessionId, record, nodeId);
				result = result || recordChanged;
			}
			tempFiles.clear();
			return result;
		} catch (IOException e) {
			throw new RecordFileException(e);
		}
	}
	
	public void deleteAllTempFiles(String sessionId) {
		Set<Entry<Integer,String>> entrySet = tempFiles.entrySet();
		for (Entry<Integer, String> entry : entrySet) {
			int nodeId = entry.getKey();
			java.io.File tempFile = getTempFile(sessionId, nodeId);
			tempFile.delete();
		}
		tempFiles.clear();
	}
	
	public void prepareDeleteAllFiles(String sessionId, CollectRecord record) {
		for (FileAttribute fileAttribute : record.getFileAttributes()) {
			prepareDeleteFile(sessionId, record, fileAttribute.getInternalId());
		}
	}
	
	public void deleteAllFiles(CollectRecord record) {
		List<java.io.File> files = getAllFiles(record);
		for (java.io.File file : files) {
			file.delete();
		}
	}
	
	public List<java.io.File> getAllFiles(final CollectRecord record) {
		final List<java.io.File> result = new ArrayList<java.io.File>();
		for (FileAttribute fileAttribute : record.getFileAttributes()) {
			java.io.File repositoryFile = getRepositoryFile(fileAttribute);
			if (repositoryFile != null ) {
				result.add(repositoryFile);
			}
		}
		return result;
	}
	
	public void prepareDeleteFile(String sessionId, CollectRecord record, int nodeId) {
		if ( tempFiles.containsKey(nodeId) ) {
			java.io.File tempFile = getTempFile(sessionId, nodeId);
			tempFile.delete();
			tempFiles.remove(nodeId);
		} else {
			java.io.File repositoryFile = getRepositoryFile(record, nodeId);
			if ( repositoryFile != null ) {
				String fileName = repositoryFile.getName();
				filesToDelete.put(nodeId, fileName);
			}
		}
	}
	
	/**
	 * Apply the changes from the temp directory to the final repository directory.
	 * Returns true if the passed record have been changed during the saving process
	 * (for example fileName attribute changed)
	 * 
	 * @param sessionId
	 * @param record
	 * @return
	 * @throws RecordFileException
	 */
	public boolean commitChanges(String sessionId, CollectRecord record) throws RecordFileException {
		performFilesDelete(record);
		boolean recordChanged = moveTempFilesToRepository(sessionId, record);
		return recordChanged;
	}
	
	public java.io.File getFile(String sessionId, CollectRecord record, int nodeId) {
		java.io.File file;
		if ( tempFiles.containsKey(nodeId) ) {
			file = getTempFile(sessionId, nodeId);
		} else {
			file = getRepositoryFile(record, nodeId);
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
			java.io.File repositoryFile = getRepositoryFile(defn, fileName);
			repositoryFile.delete();
			result = true;
		}
		filesToDelete.clear();
		return result;
	}
	
	protected boolean moveTempFileToRepository(String sessionId, CollectRecord record, int nodeId) throws IOException {
		boolean recordUpdated = false;
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		FileAttributeDefinition defn = fileAttribute.getDefinition();
		java.io.File repositoryDir = getRepositoryDir(defn);
		java.io.File tempFile = getTempFile(sessionId, nodeId);
		String repositoryFileName = generateRepositoryFilename(fileAttribute, tempFile.getName());
		java.io.File repositoryFile = new java.io.File(repositoryDir, repositoryFileName);
		long repositoryFileSize = repositoryFile.length();
		if ( ! repositoryFileName.equals(fileAttribute.getFilename() ) || 
				! new Long(repositoryFileSize).equals(fileAttribute.getSize()) ) {
			recordUpdated = true;
			fileAttribute.setFilename(repositoryFileName);
			fileAttribute.setSize(repositoryFileSize);
		}
		FileUtils.deleteQuietly(repositoryFile);
		FileUtils.moveFile(tempFile, repositoryFile);
		return recordUpdated;
	}

	private String generateRepositoryFilename(FileAttribute fileAttribute, String tempFileName) {
		Record record = fileAttribute.getRecord();
		String extension = FilenameUtils.getExtension(tempFileName);
		return String.format("%d_%d.%s", record.getId(), fileAttribute.getInternalId(), extension);
	}

	protected String generateUniqueFilename(java.io.File parentDir, String originalFileName) {
		String extension = FilenameUtils.getExtension(originalFileName);
		String fileName;
		java.io.File file;
		do {
			fileName = UUID.randomUUID().toString() + "." + extension;
			file = new java.io.File(parentDir, fileName);
		} while ( file.exists() );
		return fileName;
	}
	
	protected java.io.File getTempDestRootDirPerSession(String sessionId) {
		java.io.File file = new java.io.File(tempRootDir, sessionId);
		return file;
	}

	protected java.io.File getTempDestDir(String sessionId, int nodeId) {
		java.io.File tempDestRootDirPerSession = getTempDestRootDirPerSession(sessionId);
		java.io.File file = new java.io.File(tempDestRootDirPerSession, Integer.toString(nodeId));
		return file;
	}
	
	protected java.io.File getTempFile(String sessionId, int nodeId) {
		java.io.File tempDestDir = getTempDestDir(sessionId, nodeId);
		java.io.File[] listFiles = tempDestDir.listFiles();
		if ( listFiles != null && listFiles.length > 0 ) {
			return listFiles[0];
		} else {
			return null;
		}
	}
	
	protected java.io.File getRepositoryDir(FileAttributeDefinition defn) {
		java.io.File baseDirectory = storageDirectory;
		String relativePath = getRepositoryRelativePath(defn);
		java.io.File file = new java.io.File(baseDirectory, relativePath);
		return file;
	}

	public static String getRepositoryRelativePath(FileAttributeDefinition defn) {
		return getRepositoryRelativePath(defn, java.io.File.separator, true);
	}

	public static String getRepositoryRelativePath(FileAttributeDefinition defn, 
			String directorySeparator, boolean surveyRelative) {
		Survey survey = defn.getSurvey();
		StringBuilder sb = new StringBuilder();
		if ( surveyRelative ) {
			sb.append(survey.getId());
			sb.append(directorySeparator);
		}
		sb.append(defn.getId());
		return sb.toString();
	}
	
	public java.io.File getRepositoryFile(CollectRecord record, int nodeId) {
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		if ( fileAttribute != null ) {
			return getRepositoryFile(fileAttribute);
		} else {
			return null;
		}
	}
	
	public java.io.File getRepositoryFile(FileAttribute fileAttribute) {
		FileAttributeDefinition defn = fileAttribute.getDefinition();
		String filename = fileAttribute.getFilename();
		if ( StringUtils.isNotBlank(filename) ) {
			java.io.File file = getRepositoryFile(defn, filename);
			return file;
		} else {
			return null;
		}
	}

	protected java.io.File getRepositoryFile(FileAttributeDefinition fileAttributeDefn, String fileName) {
		java.io.File repositoryDir = getRepositoryDir(fileAttributeDefn);
		java.io.File file = new java.io.File(repositoryDir, fileName);
		return file;
	}

}
