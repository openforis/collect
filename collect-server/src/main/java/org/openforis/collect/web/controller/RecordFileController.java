package org.openforis.collect.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.manager.SessionRecordFileManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.Files;
import org.openforis.collect.web.manager.SessionRecordProvider;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;

import net.coobird.thumbnailator.Thumbnails;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("api")
public class RecordFileController extends BasicController implements Serializable {

	private static final int THUMBNAIL_SIZE = 150;

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LogManager.getLogger(RecordFileController.class);

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SessionRecordFileManager sessionRecordFileManager;
	@Autowired
	private SessionRecordProvider recordProvider;

	@RequestMapping(value = "/downloadRecordFile.htm", method = RequestMethod.POST)
	@Deprecated
	public void download(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("nodeId") Integer nodeId) throws Exception {
		SessionState sessionState = getSessionState(request);
		CollectRecord record = sessionState.getActiveRecord();
		File file = sessionRecordFileManager.getFile(record, nodeId);
		if (file != null && file.exists()) {
			Controllers.writeFileToResponse(response, file);
		} else {
			Integer recordId = record == null ? null : record.getId();
			String fileName = file == null ? null : file.getName();
			Exception e = new Exception(
					"File not found: " + fileName + " record id: " + recordId + " node id: " + nodeId);
			LOG.error(e);
			throw e;
		}
	}

	@RequestMapping(value = "/survey/{surveyId}/data/records/{recordId}/{recordStep}/node/{nodeId}/file", method = RequestMethod.GET)
	public void downloadFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("surveyId") int surveyId, @PathVariable("recordId") int recordId,
			@PathVariable("recordStep") Step recordStep, @PathVariable("nodeId") int nodeId) throws Exception {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CollectRecord record = recordProvider.provide(survey, recordId == 0 ? null : recordId, recordStep);
		File file = sessionRecordFileManager.getFile(record, nodeId);
		writeFileToResponse(response, file, surveyId, recordId, nodeId);
	}

	@RequestMapping(value = "/survey/{surveyId}/data/records/{recordId}/{recordStep}/node/{nodeId}/thumbnail", method = RequestMethod.GET)
	public void downloadThumbnail(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("surveyId") int surveyId, @PathVariable("recordId") int recordId,
			@PathVariable("recordStep") Step recordStep, @PathVariable("nodeId") int nodeId) throws Exception {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CollectRecord record = recordProvider.provide(survey, recordId == 0 ? null : recordId, recordStep);
		File file = sessionRecordFileManager.getFile(record, nodeId);
		try {
			if (file != null) {
				String extension = FilenameUtils.getExtension(file.getName());
				String outputFileName = String.format("node-%d-file-thumbnail.%s", nodeId, extension);
				String contentType = Files.getContentType(outputFileName);
				response.setContentType(contentType);
				Thumbnails.of(file).size(THUMBNAIL_SIZE, THUMBNAIL_SIZE).toOutputStream(response.getOutputStream());
				return;
			}
		} catch (Exception e) {}
		// Try to write original file to response
		writeFileToResponse(response, file, surveyId, recordId, nodeId);
	}

	private void writeFileToResponse(HttpServletResponse response, File file, int surveyId, int recordId, int nodeId)
			throws IOException, Exception {
		if (file != null && file.exists()) {
			Controllers.writeFileToResponse(response, file);
		} else {
			String fileName = file == null ? null : file.getName();
			Exception e = new Exception(
					String.format("File not found: %s - survey id : %d - record id: %d - node id : %d", fileName,
							surveyId, recordId, nodeId));
			LOG.error(e);
			throw e;
		}
	}

}
