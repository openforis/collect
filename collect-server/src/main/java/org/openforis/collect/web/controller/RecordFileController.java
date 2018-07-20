package org.openforis.collect.web.controller;

import java.io.File;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.SessionRecordFileManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class RecordFileController extends BasicController implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogManager.getLogger(RecordFileController.class);
	
	@Autowired
	private SessionRecordFileManager fileManager;
	
	@RequestMapping(value = "/downloadRecordFile.htm", method = RequestMethod.POST)
	public void download(HttpServletRequest request, HttpServletResponse response, @RequestParam("nodeId") Integer nodeId) throws Exception {
		SessionState sessionState = getSessionState(request);
		CollectRecord record = sessionState.getActiveRecord();
		File file = fileManager.getFile(record, nodeId);
		if(file != null && file.exists()) {
			Controllers.writeFileToResponse(response, file);
		} else {
			Integer recordId = record == null ? null: record.getId();
			String fileName = file == null ? null: file.getName();
			Exception e = new Exception("File not found: " + fileName + " record id: " + recordId + " node id: " + nodeId);
			LOG.error(e);
			throw e;
		}
	}
	
}
