package org.openforis.collect.datacleansing.controller;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryExecutor;
import org.openforis.collect.datacleansing.DataQueryResultIterator;
import org.openforis.collect.datacleansing.form.DataUpdateForm;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.web.controller.BasicController;
import org.openforis.commons.web.Response;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.Worker.Status;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
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
	
	private DataUpdateAttributeProcessor itemProcessor;
	private DataUpdateJob upadteJob;
	
	@RequestMapping(value="start.json", method = RequestMethod.POST)
	public @ResponseBody
	Response start(@Validated DataUpdateForm form) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQuery query = new DataQuery(survey);
		form.copyTo(query);
		
		itemProcessor = new DataUpdateAttributeProcessor(query, form.getUpdateExpression());
		upadteJob = collectJobManager.createJob(DataUpdateJob.class);
		upadteJob.setQuery(query);
		upadteJob.setRecordStep(form.getRecordStep());
		upadteJob.setResultItemProcessor(itemProcessor);
		collectJobManager.start(upadteJob);
		Response response = new Response();
		return response;
	}
	
	@RequestMapping(value="job.json", method = RequestMethod.GET)
	public @ResponseBody
	JobView getUpdateJob(HttpServletResponse response) {
		return createJobView(response, upadteJob);
	}

	@RequestMapping(value="job.json", method = RequestMethod.DELETE)
	public @ResponseBody
	JobView cancelUpdateJob(HttpServletResponse response) {
		if (upadteJob != null) {
			upadteJob.abort();
		}
		return createJobView(response, upadteJob);
	}
	
	private JobView createJobView(HttpServletResponse response, DataUpdateJob job) {
		if (job == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
		} else {
			return new JobView(job);
		}
	}
	
	@Component
	@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	private static class DataUpdateJob extends Job {

		@Autowired
		private DataQueryExecutor queryExecutor;
		
		private DataQueryResultItemProcessor resultItemProcessor;
		
		//input
		private DataQuery query;
		private Step recordStep;
		
		//output
		private List<DataUpdateError> errors;

		public DataUpdateJob() {
			this.errors = new ArrayList<DataUpdateError>();
		}
		
		@Override
		protected void buildTasks() throws Throwable {
			addTask(new Task() {
				protected void execute() throws Throwable {
					try {
						resultItemProcessor.init();
						DataQueryResultIterator it = queryExecutor.execute(query, recordStep);
						while(isRunning() && it.hasNext()) {
							Attribute<?, ?> attr = (Attribute<?, ?>) it.next();
							try {
								resultItemProcessor.process(attr);
							} catch(Exception e) {
								errors.add(createError(attr, e));
							}
						}
					} finally {
						resultItemProcessor.close();
					}
				}

				private DataUpdateError createError(Attribute<?, ?> attr,
						Exception e) {
					DataUpdateError error = new DataUpdateError();
					CollectRecord record = (CollectRecord) attr.getRecord();
					error.recordId = record.getId();
					error.recordKeys = record.getRootEntityKeyValues();
					error.attributePath = attr.getPath();
					error.errorMessage = e.getMessage();
					return error;
				}

			});
		}
		
		public DataQueryResultItemProcessor getResultItemProcessor() {
			return resultItemProcessor;
		}
		
		public void setResultItemProcessor(DataQueryResultItemProcessor resultItemProcessor) {
			this.resultItemProcessor = resultItemProcessor;
		}

		public void setQuery(DataQuery query) {
			this.query = query;
		}
		
		public void setRecordStep(Step recordStep) {
			this.recordStep = recordStep;
		}
		
		public List<DataUpdateError> getErrors() {
			return errors;
		}
		
	}
	
	private static interface DataQueryResultItemProcessor extends Closeable {
		void init() throws Exception;
		<V extends Value> void process(Attribute<?, V> attribute) throws Exception;
	}
	
	private static class DataUpdateAttributeProcessor implements DataQueryResultItemProcessor {
		
		private DataQuery query;
		private RecordUpdater recordUpdater;
		private String updateExpression;
		
		public DataUpdateAttributeProcessor(DataQuery query, String updateExpression) {
			this.query = query;
			this.updateExpression = updateExpression;
		}
		
		@Override
		public void init() throws Exception {
		}
		
		@Override
		public <V extends Value> void process(Attribute<?, V> attribute) throws Exception {
			AttributeDefinition attrDefn = (AttributeDefinition) attribute.getDefinition();
			CollectRecord record = (CollectRecord) attribute.getRecord();
			SurveyContext surveyContext = record.getSurveyContext();
			ExpressionEvaluator expressionEvaluator = surveyContext.getExpressionEvaluator();
			Entity parent = attribute.getParent();
			V newValue = expressionEvaluator.evaluateAttributeValue(parent, attribute, attrDefn, updateExpression);
			recordUpdater.updateAttribute(attribute, newValue);
		}
		
		@Override
		public void close() throws IOException {
		}
	}
	
	private static class DataUpdateError {
		private List<String> recordKeys;
		private int recordId;
		private String attributePath;
		private String errorMessage;
		
		public DataUpdateError() {
		}
		
		public int getRecordId() {
			return recordId;
		}
		
		public List<String> getRecordKeys() {
			return recordKeys;
		}
		
		public String getAttributePath() {
			return attributePath;
		}
		
		public String getErrorMessage() {
			return errorMessage;
		}
	}
	
	private static class JobView {
		
		private int progressPercent;
		private Status status;
		private List<DataUpdateError> errors;

		public JobView(DataUpdateJob job) {
			progressPercent = job.getProgressPercent();
			status = job.getStatus();
			errors = job.getErrors();
		}
		
		public int getProgressPercent() {
			return progressPercent;
		}
		
		public Status getStatus() {
			return status;
		}
		
		public List<DataUpdateError> getErrors() {
			return errors;
		}
		
	}
	
}
