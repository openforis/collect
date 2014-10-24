package org.openforis.idm.testfixture;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.*;
import org.openforis.idm.metamodel.validation.CustomCheck;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class NodeDefinitionBuilder {
	
	protected final String name;
	protected boolean multiple;
	protected boolean required;
	private String relevantExpression;
	private String requiredExpression;

	public NodeDefinitionBuilder(String name) {
		this.name = name;
	}
	
	public NodeDefinitionBuilder multiple() {
		this.multiple = true;
		return this;
	}
	
	public NodeDefinitionBuilder required() {
		this.required = true;
		return this;
	}
	
	public NodeDefinitionBuilder relevant(String expression) {
		this.relevantExpression = expression;
		return this;
	}
	
	public NodeDefinitionBuilder required(String expression) {
		this.requiredExpression = expression;
		return this;
	}

	public static EntityDefinition rootEntityDef(Survey survey, String name, NodeDefinitionBuilder... builders) {
		EntityDefinitionBuilder entityBuilder = new EntityDefinitionBuilder(name, builders);
		EntityDefinition rootEntityDef = (EntityDefinition) entityBuilder.buildInternal(survey);
		Schema schema = survey.getSchema();
		if ( schema.getRootEntityDefinition(name) != null ) {
			schema.removeRootEntityDefinition(name);
		}
		schema.addRootEntityDefinition(rootEntityDef);
		survey.refreshSurveyDependencies();
		return rootEntityDef;
	}

	public static EntityDefinitionBuilder rootEntityDef(String name, NodeDefinitionBuilder... builders) {
		return new EntityDefinitionBuilder(name, builders);
	}
	
	public static NodeDefinitionBuilder entityDef(String name, NodeDefinitionBuilder... builders) {
		return new EntityDefinitionBuilder(name, builders);
	}
	
	public static AttributeDefinitionBuilder attributeDef(String name) {
		return new AttributeDefinitionBuilder(name);
	}
	
	protected abstract NodeDefinition buildInternal(Survey survey);
	
	protected void initNodeDefinition(NodeDefinition def) {
		def.setName(name);
		def.setMultiple(multiple);
		if ( requiredExpression == null && required ) {
			def.setMinCount(1);
		} else {
			def.setRequiredExpression(requiredExpression);
		}
		def.setRelevantExpression(relevantExpression);
	}
	
	public static class EntityDefinitionBuilder extends NodeDefinitionBuilder {
		
		private NodeDefinitionBuilder[] builders;

		private EntityDefinitionBuilder(String name, NodeDefinitionBuilder... builders) {
			super(name);
			this.builders = builders;
		}
		
		@Override
		public EntityDefinitionBuilder multiple() {
			return (EntityDefinitionBuilder) super.multiple();
		}
		
		@Override
		public EntityDefinitionBuilder required() {
			return (EntityDefinitionBuilder) super.required();
		}

		@Override
		public EntityDefinitionBuilder relevant(String expression) {
			return (EntityDefinitionBuilder) super.relevant(expression);
		}
		
		@Override
		public EntityDefinitionBuilder required(String expression) {
			return (EntityDefinitionBuilder) super.required(expression);
		}
		
		
		@Override
		protected NodeDefinition buildInternal(Survey survey) {
			EntityDefinition def = survey.getSchema().createEntityDefinition();
			initNodeDefinition(def);
			for (NodeDefinitionBuilder childBuilder : builders) {
				NodeDefinition childDef = childBuilder.buildInternal(survey);
				def.addChildDefinition(childDef);
			}
			return def;
		}

	}
	
	public static class AttributeDefinitionBuilder extends NodeDefinitionBuilder {

		private boolean key;
		private List<String> calculatedExpressions;
		private String validationExpression;

		private AttributeDefinitionBuilder(String name) {
			super(name);
			calculatedExpressions = new ArrayList<String>();
		}
		
		@Override
		public AttributeDefinitionBuilder relevant(String expression) {
			return (AttributeDefinitionBuilder) super.relevant(expression);
		}
		
		@Override
		public AttributeDefinitionBuilder required(String expression) {
			return (AttributeDefinitionBuilder) super.required(expression);
		}
		
		public AttributeDefinitionBuilder calculated(String expression) {
			this.calculatedExpressions.add(expression);
			return this;
		}
		
		public AttributeDefinitionBuilder validate(String expression) {
			this.validationExpression = expression;
			return this;
		}
		
		@Override
		public AttributeDefinitionBuilder multiple() {
			return (AttributeDefinitionBuilder) super.multiple();
		}
		
		public NodeDefinitionBuilder key() {
			this.key = true;
			return this;
		}
		
		@Override
		protected NodeDefinition buildInternal(Survey survey) {
			AttributeDefinition def = survey.getSchema().createTextAttributeDefinition();
			initNodeDefinition(def);
			if ( def instanceof KeyAttributeDefinition) {
				((KeyAttributeDefinition) def).setKey(key);
			}
			def.setCalculated(! calculatedExpressions.isEmpty());
			for (String calculatedExpression : calculatedExpressions) {
				AttributeDefault attributeDefault = new AttributeDefault(calculatedExpression);
				def.addAttributeDefault(attributeDefault);
			}
			if ( validationExpression != null ) {
				def.addCheck(new CustomCheck(validationExpression));
			}
			return def;
		}

	}
	
}