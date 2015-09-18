package org.openforis.collect.datacleansing.controller;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataCleansingChain;
import org.openforis.collect.datacleansing.DataCleansingChainExecutorJob;
import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.form.DataCleansingChainForm;
import org.openforis.collect.datacleansing.form.validation.DataCleansingChainValidator;
import org.openforis.collect.datacleansing.manager.DataCleansingChainManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.openforis.commons.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/datacleansingchains")
public class DataCleansingChainController extends AbstractSurveyObjectEditFormController<DataCleansingChain, DataCleansingChainForm, DataCleansingChainManager> {
	
	@Autowired
	private CollectJobManager collectJobManager;
	@Autowired
	private DataCleansingChainValidator dataCleansingChainValidator;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(dataCleansingChainValidator);
	}
	
	@Override
	@Autowired
	@Qualifier("dataCleansingChainManager")
	public void setItemManager(DataCleansingChainManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataCleansingChainForm createFormInstance(DataCleansingChain item) {
		return new DataCleansingChainForm(item);
	}
	
	@Override
	protected DataCleansingChain createItemInstance(CollectSurvey survey) {
		return new DataCleansingChain(survey);
	};
	
	@RequestMapping(value="run.json", method = RequestMethod.POST)
	public @ResponseBody
	Response run(@RequestParam int chainId, @RequestParam Step recordStep) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataCleansingChain chain = itemManager.loadById(survey, chainId);
		DataCleansingChainExecutorJob job = collectJobManager.createJob(DataCleansingChainExecutorJob.class);
		job.setChain(chain);
		job.setRecordStep(recordStep);
		collectJobManager.startSurveyJob(job);
		Response response = new Response();
		return response;
	}
	
	@Override
	protected void copyFormIntoItem(DataCleansingChainForm form, DataCleansingChain item) {
		super.copyFormIntoItem(form, item);
		item.removeAllSteps();
		for (Integer stepId : form.getStepIds()) {
			DataCleansingStep step = new DataCleansingStep((CollectSurvey) item.getSurvey());
			step.setId(stepId);
			item.addStep(step);
		}
	}
	
}
