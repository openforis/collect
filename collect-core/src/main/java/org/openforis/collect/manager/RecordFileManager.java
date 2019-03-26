package org.openforis.collect.manager;

import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.RecordFileManager.RecordFileHandle.Type;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.model.RecordFile;
import org.openforis.collect.persistence.RecordFileDao;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 * 
 */
@Transactional(readOnly=true, propagation=SUPPORTS)
public class RecordFileManager extends BaseStorageManager {
	
	private static final long serialVersionUID = 1L;
	private static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	protected static final Logger LOG = LogManager.getLogger(RecordFileManager.class);

	private static final String DEFAULT_RECORD_FILES_SUBFOLDER = "collect_upload";
	
	@Autowired
	private RecordFileDao dao;
	
	public RecordFileManager() {
		super(DEFAULT_RECORD_FILES_SUBFOLDER);
	}
	
	public void init() {
		initStorageDirectory();
	}

	protected void initStorageDirectory() {
		super.initStorageDirectory(ConfigurationItem.RECORD_FILE_UPLOAD_PATH);
	}

	public void deleteAllFiles(int surveyId) {
		dao.deleteBySurveyId(surveyId);
	}
	
	public void deleteAllFiles(CollectRecord record) {
		dao.deleteByRecordId(record.getId());
		
		List<RecordFileHandle> filesInFS = getAllFilesInFS(record);
		for (RecordFileHandle handle : filesInFS) {
			File file = new File(handle.getFilePath());
			file.delete();
		}
	}
	
	public List<RecordFileHandle> getAllFileHandles(CollectRecord record) {
		List<RecordFileHandle> result = new ArrayList<RecordFileHandle>();
		List<RecordFile> recordFiles = dao.loadSummaryByRecordId(record.getId());
		for (RecordFile recordFile : recordFiles) {
			RecordFileHandle handle = new RecordFileHandle(Type.DB);
			handle.setUuid(recordFile.getUuid());
			result.add(handle);
		}
		result.addAll(getAllFilesInFS(record));
		return result;
	}

	private List<RecordFileHandle> getAllFilesInFS(CollectRecord record) {
		List<RecordFileHandle> result = new ArrayList<RecordFileHandle>();
		for (FileAttribute fileAttribute : record.getFileAttributes()) {
			java.io.File repositoryFile = getRepositoryFile(fileAttribute);
			if (repositoryFile != null ) {
				RecordFileHandle handle = new RecordFileHandle(Type.FS);
				handle.setFilePath(repositoryFile.getAbsolutePath());
				result.add(handle);
			}
		}
		return result;
	}
	
	/**
	 * Moves a file into the repository and associates the file name to the corresponding file attribute node 
	 * Returns true if the record is modified (file name or size different from the old one).
	 */
	@Transactional(readOnly=false, propagation=REQUIRED)
	public boolean moveFileIntoRepository(CollectRecord record, int nodeId, java.io.File newFile, String originalFileName) throws IOException {
		boolean recordUpdated = false;
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		
		String repositoryFileName = isUuid(originalFileName) ? originalFileName
				: generateUniqueRepositoryFileName(fileAttribute, newFile);

		byte[] content = FileUtils.readFileToByteArray(newFile);
		
		RecordFile recordFile = new RecordFile();
		recordFile.setUuid(extractUuidFromFileName(repositoryFileName));
		recordFile.setSurveyId(record.getSurvey().getId());
		recordFile.setRecordId(record.getId());
		recordFile.setAttributeDefId(fileAttribute.getDefinition().getId());
		recordFile.setOriginalName(originalFileName);
		recordFile.setCreatedById(record.getCreatedBy().getId());
		recordFile.setModifiedById(record.getModifiedBy().getId());
		recordFile.setContent(content);
		recordFile.setSize(content.length);
		dao.insert(recordFile);
		
		long newFileSize = newFile.length();
		if ( ! repositoryFileName.equals(fileAttribute.getFilename() ) || 
				! Long.valueOf(newFileSize).equals(fileAttribute.getSize()) ) {
			recordUpdated = true;
			fileAttribute.setFilename(repositoryFileName);
			fileAttribute.setSize(newFileSize);
		}
		
		deleteRepositoryFileFromFS(fileAttribute);
//		FileAttributeDefinition defn = fileAttribute.getDefinition();
//		File repositoryFile = new java.io.File(getRepositoryDir(defn), repositoryFileName);
//		FileUtils.moveFile(newFile, repositoryFile);
		
		return recordUpdated;
	}
	
