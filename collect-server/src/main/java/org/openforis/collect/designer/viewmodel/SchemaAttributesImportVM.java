package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.designer.viewmodel.SchemaTreePopUpVM.NodeSelectedEvent;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaAttributesImportVM extends SurveyBaseVM {

	private static final AttributeType DEFAULT_ATTRIBUTE_TYPE = AttributeType.TEXT;
	
	private Map<String,String> form;
	
	private File uploadedFile;
	private String uploadedFileName;
	private EntityDefinition parentEntityDefinition;
	
	public SchemaAttributesImportVM() {
		form = new HashMap<String, String>();
		reset();
	}
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
		parentEntityDefinition = survey.getSchema().getRootEntityDefinitions().get(0);
		notifyChange("parentEntityDefinitionPath");
	}

	protected void reset() {
		if ( uploadedFile != null ) {
			uploadedFile.delete();
			uploadedFile = null;
		}
		uploadedFileName = null;
		notifyChange("uploadedFileName");
	}
	
	@Command
	public void importAttributes(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if ( validateForm(ctx) ) {
			try {
				Map<String, AttributeType> attributeTypeByColumn = guessAttributeTypeByColumn();
				Set<Entry<String,AttributeType>> entrySet = attributeTypeByColumn.entrySet();
				int newAttributesCount = 0;
				int skippedAttributesCount = 0;
				for (Entry<String, AttributeType> entry : entrySet) {
					String colName = entry.getKey();
					AttributeType attributeType = entry.getValue();
					AttributeDefinition attrDef = (AttributeDefinition) NodeType.createNodeDefinition(survey, NodeType.ATTRIBUTE, attributeType);
					String attributeName = adjustInternalName(colName);
					if (parentEntityDefinition.containsChildDefinition(attributeName)) {
						skippedAttributesCount ++;
					} else {
						attrDef.setName(attributeName);
						parentEntityDefinition.addChildDefinition(attrDef);
						newAttributesCount ++;
					}
				}
				MessageUtil.showInfo("survey.schema.attributes_import.import_complete", 
						new Object[]{newAttributesCount, skippedAttributesCount});
				dispatchSchemaChangedCommand();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	private Map<String, AttributeType> guessAttributeTypeByColumn() {
		CsvReader reader = null;
		try {
			reader = new CsvReader(uploadedFile);
			reader.readHeaders();
			List<String> columnNames = reader.getColumnNames();
			Map<String, AttributeType> typeByColumn = new LinkedHashMap<String, AttributeType>(columnNames.size());
			for (String colName : columnNames) {
				typeByColumn.put(colName, null);
			}
			CsvLine line = reader.readNextLine();
			
			while (line != null && reader.getLinesRead() <= 100) {
				String[] values = line.getLine();
				for (int i = 0; i < columnNames.size(); i++) {
					String val = values[i];
					if (StringUtils.isNotBlank(val)) {
						String colName = columnNames.get(i);
						AttributeType currentAttributeType = typeByColumn.get(colName);
						if (currentAttributeType == null || currentAttributeType != DEFAULT_ATTRIBUTE_TYPE) {
							boolean isDate = isDate(reader, val);
							boolean isNumber = NumberUtils.isNumber(val);
							AttributeType attributeType;
							if (isDate) {
								attributeType = AttributeType.DATE;
							} else if (isNumber) {
								attributeType = AttributeType.NUMBER;	
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

	@Command
	public void fileUploaded(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
 		Media media = event.getMedia();
		String fileName = media.getName();
		File tempFile = OpenForisIOUtils.copyToTempFile(media.getStreamData(), FilenameUtils.getExtension(fileName));
		this.uploadedFile = tempFile;
		this.uploadedFileName = fileName;
		notifyChange("uploadedFileName");
	}

	@Command
	public void openParentEntitySelectionButton() {
		Predicate<SurveyObject> includedNodePredicate = new Predicate<SurveyObject>() {
			public boolean evaluate(SurveyObject item) {
				return item instanceof EntityDefinition;
			}
		};
		String title = Labels.getLabel("survey.schema.attributes_import.select_entity.popup.title");
		
		//calculate parent item (tab or entity)
		final Window popup = SchemaTreePopUpVM.openPopup(title, parentEntityDefinition.getRootEntity(), null, includedNodePredicate, 
				true, true, null, null, parentEntityDefinition);
		popup.addEventListener(SchemaTreePopUpVM.NODE_SELECTED_EVENT_NAME, new EventListener<NodeSelectedEvent>() {
			public void onEvent(NodeSelectedEvent event) throws Exception {
				SurveyObject selectedParent = event.getSelectedItem();
				parentEntityDefinition = (EntityDefinition) selectedParent;
				notifyChange("parentEntityDefinitionPath");
				closePopUp(popup);
			}
		});
	}
	
	protected boolean validateForm(BindContext ctx) {
		String messageKey = null;
		if ( uploadedFile == null ) {
			messageKey = "global.file_not_selected";
		}
		if ( messageKey == null ) {
			return true;
		} else {
			MessageUtil.showWarning(messageKey);
			return false;
		}
	}
	
	public String getParentEntityDefinitionPath() {
		return parentEntityDefinition == null ? null : parentEntityDefinition.getPath();
	}
	
	public Map<String, String> getForm() {
		return form;
	}
	
	public void setForm(Map<String, String> form) {
		this.form = form;
	}
	
	public String getUploadedFileName() {
		return uploadedFileName;
	}
}
