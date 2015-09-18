package org.openforis.collect.datacleansing.controller;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.IOUtils;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataErrorQuery.Severity;
import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.DataErrorReportGeneratorJob;
import org.openforis.collect.datacleansing.DataErrorReportItem;
import org.openforis.collect.datacleansing.form.DataErrorReportForm;
import org.openforis.collect.datacleansing.form.DataErrorReportItemForm;
import org.openforis.collect.datacleansing.manager.DataErrorQueryGroupManager;
import org.openforis.collect.datacleansing.manager.DataErrorReportManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.openforis.collect.web.controller.CollectJobController.JobView;
import org.openforis.collect.web.controller.PaginatedResponse;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.web.Response;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/dataerrorreports")
public class DataErrorReportController extends AbstractSurveyObjectEditFormController<DataErrorReport, DataErrorReportForm, DataErrorReportManager> {
	
	@Autowired
	private DataErrorQueryGroupManager dataErrorQueryGroupManager;
	@Autowired
	private CollectJobManager collectJobManager;
	
	private DataErrorReportGeneratorJob generationJob;
	
	@Override
	@Autowired
	@Qualifier("dataErrorReportManager")
	public void setItemManager(DataErrorReportManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataErrorReportForm createFormInstance(DataErrorReport item) {
		DataErrorReportForm form = new DataErrorReportForm(item);
		int itemCount = itemManager.countItems(item);
		form.setItemCount(itemCount);
		return form;
	}
	
	@Override
	protected DataErrorReport createItemInstance(CollectSurvey survey) {
		return new DataErrorReport(survey);
	}
	
	@RequestMapping(value="generate.json", method = RequestMethod.POST)
	public @ResponseBody
	Response generate(@RequestParam int queryGroupId, @RequestParam Step recordStep) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataErrorQueryGroup queryGroup = dataErrorQueryGroupManager.loadById(survey, queryGroupId);
		generationJob = collectJobManager.createJob(DataErrorReportGeneratorJob.class);
		generationJob.setErrorQueryGroup(queryGroup);
		generationJob.setRecordStep(recordStep);
		collectJobManager.start(generationJob);
		Response response = new Response();
		return response;
	}
	
	@RequestMapping(value="{reportId}/export.csv", method = RequestMethod.GET)
	public void export(HttpServletResponse response, @PathVariable int reportId) throws Exception {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataErrorReport report = itemManager.loadById(survey, reportId);
		
		EntityDefinition rootEntityDefinition = survey.getSchema().getRootEntityDefinitions().get(0);
		CSVWriterDataErrorItemProcessor itemProcessor = new CSVWriterDataErrorItemProcessor(rootEntityDefinition);
		itemProcessor.init();
		int count = itemManager.countItems(report);
		int itemsPerPage = 100;
		int pages = Double.valueOf(Math.ceil((double) count / itemsPerPage)).intValue();
		for (int page = 1; page <= pages ; page++) {
			List<DataErrorReportItem> items = itemManager.loadItems(report, (page - 1) * itemsPerPage, itemsPerPage);
			for (DataErrorReportItem item : items) {
				itemProcessor.process(item);
			}
		}
		itemProcessor.close();
		File file = itemProcessor.getOutputFile();
		Controllers.writeFileToResponse(file, "text/csv", response, "data-error-report.csv");
	}
	
