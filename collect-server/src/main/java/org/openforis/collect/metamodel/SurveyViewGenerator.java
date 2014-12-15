package org.openforis.collect.metamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.model.CollectSurvey;
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
		final SurveyView surveyView = new SurveyView();
		final String langCode = locale.getLanguage();
		final Map<Integer, NodeDefView> viewById = new HashMap<Integer, NodeDefView>();;
		survey.getSchema().traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition def) {
				int id = def.getId();
				String name = def.getName();
				String label = def.getLabel(Type.INSTANCE, langCode);
				NodeDefView view;
				if (def instanceof EntityDefinition) {
					view = new EntityDefView(id, name, label);
				} else {
					view = new AttributeDefView(id, name, label, def.getClass().getSimpleName());
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
	
	public static abstract class NodeDefView {
		
		private int id;
		private String name;
		private String label;
		private String type;
		
		public NodeDefView(int id, String name, String label, String type) {
			super();
			this.id = id;
			this.name = name;
			this.label = label;
			this.type = type;
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

		public String getType() {
			return type;
		}
		
	}
	
	public static class EntityDefView extends NodeDefView {

		private List<NodeDefView> children;
		
		public EntityDefView(int id, String name, String label) {
			super(id, name, label, EntityDefinition.class.getSimpleName());
			children = new ArrayList<NodeDefView>();
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

		public AttributeDefView(int id, String name, String label, String type) {
			super(id, name, label, type);
		}
		
	}
	
	public static class SurveyView {
		
		private List<EntityDefView> rootEntities;
		
		public SurveyView() {
			rootEntities = new ArrayList<EntityDefView>();
		}
		
		public void addRootEntity(EntityDefView rootEntity) {
			rootEntities.add(rootEntity);
		}
		
		public List<EntityDefView> getRootEntities() {
			return rootEntities;
		}
		
	}
}
