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
	private String minCountExpression;
	private String maxCountExpression;
	private String deprecatedVersion;
	private String sinceVersion;

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

	public NodeDefinitionBuilder minCount(String expression) {
		this.minCountExpression = expression;
		return this;
	}
	
	public NodeDefinitionBuilder maxCount(String expression) {
		this.maxCountExpression = expression;
		return this;
	}
	
	public NodeDefinitionBuilder deprecated(String versionName) {
		this.deprecatedVersion = versionName;
		return this;
	}
	
	public NodeDefinitionBuilder since(String versionName) {
		this.sinceVersion = versionName;
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
	
	public static EntityDefinitionBuilder entityDef(String name, NodeDefinitionBuilder... builders) {
		return new EntityDefinitionBuilder(name, builders);
	}
	
	public static AttributeDefinitionBuilder attributeDef(String name) {
		return new AttributeDefinitionBuilder(name);
	}
	
	protected abstract NodeDefinition buildInternal(Survey survey);
	
	protected void initNodeDefinition(NodeDefinition def) {
		def.setName(name);
		def.setMultiple(multiple);
		if ( requiredExpression != null) {
			def.setRequiredExpression(requiredExpression);
		} else if( required ) {
			def.setAlwaysRequired();
		} else if (minCountExpression != null) {
			def.setMinCountExpression(minCountExpression);
		}
		def.setMaxCountExpression(maxCountExpression);
		def.setRelevantExpression(relevantExpression);
		def.setDeprecatedVersionByName(deprecatedVersion);
		def.setSinceVersionByName(sinceVersion);
	}
	
	public static class EntityDefinitionBuilder extends NodeDefinitionBuilder {
		
		private NodeDefinitionBuilder[] builders;
		private boolean virtual;
		private String generatorExpression;

		EntityDefinitionBuilder(String name, NodeDefinitionBuilder... builders) {
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
		public EntityDefinitionBuilder required(String expression) {
			return (EntityDefinitionBuilder) super.required(expression);
		}
		
		@Override
		public EntityDefinitionBuilder relevant(String expression) {
			return (EntityDefinitionBuilder) super.relevant(expression);
		}
		
		public EntityDefinitionBuilder virtual() {
			this.virtual = true;
			return this;
		}

		public EntityDefinitionBuilder generatorExpression(String generatorExpression) {
			this.generatorExpression = generatorExpression;
			return this;
		}

		@Override
		protected NodeDefinition buildInternal(Survey survey) {
			EntityDefinition def = survey.getSchema().createEntityDefinition();
			initNodeDefinition(def);
			for (NodeDefinitionBuilder childBuilder : builders) {
				NodeDefinition childDef = childBuilder.buildInternal(survey);
				def.addChildDefinition(childDef);
			}
			def.setVirtual(virtual);
			def.setGeneratorExpression(generatorExpression);
			return def;
		}

	}
	
	public static class AttributeDefinitionBuilder extends NodeDefinitionBuilder {

		private boolean key;
		private boolean calculated;
		private List<AttributeDefault> defaultValues;
		private String validationExpression;

		AttributeDefinitionBuilder(String name) {
			super(name);
			defaultValues = new ArrayList<AttributeDefault>();
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
			return calculated(expression, null);
		}
		
		public AttributeDefinitionBuilder calculated(String expression, String condition) {
			this.calculated = true;
			return defaultValue(expression, condition);
		}
		
		public AttributeDefinitionBuilder defaultValue(String expression) {
			return defaultValue(expression, null);
		}
		
		public AttributeDefinitionBuilder defaultValue(String expression, String condition) {
			this.defaultValues.add(new AttributeDefault(expression, condition));
			return this;
		}
		
		public AttributeDefinitionBuilder validate(String expression) {
			this.validationExpression = expression;
			return this;
		}
		
		@Override
		public AttributeDefinitionBuilder deprecated(String versionName) {
			return (AttributeDefinitionBuilder) super.deprecated(versionName);
		}
		
		@Override
		public AttributeDefinitionBuilder since(String versionName) {
			return (AttributeDefinitionBuilder) super.since(versionName);
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
			def.setCalculated(calculated);
			for (AttributeDefault attributeDefault : defaultValues) {
				def.addAttributeDefault(attributeDefault);
			}
			if ( validationExpression != null ) {
				def.addCheck(new CustomCheck(validationExpression));
			}
			return def;
		}

	}
	
}