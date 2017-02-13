package org.openforis.collect.metamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.designer.metamodel.NodeType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel.Type;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyViewGenerator {

	private Locale locale;
	
	public SurveyViewGenerator(Locale locale) {
		super();
		this.locale = locale;
	}

	public SurveyView generateView(CollectSurvey survey) {
		final SurveyView surveyView = new SurveyView(survey.getId(), survey.getName(), survey.isTemporary(), survey.getTarget());
		final String langCode = locale.getLanguage();
		final Map<Integer, NodeDefView> viewById = new HashMap<Integer, NodeDefView>();;
		survey.getSchema().traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				int id = def.getId();
				String name = def.getName();
				String label = getLabel(def, langCode);
				NodeDefView view;
				if (def instanceof EntityDefinition) {
					view = new EntityDefView(((EntityDefinition) def).isRoot(), id, name, label, def.isMultiple());
				} else {
					AttributeDefinition attrDef = (AttributeDefinition) def;
					view = new AttributeDefView(id, name, label, AttributeType.valueOf(attrDef), attrDef.getFieldNames(),
							attrDef.isKey(), attrDef.isMultiple());
				}
				NodeDefinition parentDef = def.getParentDefinition();
				if (parentDef == null) {
					surveyView.addRootEntity((EntityDefView) view);
				} else {
					EntityDefView parentView = (EntityDefView) viewById.get(parentDef.getId());
					parentView.addChild(view);
				}
				viewById.put(id, view);
			}

		});
		return surveyView;
	}
	
	public List<SurveyView> generateViews(List<CollectSurvey> surveys) {
		List<SurveyView> result = new ArrayList<SurveyView>(surveys.size());
		for (CollectSurvey s : surveys) {
			result.add(generateView(s));
		}
		return result;
	}
	
	private String getLabel(NodeDefinition def, String langCode) {
		String label = def.getLabel(Type.INSTANCE, langCode);
		if (label == null && ! def.getSurvey().isDefaultLanguage(langCode)) {
			label = def.getLabel(Type.INSTANCE);
		}
		return label;
	}

	public static abstract class NodeDefView {
		
		private int id;
		private String name;
		private String label;
		private NodeType type;
		private boolean key;
		private boolean multiple;
		
		public NodeDefView(int id, String name, String label, NodeType type, boolean key, boolean multiple) {
			super();
			this.id = id;
			this.name = name;
			this.label = label;
			this.type = type;
			this.key = key;
			this.multiple = multiple;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public String getLabel() {
			return label;
		}

		public NodeType getType() {
			return type;
		}
		
		public boolean isKey() {
			return key;
		}
		
		public boolean isMultiple() {
			return multiple;
		}
		
	}
	
	public static class EntityDefView extends NodeDefView {

		private List<NodeDefView> children;
		private boolean root;
		
		public EntityDefView(boolean root, int id, String name, String label, 
				boolean multiple) {
			super(id, name, label, NodeType.ENTITY, false, multiple);
			this.root = root;
			children = new ArrayList<NodeDefView>();
		}

		public boolean isRoot() {
			return root;
		}
		
		public List<NodeDefView> getChildren() {
			return children;
		}

		public void addChild(NodeDefView child) {
			children.add(child);
		}
		
		public void setChildren(List<NodeDefView> children) {
			this.children = children;
		}
		
	}
	
	public static class AttributeDefView extends NodeDefView {

		private AttributeType attributeType;
		private List<String> fieldNames;
		
		public AttributeDefView(int id, String name, String label, AttributeType type, 
				List<String> fieldNames, boolean key, boolean multiple) {
			super(id, name, label, NodeType.ATTRIBUTE, key, multiple);
			this.attributeType = type;
			this.fieldNames = fieldNames;
		}
		
		public AttributeType getAttributeType() {
			return attributeType;
		}
		
		public List<String> getFieldNames() {
			return this.fieldNames;
		}
	}
	
	public static class SurveyView {
		
		private Integer id;
		private String name;
		private boolean temporary;
		private SurveyTarget target;
		private List<EntityDefView> rootEntities;
		
		public SurveyView(Integer id, String name, boolean temporary, SurveyTarget target) {
			this.id = id;
			this.name = name;
			this.temporary = temporary;
			this.target = target;
			this.rootEntities = new ArrayList<EntityDefView>();
		}
		
		public Integer getId() {
			return id;
		}
		
		public String getName() {
			return name;
		}
		
		public boolean isTemporary() {
			return temporary;
		}
		
		public void setTemporary(boolean temporary) {
			this.temporary = temporary;
		}
		
		public SurveyTarget getTarget() {
			return target;
		}
		
		public void setTarget(SurveyTarget target) {
			this.target = target;
		}
		
		public void addRootEntity(EntityDefView rootEntity) {
			rootEntities.add(rootEntity);
		}
		
		public List<EntityDefView> getRootEntities() {
			return rootEntities;
		}
		
	}
}
