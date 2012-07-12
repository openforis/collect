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
import org.apache.commons.lang3.StringUtils;
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
 * It needs to be associated to a user session
 *
 */
public class RecordFileManager {
	
	private static final String TEMP_PATH = "temp" + File.separator + "recordFiles";

	private static final String UPLOAD_PATH_CONFIGURATION_KEY = "upload_path";
	
	@Autowired
	private ConfigurationManager configurationManager;
	
	@Autowired 
	private ServletContext servletContext;
	
	private Map<Integer, String> tempFiles;
	private Map<Integer, String> filesToDelete;

	protected File tempRootDir;
	protected File respositoryRootDir;
	
	protected void init() {
		tempFiles = new HashMap<Integer, String>();
		filesToDelete = new HashMap<Integer, String>();
		
		initTempDir();
		initRepositoryDir();
	}

	protected void initTempDir() {
		String tempRealPath = servletContext.getRealPath(TEMP_PATH);
		tempRootDir = new File(tempRealPath);
		if ( ! tempRootDir.exists() ) {
			tempRootDir.mkdirs();
		}	
		if ( ! tempRootDir.canRead() ) {
			throw new IllegalStateException("Cannot access temp directory: " + tempRealPath);
		}
	}
	
	protected void initRepositoryDir() {
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
			prepareDeleteFile(sessionId, record, nodeId);
			
			File tempDestinationFolder = getTempDestDir(sessionId, nodeId);
			if (tempDestinationFolder.exists() || tempDestinationFolder.mkdirs()) {
				String fileId = generateUniqueFilename(originalFileName);
				File tempDestinationFile = new File(tempDestinationFolder, fileId);
				if (!tempDestinationFile.exists() && tempDestinationFile.createNewFile()) {
					writeToFile(is, tempDestinationFile);
					tempFiles.put(nodeId, fileId);
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
		Set<Entry<Integer,String>> entrySet = tempFiles.entrySet();
		Iterator<Entry<Integer, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<Integer, String> entry = iterator.next();
			int nodeId = entry.getKey();
			moveTempFileToRepository(sessionId, record, nodeId);
			iterator.remove();
		}
	}
	
	public void deleteAllTempFiles(String sessionId, CollectRecord record) {
		Set<Entry<Integer,String>> entrySet = tempFiles.entrySet();
		Iterator<Entry<Integer, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<Integer, String> entry = iterator.next();
			int nodeId = entry.getKey();
			File tempFile = getTempFile(sessionId, nodeId);
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
					File repositoryFile = getRepositoryFile(record, node.getId());
					repositoryFile.delete();
				}
			}
		});
	}
	
	public void completeFilesDeletion(CollectRecord record) {
		Set<Entry<Integer,String>> entrySet = filesToDelete.entrySet();
		Iterator<Entry<Integer, String>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			Entry<Integer, String> entry = iterator.next();
			int nodeId = entry.getKey();
			File repositoryFile = getRepositoryFile(record, nodeId);
			repositoryFile.delete();
			iterator.remove();
		}
	}
	
	private void moveTempFileToRepository(String sessionId, CollectRecord record, int nodeId) throws IOException {
		File tempFile = getTempFile(sessionId, nodeId);
		File repositoryFile = getRepositoryFile(record, nodeId);
		FileUtils.copyFile(tempFile, repositoryFile, true);
		tempFile.delete();
	}

	public File getFile(String sessionId, CollectRecord record, int nodeId) {
		File file;
		if ( tempFiles.containsKey(nodeId) ) {
			file = getTempFile(sessionId, nodeId);
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

	protected File getTempDestDir(String sessionId, int nodeId) {
		File tempDestRootDirPerSession = getTempDestRootDirPerSession(sessionId);
		File file = new File(tempDestRootDirPerSession, Integer.toString(nodeId));
		return file;
	}
	
	protected File getTempFile(String sessionId, int nodeId) {
		File tempDestDir = getTempDestDir(sessionId, nodeId);
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
		File file = null;
		Survey survey = record.getSurvey();
		Integer surveyId = survey.getId();
		String filename = null;
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		if ( fileAttribute != null ) {
			FileAttributeDefinition definition = fileAttribute.getDefinition();
			filename = fileAttribute.getFilename();
			if ( StringUtils.isNotBlank(filename) ) {
				File repositoryDir = getRepositoryDir(surveyId, definition.getId());
				file = new File(repositoryDir, filename);
			}
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

	public void prepareDeleteFile(String sessionId, CollectRecord record, int nodeId) {
		if ( tempFiles.containsKey(nodeId) ) {
			File tempFile = getTempFile(sessionId, nodeId);
			tempFile.delete();
			tempFiles.remove(nodeId);
		} else {
			File repositoryFile = getRepositoryFile(record, nodeId);
			if ( repositoryFile != null ) {
				String fileName = repositoryFile.getName();
				filesToDelete.put(nodeId, fileName);
			}
		}
	}
	
}
