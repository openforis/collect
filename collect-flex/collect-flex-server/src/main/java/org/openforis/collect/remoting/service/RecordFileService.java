package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.proxy.FileProxy;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.model.File;
import org.openforis.idm.model.FileAttribute;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordFileService {

	@Autowired
	private RecordFileManager fileManager;
	
	@Autowired
	private SessionManager sessionManager;
	
	public FileProxy upload(byte[] data, String originalFileName, int nodeId) throws Exception {
		SessionState sessionState = sessionManager.getSessionState();
		String sessionId = sessionState.getSessionId();
		CollectRecord record = sessionState.getActiveRecord();
		String fileName = fileManager.saveToTempFolder(data, originalFileName, sessionId, record, nodeId);
		FileAttribute fileAttr = (FileAttribute) record.getNodeByInternalId(nodeId);
		File file = new File(fileName, new Long(data.length));
		fileAttr.setValue(file);
		FileProxy fileProxy = new FileProxy(file);
		return fileProxy;
	}
	
	public void deleteFile(int nodeId) {
		SessionState sessionState = sessionManager.getSessionState();
		String sessionId = sessionState.getSessionId();
		CollectRecord record = sessionState.getActiveRecord();
		FileAttribute fileAttr = (FileAttribute) record.getNodeByInternalId(nodeId);
		fileManager.prepareDeleteFile(sessionId, record, nodeId);
		fileAttr.setValue(null);
	}
	
}
