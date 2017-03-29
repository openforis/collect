package org.openforis.collect.manager.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.designer.metamodel.NodeType;
import org.openforis.collect.earth.app.EarthConstants;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
public class CollectEarthSurveyValidator extends SurveyValidator {

	private Pattern INVALID_NODE_NAME_PATTERN = Pattern.compile(".*_\\d*$");
	
	//TODO use CollectEarthBalloonGenerator.HIDDEN_ATTRIBUTE_NAMES 
	public static final CollectEarthField[] REQUIRED_FIELDS = new CollectEarthField[] {
		new CollectEarthField("id", TextAttributeDefinition.class),
		new CollectEarthField("operator", TextAttributeDefinition.class),
		new CollectEarthField("location", CoordinateAttributeDefinition.class),
		new CollectEarthField("plot_file", TextAttributeDefinition.class),
		new CollectEarthField("actively_saved", BooleanAttributeDefinition.class),
		new CollectEarthField("actively_saved_on", DateAttributeDefinition.class)
	};
	
	public static final List<String> REQUIRED_FIELD_NAMES = 
			CollectionUtils.project(Arrays.<CollectEarthField>asList(REQUIRED_FIELDS), "name");
	
	private static class CollectEarthField {
		private String name;
		private Class<? extends AttributeDefinition> type;
		
		public CollectEarthField(String name, Class<? extends AttributeDefinition> type) {
			super();
			this.name = name;
			this.type = type;
		}
		
		public String getName() {
			return name;
		}
		
		public Class<? extends AttributeDefinition> getType() {
			return type;
		}
	}
	
	public SurveyValidationResults validate(CollectSurvey survey) {
		final SurveyValidationResults results = super.validate(survey);

		//check all required fields defined
		EntityDefinition rootEntityDef = getMainRootEntityDefinition(survey);
		for (CollectEarthField field: REQUIRED_FIELDS) {
			String fieldName = field.getName();
			try {
				NodeDefinition foundFieldDef = rootEntityDef.getChildDefinition(fieldName);
				if (! field.getType().isAssignableFrom(foundFieldDef.getClass())) {
					Class<? extends AttributeDefinition> type = field.getType();
					AttributeType expectedAttributeType = AttributeType.valueOf(type);
					String expectedType = expectedAttributeType.getLabel();
					
					String foundType;
					if (foundFieldDef instanceof AttributeDefinition) {
						AttributeType foundTypeEnum = AttributeType.valueOf((AttributeDefinition) foundFieldDef);
						foundType = foundTypeEnum.getLabel();
					} else {
						foundType = NodeType.ENTITY.getLabel();
					}
					results.addResult(new SurveyValidationResult(rootEntityDef.getPath() + "/" + fieldName, 
							"survey.validation.collect_earth.unexpected_field_type", expectedType, foundType));
				}
			} catch(Exception e) {
				results.addResult(new SurveyValidationResult(rootEntityDef.getPath() + "/" + fieldName, 
						"survey.validation.collect_earth.missing_required_field"));
			}
		}
		
		//check valid node definitions (not nested multiple entities, only enumerable entities)
		List<NodeDefinition> nextLevelDefs = new ArrayList<NodeDefinition>();
		nextLevelDefs.addAll(rootEntityDef.getChildDefinitions());
		
		for (int currentLevelIndex = 0; currentLevelIndex < 2; currentLevelIndex ++) {
			List<NodeDefinition> currentLevelDefs = nextLevelDefs;
			nextLevelDefs = new ArrayList<NodeDefinition>();
			for (NodeDefinition nodeDef : currentLevelDefs) {
				if (nodeDef instanceof EntityDefinition) {
					EntityDefinition entityDef = (EntityDefinition) nodeDef;
					if (currentLevelIndex == 0) {
						if (nodeDef.isMultiple() && ! entityDef.isEnumerable()) {
							results.addResult(new SurveyValidationResult(entityDef.getPath(), "survey.validation.collect_earth.multiple_entities_not_supported"));
						} else {
							nextLevelDefs.addAll(entityDef.getChildDefinitions());
						}
					} else {
						results.addResult(new SurveyValidationResult(nodeDef.getPath(), "survey.validation.collect_earth.nested_entities_not_supported"));
					}
				}
			}
		}
		return results;
	}

	public boolean validateRootEntityName(String name) {
		return EarthConstants.ROOT_ENTITY_NAME.equals(name);
	}
	
	public boolean validateNodeName(String name) {
		return super.validateNodeName(name) && 
				! INVALID_NODE_NAME_PATTERN.matcher(name).matches();
	}
	
	protected String getInvalidNodeNameMessageKey() {
		return "survey.validation.collect_earth.invalid_node_name";
	}
	
	protected EntityDefinition getMainRootEntityDefinition(CollectSurvey survey) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		EntityDefinition rootEntityDef = rootEntityDefinitions.get(0);
		return rootEntityDef;
	}

}
