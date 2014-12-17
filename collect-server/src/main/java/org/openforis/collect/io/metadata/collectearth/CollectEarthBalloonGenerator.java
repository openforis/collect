package org.openforis.collect.io.metadata.collectearth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.CodeAttributeLayoutType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
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
	
	private CollectSurvey survey;
	private Map<String, CEComponent> componentByName;

	public CollectEarthBalloonGenerator(CollectSurvey survey) {
		this.survey = survey;
		this.componentByName = new HashMap<String, CEComponent>();
	}

	public String generateHTML() {
		CEComponent rootComponent = generateRootComponent();
		
		
		
		return "";
	}
	
	private CEComponent generateRootComponent() {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDef = schema.getRootEntityDefinitions().get(0);
		CEComponent rootComponent = createComponent(rootEntityDef);
		
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
			CEField.CEFieldType type = getFieldType(def);
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
				comp = new CECodeField(def.getName(), label, type, multiple, key, codeItemsByParentCode, parentName);
			} else {
				comp = new CEField(def.getName(), label, multiple, type, key);
			}
		}
		comp.hideWhenNotRelevant = hideWhenNotRelevant;
		componentByName.put(comp.getName(), comp);
		return comp;
	}
	
	private CEField.CEFieldType getFieldType(NodeDefinition def) {
		if (def instanceof BooleanAttributeDefinition) {
			return CEField.CEFieldType.BOOLEAN;
		} else if (def instanceof CodeAttributeDefinition) {
			UIOptions uiOptions = ((CollectSurvey) def.getSurvey()).getUIOptions();
			CodeAttributeLayoutType layoutType = uiOptions.getLayoutType((CodeAttributeDefinition) def);
			switch (layoutType) {
			case DROPDOWN:
				return CEField.CEFieldType.CODE_SELECT;
			default:
				return CEField.CEFieldType.CODE_BUTTON_GROUP;
			}
		} else if (def instanceof DateAttributeDefinition) {
			return CEField.CEFieldType.DATE;
		} else if (def instanceof NumberAttributeDefinition) {
			if (((NumericAttributeDefinition) def).getType() == NumericAttributeDefinition.Type.INTEGER) {
				return CEField.CEFieldType.INTEGER;
			} else {
				return CEField.CEFieldType.REAL;
			}
		} else if (def instanceof TextAttributeDefinition) {
			if (((TextAttributeDefinition) def).getType() == TextAttributeDefinition.Type.SHORT) {
				return CEField.CEFieldType.SHORT_TEXT;
			} else {
				return CEField.CEFieldType.LONG_TEXT;
			}
		} else if (def instanceof TimeAttributeDefinition) {
			return CEField.CEFieldType.TIME;
		} else {
			throw new IllegalArgumentException("Attribute type not supported: " + def.getClass().getName());
		}
	}

	private static class CEComponent {

		private String name;
		private String label;
		private boolean multiple;
		private List<CEComponent> relevanceSources = new ArrayList<CEComponent>();
		private boolean hideWhenNotRelevant = false;

		public CEComponent(String name, String label, boolean multiple) {
			super();
			this.name = name;
			this.label = label;
			this.multiple = multiple;
		}

		public String getName() {
			return name;
		}

		public String getLabel() {
			return label;
		}

		public boolean isMultiple() {
			return multiple;
		}
	}
	
	private static class CEEntity extends CEComponent {

		private List<CEComponent> children = new ArrayList<CEComponent>();
		
		public CEEntity(String name, String label, boolean multiple) {
			super(name, label, multiple);
		}
		
		public void addChild(CEComponent child) {
			children.add(child);
		}
		
		public List<CEComponent> getChildren() {
			return children;
		}
		
	}
	
	private static class CEField extends CEComponent {
		
		public enum CEFieldType {
			BOOLEAN, CODE_SELECT, CODE_BUTTON_GROUP, DATE, INTEGER, REAL, SHORT_TEXT, LONG_TEXT, TIME
		}
		
		private CEFieldType type;
		private boolean key;
		
		public CEField(String name, String label, boolean multiple, CEFieldType type, boolean key) {
			super(name, label, multiple);
			this.type = type;
			this.key = key;
		}

		public CEFieldType getType() {
			return type;
		}

		public boolean isKey() {
			return key;
		}
		
	}
	
	private static class CECodeField extends CEField {
		
		private CodeList listName;
		private String parentName;
		private Map<String, List<CodeListItem>> codeItemsByParentCode = new HashMap<String, List<CodeListItem>>();

		public CECodeField(String name, String label, CEFieldType type, boolean multiple, boolean key, Map<String, List<CodeListItem>> codeItemsByParentCode, String parentName) {
			super(name, label, multiple, type, key);
			this.codeItemsByParentCode = codeItemsByParentCode;
			this.parentName = parentName;
		}
		
		public Map<String, List<CodeListItem>> getCodeItemsByParentCode() {
			return codeItemsByParentCode;
		}
		
		public String getParentName() {
			return parentName;
		}
		
	}

}
