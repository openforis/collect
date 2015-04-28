package org.openforis.collect.datacleansing.controller;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryExecutor;
import org.openforis.collect.datacleansing.DataQueryResultItem;
import org.openforis.collect.datacleansing.DataQueryResultIterator;
import org.openforis.collect.datacleansing.form.DataQueryForm;
import org.openforis.collect.datacleansing.form.DataQueryResultItemForm;
import org.openforis.collect.datacleansing.json.JSONValueFormatter;
import org.openforis.collect.datacleansing.manager.DataQueryManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.web.Response;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.Worker.Status;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/dataqueries/")
public class DataQueryController extends AbstractSurveyObjectEditFormController<DataQuery, DataQueryForm, DataQueryManager> {

	@Autowired
	protected SessionManager sessionManager;
	@Autowired
	private CollectJobManager collectJobManager;
	
	private CSVWriterDataQueryResultItemProcessor csvExportItemProcessor;
	private QueryExecutorJob exportJob;
	private QueryExecutorJob testJob;
	
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
	Response startExport(@Validated DataQueryForm form, @RequestParam Step recordStep) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQuery query = new DataQuery(survey);
		form.copyTo(query);
		csvExportItemProcessor = new CSVWriterDataQueryResultItemProcessor(query);
		exportJob = collectJobManager.createJob(QueryExecutorJob.class);
		exportJob.setQuery(query);
		exportJob.setRecordStep(recordStep);
		exportJob.setResultItemProcessor(csvExportItemProcessor);
		collectJobManager.start(exportJob);
		Response response = new Response();
		return response;
	}
	
	@RequestMapping(value="start-test.json", method = RequestMethod.POST)
	public @ResponseBody
	Response startTest(@Validated DataQueryForm form, @RequestParam Step recordStep) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQuery query = new DataQuery(survey);
		form.copyTo(query);
		testJob = collectJobManager.createJob(QueryExecutorJob.class);
		testJob.setQuery(query);
		testJob.setRecordStep(recordStep);
		testJob.setResultItemProcessor(new MemoryStoreDataQueryResultItemProcessor());
		collectJobManager.start(testJob);
		Response response = new Response();
		return response;
	}
	
	@RequestMapping(value="result.csv", method = RequestMethod.GET)
	public void downloadResult(HttpServletResponse response) throws FileNotFoundException, IOException {
		File file = csvExportItemProcessor.getOutputFile();
		writeFileToResponse(file, "text/csv", response, "collect-query.csv");
	}
	
	@RequestMapping(value = "test-result.json", method = RequestMethod.GET)
	public @ResponseBody List<DataQueryResultItemForm> downloadTestResult(HttpServletResponse response)
			throws FileNotFoundException, IOException {
		if (testJob == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
		}
		List<DataQueryResultItem> items = ((MemoryStoreDataQueryResultItemProcessor) testJob
				.getResultItemProcessor()).getItems();
		List<DataQueryResultItemForm> result = new ArrayList<DataQueryResultItemForm>(items.size());
		for (DataQueryResultItem item : items) {
			result.add(new DataQueryResultItemForm(item));
		}
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
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
		} else {
			return new JobView(job);
		}
	}
	
	@Component
	@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	private static class QueryExecutorJob extends Job {

		@Autowired
		private DataQueryExecutor queryExecutor;
		
		private DataQueryResultItemProcessor resultItemProcessor;
		
		//input
		private DataQuery query;
		private Step recordStep;
		
		@Override
		protected void buildTasks() throws Throwable {
			addTask(new Task() {
				protected void execute() throws Throwable {
					try {
						resultItemProcessor.init();
						DataQueryResultIterator it = queryExecutor.execute(query, recordStep);
						while(isRunning() && it.hasNext()) {
							Node<?> node = it.next();
							CollectRecord record = (CollectRecord) node.getRecord();
							DataQueryResultItem item = new DataQueryResultItem(query);
							item.setRecord(record);
							item.setRecordId(record.getId());
							item.setNodeIndex(node.getIndex());
							item.setParentEntityId(node.getParent().getInternalId());
							item.setValue(new JSONValueFormatter().formatValue((Attribute<?, ?>) node));
							resultItemProcessor.process(item);
						}
					} finally {
						resultItemProcessor.close();
					}
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
		
	}
	
	private static interface DataQueryResultItemProcessor extends Closeable {
		void init() throws Exception;
		void process(DataQueryResultItem item) throws Exception;
	}
	
	private static class MemoryStoreDataQueryResultItemProcessor implements DataQueryResultItemProcessor {
		private List<DataQueryResultItem> items;
		
		@Override
		public void close() throws IOException {
		}
		
		@Override
		public void init() throws Exception {
			items = new ArrayList<DataQueryResultItem>();
		}
		
		@Override
		public void process(DataQueryResultItem item) throws Exception {
			items.add(item);
		}
		
		public List<DataQueryResultItem> getItems() {
			return items;
		}
	}
	
	private static class CSVWriterDataQueryResultItemProcessor implements DataQueryResultItemProcessor {
		
		private CsvWriter csvWriter;
		
		private DataQuery query;
		
		//output
		private File tempFile;
		
		public CSVWriterDataQueryResultItemProcessor(DataQuery query) {
			this.query = query;
		}
		
		@Override
		public void init() throws Exception {
			tempFile = File.createTempFile("collect-data-cleansing-query", ".csv");
			csvWriter = new CsvWriter(new FileOutputStream(tempFile));
			writeCSVHeader();
		}
		
		private void writeCSVHeader() {
			List<String> headers = new ArrayList<String>();
			EntityDefinition rootEntity = query.getEntityDefinition().getRootEntity();
			List<AttributeDefinition> keyAttributeDefinitions = rootEntity.getKeyAttributeDefinitions();
			for (AttributeDefinition def : keyAttributeDefinitions) {
				String keyLabel = def.getLabel(Type.INSTANCE);
				if (StringUtils.isBlank(keyLabel)) {
					keyLabel = def.getName();
				}
				headers.add(keyLabel);
			}
			headers.add("Path");
			AttributeDefinition attrDef = (AttributeDefinition) query.getSchema().getDefinitionById(query.getAttributeDefinitionId());
			String attrName = attrDef.getName();
			List<String> fieldNames = attrDef.getFieldNames();
			if (fieldNames.size() > 1) {
				for (String fieldName : fieldNames) {
					headers.add(attrName + "_" + fieldName);
				}
			} else {
				headers.add(attrName);
			}
			csvWriter.writeHeaders(headers.toArray(new String[headers.size()]));
		}

		@Override
		public void process(DataQueryResultItem item) {
			List<String> lineValues = new ArrayList<String>();
			lineValues.addAll(item.getRecordKeyValues());
			lineValues.add(item.extractNodePath());
			Value value = item.extractAttributeValue();
			AttributeDefinition attrDef = item.getAttributeDefinition();
			Map<String, Object> valueMap = value.toMap();
			List<String> fieldNames = attrDef.getFieldNames();
			for (String fieldName : fieldNames) {
				Object fieldValue = valueMap.get(fieldName);
				lineValues.add(fieldValue == null ? "": fieldValue.toString());
			}
			csvWriter.writeNext(lineValues.toArray(new String[lineValues.size()]));
		}
		
		@Override
		public void close() throws IOException {
			csvWriter.close();
		}

		public File getOutputFile() {
			return tempFile;
		}
	}
	
	private static class JobView {
		
		private int progressPercent;
		private Status status;

		public JobView(Job job) {
			progressPercent = job.getProgressPercent();
			status = job.getStatus();
		}
		
		public int getProgressPercent() {
			return progressPercent;
		}
		
		public Status getStatus() {
			return status;
		}
		
	}
	
	private void writeFileToResponse(File file, String contentType, HttpServletResponse response,
			String outputFileName) throws FileNotFoundException, IOException {
		writeFileToResponse(new FileInputStream(file), contentType, new Long(file.length()).intValue(), response, outputFileName);
	}
	
	private void writeFileToResponse(InputStream is,
			String contentType, int fileSize, HttpServletResponse response,
			String outputFileName) throws IOException {
		ServletOutputStream outputStream = response.getOutputStream();
		BufferedInputStream buf = null;
		try {
			response.setContentType(contentType); 
			response.setContentLength(fileSize);
			response.setHeader("Content-Disposition", "attachment; filename=" + outputFileName);
			buf = new BufferedInputStream(is);
			int readBytes = 0;
			//read from the file; write to the ServletOutputStream
			while ((readBytes = buf.read()) != -1) {
				outputStream.write(readBytes);
			}
		} finally {
			IOUtils.closeQuietly(buf);
			IOUtils.closeQuietly(is);
		}
	}

}
