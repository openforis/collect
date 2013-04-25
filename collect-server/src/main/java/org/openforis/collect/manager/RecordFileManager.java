package org.openforis.collect.manager;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.File;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.springframework.web.multipart.MultipartFile;

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
	

	protected void init() {
		initTempDir();
		initStorageDirectory(UPLOAD_PATH_CONFIGURATION_KEY, DEFAULT_RECORD_FILES_SUBFOLDER);
		reset();
	}

	public void reset() {
		tempFiles = new HashMap<Integer, String>();
		filesToDelete = new HashMap<Integer, String>();
	}

	@Override
	protected void initStorageDirectory(String pathConfigurationKey, String subFolder) {
		super.initStorageDirectory(pathConfigurationKey, subFolder);
		if ( storageDirectory == null ) {
			String message = "Upload directory not configured conrrectly";
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
	
	public File saveToTempFolder(MultipartFile file, String sessionId, CollectRecord record, int nodeId) throws Exception {
		if (!file.isEmpty()) {
			File result = saveToTempFolder(file.getInputStream(), file.getOriginalFilename(), sessionId, record, nodeId);
			return result;
		} else {
			throw new Exception("file is empty");
		}
	}
	
	public File saveToTempFolder(InputStream is, String originalFileName, String sessionId, CollectRecord record, int nodeId) throws RecordFileException {
		try {
			prepareDeleteFile(sessionId, record, nodeId);
			
			java.io.File tempDestinationFolder = getTempDestDir(sessionId, nodeId);
			if (tempDestinationFolder.exists() || tempDestinationFolder.mkdirs()) {
				String fileId = generateUniqueFilename(originalFileName);
				java.io.File tempDestinationFile = new java.io.File(tempDestinationFolder, fileId);
				if (!tempDestinationFile.exists() && tempDestinationFile.createNewFile()) {
					writeToFile(is, tempDestinationFile);
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
	
	public void deleteAllFiles(final CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		rootEntity.traverse(new NodeVisitor() {
			@Override
			public void visit(Node<? extends NodeDefinition> node, int pos) {
				if ( node instanceof FileAttribute ) {
					java.io.File repositoryFile = getRepositoryFile(record, node.getInternalId());
					if (repositoryFile != null ) {
						repositoryFile.delete();
					}
				}
			}
		});
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
		String extension = getExtension(originalFileName);
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
	
	protected java.io.File getRepositoryDir(Integer surveyId, Integer nodeDefnId) {
		java.io.File file = new java.io.File(storageDirectory, surveyId + java.io.File.separator + nodeDefnId);
		return file;
	}
	
	protected java.io.File getRepositoryFile(CollectRecord record, int nodeId) {
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
		Survey survey = record.getSurvey();
		Integer surveyId = survey.getId();
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		if ( fileAttribute != null ) {
			FileAttributeDefinition definition = fileAttribute.getDefinition();
			java.io.File repositoryDir = getRepositoryDir(surveyId, definition.getId());
			file = new java.io.File(repositoryDir, fileName);
		}
		return file;
	}

	protected String getExtension(String fileName) {
		String extension = null;
		if (fileName != null) {
			int lastIndexOfDot = fileName.lastIndexOf('.');
			if (lastIndexOfDot > 0) {
				extension = fileName.substring(fileName.lastIndexOf('.') + 1);
			}
		}
		return extension;
	}
	
	protected static void writeToFile(InputStream is, java.io.File file) throws IOException {
		try {  
			OutputStream os = new FileOutputStream(file);  
			try {  
				byte[] buffer = new byte[4096];  
				for (int n; (n = is.read(buffer)) != -1; ) {   
					os.write(buffer, 0, n);  
				}
			}
			finally { 
				os.close(); 
			}
		}
		finally { 
			is.close(); 
		}  
	}

}
