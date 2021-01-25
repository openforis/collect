package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.util.MessageUtil;
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

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaLabelsImportVM extends BaseSurveyFileImportVM {

	private static final List<Type> LABEL_TYPES = Arrays.asList(NodeLabel.Type.INSTANCE, NodeLabel.Type.REPORTING);

	private static final String CLOSE_POP_UP_GLOBAL_COMMAND = "closeSchemaLabelsImportPopUp";

	private static final String IMPORT_IMPORT_COMPLETE_MESSAGE_KEY = "survey.schema.labels_import.import_complete";
	private static final String MISSING_COLUMN_MESSAGE_KEY = "survey.schema.labels_import.error.missing_column";
	private static final String MISSING_NODE_DEF_ID_MESSAGE_KEY = "survey.schema.labels_import.error.missing_node_def_id";
	private static final String MISSING_NODE_DEF_MESSAGE_KEY = "survey.schema.labels_import.error.missing_node_def";
	private static final String MISSING_LABEL_DEFAULT_LANG_MESSAGE_KEY = "survey.schema.labels_import.error.missing_label_default_lang";

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
	public void startImport(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if (validateForm(ctx)) {
			Map<Integer, List<NodeLabel>> labelsByNodeDefId = new LabelsExtractor(uploadedFile, survey)
					.extractLabelsByNodeDefId();

			if (labelsByNodeDefId != null) {
				new LabelsImporter(labelsByNodeDefId, survey).importLabels();

				MessageUtil.showInfo(IMPORT_IMPORT_COMPLETE_MESSAGE_KEY, labelsByNodeDefId.size());
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
			Map<Integer, List<NodeLabel>> result = new HashMap<Integer, List<NodeLabel>>();
			CsvReader reader = null;
			try {
				// init CSV reader
				reader = new CsvReader(file);
				reader.readHeaders();

				// validate column names
				List<String> columnNames = reader.getColumnNames();
				validateColumnNames(columnNames);

				// parse rows
				boolean errorFound = false;
				FlatRecord row = reader.nextRecord();
				while (row != null) {
					if (!validateRow(reader.getLinesRead() + 1, row, survey.getDefaultLanguage())) {
						errorFound = true;
						break;
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
				return errorFound ? null : result;
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}

		private boolean validateRow(long rowNumber, FlatRecord row, String defaultLanguage) {
			Integer nodeDefId = row.getValue("id", Integer.class);
			if (nodeDefId == null) {
				handleError(MISSING_NODE_DEF_ID_MESSAGE_KEY, new Long[] { rowNumber });
				return false;
			}
			if (!survey.getSchema().containsDefinitionWithId(nodeDefId)) {
				handleError(MISSING_NODE_DEF_MESSAGE_KEY, new Integer[] { nodeDefId });
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

		private void validateColumnNames(List<String> columnNames) {
			List<String> requiredColumnNames = new ArrayList<>();
			requiredColumnNames.add("id");
			requiredColumnNames.add(getColumnPrefix(Type.INSTANCE) + "_" + survey.getDefaultLanguage());
			for (String colName : requiredColumnNames) {
				if (!columnNames.contains(colName)) {
					handleError(MISSING_COLUMN_MESSAGE_KEY, new String[] { colName, requiredColumnNames.toString() });
					return;
				}
			}
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
	}

}
