package org.openforis.collect.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.Configuration;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Record;

/**
 * 
 * @author S. Ricci
 * 
 */
public class RecordFileManager extends BaseStorageManager {
	
	private static final long serialVersionUID = 1L;

	protected static Log LOG = LogFactory.getLog(RecordFileManager.class);

	private static final String DEFAULT_RECORD_FILES_SUBFOLDER = "collect_upload";
	
	public RecordFileManager() {
		super(DEFAULT_RECORD_FILES_SUBFOLDER);
	}
	
	public void init() {
		initStorageDirectory();
	}

	protected void initStorageDirectory() {
		super.initStorageDirectory(Configuration.UPLOAD_PATH_KEY);
		if ( storageDirectory == null ) {
			String message = "Upload directory not configured properly";
			LOG.error(message);
			throw new IllegalStateException(message);
		} else if ( LOG.isInfoEnabled() ) {
			LOG.info("Using storage directory: " + storageDirectory.getAbsolutePath());
		}
	}
	
	public void deleteAllFiles(CollectRecord record) {
		List<java.io.File> files = getAllFiles(record);
		for (java.io.File file : files) {
			file.delete();
		}
	}
	
	public List<java.io.File> getAllFiles(CollectRecord record) {
		List<java.io.File> result = new ArrayList<java.io.File>();
		for (FileAttribute fileAttribute : record.getFileAttributes()) {
			java.io.File repositoryFile = getRepositoryFile(fileAttribute);
			if (repositoryFile != null ) {
				result.add(repositoryFile);
			}
		}
		return result;
	}
	
	public boolean moveFileIntoRepository(CollectRecord record, int nodeId, java.io.File newFile) throws IOException {
		boolean recordUpdated = false;
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		FileAttributeDefinition defn = fileAttribute.getDefinition();
		java.io.File repositoryDir = getRepositoryDir(defn);
		String repositoryFileName = generateRepositoryFilename(fileAttribute, newFile.getName());
		java.io.File repositoryFile = new java.io.File(repositoryDir, repositoryFileName);
		long repositoryFileSize = repositoryFile.length();
		if ( ! repositoryFileName.equals(fileAttribute.getFilename() ) || 
				! new Long(repositoryFileSize).equals(fileAttribute.getSize()) ) {
			recordUpdated = true;
			fileAttribute.setFilename(repositoryFileName);
			fileAttribute.setSize(repositoryFileSize);
		}
		FileUtils.deleteQuietly(repositoryFile);
		FileUtils.moveFile(newFile, repositoryFile);
		return recordUpdated;
	}

	private String generateRepositoryFilename(FileAttribute fileAttribute, String tempFileName) {
		Record record = fileAttribute.getRecord();
		String extension = FilenameUtils.getExtension(tempFileName);
		String result = String.format("%d_%d.%s", record.getId(), fileAttribute.getInternalId(), extension);
		return result;
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
