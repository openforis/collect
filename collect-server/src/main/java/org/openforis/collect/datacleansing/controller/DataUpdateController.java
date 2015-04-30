package org.openforis.collect.datacleansing.controller;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryExecutorJob;
import org.openforis.collect.datacleansing.DataQueryExecutorJob.DataQueryExecutorJobInput;
import org.openforis.collect.datacleansing.NodeProcessor;
import org.openforis.collect.datacleansing.form.DataUpdateForm;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.web.controller.BasicController;
import org.openforis.commons.web.Response;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/dataupdate/")
public class DataUpdateController extends BasicController {

	@Autowired
	protected SessionManager sessionManager;
	@Autowired
	private CollectJobManager collectJobManager;
	
	private DataUpdateNodeProcessor itemProcessor;
	
	@RequestMapping(value="start.json", method = RequestMethod.POST)
	public @ResponseBody
	Response start(@Validated DataUpdateForm form) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQuery query = new DataQuery(survey);
		form.copyTo(query);
		
		itemProcessor = new DataUpdateNodeProcessor(form.getUpdateExpression());
		DataQueryExecutorJob job = collectJobManager.createJob(DataQueryExecutorJob.class);
		DataQueryExecutorJobInput input = new DataQueryExecutorJobInput(query, form.getRecordStep(), itemProcessor);
		job.setInput(input);
		collectJobManager.startSurveyJob(job);
		Response response = new Response();
		return response;
	}
	
	private static class DataUpdateNodeProcessor implements NodeProcessor {
		
		private RecordUpdater recordUpdater;
		private String updateExpression;
		
		public DataUpdateNodeProcessor(String updateExpression) {
			this.updateExpression = updateExpression;
		}
		
		@Override
		public void process(Node<?> node) throws Exception {
			AttributeDefinition attrDefn = (AttributeDefinition) node.getDefinition();
			@SuppressWarnings("unchecked")
			Attribute<?, Value> attribute = (Attribute<?, Value>) node;
			CollectRecord record = (CollectRecord) attribute.getRecord();
			SurveyContext surveyContext = record.getSurveyContext();
			ExpressionEvaluator expressionEvaluator = surveyContext.getExpressionEvaluator();
			Entity parent = attribute.getParent();
			Value newValue = expressionEvaluator.evaluateAttributeValue(parent, attribute, attrDefn, updateExpression);
			recordUpdater.updateAttribute(attribute, newValue);
		}
		
	}
	
}
