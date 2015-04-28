package org.openforis.collect.datacleansing.controller;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.form.DataErrorQueryForm;
import org.openforis.collect.datacleansing.manager.DataErrorQueryManager;
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
@RequestMapping(value = "/datacleansing/dataerrorqueries")
public class DataErrorQueryController extends AbstractSurveyObjectEditFormController<DataErrorQuery, DataErrorQueryForm, DataErrorQueryManager> {
	
	@Override
	@Autowired
	@Qualifier("dataErrorQueryManager")
	public void setItemManager(DataErrorQueryManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataErrorQueryForm createFormInstance(DataErrorQuery item) {
		return new DataErrorQueryForm(item);
	}
	
	@Override
	protected DataErrorQuery createItemInstance(CollectSurvey survey) {
		return new DataErrorQuery(survey);
	};
}
