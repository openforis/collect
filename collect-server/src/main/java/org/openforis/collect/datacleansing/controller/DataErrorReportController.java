package org.openforis.collect.datacleansing.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.DataErrorReportGeneratorJob;
import org.openforis.collect.datacleansing.DataErrorReportItem;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.form.DataErrorReportForm;
import org.openforis.collect.datacleansing.form.DataErrorReportItemForm;
import org.openforis.collect.datacleansing.manager.DataErrorQueryManager;
import org.openforis.collect.datacleansing.manager.DataErrorReportManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.openforis.collect.web.controller.CollectJobController.JobView;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.web.Response;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.model.Value;
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
	@Qualifier("dataErrorQueryManager")
	private DataErrorQueryManager dataErrorQueryManager;
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
		return new DataErrorReportForm(item);
	}
	
	@Override
	protected DataErrorReport createItemInstance(CollectSurvey survey) {
		return new DataErrorReport(survey);
	}
	
	@RequestMapping(value="generate.json", method = RequestMethod.POST)
	public @ResponseBody
	Response generate(@RequestParam int queryId, @RequestParam Step recordStep) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataErrorQuery query = dataErrorQueryManager.loadById(survey, queryId);
		generationJob = collectJobManager.createJob(DataErrorReportGeneratorJob.class);
		generationJob.setErrorQuery(query);
		generationJob.setRecordStep(recordStep);
		collectJobManager.start(generationJob);
		Response response = new Response();
		return response;
	}
	
	@RequestMapping(value="start-export.json", method = RequestMethod.POST)
	public @ResponseBody
	Response startExport(@PathVariable int reportId) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataErrorReport report = itemManager.loadById(survey, reportId);
		List<DataErrorReportItem> items = itemManager.loadItems(report, 0, Integer.MAX_VALUE);
		
//		csvExportItemProcessor = new CSVWriterDataQueryResultItemProcessor(report);
//		exportJob = collectJobManager.createJob(DataQueryExecutorJob.class);
//		exportJob.setInput(new DataQueryExecutorJobInput(query, recordStep, csvExportItemProcessor));
//		collectJobManager.start(exportJob);
		Response response = new Response();
		return response;
	}
	
	@RequestMapping(value="{reportId}/items.json", method = RequestMethod.GET)
	public @ResponseBody
	List<DataErrorReportItemForm> loadItems(@PathVariable int reportId, 
			@RequestParam int offset, @RequestParam int limit) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataErrorReport report = itemManager.loadById(survey, reportId);
		List<DataErrorReportItem> items = itemManager.loadItems(report, offset, limit);
		List<DataErrorReportItemForm> result = new ArrayList<DataErrorReportItemForm>(items.size());
		for (DataErrorReportItem item : items) {
			result.add(new DataErrorReportItemForm(item));
		}
		return result;
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
	
	private static class CSVWriterDataQueryResultItemProcessor {
		
		//input
		private DataErrorReport report;
		
		//output
		private File tempFile;
		
		//temporary
		private CsvWriter csvWriter;
		
		public CSVWriterDataQueryResultItemProcessor(DataErrorReport report) {
			this.report = report;
		}
		
		public void init() throws Exception {
			tempFile = File.createTempFile("collect-data-cleansing-query", ".csv");
			csvWriter = new CsvWriter(new FileOutputStream(tempFile));
			writeCSVHeader();
		}
		
		private void writeCSVHeader() {
			List<String> headers = new ArrayList<String>();
			
			DataQuery dataQuery = report.getQuery().getQuery();
			EntityDefinition rootEntity = dataQuery.getEntityDefinition().getRootEntity();
			List<AttributeDefinition> keyAttributeDefinitions = rootEntity.getKeyAttributeDefinitions();
			for (AttributeDefinition def : keyAttributeDefinitions) {
				String keyLabel = def.getLabel(Type.INSTANCE);
				if (StringUtils.isBlank(keyLabel)) {
					keyLabel = def.getName();
				}
				headers.add(keyLabel);
			}
			headers.add("Path");
			AttributeDefinition attrDef = dataQuery.getAttributeDefinition();
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
		
		public void process(DataErrorReportItem item) {
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
		
		public void close() {
			IOUtils.closeQuietly(csvWriter);
		}
	}
	
}
