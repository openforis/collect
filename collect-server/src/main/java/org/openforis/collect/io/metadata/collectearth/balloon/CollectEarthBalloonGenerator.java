package org.openforis.collect.io.metadata.collectearth.balloon;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.earth.core.handlers.BalloonInputFieldsUtils;
import org.openforis.collect.io.metadata.collectearth.CollectEarthProjectFileCreator;
import org.openforis.collect.io.metadata.collectearth.balloon.CEField.CEFieldType;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.CodeAttributeLayoutType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
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
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.NodePathPointer;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
public class CollectEarthBalloonGenerator {
	
	private static final String BALLOON_TEMPLATE_TXT = "org/openforis/collect/designer/templates/collect_earth_balloon_template.txt";
	private static final String PLACEHOLDER_FOR_DYNAMIC_FIELDS = "PLACEHOLDER_FOR_DYNAMIC_FIELDS";

	private CollectSurvey survey;
	private Map<String, CEComponent> componentByName;
	
	private BalloonInputFieldsUtils balloonInputFieldsUtils;
	private Map<NodeDefinition, String> htmlParameterNameByNodeDef;

	public CollectEarthBalloonGenerator(CollectSurvey survey) {
		this.survey = survey;
		this.componentByName = new HashMap<String, CEComponent>();
		this.balloonInputFieldsUtils = new BalloonInputFieldsUtils();
		this.htmlParameterNameByNodeDef = balloonInputFieldsUtils.getHtmlParameterNameByNodeDef(getRootEntity());
	}

	public String generateHTML() throws IOException {
		String htmlTemplate = getHTMLTemplate();
		String result = fillWithExtraCSVFields(htmlTemplate);
		result = fillWithSurveyDefinitionFields(result);
		return result;
	}

	private String fillWithSurveyDefinitionFields(String template) {
		CEComponentHTMLBuilder htmlBuilder = new CEComponentHTMLBuilder();
		
		CEEntity rootComponent = generateRootComponent();
		
		List<CEComponent> children = rootComponent.getChildren();
		for (CEComponent child : children) {
			htmlBuilder.append(child);
		}
		String dynamicContent = htmlBuilder.toString();
		String result = template.replace(PLACEHOLDER_FOR_DYNAMIC_FIELDS, dynamicContent);
		return result;
	}

	private String getHTMLTemplate() throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(BALLOON_TEMPLATE_TXT);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		String template = writer.toString();
		return template;
	}

	private String fillWithExtraCSVFields(String templateContent) {
		List<AttributeDefinition> nodesFromCSV = getNodesFromCSV();
		StringBuilder sb = new StringBuilder();
		for (AttributeDefinition def : nodesFromCSV) {
			sb.append("<input type=\"hidden\" id=\"");
			String name = htmlParameterNameByNodeDef.get(def);
			sb.append(name);
			sb.append("\" name=\"");
			sb.append(name);
			sb.append("\" value=\"$[");
			sb.append(def.getName());
			sb.append("]\" />");
			sb.append('\n');
		}
		String result = templateContent.replace(CollectEarthProjectFileCreator.PLACEHOLDER_FOR_EXTRA_CSV_DATA, sb.toString());
		return result;
	}
	
	private List<AttributeDefinition> getNodesFromCSV() {
		final List<AttributeDefinition> nodesFromCSV = new ArrayList<AttributeDefinition>();
		final CollectAnnotations annotations = survey.getAnnotations();
		Schema schema = survey.getSchema();
		schema.traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition definition) {
				if (definition instanceof AttributeDefinition && 
						annotations.isFromCollectEarthCSV((AttributeDefinition) definition)) {
					nodesFromCSV.add((AttributeDefinition) definition);
				}
			}
		});
		return nodesFromCSV;
	}
	
	private CEEntity generateRootComponent() {
		EntityDefinition rootEntityDef = getRootEntity();
		CEEntity rootComponent = (CEEntity) createComponent(rootEntityDef);
		
		rootEntityDef.traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				Set<NodePathPointer> relevanceSourceDefs = survey.getRelevanceSources(def);
				if (! relevanceSourceDefs.isEmpty()) {
					CEComponent comp = componentByName.get(def.getName());
					for (NodePathPointer relevancePointer : relevanceSourceDefs) {
						NodeDefinition referencedDef = relevancePointer.getReferencedNodeDefinition();
						CEComponent referencedComp = componentByName.get(referencedDef.getName());
						comp.relevanceSources.add(referencedComp);
					}
				}
			}
		});
		
		return rootComponent;
	}

	private EntityDefinition getRootEntity() {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDef = schema.getRootEntityDefinitions().get(0);
		return rootEntityDef;
	}
	
	private CEComponent createComponent(NodeDefinition def) {
		String label = def.getLabel(Type.INSTANCE);
		boolean multiple = def.isMultiple();
		UIOptions uiOptions = survey.getUIOptions();
		boolean hideWhenNotRelevant = uiOptions.isHideWhenNotRelevant(def);
		CEComponent comp;
		if (def instanceof EntityDefinition) {
			CEEntity ceEntity = new CEEntity(def.getName(), label, def.isMultiple());
			List<NodeDefinition> childDefinitions = ((EntityDefinition) def).getChildDefinitions();
			for (NodeDefinition child : childDefinitions) {
				ceEntity.addChild(createComponent(child));
			}
			comp = ceEntity;
		} else {
			String htmlParameterName = htmlParameterNameByNodeDef.get(def);
			CEFieldType type = getFieldType(def);
			boolean key = def instanceof KeyAttributeDefinition ? ((KeyAttributeDefinition) def).isKey(): false;
			if (def instanceof CodeAttributeDefinition) {
				CodeListService codeListService = def.getSurvey().getContext().getCodeListService();
				CodeAttributeDefinition parentCodeAttributeDef = ((CodeAttributeDefinition) def).getParentCodeAttributeDefinition();
				Map<String, List<CodeListItem>> codeItemsByParentCode = new HashMap<String, List<CodeListItem>>();
				List<CodeListItem> rootCodeItems = codeListService.loadRootItems(((CodeAttributeDefinition) def).getList());
				if (parentCodeAttributeDef == null) {
					codeItemsByParentCode.put("", rootCodeItems);
				} else {
					for (CodeListItem rootCodeItem : rootCodeItems) {
						List<CodeListItem> childItems = codeListService.loadChildItems(rootCodeItem);
						codeItemsByParentCode.put(rootCodeItem.getCode(), childItems);
					}
				}
				String parentName = parentCodeAttributeDef == null ? null: parentCodeAttributeDef.getName();
				comp = new CECodeField(htmlParameterName, def.getName(), label, type, multiple, key, codeItemsByParentCode, parentName);
			} else {
				comp = new CEField(htmlParameterName, def.getName(), label, multiple, type, key);
			}
		}
		comp.hideWhenNotRelevant = hideWhenNotRelevant;
		componentByName.put(comp.getName(), comp);
		return comp;
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
		} else {
			throw new IllegalArgumentException("Attribute type not supported: " + def.getClass().getName());
		}
	}

}
