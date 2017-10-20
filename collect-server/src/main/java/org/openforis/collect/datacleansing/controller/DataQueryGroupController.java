package org.openforis.collect.datacleansing.controller;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.form.DataQueryGroupForm;
import org.openforis.collect.datacleansing.form.validation.DataQueryGroupValidator;
import org.openforis.collect.datacleansing.manager.DataQueryGroupManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "api/datacleansing/dataquerygroups")
public class DataQueryGroupController extends AbstractSurveyObjectEditFormController<DataQueryGroup, DataQueryGroupForm, DataQueryGroupManager> {
	
	@Autowired
	private DataQueryGroupValidator dataQueryGroupValidator;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(dataQueryGroupValidator);
	}
	
	@Override
	protected DataQueryGroupForm createFormInstance(DataQueryGroup item) {
		return new DataQueryGroupForm(item);
	}
	
	@Override
	protected DataQueryGroup createItemInstance(CollectSurvey survey) {
		return new DataQueryGroup(survey);
	};
	
	@Override
	protected void copyFormIntoItem(DataQueryGroupForm form, DataQueryGroup item) {
		super.copyFormIntoItem(form, item);
		List<Integer> queryIds = new ArrayList<Integer>(form.getQueryIds());
		item.removeAllQueries();
		for (Integer id : queryIds) {
			DataQuery query = new DataQuery((CollectSurvey) item.getSurvey());
			query.setId(id);
			item.addQuery(query);
		}
	}
	
}
