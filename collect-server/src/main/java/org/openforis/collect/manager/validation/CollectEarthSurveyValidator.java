package org.openforis.collect.manager.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.openforis.collect.designer.metamodel.AttributeTypeUtils;
import org.openforis.collect.designer.metamodel.NodeType;
import org.openforis.collect.earth.app.EarthConstants;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeType;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
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
	
	@Override
	public SurveyValidationResults validate(CollectSurvey survey, ValidationParameters parameters) {
		SurveyValidationResults results = super.validate(survey, parameters);
		results.addResults(validateAllRequiredFieldsDefined(survey));
		return results;
	}
	
	/**
	 * Only single entities or multiple enumerated entities are supported
	 */
	@Override
	protected List<SurveyValidationResult> validateEntity(EntityDefinition def, ValidationParameters validationParameters) {
		List<SurveyValidationResult> results = super.validateEntity(def, validationParameters);
		EntityDefinition rootEntityDef = def.getRootEntity();
		if (def != rootEntityDef) {
			if (def.getAncestorEntityDefinitions().size() > 1) {
				results.add(new SurveyValidationResult(def.getPath(), 
						"survey.validation.collect_earth.nested_entities_not_supported"));
			}
		}
		return results;
	}

	private List<SurveyValidationResult> validateAllRequiredFieldsDefined(CollectSurvey survey) {
		final List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		final EntityDefinition rootEntityDef = getMainRootEntityDefinition(survey);
		for (CollectEarthField field: REQUIRED_FIELDS) {
			String fieldName = field.getName();
			try {
				NodeDefinition foundFieldDef = rootEntityDef.getChildDefinition(fieldName);
				if (! field.getType().isAssignableFrom(foundFieldDef.getClass())) {
					Class<? extends AttributeDefinition> type = field.getType();
					AttributeType expectedAttributeType = AttributeType.valueOf(type);
					String expectedType = AttributeTypeUtils.getLabel(expectedAttributeType);
					
					String foundType;
					if (foundFieldDef instanceof AttributeDefinition) {
						foundType = AttributeTypeUtils.getLabel((AttributeDefinition) foundFieldDef);
					} else {
						foundType = NodeType.ENTITY.getLabel();
					}
					results.add(new SurveyValidationResult(rootEntityDef.getPath() + "/" + fieldName, 
							"survey.validation.collect_earth.unexpected_field_type", expectedType, foundType));
				}
			} catch(Exception e) {
				results.add(new SurveyValidationResult(rootEntityDef.getPath() + "/" + fieldName, 
						"survey.validation.collect_earth.missing_required_field"));
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
		return survey.getSchema().getFirstRootEntityDefinition();
	}

}
