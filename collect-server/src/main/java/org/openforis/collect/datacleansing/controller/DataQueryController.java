package org.openforis.collect.datacleansing.controller;

import java.io.BufferedInputStream;
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
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQueryExecutor;
import org.openforis.collect.datacleansing.DataQueryResultItem;
import org.openforis.collect.datacleansing.DataQueryResultIterator;
import org.openforis.collect.datacleansing.form.DataQueryForm;
import org.openforis.collect.datacleansing.json.JSONValueFormatter;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.web.Response;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.Worker.Status;
import org.openforis.concurrency.spring.SpringJobManager;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/datacleansing/dataquery/")
public class DataQueryController {

	@Autowired
	protected SessionManager sessionManager;
	@Autowired
	private SpringJobManager springJobManager;
	
	private QueryExecutorJob executorJob;
	
	@RequestMapping(value="start.json", method = RequestMethod.POST)
	public @ResponseBody
	Response startQuery(@Validated DataQueryForm form, @RequestParam Step recordStep) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQuery query = new DataQuery(survey);
		form.copyTo(query);
		executorJob = springJobManager.createJob(QueryExecutorJob.class);
		executorJob.setQuery(query);
		executorJob.setRecordStep(recordStep);
		springJobManager.start(executorJob);
		Response response = new Response();
		return response;
	}
	
	@RequestMapping(value="result.csv", method = RequestMethod.GET)
	public void downloadResult(HttpServletResponse response) throws FileNotFoundException, IOException {
		File file = executorJob.getOutputFile();
		writeFileToResponse(file, "text/csv", response, "collect-query.csv");
	}
	
	@RequestMapping(value="job.json", method = RequestMethod.GET)
	public @ResponseBody
	JobView getCurrentGenearationJob(HttpServletResponse response) {
		if (executorJob == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
		} else {
			return new JobView(executorJob);
		}
	}
	
	@RequestMapping(value="job.json", method = RequestMethod.DELETE)
	public @ResponseBody
	JobView cancelCurrentGenearationJob(HttpServletResponse response) {
		if (executorJob == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return null;
		} else {
			executorJob.abort();
			return new JobView(executorJob);
		}
	}
	
	@Component
	@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	private static class QueryExecutorJob extends Job {

		@Autowired
		private DataQueryExecutor queryExecutor;
		
		//input
		private DataQuery query;
		private Step recordStep;
		
		//output
		private File tempFile;
		
		@Override
		protected void buildTasks() throws Throwable {
			addTask(new Task() {
				private CsvWriter csvWriter;

				@Override
				protected void initInternal() throws Throwable {
					tempFile = File.createTempFile("collect-data-cleansing-query", ".csv");
					csvWriter = new CsvWriter(new FileOutputStream(tempFile));
					super.initInternal();
				}
				
				protected void execute() throws Throwable {
					try {
						writeCSVHeader();
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
							writeCSVLine(item);
						}
					} finally {
						csvWriter.close();
					}
				}

				private void writeCSVLine(DataQueryResultItem item) {
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
			});
		}
		
		public void setQuery(DataQuery query) {
			this.query = query;
		}
		
		public void setRecordStep(Step recordStep) {
			this.recordStep = recordStep;
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
