package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.codelist.CodeListExportProcess;
import org.openforis.collect.metamodel.view.CodeListItemView;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.collect.web.manager.SessionRecordProvider;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Scope(SCOPE_SESSION)
@RequestMapping("api")
public class CodeListController {

	private static final String CSV_EXTENSION = ".csv";
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private SessionRecordProvider sessionRecordProvider;
	
	@RequestMapping(value = "survey/{surveyId}/codelist/{codeListId}.csv", method=GET)
	public @ResponseBody String exportCodeListWork(HttpServletResponse response,
			@PathVariable("surveyId") Integer surveyId,
			@PathVariable("codeListId") Integer codeListId) throws IOException {
		return exportCodeList(response, surveyId, codeListId);
	}
	
	@RequestMapping(value = "survey/{surveyId}/codelist/{codeListId}", method=GET)
	public @ResponseBody List<CodeListItemView> loadAvailableItems(
			@PathVariable Integer surveyId,
			@PathVariable Integer codeListId,
			@RequestParam(required=false) Integer recordId,
			@RequestParam Step recordStep,
			@RequestParam String parentEntityPath,
			@RequestParam Integer codeAttrDefId) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CollectRecord record = sessionRecordProvider.provide(survey, recordId, recordStep);
		Entity parentEntity = (Entity) record.findNodeByPath(parentEntityPath);
		CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) survey.getSchema().getDefinitionById(codeAttrDefId);
		List<CodeListItem> items = codeListManager.loadValidItems(parentEntity, codeAttrDef);
		return toViews(items);
	}

	protected String exportCodeList(HttpServletResponse response,
			int surveyId, int codeListId) throws IOException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CodeList list = survey.getCodeListById(codeListId);
		String fileName = list.getName() + CSV_EXTENSION;
		Controllers.setOutputContent(response, fileName, MediaTypes.CSV_CONTENT_TYPE);
		ServletOutputStream out = response.getOutputStream();
		CodeListExportProcess process = new CodeListExportProcess(codeListManager);
		process.exportToCSV(out, survey, codeListId);
		return "ok";
	}
	
	private List<CodeListItemView> toViews(List<CodeListItem> items) {
		List<CodeListItemView> views = new ArrayList<CodeListItemView>(items.size());
		for (CodeListItem item : items) {
			CodeListItemView view = new CodeListItemView();
			view.setCode(item.getCode());
			view.setLabel(item.getLabel());
			views.add(view);
		}
		return views;
	}
}
