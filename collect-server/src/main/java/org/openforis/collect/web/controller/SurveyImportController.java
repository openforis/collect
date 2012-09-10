package org.openforis.collect.web.controller;

import java.io.IOException;
import java.io.InputStream;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * @author S. Ricci
 * 
 */
@Controller
public class SurveyImportController {

	//private static Log LOG = LogFactory.getLog(SurveyImportController.class);

	@Autowired
	private SurveyDao surveyDao;
	
	@Autowired
	private SurveyManager surveyManager;
	
	@RequestMapping(value = "/uploadSurvey.htm", method = RequestMethod.POST)
	public @ResponseBody String uploadSurvey(UploadItem uploadItem, BindingResult result, @RequestParam String name) 
			throws IOException, InvalidIdmlException, SurveyImportException {
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


