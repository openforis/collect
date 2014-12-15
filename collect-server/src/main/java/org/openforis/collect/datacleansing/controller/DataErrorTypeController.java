package org.openforis.collect.datacleansing.controller;

import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.form.DataErrorTypeForm;
import org.openforis.collect.datacleansing.manager.DataErrorTypeManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.controller.AbstractFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/dataerrortype")
public class DataErrorTypeController extends AbstractFormController<DataErrorType, DataErrorTypeForm, DataErrorTypeManager> {
	
	@Autowired
	@Override
	public void setItemManager(DataErrorTypeManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataErrorTypeForm createFormInstance(DataErrorType item) {
		return new DataErrorTypeForm(item);
	}
	
	protected DataErrorType createItemInstance(CollectSurvey survey) {
		return new DataErrorType(survey);
	};
}
