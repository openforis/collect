package org.openforis.collect.web.controller;

import java.io.IOException;
import java.io.InputStream;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.collect.web.controller.upload.UploadItem;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.expression.ExpressionFactory;
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

	@RequestMapping(value = "/uploadSurvey.htm", method = RequestMethod.POST)
	public @ResponseBody String uploadSurvey(UploadItem uploadItem, BindingResult result, @RequestParam String name) 
			throws IOException, InvalidIdmlException, SurveyImportException {
		CollectSurvey survey = surveyDao.load(name);
		if(survey == null){
			CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext(new CollectSurveyContext(new ExpressionFactory(), null, null));
			SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
			CommonsMultipartFile fileData = uploadItem.getFileData();
			InputStream is = fileData.getInputStream();
			survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
			survey.setName(name);
			surveyDao.importModel(survey);
			return "ok";
		} else {
			return "Survey " + name + " already inserted into the database";
		}
	}
}


