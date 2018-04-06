package org.openforis.collect.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.Configuration.ConfigurationItem;
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
		super.initStorageDirectory(ConfigurationItem.RECORD_FILE_UPLOAD_PATH);
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
	
	/**
	 * Moves a file into the repository and associates the file name to the corresponding file attribute node 
	 * Returns true if the record is modified (file name or size different from the old one).
	 */
	public boolean moveFileIntoRepository(CollectRecord record, int nodeId, java.io.File newFile) throws IOException {
		boolean recordUpdated = false;
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);

		FileAttributeDefinition defn = fileAttribute.getDefinition();
		
		String repositoryFileName = generateUniqueRepositoryFileName(fileAttribute, newFile);
		File repositoryFile = new java.io.File(getRepositoryDir(defn), repositoryFileName);
		
		long repositoryFileSize = newFile.length();
		if ( ! repositoryFileName.equals(fileAttribute.getFilename() ) || 
				! Long.valueOf(repositoryFileSize).equals(fileAttribute.getSize()) ) {
			recordUpdated = true;
			fileAttribute.setFilename(repositoryFileName);
			fileAttribute.setSize(repositoryFileSize);
		}
		FileUtils.moveFile(newFile, repositoryFile);
		
		return recordUpdated;
	}
	
	private String generateUniqueRepositoryFileName(FileAttribute fileAttribute, java.io.File file) {
		java.io.File repositoryDir = getRepositoryDir(fileAttribute.getDefinition());
		String repositoryFileName;
		File repositoryFile;
		do {
			repositoryFileName = generateNewRepositoryFilename(fileAttribute, file.getName());
			repositoryFile = new java.io.File(repositoryDir, repositoryFileName);
		} while (repositoryFile.exists());
		
		return repositoryFileName;
	}

	private String generateNewRepositoryFilename(FileAttribute fileAttribute, String tempFileName) {
		Record record = fileAttribute.getRecord();
		String extension = FilenameUtils.getExtension(tempFileName);
		return String.format("%d_%d.%s", record.getId(), System.currentTimeMillis(), extension);
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
		if ( fileAttribute == null ) {
			return null;
		} else {
			return getRepositoryFile(fileAttribute);
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
	
	public boolean deleteRepositoryFile(FileAttribute fileAttribute) {
		File file = getRepositoryFile(fileAttribute);
		return FileUtils.deleteQuietly(file);
	}

	protected java.io.File getRepositoryFile(FileAttributeDefinition fileAttributeDefn, String fileName) {
		java.io.File repositoryDir = getRepositoryDir(fileAttributeDefn);
		java.io.File file = new java.io.File(repositoryDir, fileName);
		return file;
	}

	public String getRepositoryFileAbsolutePath(FileAttribute fileAttribute) {
		FileAttributeDefinition defn = fileAttribute.getDefinition();
		String filename = fileAttribute.getFilename();
		if ( StringUtils.isNotBlank(filename) ) {
			String path = getRepositoryFileAbsolutePath(defn, filename);
			return path;
		} else {
			return null;
		}
	}
	
	public String getRepositoryFileAbsolutePath(FileAttributeDefinition fileAttributeDefn, String fileName) {
		java.io.File repositoryDir = getRepositoryDir(fileAttributeDefn);
		java.io.File file = new java.io.File(repositoryDir, fileName);
		return file.getAbsolutePath();
	}
		
}
