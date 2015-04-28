package org.openforis.collect.datacleansing.controller;

import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.form.DataCleansingStepForm;
import org.openforis.collect.datacleansing.manager.DataCleansingStepManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/datacleansingsteps")
public class DataCleansingStepController extends AbstractSurveyObjectEditFormController<DataCleansingStep, DataCleansingStepForm, DataCleansingStepManager> {
	
	@Override
	@Autowired
	@Qualifier("dataCleansingStepManager")
	public void setItemManager(DataCleansingStepManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataCleansingStepForm createFormInstance(DataCleansingStep item) {
		return new DataCleansingStepForm(item);
	}
	
	@Override
	protected DataCleansingStep createItemInstance(CollectSurvey survey) {
		return new DataCleansingStep(survey);
	};
}
