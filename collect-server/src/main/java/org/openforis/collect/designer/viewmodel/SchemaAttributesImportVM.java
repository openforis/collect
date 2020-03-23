package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.designer.metamodel.NodeType;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.designer.viewmodel.SchemaObjectSelectorPopUpVM.NodeSelectedEvent;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.utils.Files;
import org.openforis.collect.utils.SurveyObjects;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.SurveyObject;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaAttributesImportVM extends BaseSurveyFileImportVM {

	private EntityDefinition parentEntityDefinition;
	private boolean booleanAsCode;
	private boolean labelsInSecondRow;
	
	public SchemaAttributesImportVM() {
		reset();
	}
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
		parentEntityDefinition = survey.getSchema().getFirstRootEntityDefinition();
		notifyChange("parentEntityDefinitionPath");
	}

	@Command
	public void importAttributes(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if ( validateForm(ctx) ) {
			Map<String, AttributeDetails> attributeDetailsByColumn = new AttributeDetailsExtractor(uploadedFile, labelsInSecondRow)
					.extractAttributeDetailsByColumn();
			List<AttributeDefinition> newAttributes = new AttributesImporter(parentEntityDefinition, booleanAsCode)
					.importAttributes(attributeDetailsByColumn);
			MessageUtil.showInfo("survey.schema.attributes_import.import_complete", 
					newAttributes.size(), attributeDetailsByColumn.size() - newAttributes.size());
			dispatchSchemaChangedCommand();
		}
	}
	
	@Override
	protected void checkCanImportFile(Media media) {
		String fileName = media.getName();
		String extension = FilenameUtils.getExtension(fileName);
		if (!Files.CSV_FILE_EXTENSION.equalsIgnoreCase(extension)) {
			throw new RuntimeException(String.format("Only CSV file upload is supported, found: %s", extension));
		}
	}

	@Command
	public void openParentEntitySelectionButton() {
		Predicate<SurveyObject> includedNodePredicate = new Predicate<SurveyObject>() {
			public boolean evaluate(SurveyObject item) {
				return item instanceof UITab || item instanceof EntityDefinition;
			}
		};
		Predicate<SurveyObject> selectableNodePredicate = new Predicate<SurveyObject>() {
			public boolean evaluate(SurveyObject item) {
				return item instanceof EntityDefinition;
			}
		};
		String title = Labels.getLabel("survey.schema.attributes_import.select_entity.popup.title");
		
		//calculate parent item (tab or entity)
		final Window popup = SchemaObjectSelectorPopUpVM.openPopup(title, false, parentEntityDefinition.getRootEntity(), null, includedNodePredicate, 
				true, true, null, selectableNodePredicate, parentEntityDefinition, false);
		popup.addEventListener(SchemaObjectSelectorPopUpVM.NODE_SELECTED_EVENT_NAME, new EventListener<NodeSelectedEvent>() {
			public void onEvent(NodeSelectedEvent event) throws Exception {
				SurveyObject selectedParent = event.getSelectedItem();
				parentEntityDefinition = (EntityDefinition) selectedParent;
				notifyChange("parentEntityDefinitionPath");
				closePopUp(popup);
			}
		});
	}
	
	public String getParentEntityDefinitionPath() {
		return parentEntityDefinition == null ? null : parentEntityDefinition.getPath();
	}
	
	public boolean isBooleanAsCode() {
		return booleanAsCode;
	}
	
	public void setBooleanAsCode(boolean booleanAsCode) {
		this.booleanAsCode = booleanAsCode;
	}
	
	public boolean isLabelsInSecondRow() {
		return labelsInSecondRow;
	}
	
	public void setLabelsInSecondRow(boolean labelsInSecondRow) {
		this.labelsInSecondRow = labelsInSecondRow;
	}
	
	private static class AttributeDetails {
		
		private AttributeType type;
		private String label;

		public AttributeType getType() {
			return type;
		}
		
		public void setType(AttributeType type) {
			this.type = type;
		}
		
		public String getLabel() {
			return label;
		}
		
		public void setLabel(String label) {
			this.label = label;
		}
		
	}
	
	private static class AttributesImporter {
		
		private static final String YES_NO_LIST_NAME = "yes_no";
		
		private EntityDefinition parentEntityDefinition;
		private boolean booleanAttributeAsCode = false;

		public AttributesImporter(EntityDefinition parentEntityDefinition, boolean booleanAttributeAsCode) {
			super();
			this.parentEntityDefinition = parentEntityDefinition;
			this.booleanAttributeAsCode = booleanAttributeAsCode;
		}

		public List<AttributeDefinition> importAttributes(Map<String, AttributeDetails> attributeDetailsByColumn) {
			List<AttributeDefinition> result = new ArrayList<AttributeDefinition>();
			CollectSurvey survey = parentEntityDefinition.getSurvey();
			for (Entry<String, AttributeDetails> entry : attributeDetailsByColumn.entrySet()) {
				String colName = entry.getKey();
				AttributeDetails details = entry.getValue();
				CodeList codeList = null;
				AttributeType foundType = details.getType();
				AttributeType type;
				if (foundType == AttributeType.BOOLEAN && booleanAttributeAsCode) {
					type = AttributeType.CODE;
					codeList = getOrCreateYesNoList(survey);
				} else {
					type = foundType;
				}
				AttributeDefinition attrDef = (AttributeDefinition) NodeType.createNodeDefinition(survey, NodeType.ATTRIBUTE, type);
				switch(type) {
					case CODE:
						((CodeAttributeDefinition) attrDef).setList(codeList);
						break;
					case TEXT:
						((TextAttributeDefinition) attrDef).setType(TextAttributeDefinition.Type.SHORT);
						break;
					default:
				}
				String attributeName = SurveyObjects.adjustInternalName(colName);
				if (! parentEntityDefinition.containsChildDefinition(attributeName)) {
					attrDef.setName(attributeName);
					String label = details.getLabel();
					if (StringUtils.isNotBlank(label)) {
						attrDef.setLabel(Type.INSTANCE, survey.getDefaultLanguage(), label);
					}
					parentEntityDefinition.addChildDefinition(attrDef);
					result.add(attrDef);
				}
			}
			return result;
		}

		private CodeList getOrCreateYesNoList(CollectSurvey survey) {
			CodeList list = survey.getCodeList(YES_NO_LIST_NAME);
			if (list == null) {
				list = survey.createCodeList();
				list.setName(YES_NO_LIST_NAME);
				{
					CodeListItem item = list.createItem(1);
					item.setCode("yes");
					item.setLabel(survey.getDefaultLanguage(), "Yes");
					list.addItem(item);
				}
				{
					CodeListItem item = list.createItem(1);
					item.setCode("no");
					item.setLabel(survey.getDefaultLanguage(), "No");
					list.addItem(item);
				}
				survey.addCodeList(list);
			}
			return list;
		}
	}
	
	private static class AttributeDetailsExtractor {
		
		private static final AttributeType DEFAULT_ATTRIBUTE_TYPE = AttributeType.TEXT;
		private static final String[] BOOLEAN_VALUES = new String[]{"true", "false", "yes", "no"};
		
		private File file;
		private boolean labelsInSecondRow = false;
		
		public AttributeDetailsExtractor(File file, boolean labelsInSecondRow) {
			super();
			this.file = file;
			this.labelsInSecondRow = labelsInSecondRow;
		}

		public Map<String, AttributeDetails> extractAttributeDetailsByColumn() {
			Map<String, AttributeType> attributeTypeByColumn = guessAttributeTypeByColumn();
			Map<String, AttributeDetails> result = new LinkedHashMap<String, AttributeDetails>(attributeTypeByColumn.size());
			Map<String, String> labelByColumn = labelsInSecondRow ? extractLabelByColumn() : null;
			for (Entry<String, AttributeType> entry : attributeTypeByColumn.entrySet()) {
				AttributeDetails details = new AttributeDetails();
				String colName = entry.getKey();
				AttributeType type = entry.getValue();
				details.setType(type == null ? DEFAULT_ATTRIBUTE_TYPE : type);
				if (labelsInSecondRow) {
					details.setLabel(labelByColumn.get(colName));
				}
				result.put(colName, details);
			}
			return result;
		}
		
		private Map<String, String> extractLabelByColumn() {
			CsvReader reader = null;
			try {
				reader = new CsvReader(file);
				reader.readHeaders();
				List<String> columnNames = reader.getColumnNames();
				CsvLine labelsLine = reader.readNextLine();
				
				Map<String, String> labelByColumn = new LinkedHashMap<String, String>(columnNames.size());
				String[] labels = labelsLine.getLine();
				for (int i = 0; i < labels.length; i++) {
					String colName = columnNames.get(i);
					String label = labels[i];
					labelByColumn.put(colName, label);
				}
				return labelByColumn;
			} catch(Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}
		
		private Map<String, AttributeType> guessAttributeTypeByColumn() {
			CsvReader reader = null;
			try {
				reader = new CsvReader(file);
				reader.readHeaders();
				List<String> columnNames = reader.getColumnNames();
				Map<String, AttributeType> typeByColumn = new LinkedHashMap<String, AttributeType>(columnNames.size());
				for (String colName : columnNames) {
					typeByColumn.put(colName, null);
				}
				CsvLine line = reader.readNextLine();
				if (labelsInSecondRow) {
					line = reader.readNextLine();
				}
				while (line != null && reader.getLinesRead() <= 100) {
					String[] values = line.getLine();
					for (int i = 0; i < columnNames.size(); i++) {
						String val = values[i];
						if (StringUtils.isNotBlank(val)) {
							String colName = columnNames.get(i);
							AttributeType currentAttributeType = typeByColumn.get(colName);
							if (currentAttributeType == null || currentAttributeType != DEFAULT_ATTRIBUTE_TYPE) {
								AttributeType attributeType;
								if (isDate(reader, val)) {
									attributeType = AttributeType.DATE;
								} else if (NumberUtils.isNumber(val)) {
									attributeType = AttributeType.NUMBER;
								} else if (isBoolean(reader, val)) {
									attributeType = AttributeType.BOOLEAN;
								} else {
									attributeType = DEFAULT_ATTRIBUTE_TYPE;
								}
								if (currentAttributeType == null) {
									typeByColumn.put(colName, attributeType);
								} else if (attributeType == DEFAULT_ATTRIBUTE_TYPE && currentAttributeType != attributeType) {
									typeByColumn.put(colName, DEFAULT_ATTRIBUTE_TYPE);
								}
							}
						}
					}
					line = reader.readNextLine();
				}
				for (String colName : columnNames) {
					if (typeByColumn.get(colName) == null) {
						typeByColumn.put(colName, DEFAULT_ATTRIBUTE_TYPE);
					}
				}
				return typeByColumn;
			} catch(Exception e) {
				throw new RuntimeException(e);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		}
		
		private boolean isDate(CsvReader reader, String val) {
			try {
				return reader.getDateFormat().parse(val) != null;
			} catch (ParseException e) {
				return false;
			}
		}
		
		public static boolean isBoolean(CsvReader reader, String val) {
			return ArrayUtils.contains(BOOLEAN_VALUES, val.toLowerCase(Locale.ENGLISH));
		}
		
	}

}
