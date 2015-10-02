package org.openforis.collect.web.controller;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.codelist.CodeListExportProcess;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CodeListController {

	private static final String CSV_CONTENT_TYPE = "text/csv";
	private static final String CSV_EXTENSION = ".csv";
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CodeListManager codeListManager;
	
	@RequestMapping(value = "/codelist/export/{surveyId}/{codeListId}", method = RequestMethod.GET)
	public @ResponseBody String exportCodeList(HttpServletResponse response,
			@PathVariable("surveyId") Integer surveyId, 
			@PathVariable("codeListId") Integer codeListId) throws IOException {
		return exportCodeList(response, surveyId, false, codeListId);
	}
	
	@RequestMapping(value = "/codelist/export/work/{surveyId}/{codeListId}", method = RequestMethod.GET)
	public @ResponseBody String exportCodeListWork(HttpServletResponse response,
			@PathVariable("surveyId") Integer surveyId,
			@PathVariable("codeListId") Integer codeListId) throws IOException {
		return exportCodeList(response, surveyId, true, codeListId);
	}
	
	protected String exportCodeList(HttpServletResponse response,
			int surveyId, boolean work, int codeListId) throws IOException {
		CollectSurvey survey = work ? surveyManager.loadSurvey(surveyId): surveyManager.getById(surveyId);
		CodeList list = survey.getCodeListById(codeListId);
		String fileName = list.getName() + CSV_EXTENSION;
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setContentType(CSV_CONTENT_TYPE); 
		ServletOutputStream out = response.getOutputStream();
		CodeListExportProcess process = new CodeListExportProcess(codeListManager);
		process.exportToCSV(out, survey, codeListId);
		return "ok";
	}
	
}
