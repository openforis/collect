package org.openforis.collect.manager.validation;

import static org.openforis.collect.metamodel.ui.UIOptions.Layout.TABLE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.io.metadata.collectearth.CSVFileValidationResult;
import org.openforis.collect.io.metadata.collectearth.CSVRowValidationResult;
import org.openforis.collect.io.metadata.collectearth.CollectEarthGridTemplateGenerator;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult.Flag;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.io.OpenForisIOUtils;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionType;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.ComparisonCheck;
import org.openforis.idm.metamodel.validation.CustomCheck;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.PatternCheck;
import org.openforis.idm.metamodel.validation.UniquenessCheck;
import org.openforis.idm.path.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyValidator {

	public static final int MAX_NODE_NAME_LENGTH = 63;
	public static final int MAX_KEY_ATTRIBUTE_DEFINITION_COUNT = 3;
	
	private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
	private static final String XML_XSD_FILE_NAME = "xml.xsd";
	private static final String IDML_XSD_FILE_NAME = "idml3.xsd";
	private static final String IDML_XSD_3_1_5_FILE_NAME = "idml3.1.5.xsd";
	private static final String IDML_UI_XSD_FILE_NAME = "idml3-ui.xsd";

	private static final String[] SURVEY_XSD_3_0_FILE_NAMES = new String[] {
			XML_XSD_FILE_NAME, 
			IDML_XSD_FILE_NAME,
			IDML_UI_XSD_FILE_NAME 
	};
	
	private static final String[] SURVEY_LATEST_VERSION_XSD_FILE_NAMES = new String[] {
		XML_XSD_FILE_NAME, 
		IDML_XSD_3_1_5_FILE_NAME,
		IDML_UI_XSD_FILE_NAME 
	};
	
	private static final String CODE_LIST_PATH_FORMAT = "codeList/%s";
	private static final String SURVEY_FILE_PATH_FORMAT = "survey files / %s";

	private static final int MAX_SHOW_COUNT_IN_RECORD_LIST_ENTITY_COUNT = 5;

	private static final String NESTED_TABLES_MESSAGE_KEY = 
			"survey.validation.error.nested_tables";
	private static final String EMPTY_ENTITY_MESSAGE_KEY = 
			"survey.validation.error.empty_entity";
	private static final String EMPTY_CODE_LIST_MESSAGE_KEY = 
			"survey.validation.error.empty_code_list";
	private static final String UNUSED_CODE_LIST_MESSAGE_KEY = 
			"survey.validation.error.unused_code_list";
	private static final String MAXIMUM_COUNT_IN_RECORD_LIST_ENTITY_DEFINITIONS_EXCEEDED_MESSAGE_KEY = 
			"survey.validation.error.maximum_count_in_record_list_entity_definitions_exceeded";
	private static final String MAXIMUM_KEY_ATTRIBUTE_DEFINITIONS_EXCEEDED_MESSAGE_KEY = 
			"survey.validation.error.maximum_key_attribute_definitions_exceeded";
	private static final String KEY_ATTRIBUTE_NOT_SPECIFIED_MESSAGE_KEY = 
			"survey.validation.error.key_attribute_not_specified";
	private static final String ERROR_ENUMERATING_CODE_LIST_CHANGED_CODE_REMOVED_MESSAGE_KEY = 
			"survey.validation.error.enumerating_code_list_changed.code_removed";
	private static final String CARDINALITY_CHANGED_FROM_MULTIPLE_TO_SINGLE_MESSAGE_KEY = 
			"survey.validation.error.cardinality_changed_from_multiple_to_single";
	private static final String DATA_TYPE_CHANGED_MESSAGE_KEY = 
			"survey.validation.error.data_type_changed";
	private static final String PARENT_CHANGED_MESSAGE_KEY = 
			"survey.validation.error.parent_changed";
	private static final String GENERATOR_EXPRESSION_ERROR_MESSAGE_KEY = 
			"survey.validation.entity.error.generator_expression";
	private static final String SOURCE_NODE_NOT_FOUND_FOR_VIRTUAL_NODE_MESSAGE_KEY = 
			"survey.validation.entity.error.source_node_not_found_for_virtual_node";
	private static final String MISSING_VIRTUAL_NODE = 
			"survey.validation.entity.error.missing_virtual_node";
	private static final String INVALID_VIRTUAL_NODE_TYPE_MESSAGE_KEY = 
			"survey.validation.entity.error.invalid_virtual_node_type";
	private static final String DEFAULT_VALUE_INVALID_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.attribute.default_value.error.invalid_expression";
	private static final String DEFAULT_VALUE_INVALID_VALUE_MESSAGE_KEY = 
			"survey.validation.attribute.default_value.error.invalid_value";
	private static final String DEFAULT_VALUE_INVALID_CONDITION_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.attribute.default_value.error.invalid_condition_expression";
	private static final String INVALID_RELEVANT_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.node.error.invalid_relevant_expression";
	private static final String INVALID_PARENT_ATTRIBUTE_RELATION_MESSAGE_KEY = 
			"survey.validation.attribute.code.invalid_parent_attribute_relation";
	private static final String INVALID_PARENT_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.attribute.code.invalid_parent_expression";
	private static final String INCOMPATIBILITY_WITH_CALC_COORDINATE_ATTRIBUTE =
			"survey.validation.attribute.coordinate.incompatibility_with_calc";
	private static final String INVALID_QUALIFIER_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.attribute.taxon.error.invalid_qualifier_expression";
	private static final String INVALID_REFERENCED_KEY_ATTRIBUTE_MESSAGE_KEY = 
			"survey.validation.attribute.invalid_referenced_key_attribute";
	private static final String KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY = 
			"survey.validation.attribute.key_attribute_cannot_be_multiple";
	private static final String INVALID_TAXONOMY_MESSAGE_KEY = 
			"survey.validation.attribute.taxon.invalid_taxonomy";
	private static final String CHECK_UNIQUENESS_INVALID_UNIQUENESS_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.check.uniqueness.error.invalid_uniqueness_expression";
	private static final String CHECK_PATTERN_INVALID_PATTERN_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.check.pattern.error.invalid_pattern_expression";
	private static final String CHECK_DISTANCE_INVALID_MAX_DISTANCE_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.check.distance.error.invalid_max_distance_expression";
	private static final String CHECK_DISTANCE_INVALID_MIN_DISTANCE_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.check.distance.error.invalid_min_distance_expression";
	private static final String CHECK_DISTANCE_INVALID_DESTINATION_POINT_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.check.distance.error.invalid_destination_point_expression";
	private static final String CHECK_DISTANCE_INVALID_SOURCE_POINT_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.check.distance.error.invalid_source_point_expression";
	private static final String CHECK_CUSTOM_ERROR_INVALID_CUSTOM_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.check.custom.error.error.invalid_custom_expression";
	private static final String CHECK_COMPARISON_INVALID_COMPARISON_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.check.comparison.error.invalid_comparison_expression";
	private static final String CHECK_INVALID_CONDITION_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.check.error.invalid_condition_expression";
	private static final String INVALID_MAX_COUNT_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.node.error.invalid_max_count_expression";
	private static final String INVALID_MIN_COUNT_EXPRESSION_MESSAGE_KEY = 
			"survey.validation.node.error.invalid_min_count_expression";
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private ExpressionValidator expressionValidator;

	/**
	 * Verifies that the published survey is compatible with the new one and that there won't be any data data loss.
	 */
	public SurveyValidationResults validateCompatibilityForPublishing(CollectSurvey publishedSurvey, CollectSurvey newSurvey) {
		return validateCompatibilityForPublishing(publishedSurvey, newSurvey, new ValidationParameters());
	}
	
	public SurveyValidationResults validateCompatibilityForPublishing(CollectSurvey publishedSurvey, CollectSurvey newSurvey, 
			ValidationParameters parameters) {
		SurveyValidationResults results = validate(newSurvey, parameters);
		if ( publishedSurvey != null ) {
			results.addResults(validateChanges(publishedSurvey, newSurvey));
		}
		return results;
	}

	
	/**
	 * Verifies that the published survey is compatible with the one packaged with the data to import
	 */
	public SurveyValidationResults validateCompatibilityForDataImport(CollectSurvey publishedSurvey, CollectSurvey dataToImportSurvey) {
		return validateCompatibilityForDataImport(publishedSurvey, dataToImportSurvey, new ValidationParameters());
	}
	
	public SurveyValidationResults validateCompatibilityForDataImport(CollectSurvey publishedSurvey, CollectSurvey dataToImportSurvey, 
			ValidationParameters parameters) {
		SurveyValidationResults results = validate(dataToImportSurvey, parameters);
		if ( publishedSurvey != null ) {
			results.addResults(validateChanges(dataToImportSurvey, publishedSurvey));
		}
		return results;
	}

	public SurveyValidationResults validate(CollectSurvey survey) {
		return validate(survey, new ValidationParameters());
	}

	public SurveyValidationResults validate(CollectSurvey survey, final ValidationParameters validationParameters) {
		SurveyValidationResults results = new SurveyValidationResults();
		
		results.addResults(validateRootKeyAttributeSpecified(survey));
		results.addResults(validateShowCountInRecordListEntityCount(survey));
		results.addResults(validateSchemaNodes(survey, validationParameters));
		results.addResults(validateCodeLists(survey, validationParameters));
		results.addResults(validateSurveyFiles(survey, validationParameters ));
		
		return results;
	}
	
	private List<SurveyValidationResult> validateRootKeyAttributeSpecified(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		Schema schema = survey.getSchema();
		for (EntityDefinition rootEntityDef : schema.getRootEntityDefinitions()) {
			List<AttributeDefinition> keyAttributeDefinitions = rootEntityDef.getKeyAttributeDefinitions();
			if ( keyAttributeDefinitions.isEmpty() ) {
				SurveyValidationResult validationResult = new SurveyValidationResult(rootEntityDef.getPath(), 
						KEY_ATTRIBUTE_NOT_SPECIFIED_MESSAGE_KEY);
				results.add(validationResult);
			} else if ( keyAttributeDefinitions.size() > MAX_KEY_ATTRIBUTE_DEFINITION_COUNT ) {
				SurveyValidationResult validationResult = new SurveyValidationResult(rootEntityDef.getPath(), 
						MAXIMUM_KEY_ATTRIBUTE_DEFINITIONS_EXCEEDED_MESSAGE_KEY);
				results.add(validationResult);
			}
		}
		return results;
	}
	
	private List<SurveyValidationResult> validateShowCountInRecordListEntityCount(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		for (EntityDefinition rootEntityDef : survey.getSchema().getRootEntityDefinitions()) {
			List<EntityDefinition> countableEntities = survey.getSchema().getCountableEntitiesInRecordList(rootEntityDef);
			if (countableEntities.size() > MAX_SHOW_COUNT_IN_RECORD_LIST_ENTITY_COUNT) {
				SurveyValidationResult validationResult = new SurveyValidationResult(rootEntityDef.getPath(), 
						MAXIMUM_COUNT_IN_RECORD_LIST_ENTITY_DEFINITIONS_EXCEEDED_MESSAGE_KEY);
				results.add(validationResult);
			}
		}
		return results;
	}
	
	private List<SurveyValidationResult> validateCodeLists(CollectSurvey survey, ValidationParameters validationParameters) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		if (!validationParameters.warningsIgnored) {
			for (CodeList list : survey.getCodeLists()) {
				if ( ! survey.isPredefinedCodeList(list) ) {
					if ( ! codeListManager.isInUse(list) ) {
						//unused code list not allowed
						SurveyValidationResult validationResult = new SurveyValidationResult(Flag.WARNING, 
								String.format(CODE_LIST_PATH_FORMAT, list.getName()), UNUSED_CODE_LIST_MESSAGE_KEY);
						results.add(validationResult);
					} else if ( ! list.isExternal() && codeListManager.isEmpty(list) ) {
						//empty code list not allowed
						SurveyValidationResult validationResult = new SurveyValidationResult(Flag.WARNING, 
								String.format(CODE_LIST_PATH_FORMAT, list.getName()), EMPTY_CODE_LIST_MESSAGE_KEY);
						results.add(validationResult);
					}
				}
			}
		}
		return results;
	}

	private List<SurveyValidationResult> validateSurveyFiles(CollectSurvey survey, ValidationParameters validationParameters) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		List<SurveyFile> surveyFileSummaries = surveyManager.loadSurveyFileSummaries(survey);
		for (SurveyFile surveyFile : surveyFileSummaries) {
			if (surveyFile.getType() == SurveyFileType.COLLECT_EARTH_GRID) {
				results.addAll(validateCollectEarthGridFile(survey, surveyFile, validationParameters));
			}
		}
		return results;
	}

	private List<SurveyValidationResult> validateCollectEarthGridFile(CollectSurvey survey, SurveyFile surveyFile, ValidationParameters validationParameters) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		byte[] fileContent = surveyManager.loadSurveyFileContent(surveyFile);
		ByteArrayInputStream is = new ByteArrayInputStream(fileContent);
		File file = OpenForisIOUtils.copyToTempFile(is);
		CSVFileValidationResult fileValidationResult = new CollectEarthGridTemplateGenerator().validate(file, survey, validationParameters);
		if (! fileValidationResult.isSuccessful()) {
			SurveyValidationResult validationResult = null;
			switch(fileValidationResult.getErrorType()) {
			case INVALID_FILE_TYPE:
				validationResult = new SurveyValidationResult(Flag.WARNING, 
						String.format(SURVEY_FILE_PATH_FORMAT, surveyFile.getFilename()), 
						"survey.file.error.invalid_file_type", "CSV (Comma Separated Values)");
				break;
			case INVALID_HEADERS:
				validationResult = new SurveyValidationResult(Flag.WARNING, 
						String.format(SURVEY_FILE_PATH_FORMAT, surveyFile.getFilename()), 
						"survey.file.type.collect_earth_grid.error.invalid_file_structure", 
						fileValidationResult.getExpectedHeaders().toString(), 
						fileValidationResult.getFoundHeaders().toString());
				break;
			case INVALID_VALUES_IN_CSV:
				validationResult = new SurveyValidationResult(Flag.WARNING, 
						String.format(SURVEY_FILE_PATH_FORMAT, surveyFile.getFilename()), 
						"survey.file.error.invalid_content", 
						getRowValidationMessages(fileValidationResult.getRowValidations())
						);
				break;
			case INVALID_NUMBER_OF_COLUMNS:
				validationResult = new SurveyValidationResult(Flag.WARNING, 
						String.format(SURVEY_FILE_PATH_FORMAT, surveyFile.getFilename()), 
						"survey.file.type.collect_earth_grid.error.invalid_file_structure", 
						fileValidationResult.getExpectedHeaders().toString(), 
						fileValidationResult.getFoundHeaders().toString());
				break;
				
			case INVALID_NUMBER_OF_PLOTS_TOO_LARGE:
				validationResult = new SurveyValidationResult(Flag.WARNING, 
						String.format(SURVEY_FILE_PATH_FORMAT, surveyFile.getFilename()), 
						"survey.file.error.error_csv_size", 
						fileValidationResult.getNumberOfRows().toString());
				break;
			case INVALID_NUMBER_OF_PLOTS_WARNING:
				validationResult = new SurveyValidationResult(Flag.WARNING, 
						String.format(SURVEY_FILE_PATH_FORMAT, surveyFile.getFilename()), 
						"survey.file.error.warning_csv_size", 
						fileValidationResult.getNumberOfRows().toString());
				break;
			default:
			}
			if( validationResult != null && (!validationParameters.warningsIgnored || validationResult.getFlag() != Flag.WARNING))
				results.add(validationResult);
		}
		return results;
	}
		
	private String getRowValidationMessages(
			List<CSVRowValidationResult> rowValidations) {
		String message  = "";
		for (CSVRowValidationResult csvRowValidationResult : rowValidations) {
			message += "ROW " + csvRowValidationResult.getRowNumber() + " - "+ csvRowValidationResult.getMessage() + "\n";
		}
		return message;
	}

	public List<SurveyValidationResult> validateChanges(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		List<SurveyValidationResult> partialResults;
		partialResults = validateParentRelationship(oldPublishedSurvey, newSurvey);
		results.addAll(partialResults);
		partialResults = validateMultiplicityNotChanged(oldPublishedSurvey, newSurvey);
		results.addAll(partialResults);
		partialResults = validateDataTypeNotChanged(oldPublishedSurvey, newSurvey);
		results.addAll(partialResults);
		partialResults = validateEnumeratingCodeListsNotChanged(oldPublishedSurvey, newSurvey);
		results.addAll(partialResults);
		return results;
	}
	
	/**
	 * Checks for the existence of empty entities
	 * 
	 * @param survey
	 * @return
	 */
	protected List<SurveyValidationResult> validateSchemaNodes(CollectSurvey survey, final ValidationParameters validationParameters) {
		final List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		Schema schema = survey.getSchema();
		schema.traverse(new NodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition def) {
				results.addAll(validateSchemaNode(def, validationParameters));
			}
		});
		return results;
	}

	private List<SurveyValidationResult> validateSchemaNode(NodeDefinition def, ValidationParameters validationParameters) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		
		if (! validateNodeName(def.getName())) {
			results.add(new SurveyValidationResult(def.getPath(), getInvalidNodeNameMessageKey()));
		}
		if (! validateNodeNameMaxLength(def.getParentEntityDefinition(), def.getName())) {
			results.add(new SurveyValidationResult(def.getPath(), getMaxNodeNameLengthExceededMessageKey(), 
					String.valueOf(generateFullInternalName(def.getParentEntityDefinition(), def.getName()).length()), 
					String.valueOf(MAX_NODE_NAME_LENGTH)));
		}
		
		results.addAll(validateExpressions(def));
		
		if (def instanceof EntityDefinition) {
			results.addAll(validateEntity((EntityDefinition) def, validationParameters));
		} else {
			results.addAll(validateAttribute((AttributeDefinition) def));
		}
		return results;
	}

	protected String getInvalidNodeNameMessageKey() {
		return "global.validation.internal_name.invalid_value";
	}
	
	protected String getMaxNodeNameLengthExceededMessageKey() {
		return "survey.validation.node.name.error.max_length_exceeded";
	}
	
	public boolean validateNodeName(String name) {
		return Survey.INTERNAL_NAME_PATTERN.matcher(name).matches();
	}
	
	/**
	 * Validates an internal node name preventing it to be too long when it's concatenated
	 * with the ancestor single entity names
	 */
	public boolean validateNodeNameMaxLength(EntityDefinition parentEntityDefinition, String name) {
		String fullInternalName = generateFullInternalName(parentEntityDefinition, name);
		return fullInternalName.length() <= MAX_NODE_NAME_LENGTH;
	}

	public String generateFullInternalName(EntityDefinition parentEntityDefinition, String name) {
		String fullInternalName;
		if (parentEntityDefinition == null) {
			fullInternalName = name;
		} else {
			List<EntityDefinition> ancestorEntityDefinitions = parentEntityDefinition.getAncestorEntityDefinitions();
			List<EntityDefinition> ancestorSingleEntities = new ArrayList<EntityDefinition>();
			for (EntityDefinition ancestorEntityDef : ancestorEntityDefinitions) {
				if (ancestorEntityDef.isMultiple()) {
					break;
				} else {
					ancestorSingleEntities.add(ancestorEntityDef);
				}
			}
			List<String> ancestorSingleEntityNames = CollectionUtils.project(ancestorEntityDefinitions, "name");
			fullInternalName = StringUtils.join(ancestorSingleEntityNames, "_") + "_" + name;
		}
		return fullInternalName;
	}
	
	protected List<SurveyValidationResult> validateEntity(EntityDefinition entityDef, ValidationParameters validationParameters) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		List<NodeDefinition> childDefinitions = entityDef.getChildDefinitions();
		if ( childDefinitions.size() == 0 ) {
			//empty entity
			results.add(new SurveyValidationResult(entityDef.getPath(), EMPTY_ENTITY_MESSAGE_KEY));
		}
		if (entityDef.isMultiple()) {
			UIOptions uiOptions = ((CollectSurvey) entityDef.getSurvey()).getUIOptions();
			EntityDefinition parentEntity = entityDef.getParentEntityDefinition();
			if (parentEntity != null && parentEntity.isMultiple()) {
				Layout layout = uiOptions.getLayout(entityDef);
				Layout parentLayout = uiOptions.getLayout(parentEntity);
				if (TABLE == layout && TABLE == parentLayout) {
					results.add(new SurveyValidationResult(entityDef.getPath(), NESTED_TABLES_MESSAGE_KEY));
				}
			}
		}
		if (entityDef.isVirtual()) {
			String generatorExpression = entityDef.getGeneratorExpression();
			String sourceEntityPath = Path.getAbsolutePath(generatorExpression);
			EntityDefinition sourceEntityDef = (EntityDefinition) entityDef.getParentDefinition().getDefinitionByPath(sourceEntityPath);
			for (NodeDefinition sourceChildDef : sourceEntityDef.getChildDefinitions()) {
				boolean skipNode = sourceChildDef instanceof AttributeDefinition && 
						((AttributeDefinition) sourceChildDef).getReferencedAttribute() != null;
				if (! skipNode) {
					if (entityDef.containsChildDefinition(sourceChildDef.getName())) {
						NodeDefinition foundChildDef = entityDef.getChildDefinition(sourceChildDef.getName());
						if (foundChildDef.getClass() != sourceChildDef.getClass()) {
							results.add(new SurveyValidationResult(Flag.ERROR, entityDef.getPath(), 
									INVALID_VIRTUAL_NODE_TYPE_MESSAGE_KEY, foundChildDef.getName()));
						}
					} else if (!validationParameters.isWarningsIgnored()) {
						results.add(new SurveyValidationResult(Flag.WARNING, entityDef.getPath(), 
								MISSING_VIRTUAL_NODE, sourceChildDef.getName()));
					}
				}
			}
			if (!validationParameters.isWarningsIgnored()) {
				for (NodeDefinition virtualChildDef : entityDef.getChildDefinitions()) {
					if (! sourceEntityDef.containsChildDefinition(virtualChildDef.getName())) {
						results.add(new SurveyValidationResult(Flag.WARNING, entityDef.getPath(), 
								SOURCE_NODE_NOT_FOUND_FOR_VIRTUAL_NODE_MESSAGE_KEY, 
								virtualChildDef.getName(), sourceEntityDef.getName()));
					}
				}
			}
		}
		return results;
	}
	
	protected List<SurveyValidationResult> validateAttribute(AttributeDefinition attrDef) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		if (attrDef instanceof CodeAttributeDefinition) {
			addIfNotOk(results, validateCodeAttribute((CodeAttributeDefinition) attrDef));
		} else if (attrDef instanceof CoordinateAttributeDefinition) {
			addIfNotOk(results, validateCoordinateAttribute((CoordinateAttributeDefinition) attrDef));
		} else if (attrDef instanceof TaxonAttributeDefinition) {
			addIfNotOk(results, validateTaxonomy((TaxonAttributeDefinition) attrDef));
		}
		addIfNotOk(results, validateKeyAttribute(attrDef));
		addIfNotOk(results, validateReferencedKeyAttribute(attrDef));
		return results;
	}

	private SurveyValidationResult validateCodeAttribute(CodeAttributeDefinition attrDef) {
		//validate hierarchical level in code list
		try {
			attrDef.getHierarchicalLevel();
			return new SurveyValidationResult();
		} catch (Exception e) {
			return new SurveyValidationResult(attrDef.getPath(), 
					INVALID_PARENT_ATTRIBUTE_RELATION_MESSAGE_KEY);
		}
	}

	private SurveyValidationResult validateCoordinateAttribute(CoordinateAttributeDefinition attrDef) {
		CollectAnnotations annotations = attrDef.<CollectSurvey>getSurvey().getAnnotations();
		return annotations.isIncludeCoordinateAccuracy(attrDef) || annotations.isIncludeCoordinateAltitude(attrDef)
			? new SurveyValidationResult(Flag.WARNING, attrDef.getPath(), INCOMPATIBILITY_WITH_CALC_COORDINATE_ATTRIBUTE)
			: null;
	}
	
	private SurveyValidationResult validateTaxonomy(TaxonAttributeDefinition attrDef) {
		CollectSurvey survey = (CollectSurvey) attrDef.getSurvey();
		boolean surveyIsStored = survey.getId() != null;
		if (surveyIsStored) {
			//validate taxonomies only when survey is stored
			String taxonomyName = attrDef.getTaxonomy();
			CollectTaxonomy taxonomy = findTaxonomy(survey, taxonomyName);
			if (taxonomy == null) {
				return new SurveyValidationResult(attrDef.getPath(), INVALID_TAXONOMY_MESSAGE_KEY, taxonomyName);
			}
		}
		return new SurveyValidationResult();
	}

	protected SurveyValidationResult validateKeyAttribute(KeyAttributeDefinition keyDefn) {
		if ( keyDefn.isKey() && ((NodeDefinition) keyDefn).isMultiple() ) {
			return new SurveyValidationResult(((NodeDefinition) keyDefn).getPath(), 
					KEY_ATTRIBUTE_CANNOT_BE_MULTIPLE_MESSAGE_KEY);
		} else {
			return new SurveyValidationResult();
		}
	}
	
	public SurveyValidationResult validateReferencedKeyAttribute(AttributeDefinition attrDef) {
		return validateReferencedKeyAttribute(attrDef, attrDef.getReferencedAttribute());
	}

	public SurveyValidationResult validateReferencedKeyAttribute(AttributeDefinition attrDef,
			AttributeDefinition referencedAttribute) {
		if (referencedAttribute != null) {
			ReferenceableKeyAttributeHelper referenceableKeyAttributeHelper = new ReferenceableKeyAttributeHelper(attrDef);
			Set<AttributeDefinition> referenceableAttributes = referenceableKeyAttributeHelper.determineReferenceableAttributes();
			if (! referenceableAttributes.contains(referencedAttribute)) {
				return new SurveyValidationResult(attrDef.getPath(), 
						INVALID_REFERENCED_KEY_ATTRIBUTE_MESSAGE_KEY);
			}
		}
		return new SurveyValidationResult();
	}

	private List<SurveyValidationResult> validateExpressions(NodeDefinition node) {
		List<SurveyValidationResult> results = validateGenericNodeExpressions(node);
		if ( node instanceof AttributeDefinition ) {
			List<SurveyValidationResult> attributeValidationResults = validateAttributeExpressions((AttributeDefinition) node);
			results.addAll(attributeValidationResults);
		} else {
			List<SurveyValidationResult> entityValidationResults = validateEntityExpressions((EntityDefinition) node);
			results.addAll(entityValidationResults);
		}
		return results;
	}

	private List<SurveyValidationResult> validateEntityExpressions(EntityDefinition node) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		if (node.isVirtual()) {
			addSchemaPathExpressionValidationResult(results, node, node.getGeneratorExpression(),
					GENERATOR_EXPRESSION_ERROR_MESSAGE_KEY);
		}
		return results;
	}

	private List<SurveyValidationResult> validateAttributeExpressions(AttributeDefinition node) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		if ( node instanceof CodeAttributeDefinition ) {
			addSchemaPathExpressionValidationResult(results, node, ((CodeAttributeDefinition) node).getParentExpression(),
					INVALID_PARENT_EXPRESSION_MESSAGE_KEY);
		} else if ( node instanceof TaxonAttributeDefinition ) {
			List<String> qualifiers = ((TaxonAttributeDefinition) node).getQualifiers();
			if ( qualifiers != null ) {
				for (String expr : qualifiers) {
					addSchemaPathExpressionValidationResult(results, node, expr,
							INVALID_QUALIFIER_EXPRESSION_MESSAGE_KEY);
				}
			}
		}
		List<SurveyValidationResult> defaultValuesResults = validateAttributeDefaults(node);
		results.addAll(defaultValuesResults);
		List<SurveyValidationResult> checkResults = validateChecks(node);
		results.addAll(checkResults);
		return results;
	}

	private List<SurveyValidationResult> validateGenericNodeExpressions(NodeDefinition node) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();

		// validate min count expression
		addValueExpressionValidationResult(results, node, node.getMinCountExpression(), 
				INVALID_MIN_COUNT_EXPRESSION_MESSAGE_KEY);
		
		// validate max count expression
		addValueExpressionValidationResult(results, node, node.getMaxCountExpression(), 
				INVALID_MAX_COUNT_EXPRESSION_MESSAGE_KEY);
		
		//validate required expression
		addBooleanExpressionValidationResult(results, node, node.getRelevantExpression(), 
				INVALID_RELEVANT_EXPRESSION_MESSAGE_KEY);
		return results;
	}

	protected List<SurveyValidationResult> validateChecks(AttributeDefinition node) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		List<Check<?>> checks = node.getChecks();
		for (Check<?> check : checks) {
			List<SurveyValidationResult> checkValidationResults = validateCheck(node, check);
			results.addAll(checkValidationResults);
		}
		return results;
	}
	
	protected List<SurveyValidationResult> validateAttributeDefaults(AttributeDefinition node) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		List<AttributeDefault> attributeDefaults = node.getAttributeDefaults();
		for (AttributeDefault attributeDefault : attributeDefaults) {
			validateAttributeDefault(results, node, attributeDefault);
		}
		return results;
	}

	private void validateAttributeDefault(List<SurveyValidationResult> results,
			AttributeDefinition node, AttributeDefault attributeDefault) {
		addBooleanExpressionValidationResult(results, node, attributeDefault.getCondition(), 
				DEFAULT_VALUE_INVALID_CONDITION_EXPRESSION_MESSAGE_KEY);
		String value = attributeDefault.getValue();
		if ( StringUtils.isNotBlank(value)) {
			try {
				node.createValue(value);
			} catch ( Exception e) {
				results.add(new SurveyValidationResult(node.getPath(), 
					DEFAULT_VALUE_INVALID_VALUE_MESSAGE_KEY));
			}
		}
		addValueExpressionValidationResult(results, node, attributeDefault.getExpression(), 
				DEFAULT_VALUE_INVALID_EXPRESSION_MESSAGE_KEY);
	}

	private List<SurveyValidationResult> validateCheck(AttributeDefinition node, Check<?> check) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();

		//validate condition expression
		addBooleanExpressionValidationResult(results, node, check.getCondition(), 
				CHECK_INVALID_CONDITION_EXPRESSION_MESSAGE_KEY);
		
		if ( check instanceof ComparisonCheck ) {
			addBooleanExpressionValidationResult(results, node, ((ComparisonCheck) check).getExpression(),
					CHECK_COMPARISON_INVALID_COMPARISON_EXPRESSION_MESSAGE_KEY);
		} else if ( check instanceof CustomCheck ) {
			addBooleanExpressionValidationResult(results, node, ((CustomCheck) check).getExpression(),
					CHECK_CUSTOM_ERROR_INVALID_CUSTOM_EXPRESSION_MESSAGE_KEY);
		} else if ( check instanceof DistanceCheck ) {
			//validate source point
			addValueExpressionValidationResult(results, node, ((DistanceCheck) check).getSourcePointExpression(), 
					CHECK_DISTANCE_INVALID_SOURCE_POINT_EXPRESSION_MESSAGE_KEY);
			//validate destination point
			addValueExpressionValidationResult(results, node, ((DistanceCheck) check).getDestinationPointExpression(), 
					CHECK_DISTANCE_INVALID_DESTINATION_POINT_EXPRESSION_MESSAGE_KEY);
			//validate min distance
			addValueExpressionValidationResult(results, node, ((DistanceCheck) check).getMinDistanceExpression(),
					CHECK_DISTANCE_INVALID_MIN_DISTANCE_EXPRESSION_MESSAGE_KEY);
			//validate max distance
			addValueExpressionValidationResult(results, node, ((DistanceCheck) check).getMaxDistanceExpression(),
					CHECK_DISTANCE_INVALID_MAX_DISTANCE_EXPRESSION_MESSAGE_KEY);
		} else if ( check instanceof PatternCheck ) {
			String regEx = ((PatternCheck) check).getRegularExpression();
			if ( StringUtils.isNotBlank(regEx) ) {
				ExpressionValidationResult result = expressionValidator.validateRegularExpression(regEx);
				if (result.isError()) {
					results.add(new SurveyValidationResult(node.getPath(), CHECK_PATTERN_INVALID_PATTERN_EXPRESSION_MESSAGE_KEY, result.getMessage()));
				}
			}
		} else if ( check instanceof UniquenessCheck ) {
			String expression = ((UniquenessCheck) check).getExpression();
			if ( StringUtils.isNotBlank(expression) ) {
				ExpressionValidationResult result = expressionValidator.validateUniquenessExpression(node.getParentEntityDefinition(), node, expression);
				if (result.isError()) {
					results.add(new SurveyValidationResult(node.getPath(), CHECK_UNIQUENESS_INVALID_UNIQUENESS_EXPRESSION_MESSAGE_KEY, result.getMessage()));
				}
			}
		}
		return results;
	}

	private void addBooleanExpressionValidationResult(List<SurveyValidationResult> results, NodeDefinition node,
			String expression, String messageKey) {
		addExpressionValidationResult(results, node, ExpressionType.BOOLEAN, expression, messageKey);
	}

	private void addValueExpressionValidationResult(List<SurveyValidationResult> results, NodeDefinition node,
			String expression, String messageKey) {
		addExpressionValidationResult(results, node, ExpressionType.VALUE, expression, messageKey);
	}

	private void addSchemaPathExpressionValidationResult(List<SurveyValidationResult> results, NodeDefinition node,
			String expression, String messageKey) {
		addExpressionValidationResult(results, node, ExpressionType.SCHEMA_PATH, expression, messageKey);
	}

	private void addExpressionValidationResult(
			List<SurveyValidationResult> results, NodeDefinition node,
			ExpressionType type, String expression, String messageKey) {
		if ( StringUtils.isNotBlank(expression) ) {
			ExpressionValidationResult result = expressionValidator.validateExpression(type, node.getParentDefinition(), node, expression);
			if (result.isError()) {
				results.add(new SurveyValidationResult(node.getPath(), messageKey, result.getMessage()));
			} else {
				result = expressionValidator.validateCircularReferenceAbsence(node, expression);
				if (result.isError()) {
					results.add(new SurveyValidationResult(node.getPath(), messageKey, result.getMessage()));
				}
			}
		}
	}
	
	protected List<SurveyValidationResult> validateParentRelationship(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) {
		final Schema oldSchema = oldPublishedSurvey.getSchema();
		SurveyValidationNodeDefinitionVisitor visitor = new SurveyValidationNodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition nodeDefn) {
				NodeDefinition oldDefn = oldSchema.getDefinitionById(nodeDefn.getId());
				if ( oldDefn != null ) {
					NodeDefinition parentDefn = nodeDefn.getParentDefinition();
					NodeDefinition oldParentDefn = oldDefn.getParentDefinition();
					int parentDefnId = parentDefn == null ? -1: parentDefn.getId();
					int oldParentDefnId = oldParentDefn == null ? -1: oldParentDefn.getId();
					if ( parentDefnId != oldParentDefnId ) {
						SurveyValidationResult validationResult = new SurveyValidationResult(nodeDefn.getPath(), PARENT_CHANGED_MESSAGE_KEY);
						addResult(validationResult);
					}
				}
			}
		};
		visitNodeDefinitions(newSurvey, visitor);
		return visitor.getResults();
	}
	
	protected List<SurveyValidationResult> validateDataTypeNotChanged(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) {
		final Schema oldSchema = oldPublishedSurvey.getSchema();
		SurveyValidationNodeDefinitionVisitor visitor = new SurveyValidationNodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition nodeDefn) {
				NodeDefinition oldDefn = oldSchema.getDefinitionById(nodeDefn.getId());
				if ( oldDefn != null && 
					(oldDefn.getClass() != nodeDefn.getClass() || 
					oldDefn instanceof NumericAttributeDefinition && 
						((NumericAttributeDefinition) oldDefn).getType() != ((NumericAttributeDefinition) nodeDefn).getType())) {
					SurveyValidationResult result = new SurveyValidationResult(nodeDefn.getPath(), DATA_TYPE_CHANGED_MESSAGE_KEY);
					addResult(result);
				}
			}
		};
		visitNodeDefinitions(newSurvey, visitor);
		return visitor.getResults();
	}

	protected List<SurveyValidationResult> validateMultiplicityNotChanged(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) {
		final Schema oldSchema = oldPublishedSurvey.getSchema();
		SurveyValidationNodeDefinitionVisitor visitor = new SurveyValidationNodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition nodeDefn) {
				NodeDefinition oldDefn = oldSchema.getDefinitionById(nodeDefn.getId());
				if ( oldDefn != null && oldDefn.isMultiple() && ! nodeDefn.isMultiple() ) {
					SurveyValidationResult result = new SurveyValidationResult(
							nodeDefn.getPath(), CARDINALITY_CHANGED_FROM_MULTIPLE_TO_SINGLE_MESSAGE_KEY);
					addResult(result);
				}
			}
		};
		visitNodeDefinitions(newSurvey, visitor);
		return visitor.getResults();
	}
	
	protected List<SurveyValidationResult> validateEnumeratingCodeListsNotChanged(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		List<CodeList> codeLists = newSurvey.getCodeLists();
		for (CodeList codeList : codeLists) {
			CodeList oldCodeList = oldPublishedSurvey.getCodeListById(codeList.getId());
			if ( oldCodeList != null && oldCodeList.isEnumeratingList() ) {
				results.addAll(validateEnumeratingCodeListNotChanged(oldCodeList, codeList));
			}
		}
		return results;
	}
	
	protected List<SurveyValidationResult> validateEnumeratingCodeListNotChanged(CodeList oldCodeList,
			CodeList codeList) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		List<CodeListItem> oldItems = codeListManager.loadRootItems(oldCodeList);
		for (CodeListItem oldItem : oldItems) {
			CodeListItem newItem = codeListManager.loadRootItem(codeList, oldItem.getCode(), null);
			if ( newItem == null ) {
				String messageKey = ERROR_ENUMERATING_CODE_LIST_CHANGED_CODE_REMOVED_MESSAGE_KEY;
				String codeListPath = String.format(CODE_LIST_PATH_FORMAT, codeList.getName());
				String path = codeListPath + "/" + oldItem.getCode();
				SurveyValidationResult validationError = new SurveyValidationResult(path, messageKey);
				results.add(validationError);
			}
		}
		return results;
	}

	protected void visitNodeDefinitions(CollectSurvey survey, NodeDefinitionVisitor nodeDefnVisitor) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefns = schema.getRootEntityDefinitions();
		for (EntityDefinition entityDefn : rootEntityDefns) {
			entityDefn.traverse(nodeDefnVisitor);
		}
	}
	
	public void validateAgainstSchema(File file) throws SurveyValidationException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			validateAgainstSchema(is);
		} catch (IOException e) {
			throw new RuntimeException("Error validating the survey (creation of temp file): " + e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	public void validateAgainstSchema(InputStream is) throws SurveyValidationException {
		validateAgainstSchema(is, Collect.VERSION);
	}
	
	public void validateAgainstSchema(InputStream is, Version version) throws SurveyValidationException {
	    try {
	    	SchemaFactory factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
	    	String[] schemaFileNames = getSchemaFileNames(version);
	    	Source[] schemas = getSourcesFromClassPath(schemaFileNames);
	    	javax.xml.validation.Schema schema = factory.newSchema(schemas);
	        Validator validator = schema.newValidator();
	        validator.validate(new StreamSource(is));
	    } catch(SAXException e) {
	        throw new SurveyValidationException(e);
	    } catch (IOException e) {
	    	throw new SurveyValidationException(e.getMessage(), e);
		}
	}
	
	private String[] getSchemaFileNames(Version version) {
		if ( version.compareTo(new Version("3.1")) >= 0 ) {
			return SURVEY_LATEST_VERSION_XSD_FILE_NAMES;
		} else {
			return SURVEY_XSD_3_0_FILE_NAMES;
		}
	}

	private Source[] getSourcesFromClassPath(String... sources) throws IOException {
		Source[] result = new Source[sources.length];
		for (int i = 0; i < sources.length; i++) {
			String sourceName = sources[i];
			InputStream is = getClass().getClassLoader().getResourceAsStream(sourceName);
			StreamSource streamSource = new StreamSource(is);
			result[i] = streamSource;
		}
		return result;
	}

	private CollectTaxonomy findTaxonomy(CollectSurvey survey, String taxonomyName) {
		List<CollectTaxonomy> taxonomies = speciesManager.loadTaxonomiesBySurvey(survey);
		for (CollectTaxonomy taxonomy : taxonomies) {
			if (taxonomy.getName().equals(taxonomyName)) {
				return taxonomy;
			}
		}
		return null;
	}
	
	private void addIfNotOk(List<SurveyValidationResult> results, SurveyValidationResult result) {
		if (result != null && result.getFlag() != Flag.OK) {
			results.add(result);
		}
	}

	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public void setExpressionValidator(ExpressionValidator expressionValidator) {
		this.expressionValidator = expressionValidator;
	}

	public static class SurveyValidationResults implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private List<SurveyValidationResult> results;
		private List<SurveyValidationResult> errors;
		private List<SurveyValidationResult> warnings;
		
		public SurveyValidationResults() {
			results = new ArrayList<SurveyValidator.SurveyValidationResult>();
			errors = new ArrayList<SurveyValidator.SurveyValidationResult>();
			warnings = new ArrayList<SurveyValidator.SurveyValidationResult>();
		}
		
		public void addResults(Collection<SurveyValidationResult> results) {
			for (SurveyValidationResult result : results) {
				addResult(result);
			}
		}

		public List<SurveyValidationResult> getErrors() {
			return CollectionUtils.unmodifiableList(errors);
		}
		
		public List<SurveyValidationResult> getWarnings() {
			return CollectionUtils.unmodifiableList(warnings);
		}

		public boolean hasErrors() {
			return CollectionUtils.isNotEmpty(errors);
		}

		public boolean hasWarnings() {
			return CollectionUtils.isNotEmpty(warnings);
		}

		public boolean isOk() {
			return ! hasErrors() && ! hasWarnings();
		}
		
		public void addResult(SurveyValidationResult result) {
			switch ( result.getFlag() ) {
			case ERROR:
				errors.add(result);
				break;
			case WARNING:
				warnings.add(result);
				break;
			default:
				break;
			}
			results.add(result);
		}

		public List<SurveyValidationResult> getResults() {
			return CollectionUtils.unmodifiableList(results);
		}

	}
	
	public static class SurveyValidationResult implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public enum Flag {
			OK, WARNING, ERROR
		}
		
		private Flag flag;
		private String path;
		private String messageKey;
		private String[] messageArgs;

		public SurveyValidationResult() {
			this.flag = Flag.OK;
		}
		
		public SurveyValidationResult(String path, String messageKey, String... messageArgs) {
			this(Flag.ERROR, path, messageKey, messageArgs);
		}
		
		public SurveyValidationResult(Flag flag, String path, String messageKey, String... messageArgs) {
			super();
			this.flag = flag;
			this.path = path;
			this.messageKey = messageKey;
			this.messageArgs = messageArgs;
		}

		public Flag getFlag() {
			return flag;
		}
		
		public String getPath() {
			return path;
		}

		public String getMessageKey() {
			return messageKey;
		}

		public String[] getMessageArgs() {
			return messageArgs;
		}
		
	}
	
	public static abstract class SurveyValidationNodeDefinitionVisitor implements NodeDefinitionVisitor {
		
		private List<SurveyValidationResult> results;
		
		public void addResult(SurveyValidationResult result) {
			if ( results == null ) {
				results = new ArrayList<SurveyValidationResult>();
			}
			results.add(result);
		}
		
		public List<SurveyValidationResult> getResults() {
			return CollectionUtils.unmodifiableList(results);
		}
		
	}
	
	public static class ReferenceableKeyAttributeHelper {
		
		private final AttributeDefinition attributeDef;

		public ReferenceableKeyAttributeHelper(AttributeDefinition attributeDef) {
			super();
			this.attributeDef = attributeDef;
		}
		
		public Set<AttributeDefinition> determineReferenceableAttributes() {
			final Set<AttributeDefinition> referenceableAttributes = new HashSet<AttributeDefinition>();
			Set<EntityDefinition> referenceableEntityDefinitions = determineReferenceableEntities();
			for (EntityDefinition entityDef : referenceableEntityDefinitions) {
				List<AttributeDefinition> keyDefs = entityDef.getKeyAttributeDefinitions();
				AttributeDefinition keyDef = keyDefs.get(0);
				referenceableAttributes.add(keyDef);
			}
			return referenceableAttributes;
		}

		public Set<EntityDefinition> determineReferenceableEntities() {
			final Set<EntityDefinition> result = new HashSet<EntityDefinition>();
			final Set<EntityDefinition> descendantEntityDefinitions = getDescendantEntityDefinitions();
			attributeDef.getRootEntity().traverse(new NodeDefinitionVisitor() {
				public void visit(NodeDefinition def) {
					if (def instanceof EntityDefinition && ! descendantEntityDefinitions.contains(def)
							&& determineIsReferenceable((EntityDefinition) def) ) {
						result.add((EntityDefinition) def);
					}
				}
			});
			return result;
		}

		public boolean determineIsReferenceable(EntityDefinition def) {
			if (def.isMultiple() && ! ((EntityDefinition) def).isRoot()) {
				List<AttributeDefinition> keyDefs = ((EntityDefinition) def).getKeyAttributeDefinitions();
				if (keyDefs.size() == 1) {
					AttributeDefinition keyDef = keyDefs.get(0);
					if (keyDef.getClass().isAssignableFrom(attributeDef.getClass())) {
						return true;
					}
				}
			}
			return false;
		}
		
		private Set<EntityDefinition> getDescendantEntityDefinitions() {
			final Set<EntityDefinition> result = new HashSet<EntityDefinition>();
			attributeDef.getParentEntityDefinition().traverse(new NodeDefinitionVisitor() {
				public void visit(NodeDefinition def) {
					if (def instanceof EntityDefinition) {
						result.add((EntityDefinition) def);
					}
				}
			});
			return result;
		}
		
	}

	public static class ValidationParameters {
		
		private boolean warningsIgnored = false;
		private boolean validateOnlyFirstLines = true;


		public boolean isValidateOnlyFirstLines() {
			return validateOnlyFirstLines;
		}

		public void setValidateOnlyFirstLines(boolean validateOnlyFirstLines) {
			this.validateOnlyFirstLines = validateOnlyFirstLines;
		}
		
		public boolean isWarningsIgnored() {
			return warningsIgnored;
		}
		
		public void setWarningsIgnored(boolean warningsIgnored) {
			this.warningsIgnored = warningsIgnored;
		}
	}
}