	private boolean isUuid(String fileName) {
		String uuid = extractUuidFromFileName(fileName);
		return uuid != null;
	}
	
	private String extractUuidFromFileName(String fileName ) {
		String baseName = FilenameUtils.getBaseName(fileName);
		Matcher matcher = UUID_REGEX.matcher(baseName);
		return matcher.matches() ? baseName : null;
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
		String extension = FilenameUtils.getExtension(tempFileName);
		return String.format("%s.%s", UUID.randomUUID(), extension);
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
		String filename = fileAttribute.getFilename();
		if ( StringUtils.isNotBlank(filename) ) {
			FileAttributeDefinition defn = fileAttribute.getDefinition();
			java.io.File file = getRepositoryFile(defn, filename);
			return file;
		} else {
			return null;
		}
	}
	
	public boolean deleteRepositoryFileFromFS(FileAttribute fileAttribute) {
		File file = getRespotoryFileFromFS(fileAttribute.getDefinition(), fileAttribute.getFilename());
		return FileUtils.deleteQuietly(file);
	}
	
	@Transactional(readOnly=false, propagation=REQUIRED)
	public void deleteRepositoryFile(RecordFileHandle handle) {
		switch(handle.getType()) {
		case DB:
			dao.deleteByUuid(handle.getUuid());
			break;
		default:
			java.io.File file = new File(handle.getFilePath());
			if (file.exists() && !file.delete()) {
				throw new RuntimeException("Error deleting record file: " + file.getAbsolutePath());
			}
		}
	}
	
	public RecordFileHandle getRepositoryFileHandle(CollectRecord record, int nodeId) {
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		return getRepositoryFileHandle(fileAttribute.getDefinition(), fileAttribute.getFilename());
	}

	public RecordFileHandle getRepositoryFileHandle(FileAttributeDefinition fileAttributeDefn, String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return null;
		}
		String uuid = extractUuidFromFileName(fileName);
		RecordFile recordFile = dao.loadSummaryByUuid(uuid);
		if (recordFile != null) {
			RecordFileHandle handle = new RecordFileHandle(Type.DB);
			handle.setUuid(uuid);
			return handle;
		} else {
			File file = getRespotoryFileFromFS(fileAttributeDefn, fileName);
			if (file != null && file.exists() ) {
				RecordFileHandle handle = new RecordFileHandle(Type.FS);
				handle.setFilePath(file.getAbsolutePath());
				return handle;
			} else {
				return null;
			}
		}
	}

	protected java.io.File getRepositoryFile(FileAttributeDefinition fileAttributeDefn, String fileName) {
		RecordFileHandle handle = getRepositoryFileHandle(fileAttributeDefn, fileName);
		if (handle == null) {
			return null;
		}
		switch(handle.getType()) {
		case DB:
			byte[] content = dao.loadContentByUuid(extractUuidFromFileName(fileName));
			try {
				File file = File.createTempFile("collect-record-file", fileName);
				FileUtils.writeByteArrayToFile(file, content);
				return file;
			} catch (Exception e) {
				LOG.error("Error writing temp file", e);
				return null;
			}
		default:
			return getRespotoryFileFromFS(fileAttributeDefn, fileName);
		}
	}

	private java.io.File getRespotoryFileFromFS(FileAttributeDefinition fileAttributeDefn, String fileName) {
		java.io.File repositoryDir = getRepositoryDir(fileAttributeDefn);
		java.io.File file = new java.io.File(repositoryDir, fileName);
		return file;
	}

	public void setDao(RecordFileDao dao) {
		this.dao = dao;
	}
	
	public static class RecordFileHandle {
		
		public static enum Type {
			DB, FS
		}
		
		private Type type;
		private String uuid;
		private String filePath; //for FS type
		
		public RecordFileHandle(Type type) {
			this.type = type;
		}
		
		public Type getType() {
			return type;
		}
		
		public String getUuid() {
			return uuid;
		}
		
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		
		public String getFilePath() {
			return filePath;
		}
		
		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
	}

}
