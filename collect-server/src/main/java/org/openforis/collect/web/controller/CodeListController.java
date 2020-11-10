package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.dataexport.codelist.CodeListExportProcess;
import org.openforis.collect.metamodel.view.CodeListItemView;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.MediaTypes;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("api")
public class CodeListController {

	private static final String CSV_EXTENSION = ".csv";

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CodeListManager codeListManager;

	@RequestMapping(value = "survey/{surveyId}/codelist/{codeListId}.csv", method = GET)
	public @ResponseBody String exportCodeListWork(HttpServletResponse response,
			@PathVariable("surveyId") Integer surveyId, @PathVariable("codeListId") Integer codeListId)
			throws IOException {
		return exportCodeList(response, surveyId, codeListId);
	}

	@RequestMapping(value = "survey/{surveyId}/codelist/{codeListId}/validitems/count", method = POST)
	public @ResponseBody Integer countAvailableItems(@PathVariable int surveyId, @PathVariable int codeListId,
			@RequestBody SearchParameters params) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CodeList list = survey.getCodeListById(codeListId);
		ModelVersion version = params.versionId == null ? null : survey.getVersionById(params.versionId);
		List<CodeListItem> items = codeListManager.loadValidItems(list, version, params.ancestorCodes);
		return items.size();
	}

	@RequestMapping(value = "survey/{surveyId}/codelist/{codeListId}/validitems", method = POST)
	public @ResponseBody List<CodeListItemView> loadAvailableItems(@PathVariable int surveyId,
			@PathVariable int codeListId, @RequestBody SearchParameters params) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CodeList list = survey.getCodeListById(codeListId);
		ModelVersion version = params.versionId == null ? null : survey.getVersionById(params.versionId);
		List<CodeListItem> items = codeListManager.loadValidItems(list, version, params.ancestorCodes);
		return toViews(items);
	}

	@RequestMapping(value = "survey/{surveyId}/codelist/{codeListId}/finditems", method = POST)
	public @ResponseBody List<CodeListItemView> findAvailableItems(@PathVariable int surveyId,
			@PathVariable int codeListId, @RequestBody SearchParameters params) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CodeList list = survey.getCodeListById(codeListId);
		ModelVersion version = params.versionId == null ? null : survey.getVersionById(params.versionId);
		List<CodeListItem> items = codeListManager.findValidItems(list, version, params.language, params.ancestorCodes,
				params.searchString);
		return toViews(items);
	}

	@RequestMapping(value = "survey/{surveyId}/codelist/{codeListId}/item", method = POST)
	public @ResponseBody CodeListItemView loadItem(@PathVariable int surveyId,
			@PathVariable int codeListId, @RequestBody SearchParameters params) {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveyId);
		CodeList list = survey.getCodeListById(codeListId);
		ModelVersion version = params.versionId == null ? null : survey.getVersionById(params.versionId);
		CodeListItem item = codeListManager.loadItem(list, version, params.ancestorCodes, params.searchString);
		return item == null ? null : toView(item);
	}
	
	protected String exportCodeList(HttpServletResponse response, int surveyId, int codeListId) throws IOException {
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
			views.add(toView(item));
		}
		return views;
	}

	private CodeListItemView toView(CodeListItem item) {
		CodeListItemView view = new CodeListItemView();
		view.setCode(item.getCode());
		view.setLabel(item.getLabel());
		return view;
	}

	public static class SearchParameters {
		private Integer versionId;
		private List<String> ancestorCodes;
		private String searchString;
		private String language;

		public Integer getVersionId() {
			return versionId;
		}

		public void setVersionId(Integer versionId) {
			this.versionId = versionId;
		}

		public List<String> getAncestorCodes() {
			return ancestorCodes;
		}

		public void setAncestorCodes(List<String> ancestorCodes) {
			this.ancestorCodes = ancestorCodes;
		}

		public String getSearchString() {
			return searchString;
		}

		public void setSearchString(String searchString) {
			this.searchString = searchString;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

	}
}
