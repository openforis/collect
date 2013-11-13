package org.openforis.collect.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

	public RecordFileManager() {
	}
	
	public void init() {
		initTempDir();
		initStorageDirectory();
		reset();
	}

	public void reset() {
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
				String fileId = generateUniqueFilename(originalFileName);
				java.io.File tempDestinationFile = new java.io.File(tempDestinationFolder, fileId);
				if (!tempDestinationFile.exists() && tempDestinationFile.createNewFile()) {
					FileUtils.copyInputStreamToFile(is, tempDestinationFile);
					tempFiles.put(nodeId, fileId);
					long size = tempDestinationFile.length();
					File result = new File(fileId, size);
					return result;
				} else {
					throw new RecordFileException("Cannot write file");
				}
			} else {
				throw new RecordFileException("Cannot write to destination folder");
			}
		} catch (IOException e) {
			throw new RecordFileException(e);
		}
	}
	
	public void moveTempFilesToRepository(String sessionId, CollectRecord record) throws RecordFileException {
		try {
			Set<Entry<Integer,String>> entrySet = tempFiles.entrySet();
			Iterator<Entry<Integer, String>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Entry<Integer, String> entry = iterator.next();
				int nodeId = entry.getKey();
				moveTempFileToRepository(sessionId, record, nodeId);
				iterator.remove();
			}
		} catch (IOException e) {
			throw new RecordFileException(e);
		}
	}
	
	public void deleteAllTempFiles(String sessionId) {
		Set<Entry<Integer,String>> entrySet = tempFiles.entrySet();
		Iterator<Entry<Integer, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<Integer, String> entry = iterator.next();
			int nodeId = entry.getKey();
			java.io.File tempFile = getTempFile(sessionId, nodeId);
			tempFile.delete();
			iterator.remove();
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
			java.io.File repositoryFile = getRepositoryFile(record, fileAttribute.getInternalId());
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
	
	public void commitChanges(String sessionId, CollectRecord record) throws RecordFileException {
		completeFilesDeletion(record);
		moveTempFilesToRepository(sessionId, record);
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

	protected void completeFilesDeletion(CollectRecord record) {
		Set<Entry<Integer,String>> entrySet = filesToDelete.entrySet();
		Iterator<Entry<Integer, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<Integer, String> entry = iterator.next();
			int nodeId = entry.getKey();
			String fileName = entry.getValue();
			java.io.File repositoryFile = getRepositoryFile(record, nodeId, fileName);
			repositoryFile.delete();
			iterator.remove();
		}
	}
	
	protected void moveTempFileToRepository(String sessionId, CollectRecord record, int nodeId) throws IOException {
		java.io.File tempFile = getTempFile(sessionId, nodeId);
		String fileName = tempFile.getName();
		java.io.File repositoryFile = getRepositoryFile(record, nodeId, fileName);
		FileUtils.copyFile(tempFile, repositoryFile, true);
		tempFile.delete();
	}

	protected String generateUniqueFilename(String originalFileName) {
		String extension = FilenameUtils.getExtension(originalFileName);
		String fileId = UUID.randomUUID().toString() + "." + extension;
		return fileId;
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

	public String getRepositoryRelativePath(FileAttributeDefinition defn) {
		return getRepositoryRelativePath(defn, java.io.File.separator, true);
	}

	public String getRepositoryRelativePath(FileAttributeDefinition defn, 
			String directorySeparator, boolean surveyRelative) {
		Survey survey = defn.getSurvey();
		String relativePath = survey.getId() + directorySeparator + defn.getId();
		return relativePath;
	}
	
	public java.io.File getRepositoryFile(CollectRecord record, int nodeId) {
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		if ( fileAttribute != null ) {
			String filename = fileAttribute.getFilename();
			if ( StringUtils.isNotBlank(filename) ) {
				return getRepositoryFile(record, nodeId, filename);
			}
		}
		return null;
	}
	
	protected java.io.File getRepositoryFile(CollectRecord record, int nodeId, String fileName) {
		java.io.File file = null;
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		if ( fileAttribute != null ) {
			FileAttributeDefinition definition = fileAttribute.getDefinition();
			java.io.File repositoryDir = getRepositoryDir(definition);
			file = new java.io.File(repositoryDir, fileName);
		}
		return file;
	}

}
