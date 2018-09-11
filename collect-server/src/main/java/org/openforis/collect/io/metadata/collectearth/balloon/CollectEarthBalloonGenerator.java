package org.openforis.collect.io.metadata.collectearth.balloon;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.earth.core.handlers.BalloonInputFieldsUtils;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreator;
import org.openforis.collect.io.metadata.collectearth.balloon.CEField.CEFieldType;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.NodeDefinitionUIComponent;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.ui.UIField;
import org.openforis.collect.metamodel.ui.UIForm;
import org.openforis.collect.metamodel.ui.UIFormComponent;
import org.openforis.collect.metamodel.ui.UIFormSection;
import org.openforis.collect.metamodel.ui.UIFormSet;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.CodeAttributeLayoutType;
import org.openforis.collect.metamodel.ui.UITable;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CollectEarthBalloonGenerator {
	
	public static final String EXTRA_HIDDEN_PREFIX = "EXTRA_";
	protected static final String EXTRA_HIDDEN_FIELD_CLASS = "extra";
	private static final Set<String> HIDDEN_ATTRIBUTE_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"operator", "location", "plot_file", "actively_saved", "actively_saved_on"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	
	private static final String BALLOON_TEMPLATE_TXT = "org/openforis/collect/designer/templates/collectearth/balloon_template_new.txt"; //$NON-NLS-1$
	private static final String PLACEHOLDER_FOR_DYNAMIC_FIELDS = "PLACEHOLDER_FOR_DYNAMIC_FIELDS"; //$NON-NLS-1$
	
	private static final String PLACEHOLDER_FOR_FINISH_TRANSLATION = "PLACEHOLDER_FINISH"; //$NON-NLS-1$
	private static final String PLACEHOLDER_FOR_NEXT_TRANSLATION = "PLACEHOLDER_NEXT"; //$NON-NLS-1$
	private static final String PLACEHOLDER_FOR_PREVIOUS_TRANSLATION = "PLACEHOLDER_PREVIOUS"; //$NON-NLS-1$
	private static final String PLACEHOLDER_COLLECT_NOT_RUNNING = "PLACEHOLDER_COLLECT_NOT_RUNNING";//$NON-NLS-1$
	private static final String PLACEHOLDER_PLACEMARK_ALREADY_FILLED = "PLACEHOLDER_PLACEMARK_ALREADY_FILLED";//$NON-NLS-1$
	private static final String PLACEHOLDER_EXTRA_ID_ATTRIBUTES = "PLACEHOLDER_EXTRA_ID_ATTRIBUTES";//$NON-NLS-1$
	private static final String PLACEHOLDER_UI_LANGUAGE = "PLACEHOLDER_UI_LANGUAGE";
	private static final String PLACEHOLDER_FOR_EXTRA_ID_GET_REQUEST = "PLACEHOLDER_FOR_EXTRA_ID_GET_REQUEST";
	private static final String PLACEHOLDER_PREVIEW = "PLACEHOLDER_PREVIEW";

	private CollectSurvey survey;
	private String language;
	private boolean preview;

	private Map<String, CEComponent> componentByName;
	private BalloonInputFieldsUtils balloonInputFieldsUtils;
	private Map<String, String> htmlParameterNameByNodePath;

	public CollectEarthBalloonGenerator(CollectSurvey survey, String language) {
		this(survey, language, false);
	}

	public CollectEarthBalloonGenerator(CollectSurvey survey, String language, boolean preview) {
		this.survey = survey;
		this.language = language;
		this.preview = preview;
		this.componentByName = new HashMap<String, CEComponent>();
		this.balloonInputFieldsUtils = new BalloonInputFieldsUtils();
		this.htmlParameterNameByNodePath = balloonInputFieldsUtils.getHtmlParameterNameByNodePath(getRootEntity());
	}

	public String generateHTML() throws IOException {
		String result = getHTMLTemplate();
		if (preview) {
			result = replaceHostForPreview(result);
		}
		result = addHiddenFields(result);
		result = fillWithSurveyDefinitionFields(result);
		result = replaceButtonLocalizationText(result);
		return result;
	}

	private String replaceHostForPreview(String html) {
		return html.replaceAll("\\$\\[host\\]", "preview_");
	}

	private String replaceButtonLocalizationText(String htmlForBalloon) {
		htmlForBalloon = htmlForBalloon.replace(PLACEHOLDER_FOR_FINISH_TRANSLATION, HtmlUnicodeEscaperUtil.escapeHtmlUnicode( Messages.getString("CollectEarthBalloonGenerator.11", language) ) ); //$NON-NLS-1$
		htmlForBalloon = htmlForBalloon.replace(PLACEHOLDER_FOR_NEXT_TRANSLATION, HtmlUnicodeEscaperUtil.escapeHtmlUnicode(Messages.getString("CollectEarthBalloonGenerator.12", language)) ); //$NON-NLS-1$
		htmlForBalloon = htmlForBalloon.replace(PLACEHOLDER_FOR_PREVIOUS_TRANSLATION,HtmlUnicodeEscaperUtil.escapeHtmlUnicode(Messages.getString("CollectEarthBalloonGenerator.13", language)) ); //$NON-NLS-1$
		
		htmlForBalloon = htmlForBalloon.replace(PLACEHOLDER_COLLECT_NOT_RUNNING,HtmlUnicodeEscaperUtil.escapeHtmlUnicode(Messages.getString("CollectEarthBalloonGenerator.14", language)) ); //$NON-NLS-1$
		htmlForBalloon = htmlForBalloon.replace(PLACEHOLDER_PLACEMARK_ALREADY_FILLED,HtmlUnicodeEscaperUtil.escapeHtmlUnicode(Messages.getString("CollectEarthBalloonGenerator.15", language)) ); //$NON-NLS-1$
		htmlForBalloon = htmlForBalloon.replace(PLACEHOLDER_UI_LANGUAGE, language ); //$NON-NLS-1$
		
		// Added to handle multiple id attributes within a survey
		htmlForBalloon = htmlForBalloon.replace(PLACEHOLDER_EXTRA_ID_ATTRIBUTES,  getIdAttributesSurvey() ); //$NON-NLS-1$
		
		// Added to handle multiple id attributes within a survey
		htmlForBalloon = htmlForBalloon.replace(PLACEHOLDER_FOR_EXTRA_ID_GET_REQUEST,  getIdPlaceholdersSurvey() ); //$NON-NLS-1$

		htmlForBalloon = htmlForBalloon.replace(PLACEHOLDER_PREVIEW, String.valueOf(preview).toLowerCase(Locale.ENGLISH));
		
		return htmlForBalloon;
	}

	private String getIdAttributesSurvey() {
		
		String jsArrayKeys = "[";
		List<AttributeDefinition> keyAttributeDefinitions = survey.getSchema().getFirstRootEntityDefinition().getKeyAttributeDefinitions();
		BalloonInputFieldsUtils balloonUtils = new BalloonInputFieldsUtils();
		
		// TODO Fix better in the future, this is very very dirty!!
		for (AttributeDefinition keyAttribute : keyAttributeDefinitions) {
			jsArrayKeys += "'" + balloonUtils.getCollectBalloonParamName( keyAttribute )  + "',";
		}
		//Remove trailing comma
		jsArrayKeys = jsArrayKeys.substring(0, jsArrayKeys.lastIndexOf(","));
		jsArrayKeys += "]";
		return jsArrayKeys;		
		
	}
	
	private String getIdPlaceholdersSurvey() {
		
		List<AttributeDefinition> keyAttributeDefinitions = survey.getSchema().getFirstRootEntityDefinition().getKeyAttributeDefinitions();
		StringBuilder sb = new StringBuilder();
		for (AttributeDefinition def : keyAttributeDefinitions) {
			sb.append(def.getName()).
			append("=")
			.append("$[")
			.append(EXTRA_HIDDEN_PREFIX)
			.append(def.getName())
			.append("]")
			.append("&"); 
		}
		// remove the last & character
		return (String) sb.subSequence(0, sb.length() -1);
	}
	
	

	private String getHTMLTemplate() throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(BALLOON_TEMPLATE_TXT);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, OpenForisIOUtils.UTF_8);
		String template = writer.toString();
		return template;
	}

	private String fillWithSurveyDefinitionFields(String template) {
		CEComponentHTMLFormatter htmlFormatter = new CEComponentHTMLFormatter(language);
		
		CETabSet rootComponent = generateRootComponent();
		
		StringBuilder sb = new StringBuilder();
		
		String dynamicFieldsHtml = htmlFormatter.format(rootComponent);
		sb.append(dynamicFieldsHtml);
		
		String result = template.replace(PLACEHOLDER_FOR_DYNAMIC_FIELDS, sb.toString());
		return result;
	}
	
	private String addHiddenFields(String templateContent) {
		List<AttributeDefinition> nodesFromCSV = getHiddenNodeDefinitions();
		StringBuilder sb = new StringBuilder();
		for (AttributeDefinition def : nodesFromCSV) {
			String name = getHtmlParameterName(def);
			sb.append("<input type=\"hidden\" id=\""); //$NON-NLS-1$
			sb.append(name);
			sb.append("\" name=\""); //$NON-NLS-1$
			sb.append(name);
			sb.append("\" value=\"$["); //$NON-NLS-1$
			sb.append( EXTRA_HIDDEN_PREFIX);
			sb.append(def.getName());
			sb.append("]\""); //$NON-NLS-1$
			sb.append(" class=\""); //$NON-NLS-1$
			sb.append(EXTRA_HIDDEN_FIELD_CLASS);
			sb.append("\""); //$NON-NLS-1$
			sb.append(" />"); //$NON-NLS-1$
			sb.append('\n'); //$NON-NLS-1$
		}
		String result = templateContent.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_CSV_DATA, sb.toString());
		return result;
	}
	
	private List<AttributeDefinition> getHiddenNodeDefinitions() {
		final List<AttributeDefinition> nodesFromCSV = new ArrayList<AttributeDefinition>();
		
		final CollectAnnotations annotations = survey.getAnnotations();
		Schema schema = survey.getSchema();
		schema.traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition definition) {
				if (definition instanceof AttributeDefinition) {
					AttributeDefinition attrDef = (AttributeDefinition) definition;
					if (annotations.isFromCollectEarthCSV(attrDef) && ! annotations.isShowReadOnlyFieldInCollectEarth(attrDef)
						// TODO Fix how to treat surveys with multi-key combinations
						|| 
						attrDef.isKey() && definition.getParentEntityDefinition().isRoot()) {
						nodesFromCSV.add(attrDef);
					}
				}
			}
		});
		return nodesFromCSV;
	}
	
	private CETabSet generateRootComponent() {
		EntityDefinition rootEntityDef = getRootEntity();
		
		UIOptions uiOptions = survey.getUIOptions();
		UIConfiguration uiConfiguration = survey.getUIConfiguration();
		if (uiConfiguration == null) {
			throw new IllegalStateException("Error unmarshalling the survey - no UI configruration!"); //$NON-NLS-1$
		}
		if (uiConfiguration.getFormSets().isEmpty()) {
			//no ui configuration defined
			CETabSet tabSet = new CETabSet("", ""); //$NON-NLS-1$ //$NON-NLS-2$
			CETab tab = new CETab(rootEntityDef.getName(), ""); //$NON-NLS-1$
			for (NodeDefinition childDef : rootEntityDef.getChildDefinitions()) {
				if (! uiOptions.isHidden(childDef)) {
					tab.addChild(createComponent(childDef));
				}
			}
			tabSet.addTab(tab);
			return tabSet;
		} else {
			CETabSet tabSet = new CETabSet("", ""); //$NON-NLS-1$ //$NON-NLS-2$
			UIFormSet formSet = uiConfiguration.getMainFormSet();
			for (UIForm form : formSet.getForms()) {
				boolean main = tabSet.getTabs().isEmpty();
				CETab tab = createTabComponent(rootEntityDef, form, main);
				tabSet.addTab(tab);
			}
			return tabSet;
		}
	}

	private CETab createTabComponent(EntityDefinition rootEntityDef, UIForm form, boolean main) {
		final CollectAnnotations annotations = survey.getAnnotations();
		String label = form.getLabel(language, survey.getDefaultLanguage());
		CETab tab = new CETab(rootEntityDef.getName(), label);
		tab.setMain(main); //consider the first tab as the main one
		for (UIFormComponent formComponent : form.getChildren()) {
			if (formComponent instanceof NodeDefinitionUIComponent) {
				NodeDefinition nodeDef = ((NodeDefinitionUIComponent) formComponent).getNodeDefinition();
				if (formComponent instanceof UIField) {
					AttributeDefinition attrDef = ((UIField) formComponent).getAttributeDefinition();
					String nodeName = nodeDef.getName();
					boolean includeInHTML = ! (
							HIDDEN_ATTRIBUTE_NAMES.contains(nodeName) 
							|| (annotations.isFromCollectEarthCSV(attrDef) && ! annotations.isShowReadOnlyFieldInCollectEarth(attrDef))
							|| ((UIField) formComponent).isHidden()
							|| ((UIField) formComponent).getAttributeDefinition().isKey()
					);
					
					boolean includeAsAncillaryData = annotations.isIncludedInCollectEarthHeader((AttributeDefinition) nodeDef) ;
					
					if (includeInHTML) {
						CEComponent component = createComponent(nodeDef);
						tab.addChild(component);
					}else if ( includeAsAncillaryData){
						CEAncillaryFields ancillaryDataHeader = tab.getAncillaryDataHeader();
						if( ancillaryDataHeader == null ){
							ancillaryDataHeader = new CEAncillaryFields("ancillary_data", "Ancillary data");
							tab.setAncillaryDataHeader(ancillaryDataHeader);
						}
						CEComponent component = createComponent(nodeDef);
						ancillaryDataHeader.addChild( component );
					}
					
				} else if (formComponent instanceof UITable || formComponent instanceof UIFormSection) {
					CEComponent component = createComponent(nodeDef);
					tab.addChild(component);
				} else {
					throw new IllegalArgumentException("Form component not supported: " + formComponent.getClass().getName()); //$NON-NLS-1$
				}
			}
		}
		return tab;
	}

	private EntityDefinition getRootEntity() {
		return survey.getSchema().getFirstRootEntityDefinition();
	}
	
	private CEComponent createComponent(NodeDefinition def) {
		return createComponent(def, 1);
	}
	
	private CEComponent createComponent(NodeDefinition def, int entityPosition) {
		String label = def.getLabel(Type.INSTANCE, language);
		if (label == null && ! isDefaultLanguage()) {
			label = def.getLabel(Type.INSTANCE);
		}
		if (label == null) {
			label = def.getName();
		}
		
		boolean multiple = def.isMultiple();
		UIOptions uiOptions = survey.getUIOptions();
		boolean hideWhenNotRelevant = uiOptions.isHideWhenNotRelevant(def);
		CEComponent comp;
		if (def instanceof EntityDefinition) {
			if (def.isMultiple() && ((EntityDefinition) def).isEnumerable()) {
				comp = createEnumeratedEntityComponent((EntityDefinition) def);
			} else {
				String tooltip = def.getDescription(language);
				CEFieldSet fieldSet = new CEFieldSet(def.getName(), label, tooltip);
				for (NodeDefinition child : ((EntityDefinition) def).getChildDefinitions()) {
					if (! uiOptions.isHidden(child)) {
						fieldSet.addChild(createComponent(child));
					}
				}
				comp = fieldSet;
			}
		} else {
			AttributeDefinition attrDef = (AttributeDefinition) def;
			String htmlParameterName;
			boolean insideEnumeratedEntity = def.getParentEntityDefinition().isEnumerable();
			if (insideEnumeratedEntity) {
				htmlParameterName = getEnumeratedEntityComponentHtmlParameterName(def.getParentEntityDefinition(), entityPosition, def);
			} else {
				htmlParameterName = getHtmlParameterName(def);
			}
			
			String tooltip = attrDef.getDescription(language);
			
			CEFieldType type = getFieldType(def);
			boolean key = def instanceof KeyAttributeDefinition ? ((KeyAttributeDefinition) def).isKey(): false;
			if (insideEnumeratedEntity && key) {
				comp = new CEEnumeratingCodeField(htmlParameterName, def.getName(), label, tooltip, multiple, type, key);
			} else if (def instanceof CodeAttributeDefinition) {
				CodeAttributeDefinition codeAttrDef = (CodeAttributeDefinition) def;
				CodeList list = codeAttrDef.getList();
				Integer listLevelIndex = codeAttrDef.getListLevelIndex();
				Map<Integer, List<CodeListItem>> codeItemsByParentCodeItemId = getCodeListItemsByParentId(list, listLevelIndex);
				CodeAttributeDefinition parentCodeAttributeDef = codeAttrDef.getParentCodeAttributeDefinition();
				String parentName = parentCodeAttributeDef == null ? null: getHtmlParameterName(parentCodeAttributeDef);
				comp = new CECodeField(htmlParameterName, def.getName(), label, tooltip, type, multiple, key, codeItemsByParentCodeItemId, parentName);
			} else {
				comp = new CEField(htmlParameterName, def.getName(), label, tooltip,  multiple, type, key);
			}
			CollectAnnotations annotations = survey.getAnnotations();
			if (attrDef.isCalculated() || 
					(annotations.isFromCollectEarthCSV(attrDef) && annotations.isShowReadOnlyFieldInCollectEarth(attrDef))) {
				((CEField) comp).setReadOnly(true);
				((CEField) comp).setExtra(true);
			}
		}
		comp.hideWhenNotRelevant = hideWhenNotRelevant;
		componentByName.put(comp.getName(), comp);
		return comp;
	}

	private Map<Integer, List<CodeListItem>> getCodeListItemsByParentId(CodeList list, Integer listLevelIndex) {
		CodeListService codeListService = list.getSurvey().getContext().getCodeListService();
		Map<Integer, List<CodeListItem>> codeItemsByParentCodeItemId = new HashMap<Integer, List<CodeListItem>>();
		if (listLevelIndex == null || listLevelIndex == 0) {
			List<CodeListItem> rootCodeItems = codeListService.loadRootItems(list);
			codeItemsByParentCodeItemId.put(0, rootCodeItems); //root items
		} else {
			int listLevelPosition = listLevelIndex + 1;
			List<CodeListItem> parentLevelItems; 
			if (listLevelPosition == 2) {
				parentLevelItems = codeListService.loadRootItems(list);
			} else {
				parentLevelItems = codeListService.loadItems(list, listLevelPosition - 1);
			}
			for (CodeListItem parentItem : parentLevelItems) {
				List<CodeListItem> childItems = codeListService.loadChildItems(parentItem);
				if (! childItems.isEmpty()) {
					codeItemsByParentCodeItemId.put(parentItem.getId(), childItems);
				}
			}
		}
		return codeItemsByParentCodeItemId;
	}

	private CEComponent createEnumeratedEntityComponent(EntityDefinition def) {
		String label = def.getLabel(Type.INSTANCE, language);
		if (label == null && ! isDefaultLanguage()) {
			label = def.getLabel(Type.INSTANCE);
		}
		if (label == null) {
			label = def.getName();
		}
		UIOptions uiOptions = survey.getUIOptions();
		
		String tableTooltip = def.getDescription(language);
		CEEnumeratedEntityTable ceTable = new CEEnumeratedEntityTable(def.getName(), label, tableTooltip);
		for (NodeDefinition child : def.getChildDefinitions()) {
			if (! uiOptions.isHidden(child)) {
				String heading = child.getLabel(Type.INSTANCE, language);
				if (heading == null && ! isDefaultLanguage()) {
					heading = child.getLabel(Type.INSTANCE);
				}
				if (heading == null) {
					heading = child.getName();
				}
				ceTable.addHeading(heading);
			}
		}
		
		CodeAttributeDefinition enumeratingCodeAttribute = def.getEnumeratingKeyCodeAttribute();
		CodeListService codeListService = def.getSurvey().getContext().getCodeListService();
		List<CodeListItem> codeItems = codeListService.loadRootItems(enumeratingCodeAttribute.getList());
		int codeItemIdx = 0;
		for (CodeListItem item : codeItems) {
			String key = item.getCode();
			String itemLabel = CEComponentHTMLFormatter.getItemLabel(item, language);
			String tooltip = CEComponentHTMLFormatter.getDescription(item, language);
			
			CETableRow row = new CETableRow(key, itemLabel, tooltip);
			for (NodeDefinition child : def.getChildDefinitions()) {
				if (! uiOptions.isHidden(child)) {
					row.addChild(createComponent(child, codeItemIdx + 1));
				}
			}
			ceTable.addRow(row);
			codeItemIdx ++;
		}
		return ceTable;
	}

	private String getHtmlParameterName(NodeDefinition def) {
		return htmlParameterNameByNodePath.get(def.getPath());
	}
	
	private String getEnumeratedEntityComponentHtmlParameterName(EntityDefinition entityDef, int entityPosition, NodeDefinition childDef) {
		String nodePath = entityDef.getPath() + "[" + entityPosition + "]/" + childDef.getName(); //$NON-NLS-1$ //$NON-NLS-2$
		return htmlParameterNameByNodePath.get(nodePath);
	}
	
	private CEFieldType getFieldType(NodeDefinition def) {
		if (def instanceof BooleanAttributeDefinition) {
			return CEFieldType.BOOLEAN;
		} else if (def instanceof CodeAttributeDefinition) {
			UIOptions uiOptions = ((CollectSurvey) def.getSurvey()).getUIOptions();
			CodeAttributeLayoutType layoutType = uiOptions.getLayoutType((CodeAttributeDefinition) def);
			switch (layoutType) {
			case DROPDOWN:
				return CEFieldType.CODE_SELECT;
			default:
				return CEFieldType.CODE_BUTTON_GROUP;
			}
		} else if (def instanceof CoordinateAttributeDefinition) {
			return CEFieldType.COORDINATE;
		} else if (def instanceof DateAttributeDefinition) {
			return CEFieldType.DATE;
		} else if (def instanceof NumberAttributeDefinition) {
			if (((NumericAttributeDefinition) def).getType() == NumericAttributeDefinition.Type.INTEGER) {
				return CEFieldType.INTEGER;
			} else {
				return CEFieldType.REAL;
			}
		} else if (def instanceof TextAttributeDefinition) {
			if (((TextAttributeDefinition) def).getType() == TextAttributeDefinition.Type.SHORT) {
				return CEFieldType.SHORT_TEXT;
			} else {
				return CEFieldType.LONG_TEXT;
			}
		} else if (def instanceof TimeAttributeDefinition) {
			return CEFieldType.TIME;
		} else if (def instanceof RangeAttributeDefinition) {
			if(( (RangeAttributeDefinition ) def).getType().equals(org.openforis.idm.metamodel.NumericAttributeDefinition.Type.INTEGER) ){
				return CEFieldType.CODE_RANGE;
			}else{
				// SLIDER NOT SUPPRTED YET!
				throw new IllegalArgumentException("REAL TYPE RANGES NOT SUPPRTED YET!"); //$NON-NLS-1$
			}
			
		}  else {
			throw new IllegalArgumentException("Attribute type not supported: " + def.getClass().getName()); //$NON-NLS-1$
		}
	}

	private boolean isDefaultLanguage() {
		return language.equals(survey.getDefaultLanguage());
	}

}
