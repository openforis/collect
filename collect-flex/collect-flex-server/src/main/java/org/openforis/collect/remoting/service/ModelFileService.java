package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.ModelFileManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class ModelFileService {

	@Autowired
	private ModelFileManager fileManager;
	
	@Autowired
	private SessionManager sessionManager;
	
	
	public String upload(byte[] data, String originalFileName, int nodeId) throws Exception {
		SessionState sessionState = sessionManager.getSessionState();
		String sessionId = sessionState.getSessionId();
		CollectRecord record = sessionState.getActiveRecord();
		String fileId = fileManager.saveToTempFolder(data, originalFileName, sessionId, record, nodeId);
		return fileId;
	}
	
}