	@RequestMapping(value="{reportId}/items.json", method = RequestMethod.GET)
	public @ResponseBody
	PaginatedResponse loadItems(@PathVariable int reportId, 
			@RequestParam int offset, @RequestParam int limit) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataErrorReport report = itemManager.loadById(survey, reportId);
		int total = itemManager.countItems(report);
		List<DataErrorReportItem> items = itemManager.loadItems(report, offset, limit);
		List<DataErrorReportItemForm> rows = new ArrayList<DataErrorReportItemForm>(items.size());
		for (DataErrorReportItem item : items) {
			rows.add(new DataErrorReportItemForm(item));
		}
		return new PaginatedResponse(total, rows);
	}
	
	@RequestMapping(value="generate/job.json", method = RequestMethod.GET)
	public @ResponseBody
	JobView getCurrentGenearationJob(HttpServletResponse response) {
		if (generationJob == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
		} else {
			return new JobView(generationJob);
		}
	}
	
	@RequestMapping(value="generate/job.json", method = RequestMethod.DELETE)
	public @ResponseBody Response cancelGenerationJob() {
		if (generationJob != null) {
			generationJob.abort();
		}
		return new Response();
	}
	
	private static class CSVWriterDataErrorItemProcessor implements Closeable {
		
		private CsvWriter csvWriter;
		
		//output
		private File tempFile;

		private EntityDefinition rootEntityDefinition;

		private RecordReportInfo lastRecordReportInfo;
		
		public CSVWriterDataErrorItemProcessor(EntityDefinition rootEntityDefinition) {
			this.rootEntityDefinition = rootEntityDefinition;
		}
		
		public void init() throws Exception {
			tempFile = File.createTempFile("collect-data-error-report", ".csv");
			csvWriter = new CsvWriter(new FileOutputStream(tempFile), IOUtils.UTF_8, ',', '"');
			writeCSVHeader();
		}
		
		private void writeCSVHeader() {
			List<String> headers = new ArrayList<String>();
			List<AttributeDefinition> keyAttributeDefinitions = rootEntityDefinition.getKeyAttributeDefinitions();
			for (AttributeDefinition def : keyAttributeDefinitions) {
				String keyLabel = def.getLabel(Type.INSTANCE);
				if (StringUtils.isBlank(keyLabel)) {
					keyLabel = def.getName();
				}
				headers.add(keyLabel);
			}
			headers.add("Errors");
			headers.add("Warnings");
			csvWriter.writeHeaders(headers.toArray(new String[headers.size()]));
		}

		public void process(DataErrorReportItem item) {
			if (lastRecordReportInfo != null && lastRecordReportInfo.getRecordId() != item.getRecordId()) {
				writeLastRecordInfo();
			}
			if (lastRecordReportInfo == null) {
				lastRecordReportInfo = new RecordReportInfo(item.getRecordId(), item.getRecordKeyValues());
			} else {
				String queryTitle = item.getQuery().getTitle();
				if (item.getErrorQuery().getSeverity() == Severity.ERROR) {
					lastRecordReportInfo.addError(queryTitle);
				} else {
					lastRecordReportInfo.addWarning(queryTitle);
				}
			}
		}

		private void writeLastRecordInfo() {
			List<String> lineValues = new ArrayList<String>();
			lineValues.addAll(lastRecordReportInfo.getKeyValues());
			lineValues.add(StringUtils.join(lastRecordReportInfo.getErrors(), "\r\n;"));
			lineValues.add(StringUtils.join(lastRecordReportInfo.getWarnings(), "\r\n;"));
			csvWriter.writeNext(lineValues.toArray(new String[lineValues.size()]));
		}
		
		@Override
		public void close() throws IOException {
			if (lastRecordReportInfo != null) {
				writeLastRecordInfo();
			}
			csvWriter.close();
		}

		public File getOutputFile() {
			return tempFile;
		}
		
		private static class RecordReportInfo {
			
			private int recordId;
			private List<String> keyValues;
			private List<String> errors = new ArrayList<String>();
			private List<String> warnings = new ArrayList<String>();
			
			public RecordReportInfo(int recordId, List<String> keyValues) {
				super();
				this.recordId = recordId;
				this.keyValues = keyValues;
			}
			
			public List<String> getKeyValues() {
				return keyValues;
			}
			
			public void addError(String error) {
				this.errors.add(error);
			}
			
			public void addWarning(String warning) {
				this.warnings.add(warning);
			}

			public int getRecordId() {
				return recordId;
			}
			
			public List<String> getErrors() {
				return errors;
			}
			
			public List<String> getWarnings() {
				return warnings;
			}
			
		}
	}
	
}
