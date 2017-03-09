package org.openforis.collect.datacleansing.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryExecutorJob;
import org.openforis.collect.datacleansing.DataQueryExecutorJob.DataQueryExecutorJobInput;
import org.openforis.collect.datacleansing.DataQueryResultItem;
import org.openforis.collect.datacleansing.form.DataQueryForm;
import org.openforis.collect.datacleansing.form.DataQueryResultItemForm;
import org.openforis.collect.datacleansing.form.validation.DataQueryValidator;
import org.openforis.collect.datacleansing.json.JSONValueFormatter;
import org.openforis.collect.datacleansing.manager.DataQueryManager;
import org.openforis.collect.io.data.CSVDataExportJob;
import org.openforis.collect.io.data.DescendantNodeFilter;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NodeProcessor;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.openforis.collect.web.controller.CollectJobController.JobView;
import org.openforis.commons.web.Forms;
import org.openforis.commons.web.HttpResponses;
import org.openforis.commons.web.Response;
import org.openforis.concurrency.Job;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/dataqueries/")
public class DataQueryController extends AbstractSurveyObjectEditFormController<DataQuery, DataQueryForm, DataQueryManager> {

	private static final int TEST_MAX_RECORDS = 100;
	@Autowired
	protected SessionManager sessionManager;
	@Autowired
	private CollectJobManager collectJobManager;
	@Autowired
	private DataQueryValidator validator;
//	@Autowired
//	private ApplicationContext appContext;
	
//	private CSVWriterDataQueryResultItemProcessor csvExportItemProcessor;
//	private DataQueryExecutorJob exportJob;
	private DataQueryExecutorJob testJob;
	private CSVDataExportJob exportJob;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}
	
	@Override
	@Autowired
	@Qualifier("dataQueryManager")
	public void setItemManager(DataQueryManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataQuery createItemInstance(CollectSurvey survey) {
		return new DataQuery(survey);
	}

	@Override
	protected DataQueryForm createFormInstance(DataQuery item) {
		return new DataQueryForm(item);
	}
	
	@RequestMapping(value="start-export.json", method = RequestMethod.POST)
	public @ResponseBody
	Response startExport(@Validated DataQueryForm form, @RequestParam Step recordStep) throws Exception {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQuery query = new DataQuery(survey);
		form.copyTo(query);
		
		exportJob = collectJobManager.createJob(CSVDataExportJob.class);
		exportJob.setOutputFile(File.createTempFile("data-query-export", ".csv"));
		RecordFilter recordFilter = new RecordFilter(survey);
		exportJob.setRecordFilter(recordFilter);
		recordFilter.setStepGreaterOrEqual(recordStep);
		exportJob.setEntityId(query.getEntityDefinitionId());
		exportJob.setAlwaysGenerateZipFile(false);
		exportJob.setNodeFilter(new DescendantNodeFilter(query.getAttributeDefinition(), query.getConditions()));
		collectJobManager.start(exportJob);
		
		/*
		csvExportItemProcessor = new CSVWriterDataQueryResultItemProcessor(query);
		csvExportItemProcessor.init();
		exportJob = collectJobManager.createJob(DataQueryExecutorJob.class);
		exportJob.setInput(new DataQueryExecutorJobInput(query, recordStep, csvExportItemProcessor));
		collectJobManager.start(exportJob);
		*/
		Response response = new Response();
		return response;
	}
	
	@RequestMapping(value="start-test.json", method = RequestMethod.POST)
	public @ResponseBody
	Response startTest(@Validated DataQueryForm form, @RequestParam Step recordStep, BindingResult result) {
		List<ObjectError> errors = result.getAllErrors();
		if (errors.isEmpty()) {
			CollectSurvey survey = sessionManager.getActiveSurvey();
			DataQuery query = new DataQuery(survey);
			form.copyTo(query);
			testJob = collectJobManager.createJob(DataQueryExecutorJob.class);
			testJob.setInput(new DataQueryExecutorJobInput(query, recordStep, new MemoryStoreDataQueryResultItemProcessor(query), TEST_MAX_RECORDS));
			collectJobManager.start(testJob);
			return new Response();
		} else {
			return new SimpleFormUpdateResponse(errors);
		}
	}
	
	@RequestMapping(value="result.csv", method = RequestMethod.GET)
	public void downloadResult(HttpServletResponse response) throws FileNotFoundException, IOException {
//		File file = csvExportItemProcessor.getOutputFile();
		File file = exportJob.getOutputFile();
		Controllers.writeFileToResponse(response, file, "collect-query.csv", Controllers.CSV_CONTENT_TYPE);
	}
	
	@RequestMapping(value = "test-result.json", method = RequestMethod.GET)
	public @ResponseBody List<DataQueryResultItemForm> downloadTestResult(HttpServletResponse response)
			throws FileNotFoundException, IOException {
		if (testJob == null) {
			return Collections.emptyList();
		}
		List<DataQueryResultItem> items = ((MemoryStoreDataQueryResultItemProcessor) testJob
				.getInput().getNodeProcessor()).getItems();
		List<DataQueryResultItemForm> result = Forms.toForms(items, DataQueryResultItemForm.class);
		return result;
	}
	
	@RequestMapping(value="export-job.json", method = RequestMethod.GET)
	public @ResponseBody
	JobView getExportJob(HttpServletResponse response) {
		return createJobView(response, exportJob);
	}

	@RequestMapping(value="export-job.json", method = RequestMethod.DELETE)
	public @ResponseBody
	JobView cancelExportJob(HttpServletResponse response) {
		if (exportJob != null) {
			exportJob.abort();
		}
		return createJobView(response, exportJob);
	}
	
	@RequestMapping(value="test-job.json", method = RequestMethod.GET)
	public @ResponseBody
	JobView getTestJob(HttpServletResponse response) {
		return createJobView(response, testJob);
	}

	@RequestMapping(value="test-job.json", method = RequestMethod.DELETE)
	public @ResponseBody
	JobView cancelTestJob(HttpServletResponse response) {
		if (testJob != null) {
			testJob.abort();
		}
		return createJobView(response, testJob);
	}
	
	private JobView createJobView(HttpServletResponse response, Job job) {
		if (job == null) {
			HttpResponses.setNoContentStatus(response);
			return null;
		} else {
			return new JobView(job);
		}
	}
	
	static abstract class AttributeQueryResultItemProcessor implements NodeProcessor {

		protected DataQuery query;
		
		public AttributeQueryResultItemProcessor(DataQuery query) {
			super();
			this.query = query;
		}
		
		@Override
		public void process(Node<?> node) {
			CollectRecord record = (CollectRecord) node.getRecord();
			DataQueryResultItem item = new DataQueryResultItem(query);
			item.setRecord(record);
			item.setRecordId(record.getId());
			item.setNode(node);
			item.setNodeIndex(node.getIndex());
			item.setParentEntityId(node.getParent().getInternalId());
			item.setValue(new JSONValueFormatter().formatValue((Attribute<?, ?>) node));
			process(item);
		}

		public abstract void process(DataQueryResultItem item);
	}
	
	private static class MemoryStoreDataQueryResultItemProcessor extends AttributeQueryResultItemProcessor {
		
		private List<DataQueryResultItem> items;
		
		public MemoryStoreDataQueryResultItemProcessor(DataQuery query) {
			super(query);
			items = new ArrayList<DataQueryResultItem>();
		}
		
		public void process(DataQueryResultItem item) {
			items.add(item);			
		}

		public List<DataQueryResultItem> getItems() {
			return items;
		}
	}
	
}
