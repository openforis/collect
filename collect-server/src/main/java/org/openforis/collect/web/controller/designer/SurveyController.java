package org.openforis.collect.web.controller.designer;

import java.io.IOException;
import java.io.InputStream;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SurveyController {

	private static final String EDIT_SURVEY_VIEW = "editSurvey";

	@Autowired
	private SurveyDao surveyDao;
	
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(value = "/surveys/edit/temp/{surveyId}", method = RequestMethod.GET)
	public ModelAndView editTemp(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("temp_id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
	@RequestMapping(value = "/surveys/edit/{surveyId}", method = RequestMethod.GET)
	public ModelAndView edit(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
	@RequestMapping(value = "/uploadSurvey.htm", method = RequestMethod.POST)
	public @ResponseBody String uploadSurvey(UploadItem uploadItem, BindingResult result, @RequestParam String name) 
			throws IOException, SurveyImportException, IdmlParseException {
		CommonsMultipartFile fileData = uploadItem.getFileData();
		InputStream is = fileData.getInputStream();
		CollectSurvey newSurvey = surveyManager.unmarshalSurvey(is);
		newSurvey.setName(name);
		CollectSurvey survey = surveyDao.load(name);
		if(survey == null){
			surveyManager.importModel(newSurvey);
			return "ok";
		} else {
			surveyManager.updateModel(newSurvey);
			return "Survey " + name + " has been updated";
		}
	}
	
}
