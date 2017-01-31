package org.openforis.collect.datacleansing.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.IOUtils;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataQuery.ErrorSeverity;
import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.DataReport;
import org.openforis.collect.datacleansing.DataReportGeneratorJob;
import org.openforis.collect.datacleansing.DataReportItem;
import org.openforis.collect.datacleansing.form.DataReportForm;
import org.openforis.collect.datacleansing.form.DataReportItemForm;
import org.openforis.collect.datacleansing.manager.DataQueryGroupManager;
import org.openforis.collect.datacleansing.manager.DataReportManager;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Controllers;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.web.controller.AbstractSurveyObjectEditFormController;
import org.openforis.collect.web.controller.CollectJobController.JobView;
import org.openforis.collect.web.controller.PaginatedResponse;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.lang.Objects;
import org.openforis.commons.web.HttpResponses;
import org.openforis.commons.web.Response;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.model.AbstractValue;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Value;
import org.openforis.idm.path.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(value=WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/datacleansing/datareports")
public class DataReportController extends AbstractSurveyObjectEditFormController<DataReport, DataReportForm, DataReportManager> {
	
	private static final String DATA_REPORT_CSV_FILE_NAME_FORMAT = "%s (data report - %s).csv";
	@Autowired
	private DataQueryGroupManager dataQueryGroupManager;
	@Autowired
	private CollectJobManager collectJobManager;
	
	private DataReportGeneratorJob generationJob;
	private ReportExportJob exportJob;
	
	@Override
	@Autowired
	@Qualifier("dataReportManager")
	public void setItemManager(DataReportManager itemManager) {
		super.setItemManager(itemManager);
	}
	
	@Override
	protected DataReportForm createFormInstance(DataReport item) {
		DataReportForm form = new DataReportForm(item);
		form.setItemCount(itemManager.countItems(item));
		form.setAffectedRecordsCount(itemManager.countAffectedRecords(item));
		return form;
	}
	
	@Override
	protected DataReport createItemInstance(CollectSurvey survey) {
		return new DataReport(survey);
	}
	
	@RequestMapping(value="generate.json", method=POST)
	public @ResponseBody
	Response generate(@RequestParam int queryGroupId, @RequestParam Step recordStep) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataQueryGroup queryGroup = dataQueryGroupManager.loadById(survey, queryGroupId);
		generationJob = collectJobManager.createJob(DataReportGeneratorJob.class);
		generationJob.setQueryGroup(queryGroup);
		generationJob.setRecordStep(recordStep);
		collectJobManager.start(generationJob);
		return new Response();
	}
	
	@RequestMapping(value="{reportId}/start-export.json", method=POST)
	public @ResponseBody JobView startExport(@PathVariable int reportId) throws Exception {
		return startExport(reportId, GroupedByRecordCSVWriterDataReportItemProcessor.class);
	}

	@RequestMapping(value="{reportId}/start-export-for-collect-earth.json", method=POST)
	public @ResponseBody JobView startExportForCollectEarth(@PathVariable int reportId) throws Exception {
		return startExport(reportId, CollectEarthCSVWriterDataReportItemProcessor.class);
	}
	
	private JobView startExport(int reportId,
			Class<? extends CSVWriterDataReportItemProcessor> itemProcessorType)
			throws Exception {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataReport report = itemManager.loadById(survey, reportId);
		
		exportJob = new ReportExportJob();
		exportJob.setSurvey(survey);
		exportJob.setReport(report);
		exportJob.setItemProcessorType(itemProcessorType);
		exportJob.setReportManager(itemManager);
		collectJobManager.start(exportJob);
		return new JobView(exportJob);
	}
	
	@RequestMapping(value="{reportId}/report.csv", method=GET)
	private void downloadExportedFile(HttpServletResponse response, @PathVariable int reportId) throws Exception {
		File file = exportJob.getOutputFile();
		DataReport report = exportJob.report;
		String outputFileName = String.format(DATA_REPORT_CSV_FILE_NAME_FORMAT, 
				report.getQueryGroup().getTitle(), 
				Dates.formatDate(report.getCreationDate()));
		Controllers.writeFileToResponse(response, file, outputFileName, Controllers.CSV_CONTENT_TYPE);
	}
	
	@RequestMapping(value="{reportId}/items.json", method=GET)
	public @ResponseBody
	PaginatedResponse loadItems(@PathVariable int reportId, 
			@RequestParam int offset, @RequestParam int limit) {
		CollectSurvey survey = sessionManager.getActiveSurvey();
		DataReport report = itemManager.loadById(survey, reportId);
		int total = itemManager.countItems(report);
		List<DataReportItem> items = itemManager.loadItems(report, offset, limit);
		List<DataReportItemForm> rows = new ArrayList<DataReportItemForm>(items.size());
		for (DataReportItem item : items) {
			rows.add(new DataReportItemForm(item));
		}
		return new PaginatedResponse(total, rows);
	}
	
	@RequestMapping(value="generate/job.json", method=GET)
	public @ResponseBody
	JobView getCurrentGenearationJob(HttpServletResponse response) {
		return getJobView(response, generationJob);
	}

	@RequestMapping(value="export/job.json", method=GET)
	public @ResponseBody
	JobView getCurrentExportJob(HttpServletResponse response) {
		return getJobView(response, exportJob);
	}
	
	private JobView getJobView(HttpServletResponse response, Job job) {
		if (job == null) {
			HttpResponses.setNoContentStatus(response);
			return null;
		} else {
			return new JobView(job);
		}
	}
	
	@RequestMapping(value="generate/job.json", method=DELETE)
	public @ResponseBody Response cancelGenerationJob() {
		if (generationJob != null) {
			generationJob.abort();
		}
		return new Response();
	}
	
	private static abstract class CSVWriterDataReportItemProcessor implements Closeable {
		
		protected CsvWriter csvWriter;
		
		//output
		private File tempFile;

		protected EntityDefinition rootEntityDefinition;


		public CSVWriterDataReportItemProcessor(EntityDefinition rootEntityDefinition) {
			this.rootEntityDefinition = rootEntityDefinition;
		}
		
		public void init() throws Exception {
			tempFile = File.createTempFile("collect-data-report", ".csv");
			csvWriter = new CsvWriter(new FileOutputStream(tempFile), IOUtils.UTF_8, ',', '"');
			writeCSVHeader();
		}
		
		private void writeCSVHeader() {
			List<String> headers = determineHeaders();
			csvWriter.writeHeaders(headers);
		}

		protected List<String> determineHeaders() {
			List<String> headers = new ArrayList<String>();
			List<AttributeDefinition> keyAttributeDefinitions = rootEntityDefinition.getKeyAttributeDefinitions();
			for (AttributeDefinition def : keyAttributeDefinitions) {
				String keyLabel = def.getLabel(Type.INSTANCE);
				if (StringUtils.isBlank(keyLabel)) {
					keyLabel = def.getName();
				}
				headers.add(keyLabel);
			}
			headers.addAll(determineExtraHeaders());
			return headers;
		}

		protected List<String> determineExtraHeaders() {
			return Collections.emptyList();
		}

		public abstract void process(DataReportItem item);

		@Override
		public void close() throws IOException {
			csvWriter.close();
		}

		public File getOutputFile() {
			return tempFile;
		}
		
	}
	
	public static class GroupedByRecordCSVWriterDataReportItemProcessor extends CSVWriterDataReportItemProcessor {

		private static final String WARNINGS_HEADER = "warnings";
		private static final String ERRORS_HEADER = "errors";
		private static final String ERRORS_SEPARATOR = "; \r\n";

		private RecordReportInfo lastRecordReportInfo;
		
		public GroupedByRecordCSVWriterDataReportItemProcessor(
				EntityDefinition rootEntityDefinition) {
			super(rootEntityDefinition);
		}
		
		@Override
		public void process(DataReportItem item) {
			if (lastRecordReportInfo != null && lastRecordReportInfo.getRecordId() != item.getRecordId()) {
				writeLastRecordInfo();
				lastRecordReportInfo = null;
			}
			if (lastRecordReportInfo == null) {
				lastRecordReportInfo = new RecordReportInfo(item.getRecordId(), item.getRecordKeyValues());
				lastRecordReportInfo.setExtraValues(determineExtraValues(item));
			}
			String queryTitle = item.getQuery().getTitle();
			if (item.getQuery().getErrorSeverity() == ErrorSeverity.ERROR) {
				lastRecordReportInfo.addError(queryTitle);
			} else {
				lastRecordReportInfo.addWarning(queryTitle);
			}
		}
		
		protected List<String> determineExtraValues(DataReportItem item) {
			return Collections.emptyList();
		}

		private void writeLastRecordInfo() {
			List<String> lineValues = new ArrayList<String>();
			lineValues.addAll(lastRecordReportInfo.getKeyValues());
			lineValues.addAll(lastRecordReportInfo.getExtraValues());
			lineValues.add(StringUtils.join(lastRecordReportInfo.getErrors(), ERRORS_SEPARATOR));
			lineValues.add(StringUtils.join(lastRecordReportInfo.getWarnings(), ERRORS_SEPARATOR));
			csvWriter.writeNext(lineValues);
		}
		
		@Override
		public void close() throws IOException {
			if (lastRecordReportInfo != null) {
				writeLastRecordInfo();
			}
			super.close();
		}
		
		@Override
		protected List<String> determineExtraHeaders() {
			return Arrays.asList(ERRORS_HEADER, WARNINGS_HEADER);
		}

		protected class RecordReportInfo {
			
			private int recordId;
			private List<String> keyValues;
			private List<String> extraValues;
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
			
			public List<String> getExtraValues() {
				return extraValues;
			}
			
			public void setExtraValues(List<String> extraValues) {
				this.extraValues = extraValues;
			}
			
			public List<String> getErrors() {
				return errors;
			}
			
			public List<String> getWarnings() {
				return warnings;
			}
			
		}
	}
	
	public static class CollectEarthCSVWriterDataReportItemProcessor extends GroupedByRecordCSVWriterDataReportItemProcessor {

		private static final String X_COORDINATE_HEADER = "XCOORD";
		private static final String Y_COORDINATE_HEADER = "YCOORD";
		private static final String LOCATION_ATTRIBUTE_NAME = "location";
		
		private List<AttributeDefinition> fromCSVAttributes;

		private String locationAttributePath;
		private List<String> fromCSVAttributePaths;

		public CollectEarthCSVWriterDataReportItemProcessor(
				EntityDefinition rootEntityDefinition) {
			super(rootEntityDefinition);
			this.locationAttributePath = rootEntityDefinition.getName() + Path.SEPARATOR + LOCATION_ATTRIBUTE_NAME;
			this.fromCSVAttributes = determineFromCSVAttributes();
			this.fromCSVAttributePaths = new ArrayList<String>(fromCSVAttributes.size());
			for (AttributeDefinition def : fromCSVAttributes) {
				String attrPath = rootEntityDefinition.getName() + Path.SEPARATOR + def.getName();
				this.fromCSVAttributePaths.add(attrPath);
			}
		}
		
		@Override
		protected List<String> determineExtraHeaders() {
			List<String> extraHeaders = new ArrayList<String>();
			extraHeaders.add(Y_COORDINATE_HEADER);
			extraHeaders.add(X_COORDINATE_HEADER);
			for (AttributeDefinition def : fromCSVAttributes) {
				extraHeaders.add(def.getName());
			}
			extraHeaders.addAll(super.determineExtraHeaders());
			return extraHeaders;
		}
		
		@Override
		protected List<String> determineExtraValues(DataReportItem item) {
			List<String> values = new ArrayList<String>();
			CollectRecord record = item.getRecord();
			
			CoordinateAttribute locationAttr = record.findNodeByPath(locationAttributePath);
			Coordinate location = locationAttr.getValue();
			values.add(String.valueOf(location.getY()));
			values.add(String.valueOf(location.getX()));
			
			for (String attrPath : fromCSVAttributePaths) {
				Attribute<?, ?> attr = record.findNodeByPath(attrPath);
				values.add(extractCSVValue(attr));
			}
			values.addAll(super.determineExtraValues(item));
			return values;
		}

		private String extractCSVValue(Attribute<?, ?> attr) {
			if (attr == null || attr.isEmpty()) {
				return "";
			}
			Value value = attr.getValue();
			if (value instanceof AbstractValue) {
				return ((AbstractValue) value).toPrettyFormatString();
			} else {
				return value.toString();
			}
		}

		private List<AttributeDefinition> determineFromCSVAttributes() {
			CollectSurvey survey = (CollectSurvey) rootEntityDefinition.getSurvey();

			final CollectAnnotations annotations = survey.getAnnotations();
			final List<AttributeDefinition> defs = new ArrayList<AttributeDefinition>();
			rootEntityDefinition.traverse(new NodeDefinitionVisitor() {
				public void visit(NodeDefinition def) {
					if (def instanceof AttributeDefinition) {
						if (annotations.isFromCollectEarthCSV((AttributeDefinition) def)) {
							defs.add((AttributeDefinition) def);
						}
					}
				}
			});
			return defs;
		}
		
	}

	private static class ReportExportJob extends Job {
		
		//input
		private CollectSurvey survey;
		private DataReport report;
		private Class<? extends CSVWriterDataReportItemProcessor> itemProcessorType;
		private DataReportManager reportManager;

		//output
		private File outputFile;
		
		public ReportExportJob() {
		}
		
		@Override
		protected void buildTasks() throws Throwable {
			ReportExportTask task = new ReportExportTask();
			addTask(task);
		}
		
		public void setReportManager(DataReportManager reportManager) {
			this.reportManager = reportManager;
		}
		
		public void setSurvey(CollectSurvey survey) {
			this.survey = survey;
		}
		
		public void setReport(DataReport report) {
			this.report = report;
		}
		
		public void setItemProcessorType(Class<? extends CSVWriterDataReportItemProcessor> itemProcessorType) {
			this.itemProcessorType = itemProcessorType;
		}
		
		public File getOutputFile() {
			return outputFile;
		}
		
		private class ReportExportTask extends Task {
		
			public ReportExportTask() {
			}
			
			@Override
			protected long countTotalItems() {
				return Long.valueOf(reportManager.countItems(report));
			}
			
			@Override
			protected void execute() throws Throwable {
				EntityDefinition rootEntityDefinition = survey.getSchema().getRootEntityDefinitions().get(0);
				CSVWriterDataReportItemProcessor itemProcessor = Objects.newInstance(itemProcessorType, 
						rootEntityDefinition);
				itemProcessor.init();
				long total = getTotalItems();
				int itemsPerPage = 200;
				int pages = Double.valueOf(Math.ceil((double) total / itemsPerPage)).intValue();
				for (int page = 1; page <= pages ; page++) {
					if (! isRunning()) {
						break;
					}
					List<DataReportItem> items = reportManager.loadItems(report, (page - 1) * itemsPerPage, itemsPerPage);
					for (DataReportItem item : items) {
						if (! isRunning()) {
							break;
						}
						itemProcessor.process(item);
						incrementProcessedItems();
					}
				}
				itemProcessor.close();
				outputFile = itemProcessor.getOutputFile();
			}
			
		}
	}
	
}
