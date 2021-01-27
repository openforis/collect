package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.io.metadata.SchemaSummaryCSVExportJob;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.Files;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.flat.FlatRecord;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaLabelsImportVM extends BaseSurveyFileImportVM {

	private static final List<Type> LABEL_TYPES = Arrays.asList(NodeLabel.Type.INSTANCE, NodeLabel.Type.REPORTING);

	private static final String CLOSE_POP_UP_GLOBAL_COMMAND = "closeSchemaLabelsImportPopUp";

	private static final String EXPORT_JOB_STATUS_POPUP_MESSAGE = "survey.schema.labels_export.job_status_popup.message";
	private static final String LABELS_EXPORT_ERROR = "survey.schema.labels_export.error";
	private static final String IMPORT_COMPLETE_MESSAGE_KEY = "survey.schema.labels_import.import_complete";
	private static final String INVALID_COLUMN_NAME_MESSAGE_KEY = "survey.schema.labels_import.error.invalid_column_name";
	private static final String MISSING_COLUMN_MESSAGE_KEY = "survey.schema.labels_import.error.missing_column";
	private static final String MISSING_NODE_DEF_ID_MESSAGE_KEY = "survey.schema.labels_import.error.missing_node_def_id";
	private static final String MISSING_NODE_DEF_MESSAGE_KEY = "survey.schema.labels_import.error.missing_node_def";
	private static final String MISSING_LABEL_DEFAULT_LANG_MESSAGE_KEY = "survey.schema.labels_import.error.missing_label_default_lang";
	private static final String SCHEMA_LABELS_FILE_NAME_PATTERN = "%s_schema_labels_%s.%s";

	// temporary
	private Window labelsExportJobStatusPopUp;

	public SchemaLabelsImportVM() {
		super(new String[] { Files.CSV_FILE_EXTENSION, Files.EXCEL_FILE_EXTENSION }, null);
		reset();
	}

	@Override
	@Init(superclass = false)
	public void init() {
		super.init();
	}

	@Command
	public void download() {
		SchemaSummaryCSVExportJob job = new SchemaSummaryCSVExportJob();
		job.setJobManager(jobManager);
		job.setSurvey(survey);
		job.setOnlyLabels(true);
		jobManager.start(job, survey.getId().toString());

		String statusPopUpTitle = Labels.getLabel(EXPORT_JOB_STATUS_POPUP_MESSAGE, new String[] { survey.getName() });

		labelsExportJobStatusPopUp = JobStatusPopUpVM.openPopUp(statusPopUpTitle, job, true,
				new JobEndHandler<SchemaSummaryCSVExportJob>() {
					public void onJobEnd(SchemaSummaryCSVExportJob job) {
						closePopUp(labelsExportJobStatusPopUp);
						labelsExportJobStatusPopUp = null;
						File file = job.getOutputFile();
						String surveyName = survey.getName();
						String dateStr = Dates.formatLocalDateTime(new Date());
						String fileName = String.format(SCHEMA_LABELS_FILE_NAME_PATTERN, surveyName, dateStr,
								Files.EXCEL_FILE_EXTENSION);
						String contentType = URLConnection.guessContentTypeFromName(fileName);
						try {
							FileInputStream is = new FileInputStream(file);
							Filedownload.save(is, contentType, fileName);
						} catch (FileNotFoundException e) {
							MessageUtil.showError(LABELS_EXPORT_ERROR, e.getMessage());
						}
					}
				});
	}

	@Command
	public void startImport(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if (validateForm(ctx)) {
			Map<Integer, List<NodeLabel>> labelsByNodeDefId = new LabelsExtractor(uploadedFile, survey)
					.extractLabelsByNodeDefId();

			if (labelsByNodeDefId != null) {
				new LabelsImporter(labelsByNodeDefId, survey).importLabels();

				MessageUtil.showInfo(IMPORT_COMPLETE_MESSAGE_KEY, labelsByNodeDefId.size());
				dispatchSchemaChangedCommand();
				close();
			}
		}
	}

	@Command
	public void close() {
		BindUtils.postGlobalCommand(null, null, CLOSE_POP_UP_GLOBAL_COMMAND, null);
	}

	private static class LabelsImporter {

		private Map<Integer, List<NodeLabel>> labelsByNodeDefId = new HashMap<>();
		private Survey survey;

		public LabelsImporter(Map<Integer, List<NodeLabel>> labelsByNodeDefId, Survey survey) {
			super();
			this.labelsByNodeDefId = labelsByNodeDefId;
			this.survey = survey;
		}

		public void importLabels() {
			Schema schema = survey.getSchema();
			for (Entry<Integer, List<NodeLabel>> entry : labelsByNodeDefId.entrySet()) {
				int nodeDefId = entry.getKey();
				NodeDefinition nodeDef = schema.getDefinitionById(nodeDefId);
				List<NodeLabel> labels = entry.getValue();
				for (NodeLabel nodeLabel : labels) {
					String text = StringUtils.trimToNull(nodeLabel.getText());
					Type type = nodeLabel.getType();
					String language = nodeLabel.getLanguage();
					if (text == null) {
						nodeDef.removeLabel(type, language);
					} else {
						nodeDef.setLabel(type, language, text);
					}
				}
			}
		}

	}

	private static class LabelsExtractor {

		private File file;
		private Survey survey;

		public LabelsExtractor(File file, Survey survey) {
			super();
			this.file = file;
			this.survey = survey;
		}

		public Map<Integer, List<NodeLabel>> extractLabelsByNodeDefId() {
			CsvReader reader = null;
			try {
				// init CSV reader
				reader = new CsvReader(file);
				reader.readHeaders();

				// validate column names
				List<String> columnNames = reader.getColumnNames();
				if (!validateColumnNames(columnNames)) {
					return null;
				}
				// parse rows
				Map<Integer, List<NodeLabel>> result = new HashMap<Integer, List<NodeLabel>>();
				FlatRecord row = reader.nextRecord();
				while (row != null && !isEmpty(row)) {
					if (!validateRow(reader.getLinesRead() + 1, row, survey.getDefaultLanguage())) {
						return null;
					}
					List<NodeLabel> labels = new ArrayList<>();
					List<String> languages = survey.getLanguages();
					Integer nodeDefId = row.getValue("id", Integer.class);
					for (Type type : LABEL_TYPES) {
						for (String lang : languages) {
							String labelColName = getColumnPrefix(type) + "_" + lang;
							if (columnNames.contains(labelColName)) {
								String text = row.getValue(labelColName, String.class);
								labels.add(new NodeLabel(type, lang, text));
							}
						}
					}
					result.put(nodeDefId, labels);

					row = reader.nextRecord();
				}
				return result;
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}

		private boolean validateColumnNames(List<String> columnNames) {
			// determine required column names
			List<String> requiredColumnNames = new ArrayList<>();
			requiredColumnNames.add("id");
			requiredColumnNames.add(getColumnPrefix(Type.INSTANCE) + "_" + survey.getDefaultLanguage());

			// validate column names
			for (String colName : columnNames) {
				if (!validateColumnName(colName)) {
					handleError(INVALID_COLUMN_NAME_MESSAGE_KEY,
							new String[] { colName, requiredColumnNames.toString() });
					return false;
				}
			}
			// validate missing required column names
			for (String colName : requiredColumnNames) {
				if (!columnNames.contains(colName)) {
					handleError(MISSING_COLUMN_MESSAGE_KEY, new String[] { colName, requiredColumnNames.toString() });
					return false;
				}
			}
			return true;
		}

		private boolean validateColumnName(String colName) {
			if ("id".equalsIgnoreCase(colName)) {
				return true;
			}
			List<String> languages = survey.getLanguages();
			for (Type type : LABEL_TYPES) {
				for (String lang : languages) {
					String expectedColName = getColumnPrefix(type) + "_" + lang;
					if (expectedColName.equalsIgnoreCase(colName)) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean validateRow(long rowNumber, FlatRecord row, String defaultLanguage) {
			Integer nodeDefId = row.getValue("id", Integer.class);
			if (nodeDefId == null) {
				handleError(MISSING_NODE_DEF_ID_MESSAGE_KEY, new Long[] { rowNumber });
				return false;
			}
			if (!survey.getSchema().containsDefinitionWithId(nodeDefId)) {
				handleError(MISSING_NODE_DEF_MESSAGE_KEY, new Object[] { nodeDefId, rowNumber });
				return false;
			}
			String defaultLangLabelColName = getColumnPrefix(Type.INSTANCE) + "_" + defaultLanguage;
			String defaultLangLabel = row.getValue(defaultLangLabelColName, String.class);
			if (StringUtils.isBlank(defaultLangLabel)) {
				handleError(MISSING_LABEL_DEFAULT_LANG_MESSAGE_KEY,
						new Object[] { rowNumber, defaultLangLabelColName });
				return false;
			}
			return true;
		}

		private static String getColumnPrefix(Type type) {
			switch (type) {
			case INSTANCE:
				return "label";
			default:
				return "label_" + type.name().toLowerCase(Locale.ENGLISH);
			}
		}

		private void handleError(String messageKey, Object[] args) {
			MessageUtil.showError(messageKey, args);
		}

		private boolean isEmpty(FlatRecord row) {
			String[] values = row.toStringArray();
			for (String val : values) {
				if (StringUtils.isNotBlank(val)) {
					return false;
				}
			}
			return true;
		}

	}

}
