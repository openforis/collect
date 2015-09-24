package org.openforis.collect.datacleansing.controller;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.datacleansing.form.DataErrorQueryGroupForm;
import org.openforis.collect.datacleansing.form.validation.DataErrorQueryGroupValidator;
import org.openforis.collect.datacleansing.manager.DataErrorQueryGroupManager;
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
@RequestMapping(value = "/datacleansing/dataerrorquerygroups")
public class DataErrorQueryGroupController extends AbstractSurveyObjectEditFormController<DataErrorQueryGroup, DataErrorQueryGroupForm, DataErrorQueryGroupManager> {
	
	@Autowired
	private DataErrorQueryGroupValidator dataErrorQueryGroupValidator;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(dataErrorQueryGroupValidator);
	}
	
	@Override
	@Autowired
	@Qualifier("dataErrorQueryGroupManager")
	public void setItemManager(DataErrorQueryGroupManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataErrorQueryGroupForm createFormInstance(DataErrorQueryGroup item) {
		return new DataErrorQueryGroupForm(item);
	}
	
	@Override
	protected DataErrorQueryGroup createItemInstance(CollectSurvey survey) {
		return new DataErrorQueryGroup(survey);
	};
	
	@Override
	protected void copyFormIntoItem(DataErrorQueryGroupForm form, DataErrorQueryGroup item) {
		super.copyFormIntoItem(form, item);
		List<Integer> queryIds = new ArrayList<Integer>(form.getQueryIds());
		item.removeAllQueries();
		for (Integer id : queryIds) {
			DataErrorQuery query = new DataErrorQuery((CollectSurvey) item.getSurvey());
			query.setId(id);
			item.addQuery(query);
		}
	}
	
}
