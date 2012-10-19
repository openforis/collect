package org.openforis.collect.web.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordFileController extends BasicController {
	
	private static Log LOG = LogFactory.getLog(RecordFileController.class);
	
	@Autowired
	private RecordFileManager fileManager;
	
	@RequestMapping(value = "/uploadRecordFile.htm", method = RequestMethod.POST)
	public @ResponseBody String upload(@RequestParam("Filedata") MultipartFile file, HttpServletRequest request, 
			@RequestParam("sessionId") String sessionId, @RequestParam("surveyId") Integer surveyId, @RequestParam("recordId") Integer recordId,
			@RequestParam("nodeId") Integer nodeId) throws Exception {
		//String fileId = fileManager.saveToTempFolder(file, sessionId, surveyId, recordId, nodeId);
		return null;
	}
	
	@RequestMapping(value = "/downloadRecordFile.htm", method = RequestMethod.POST)
	public void download(HttpServletRequest request, HttpServletResponse response, @RequestParam("nodeId") Integer nodeId) throws Exception {
		SessionState sessionState = getSessionState(request);
		String sessionId = sessionState.getSessionId();
		CollectRecord record = sessionState.getActiveRecord();
		File file = fileManager.getFile(sessionId, record, nodeId);
		if(file != null && file.exists()) {
			writeFileToResponse(response, file);
		} else {
			Exception e = new Exception("File not found");
			LOG.error(e);
			throw e;
		}
	}
}
