package org.openforis.collect.manager;

import java.io.ByteArrayInputStream;
import java.io.File;
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

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.Configuration;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordFileManager {
	
	private static final String TEMP_PATH = "tempModelFileUpload";

	private static final String UPLOAD_PATH_CONFIGURATION_KEY = "upload_path";
	
	@Autowired
	private ConfigurationManager configurationManager;
	
	@Autowired 
	private ServletContext servletContext;
	
	private Map<FileKey, FileInfo> tempFiles;
	private Map<FileKey, FileInfo> filesToDelete;

	protected File tempRootDir;
	protected File respositoryRootDir;
	
	protected void init() {
		tempFiles = new HashMap<FileKey, RecordFileManager.FileInfo>();
		filesToDelete = new HashMap<FileKey, RecordFileManager.FileInfo>();
		
		String tempRealPath = servletContext.getRealPath(TEMP_PATH);
		tempRootDir = new File(tempRealPath);
		if ( ! tempRootDir.exists() ) {
			tempRootDir.mkdirs();
		}	
		if ( ! tempRootDir.canRead() ) {
			throw new IllegalStateException("Cannot access temp directory: " + tempRealPath);
		}
		Configuration configuration = configurationManager.getConfiguration();
		String repositoryRootPath = configuration.get(UPLOAD_PATH_CONFIGURATION_KEY);
		respositoryRootDir = new File(repositoryRootPath);
		if ( ! respositoryRootDir.exists() ) {
			respositoryRootDir.mkdirs();
		}
		if ( ! respositoryRootDir.canRead() ) {
			throw new IllegalStateException("Cannot access repository directory: " + repositoryRootPath);
		}
	}
	
	public String saveToTempFolder(byte[] data, String originalFileName, String sessionId, CollectRecord record, int nodeId) throws Exception {
		String fileId = saveToTempFolder(new ByteArrayInputStream(data), originalFileName, sessionId, record, nodeId);
		return fileId;
	}
	
	public String saveToTempFolder(MultipartFile file, String sessionId, CollectRecord record, int nodeId) throws Exception {
		if (!file.isEmpty()) {
			String fileId = saveToTempFolder(file.getInputStream(), file.getOriginalFilename(), sessionId, record, nodeId);
			return fileId;
		} else {
			throw new Exception("file is empty");
		}
	}
	
	public String saveToTempFolder(InputStream is, String originalFileName, String sessionId, CollectRecord record, int nodeId) throws Exception {
		try {
			FileKey fileKey = new FileKey(record, nodeId);
			File tempDestinationFolder = getTempDestDir(sessionId, fileKey);
			if (tempDestinationFolder.exists() || tempDestinationFolder.mkdirs()) {
				String fileId = generateUniqueFilename(originalFileName);
				File tempDestinationFile = new File(tempDestinationFolder, fileId);
				if (!tempDestinationFile.exists() && tempDestinationFile.createNewFile()) {
					writeToFile(is, tempDestinationFile);
					FileInfo fileInfo = new FileInfo(fileKey, fileId);
					tempFiles.put(fileKey, fileInfo);
					return fileId;
				} else {
					throw new Exception("Cannot write file");
				}
			} else {
				throw new Exception("Cannot write to destination folder");
			}
		} catch (IOException e) {
			throw new Exception(e);
		}
	}
	
	public void moveTempFilesToRepository(String sessionId, CollectRecord record) throws IOException {
		Set<Entry<FileKey, FileInfo>> entrySet = tempFiles.entrySet();
		Iterator<Entry<FileKey, FileInfo>> iterator = entrySet.iterator();
		Integer recordId = record.getId();
		while (iterator.hasNext()) {
			Entry<FileKey, FileInfo> entry = iterator.next();
			FileInfo fileInfo = entry.getValue();
			FileKey key = fileInfo.getKey();
			CollectRecord fileRecord = key.getRecord();
			if ( fileRecord.getId().equals(recordId) ) {
				moveTempFileToRepository(sessionId, fileInfo);
				iterator.remove();
			}
		}
	}
	
	public void deleteAllTempFiles(String sessionId, CollectRecord record) {
		Set<Entry<FileKey, FileInfo>> entrySet = tempFiles.entrySet();
		Iterator<Entry<FileKey, FileInfo>> iterator = entrySet.iterator();
		Integer recordId = record.getId();
		while (iterator.hasNext()) {
			Entry<FileKey, FileInfo> entry = iterator.next();
			FileInfo fileInfo = entry.getValue();
			FileKey key = fileInfo.getKey();
			CollectRecord fileRecord = key.getRecord();
			if ( fileRecord.getId().equals(recordId) ) {
				File tempFile = getTempFile(sessionId, key);
				tempFile.delete();
				iterator.remove();
			}
		}
	}
	
	public void deleteAllFiles(final CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		rootEntity.traverse(new NodeVisitor() {
			
			@Override
			public void visit(Node<? extends NodeDefinition> node, int pos) {
				if ( node instanceof FileAttribute ) {
					File repositoryFile = getRepositoryFile(record, node.getId());
					repositoryFile.delete();
				}
			}
		});
	}
	
	public void completeFilesDeletion(CollectRecord record) {
		Set<Entry<FileKey, FileInfo>> entrySet = filesToDelete.entrySet();
		Iterator<Entry<FileKey, FileInfo>> iterator = entrySet.iterator();
		Integer recordId = record.getId();
		while (iterator.hasNext()) {
			Entry<FileKey, FileInfo> entry = iterator.next();
			FileInfo fileInfo = entry.getValue();
			FileKey key = fileInfo.getKey();
			CollectRecord fileRecord = key.getRecord();
			if ( fileRecord.getId().equals(recordId) ) {
				File repositoryFile = getRepositoryFile(record, key.getNodeId());
				repositoryFile.delete();
				iterator.remove();
			}
		}
	}
	
	private void moveTempFileToRepository(String sessionId, FileInfo fileInfo) throws IOException {
		FileKey key = fileInfo.getKey();
		int nodeId = key.getNodeId();
		CollectRecord record = key.getRecord();
		File tempFile = getTempFile(sessionId, key);
		File repositoryFile = getRepositoryFile(record, nodeId);
		FileUtils.copyFile(tempFile, repositoryFile, true);
		tempFile.delete();
	}

	public File getFile(String sessionId, CollectRecord record, int nodeId) {
		File file;
		FileKey fileKey = new FileKey(record, nodeId);
		if ( tempFiles.containsKey(fileKey) ) {
			file = getTempFile(sessionId, fileKey);
		} else {
			file = getRepositoryFile(record, nodeId);
		}
		return file;
	}

	protected String generateUniqueFilename(String originalFileName) {
		String extension = getExtension(originalFileName);
		String fileId = UUID.randomUUID().toString() + "." + extension;
		return fileId;
	}
	
	protected File getTempDestRootDirPerSession(String sessionId) {
		File file = new File(tempRootDir, sessionId);
		return file;
	}

	protected File getTempDestDir(String sessionId, FileKey key) {
		File tempDestRootDirPerSession = getTempDestRootDirPerSession(sessionId);
		File file = new File(tempDestRootDirPerSession, Integer.toString(key.getNodeId()));
		return file;
	}
	
	protected File getTempFile(String sessionId, FileKey key) {
		File tempDestDir = getTempDestDir(sessionId, key);
		File[] listFiles = tempDestDir.listFiles();
		if ( listFiles != null && listFiles.length > 0 ) {
			return listFiles[0];
		} else {
			return null;
		}
	}
	
	protected File getRepositoryDir(Integer surveyId, Integer nodeDefnId) {
		File file = new File(respositoryRootDir, surveyId + File.separator + nodeDefnId);
		return file;
	}
	
	protected File getRepositoryFile(CollectRecord record, int nodeId) {
		Survey survey = record.getSurvey();
		Integer surveyId = survey.getId();
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		FileAttributeDefinition definition = fileAttribute.getDefinition();
		String filename = fileAttribute.getFilename();
		File repositoryDir = getRepositoryDir(surveyId, definition.getId());
		File file = new File(repositoryDir, filename);
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
	
	protected static void writeToFile(InputStream is, File file) throws IOException {
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
	
	public class FileInfo {
		
		private FileKey key;
		private String fileName;

		public FileInfo(FileKey key, String fileName) {
			super();
			this.key = key;
			this.fileName = fileName;
		}
		
		public FileKey getKey() {
			return key;
		}

		public void setKey(FileKey key) {
			this.key = key;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
	}
	
	public class FileKey {
		
		private CollectRecord record;
		private int nodeId;
		
		public FileKey(CollectRecord record, int nodeId) {
			super();
			this.record = record;
			this.nodeId = nodeId;
		}

		public CollectRecord getRecord() {
			return record;
		}

		public void setRecord(CollectRecord record) {
			this.record = record;
		}

		public int getNodeId() {
			return nodeId;
		}

		public void setNodeId(int nodeId) {
			this.nodeId = nodeId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + nodeId;
			result = prime * result + ((record == null) ? 0 : record.hashCode());
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
			FileKey other = (FileKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (nodeId != other.nodeId)
				return false;
			if (record == null) {
				if (other.record != null)
					return false;
			} else if (!record.equals(other.record))
				return false;
			return true;
		}

		private RecordFileManager getOuterType() {
			return RecordFileManager.this;
		}
		
	}

}
