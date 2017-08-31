package org.openforis.collect.datacleansing.controller;

import org.openforis.collect.datacleansing.DataQueryType;
import org.openforis.collect.datacleansing.form.DataQueryTypeForm;
import org.openforis.collect.datacleansing.form.validation.DataQueryTypeValidator;
import org.openforis.collect.datacleansing.manager.DataQueryTypeManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "api/datacleansing/dataquerytypes")
public class DataQueryTypeController extends AbstractSurveyObjectEditFormController<DataQueryType, DataQueryTypeForm, DataQueryTypeManager> {
	
	@Autowired
	private DataQueryTypeValidator validator;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}
	
	@Override
	@Autowired
	@Qualifier("dataQueryTypeManager")
	public void setItemManager(DataQueryTypeManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataQueryTypeForm createFormInstance(DataQueryType item) {
		return new DataQueryTypeForm(item);
	}
	
	protected DataQueryType createItemInstance(CollectSurvey survey) {
		return new DataQueryType(survey);
	};
}
