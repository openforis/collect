package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.viewmodel.JobStatusPopUpVM.JobEndHandler;
import org.openforis.collect.io.metadata.SchemaSummaryCSVExportJob;
import org.openforis.collect.utils.Dates;
import org.openforis.collect.utils.Files;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.flat.FlatRecord;
import org.openforis.idm.metamodel.LanguageSpecificText;
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
			Map<Integer, NodeDefLabels> labelsByNodeDefId = new LabelsExtractor(uploadedFile, survey)
					.extractLabelsByNodeDefId();
			
			if (labelsByNodeDefId != null) {
				new LabelsImporter(survey, labelsByNodeDefId).importLabels();

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

		private Survey survey;
		private Map<Integer, NodeDefLabels> labelsByNodeDefId = new HashMap<>();

		public LabelsImporter(Survey survey, Map<Integer, NodeDefLabels> labelsByNodeDefId) {
			super();
			this.survey = survey;
			this.labelsByNodeDefId = labelsByNodeDefId;
		}

		public void importLabels() {
			Schema schema = survey.getSchema();
			for (Entry<Integer, NodeDefLabels> entry : labelsByNodeDefId.entrySet()) {
				int nodeDefId = entry.getKey();
				NodeDefinition nodeDef = schema.getDefinitionById(nodeDefId);
				// labels
				NodeDefLabels nodeDefLabels = entry.getValue();
				for (NodeLabel nodeLabel : nodeDefLabels.getNodeLabels()) {
					String text = StringUtils.trimToNull(nodeLabel.getText());
					Type type = nodeLabel.getType();
					String language = nodeLabel.getLanguage();
					if (text == null) {
						nodeDef.removeLabel(type, language);
					} else {
						nodeDef.setLabel(type, language, text);
					}
				}
				for (LanguageSpecificText description: nodeDefLabels.getDescriptions()) {
					String language = description.getLanguage();
					String text = StringUtils.trimToNull(description.getText());
					if (text == null) {
						nodeDef.removeDescription(language);
					} else {
						nodeDef.setDescription(language, text);
					}
				}
			}
		}

	}

	private static class LabelsExtractor {

		private static final String LABEL_COL_PREFIX = "label";
		private static final String DESCRIPTION_COL_PREFIX = "description";
		
		private File file;
		private Survey survey;
		private Set<String> possibleColumnNames;

		public LabelsExtractor(File file, Survey survey) {
			super();
			this.file = file;
			this.survey = survey;
			this.possibleColumnNames = determinePossibleColumnNames(survey);
		}

		public Map<Integer, NodeDefLabels> extractLabelsByNodeDefId() {
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
				Map<Integer, NodeDefLabels> result = new HashMap<Integer, NodeDefLabels>();
				FlatRecord row = reader.nextRecord();
				while (row != null && !row.isEmpty()) {
					if (!validateRow(reader.getLinesRead() + 1, row, survey.getDefaultLanguage())) {
						return null;
					}
					Integer nodeDefId = row.getValue("id", Integer.class);
					List<String> languages = survey.getLanguages();

					// extract labels
					List<NodeLabel> labels = new ArrayList<>();
					for (Type type : LABEL_TYPES) {
						for (String lang : languages) {
							String labelColName = getLabelColumnPrefix(type) + "_" + lang;
							if (columnNames.contains(labelColName)) {
								String text = row.getValue(labelColName, String.class);
								labels.add(new NodeLabel(type, lang, text));
							}
						}
					}
					// extract descriptions
					List<LanguageSpecificText> descriptions = new ArrayList<>();
					for (String lang : languages) {
						String descriptionColName = DESCRIPTION_COL_PREFIX + "_" + lang;
						if (columnNames.contains(descriptionColName)) {
							String text = row.getValue(descriptionColName, String.class);
							descriptions.add(new LanguageSpecificText(lang, text));
						}
						
					}
					result.put(nodeDefId, new NodeDefLabels(labels, descriptions));

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
			requiredColumnNames.add(getLabelColumnPrefix(Type.INSTANCE) + "_" + survey.getDefaultLanguage());

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
			return possibleColumnNames.contains(colName);
		}

		private boolean validateRow(long rowNumber, FlatRecord row, String defaultLanguage) {
			// missing id
			Integer nodeDefId = row.getValue("id", Integer.class);
			if (nodeDefId == null) {
				handleError(MISSING_NODE_DEF_ID_MESSAGE_KEY, new Long[] { rowNumber });
				return false;
			}
			// missing node def
			if (!survey.getSchema().containsDefinitionWithId(nodeDefId)) {
				handleError(MISSING_NODE_DEF_MESSAGE_KEY, new Object[] { nodeDefId, rowNumber });
				return false;
			}
			// labels
			String defaultLangLabelColName = getLabelColumnPrefix(Type.INSTANCE) + "_" + defaultLanguage;
			String defaultLangLabel = row.getValue(defaultLangLabelColName, String.class);
			if (StringUtils.isBlank(defaultLangLabel)) {
				handleError(MISSING_LABEL_DEFAULT_LANG_MESSAGE_KEY,
						new Object[] { rowNumber, defaultLangLabelColName });
				return false;
			}
			
			return true;
		}

		private void handleError(String messageKey, Object[] args) {
			MessageUtil.showError(messageKey, args);
		}

		private static String getLabelColumnPrefix(Type type) {
			switch (type) {
			case INSTANCE:
				return LABEL_COL_PREFIX;
			default:
				return LABEL_COL_PREFIX + "_" + type.name().toLowerCase(Locale.ENGLISH);
			}
		}
		
		private static Set<String> determinePossibleColumnNames(Survey survey) {
			Set<String> possibleColumnNames = new HashSet<>();
			possibleColumnNames.add("id");
			List<String> languages = survey.getLanguages();
			for (Type type : LABEL_TYPES) {
				for (String lang : languages) {
					possibleColumnNames.add(getLabelColumnPrefix(type) + "_" + lang);
					possibleColumnNames.add(DESCRIPTION_COL_PREFIX + "_" + lang);
					
				}
			}
			return possibleColumnNames;
		}

	}
	
	private static class NodeDefLabels {
		List<NodeLabel> nodeLabels;
		List<LanguageSpecificText> descriptions;
		
		public NodeDefLabels(List<NodeLabel> nodeLabels, List<LanguageSpecificText> descriptions) {
			super();
			this.nodeLabels = nodeLabels;
			this.descriptions = descriptions;
		}

		public List<NodeLabel> getNodeLabels() {
			return nodeLabels;
		}
		
		public List<LanguageSpecificText> getDescriptions() {
			return descriptions;
		}
	}

}
