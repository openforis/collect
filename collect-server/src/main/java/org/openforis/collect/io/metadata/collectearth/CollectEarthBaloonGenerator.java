package org.openforis.collect.io.metadata.collectearth;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;

public class CollectEarthBaloonGenerator {
	
	private CollectSurvey survey;

	public CollectEarthBaloonGenerator(CollectSurvey survey) {
		this.survey = survey;
	}

	public String generateHTML() {
		CEComponent rootComponent = generateRootComponent();
		return "";
	}
	
	private CEComponent generateRootComponent() {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDef = schema.getRootEntityDefinitions().get(0);
		CEComponent rootComponent = createComponent(rootEntityDef);
		return rootComponent;
	}
	
	private CEComponent createComponent(NodeDefinition def) {
		String label = def.getLabel(Type.INSTANCE);
		boolean multiple = def.isMultiple();
		if (def instanceof EntityDefinition) {
			CEEntity ceEntity = new CEEntity(def.getName(), label, def.isMultiple());
			List<NodeDefinition> childDefinitions = ((EntityDefinition) def).getChildDefinitions();
			for (NodeDefinition child : childDefinitions) {
				ceEntity.addChild(createComponent(child));
			}
			return ceEntity;
		} else {
			String type = "boolean";
			boolean key = def instanceof KeyAttributeDefinition ? ((KeyAttributeDefinition) def).isKey(): false;
			if (def instanceof CodeAttributeDefinition) {
				CodeListService codeListService = def.getSurvey().getContext().getCodeListService();
				List<CodeListItem> codes = codeListService.loadRootItems(((CodeAttributeDefinition) def).getList());
				CodeAttributeDefinition parentCodeAttributeDef = ((CodeAttributeDefinition) def).getParentCodeAttributeDefinition();
				String parentName = parentCodeAttributeDef == null ? null: parentCodeAttributeDef.getName();
				return new CECodeField(def.getName(), label, type, multiple, key, codes, parentName);
			} else {
				return new CEField(def.getName(), label, multiple, type, key);
			}
		}
	}
	
	private static class CEComponent {

		private String name;
		private String label;
		private boolean multiple;
		
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
		
		private String type;
		private boolean key;
		
		public CEField(String name, String label, boolean multiple, String type, boolean key) {
			super(name, label, multiple);
			this.type = type;
			this.key = key;
		}

		public String getType() {
			return type;
		}

		public boolean isKey() {
			return key;
		}
		
	}
	
	private static class CECodeField extends CEField {
		
		private CodeList listName;
		private List<CodeListItem> codes;
		private String parentName;

		public CECodeField(String name, String label, String type, boolean multiple, boolean key, List<CodeListItem> codes, String parentName) {
			super(name, label, multiple, type, key);
			this.codes = codes;
			this.parentName = parentName;
		}
		
		public List<CodeListItem> getCodes() {
			return codes;
		}
		
		public String getParentName() {
			return parentName;
		}
		
	}

}
