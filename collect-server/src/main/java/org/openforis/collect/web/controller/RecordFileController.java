package org.openforis.collect.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.SessionRecordFileManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.Files;
import org.openforis.collect.web.manager.SessionRecordProvider;
import org.openforis.idm.model.FileAttribute;
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

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SessionRecordFileManager sessionRecordFileManager;
	@Autowired
	private SessionRecordProvider recordProvider;

	@RequestMapping(value = "/survey/{surveyId}/data/records/{recordId}/{recordStep}/file", method = RequestMethod.GET)
	public void downloadFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("surveyId") int surveyId, @PathVariable("recordId") int recordId,
			@PathVariable("recordStep") Step recordStep, @RequestParam("nodePath") String nodePath) throws IOException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CollectRecord record = recordProvider.provide(survey, recordId == 0 ? null : recordId, recordStep);
		FileAttribute fileAttribute = record.getNodeByPath(nodePath);
		File file = getFile(fileAttribute);
		String outputFileName = determineOutputFileName(survey, fileAttribute, file);
		Controllers.writeFileToResponse(response, file, outputFileName);
	}

	@RequestMapping(value = "/survey/{surveyId}/data/records/{recordId}/{recordStep}/file-thumbnail", method = RequestMethod.GET)
	public void downloadThumbnail(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("surveyId") int surveyId, @PathVariable("recordId") int recordId,
			@PathVariable("recordStep") Step recordStep, @RequestParam("nodePath") String nodePath) throws IOException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CollectRecord record = recordProvider.provide(survey, recordId == 0 ? null : recordId, recordStep);
		FileAttribute node = record.getNodeByPath(nodePath);
		File file = getFile(node);
		try {
			String extension = FilenameUtils.getExtension(file.getName());
			String outputFileName = String.format("node-%d-file-thumbnail.%s", node.getId(), extension);
			String contentType = Files.getContentType(outputFileName);
			response.setContentType(contentType);
			Thumbnails.of(file).size(THUMBNAIL_SIZE, THUMBNAIL_SIZE).toOutputStream(response.getOutputStream());
			return;
		} catch (Exception e) {
			// Try to write original file to response
			Controllers.writeFileToResponse(response, file);
		}
	}

	private File getFile(FileAttribute node) {
		File file = sessionRecordFileManager.getFile(node);
		if (file == null || !file.exists()) {
			throw new IllegalStateException(String.format("File not found for attribute %s in record %d in survey %d",
					node.getPath(), node.getRecord().getId(), node.getSurvey().getId()));
		}
		return file;
	}

	
	private String determineOutputFileName(CollectSurvey survey, FileAttribute node, File file) {
		String originalFileName = file.getName();
		CollectAnnotations annotations = survey.getAnnotations();
		String fileNameExpression = annotations.getFileNameExpression(node.getDefinition());

		if (StringUtils.isBlank(fileNameExpression)) {
			return originalFileName;
		}
		try {
			String fileName = (String) survey.getContext().getExpressionEvaluator().evaluateValue(node.getParent(), node, fileNameExpression);
			String extension = FilenameUtils.getExtension(originalFileName);
			if (StringUtils.isBlank(extension)) {
				return fileName;
			}
			return fileName + "." + extension;
		} catch (Exception e) {
			return originalFileName;
		}
	}
}
