package org.openforis.collect.manager.validation;

import static org.openforis.collect.metamodel.ui.UIOptions.Layout.TABLE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult.Flag;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyValidator {

	private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
	private static final String XML_XSD_FILE_NAME = "xml.xsd";
	private static final String IDML_XSD_FILE_NAME = "idml3.xsd";
	private static final String IDML_XSD_3_1_3_FILE_NAME = "idml3.1.3.xsd";
	private static final String IDML_UI_XSD_FILE_NAME = "idml3-ui.xsd";

	private static final String[] SURVEY_XSD_3_0_FILE_NAMES = new String[] {
			XML_XSD_FILE_NAME, 
			IDML_XSD_FILE_NAME,
			IDML_UI_XSD_FILE_NAME 
	};
	
	private static final String[] SURVEY_LATEST_VERSION_XSD_FILE_NAMES = new String[] {
		XML_XSD_FILE_NAME, 
		IDML_XSD_3_1_3_FILE_NAME,
		IDML_UI_XSD_FILE_NAME 
	};
	
	private static final String CODE_LIST_PATH_FORMAT = "codeList/%s";
	
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private ExpressionValidator expressionValidator;

	/**
	 * Verifies that the survey is compatible with an existing one and that replacing the old one
	 * will not break the inserted data (if any). 
	 * 
	 */
	public SurveyValidationResults validateCompatibility(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) {
		SurveyValidationResults results = validate(newSurvey);
		if ( oldPublishedSurvey != null ) {
			results.addResults(validateChanges(oldPublishedSurvey, newSurvey));
		}
		return results;
	}
	
	public void checkCompatibility(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) throws SurveyValidationException {
		SurveyValidationResults results = validateCompatibility(oldPublishedSurvey, newSurvey);
		if ( results.hasErrors() ) {
			throw new SurveyValidationException("The survey is not compatible with the old published one");
		}
	}
	
	public SurveyValidationResults validate(CollectSurvey survey) {
		SurveyValidationResults results = new SurveyValidationResults();
		
		results.addResults(validateRootKeyAttributeSpecified(survey));
		results.addResults(validateSchemaNodes(survey));
		results.addResults(validateCodeLists(survey));
		return results;
	}
	
	private List<SurveyValidationResult> validateRootKeyAttributeSpecified(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition rootEntity : rootEntityDefinitions) {
			List<AttributeDefinition> keyAttributeDefinitions = rootEntity.getKeyAttributeDefinitions();
			if ( keyAttributeDefinitions.isEmpty() ) {
				SurveyValidationResult validationResult = new SurveyValidationResult(rootEntity.getPath(), 
						"survey.validation.error.key_attribute_not_specified");
				results.add(validationResult);
			} else if ( keyAttributeDefinitions.size() > 3 ) {
				SurveyValidationResult validationResult = new SurveyValidationResult(rootEntity.getPath(), 
						"survey.validation.error.maximum_key_attribute_definitions_exceeded");
				results.add(validationResult);
			}
		}
		return results;
	}
	
	private List<SurveyValidationResult> validateCodeLists(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		for (CodeList list : survey.getCodeLists()) {
			if ( ! survey.isPredefinedCodeList(list) ) {
				if ( ! codeListManager.isInUse(list) ) {
					//unused code list not allowed
					SurveyValidationResult validationResult = new SurveyValidationResult(Flag.WARNING, 
							String.format(CODE_LIST_PATH_FORMAT, list.getName()), "survey.validation.error.unused_code_list");
					results.add(validationResult);
				} else if ( ! list.isExternal() && codeListManager.isEmpty(list) ) {
					//empty code list not allowed
					SurveyValidationResult validationResult = new SurveyValidationResult(Flag.WARNING, 
							String.format(CODE_LIST_PATH_FORMAT, list.getName()), "survey.validation.error.empty_code_list");
					results.add(validationResult);
				}
			}
		}
		return results;
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
	protected List<SurveyValidationResult> validateSchemaNodes(CollectSurvey survey) {
		final List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		Schema schema = survey.getSchema();
		schema.traverse(new NodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition def) {
				results.addAll(validateSchemaNode(def));
			}
		});
		return results;
	}

	private List<SurveyValidationResult> validateSchemaNode(NodeDefinition def) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		
		results.addAll(validateExpressions(def));
		
		if (def instanceof EntityDefinition) {
			results.addAll(validateEntity((EntityDefinition) def));
		} else {
			results.addAll(validateAttribute((AttributeDefinition) def));
		}
		return results;
	}
	
	private List<SurveyValidationResult> validateEntity(EntityDefinition entity) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		List<NodeDefinition> childDefinitions = entity.getChildDefinitions();
		if ( childDefinitions.size() == 0 ) {
			//empty entity
			results.add(new SurveyValidationResult(entity.getPath(), "survey.validation.error.empty_entity"));
		}
		if (entity.isMultiple()) {
			UIOptions uiOptions = ((CollectSurvey) entity.getSurvey()).getUIOptions();
			EntityDefinition parentEntity = entity.getParentEntityDefinition();
			if (parentEntity != null && parentEntity.isMultiple()) {
				Layout layout = uiOptions.getLayout(entity);
				Layout parentLayout = uiOptions.getLayout(parentEntity);
				if (TABLE == layout && TABLE == parentLayout) {
					results.add(new SurveyValidationResult(entity.getPath(), "survey.validation.error.nested_tables"));
				}
			}
		}
		return results;
	}
	
	protected List<SurveyValidationResult> validateAttribute(AttributeDefinition attrDef) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		if (attrDef instanceof TaxonAttributeDefinition) {
			String taxonomyName = ((TaxonAttributeDefinition) attrDef).getTaxonomy();
			CollectTaxonomy taxonomy = findTaxonomy((CollectSurvey) attrDef.getSurvey(), taxonomyName);
			if (taxonomy == null) {
				results.add(new SurveyValidationResult(attrDef.getPath(), "survey.validation.attribute.taxon.invalid_taxonomy", taxonomyName));
			}
		}
		if ( attrDef instanceof KeyAttributeDefinition ) {
			SurveyValidationResult result = validateKeyAttribute((KeyAttributeDefinition) attrDef);
			if ( result != null ) {
				results.add(result);
			}
		}
		return results;
	}

	protected SurveyValidationResult validateKeyAttribute(KeyAttributeDefinition keyDefn) {
		if ( keyDefn.isKey() && ((NodeDefinition) keyDefn).isMultiple() ) {
			return new SurveyValidationResult(((NodeDefinition) keyDefn).getPath(), 
					"survey.validation.attribute.key_attribute_cannot_be_multiple");
		}
		return null;
	}

	private List<SurveyValidationResult> validateExpressions(NodeDefinition node) {
		List<SurveyValidationResult> results = validateGenericNodeExpressions(node);
		if ( node instanceof AttributeDefinition ) {
			List<SurveyValidationResult> attributeValidationResults = validateAttributeExpressions((AttributeDefinition) node);
			results.addAll(attributeValidationResults);
		}
		return results;
	}

	private List<SurveyValidationResult> validateAttributeExpressions(AttributeDefinition node) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		if ( node instanceof CodeAttributeDefinition ) {
			addSchemaPathExpressionValidationResult(results, node, ((CodeAttributeDefinition) node).getParentExpression(),
					"survey.validation.attribute.code.invalid_parent_expression");
		} else if ( node instanceof TaxonAttributeDefinition ) {
			List<String> qualifiers = ((TaxonAttributeDefinition) node).getQualifiers();
			if ( qualifiers != null ) {
				for (String expr : qualifiers) {
					addSchemaPathExpressionValidationResult(results, node, expr,
							"survey.validation.attribute.taxon.error.invalid_qualifier_expression");
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
				"survey.validation.node.error.invalid_min_count_expression");
		
		// validate max count expression
		addValueExpressionValidationResult(results, node, node.getMaxCountExpression(), 
				"survey.validation.node.error.invalid_max_count_expression");
		
		//validate required expression
		addBooleanExpressionValidationResult(results, node, node.getRelevantExpression(), 
				"survey.validation.node.error.invalid_relevant_expression");
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
				"survey.validation.attribute.default_value.error.invalid_condition_expression");
		String value = attributeDefault.getValue();
		if ( StringUtils.isNotBlank(value)) {
			try {
				node.createValue(value);
			} catch ( Exception e) {
				results.add(new SurveyValidationResult(node.getPath(), 
					"survey.validation.attribute.default_value.error.invalid_value"));
			}
		}
		addValueExpressionValidationResult(results, node, attributeDefault.getExpression(), 
				"survey.validation.attribute.default_value.error.invalid_expression");
	}

	private List<SurveyValidationResult> validateCheck(AttributeDefinition node, Check<?> check) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();

		//validate condition expression
		addBooleanExpressionValidationResult(results, node, check.getCondition(), 
				"survey.validation.check.error.invalid_condition_expression");
		
		if ( check instanceof ComparisonCheck ) {
			addBooleanExpressionValidationResult(results, node, ((ComparisonCheck) check).getExpression(),
					"survey.validation.check.comparison.error.invalid_comparison_expression");
		} else if ( check instanceof CustomCheck ) {
			addBooleanExpressionValidationResult(results, node, ((CustomCheck) check).getExpression(),
					"survey.validation.check.custom.error.error.invalid_custom_expression");
		} else if ( check instanceof DistanceCheck ) {
			//validate source point
			addValueExpressionValidationResult(results, node, ((DistanceCheck) check).getSourcePointExpression(), 
					"survey.validation.check.distance.error.invalid_source_point_expression");
			//validate destination point
			addValueExpressionValidationResult(results, node, ((DistanceCheck) check).getDestinationPointExpression(), 
					"survey.validation.check.distance.error.invalid_destination_point_expression");
			//validate min distance
			addBooleanExpressionValidationResult(results, node, ((DistanceCheck) check).getMinDistanceExpression(),
					"survey.validation.check.distance.error.invalid_min_distance_expression");
			//validate max distance
			addBooleanExpressionValidationResult(results, node, ((DistanceCheck) check).getMaxDistanceExpression(),
					"survey.validation.check.distance.error.invalid_max_distance_expression");
		} else if ( check instanceof PatternCheck ) {
			String regEx = ((PatternCheck) check).getRegularExpression();
			if ( StringUtils.isNotBlank(regEx) ) {
				ExpressionValidationResult result = expressionValidator.validateRegularExpression(regEx);
				if (result.isError()) {
					results.add(new SurveyValidationResult(node.getPath(), "survey.validation.check.pattern.error.invalid_pattern_expression", result.getMessage()));
				}
			}
		} else if ( check instanceof UniquenessCheck ) {
			String expression = ((UniquenessCheck) check).getExpression();
			if ( StringUtils.isNotBlank(expression) ) {
				ExpressionValidationResult result = expressionValidator.validateUniquenessExpression(node.getParentEntityDefinition(), node, expression);
				if (result.isError()) {
					results.add(new SurveyValidationResult(node.getPath(), "survey.validation.check.uniqueness.error.invalid_uniqueness_expression", result.getMessage()));
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
						String messageKey = "survey.validation.error.parent_changed";
						String path = nodeDefn.getPath();
						SurveyValidationResult validationResult = new SurveyValidationResult(path, messageKey);
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
					String messageKey = "survey.validation.error.data_type_changed";
					String path = nodeDefn.getPath();
					SurveyValidationResult result = new SurveyValidationResult(path, messageKey);
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
					String messageKey = "survey.validation.error.cardinality_changed_from_multiple_to_single";
					String path = nodeDefn.getPath();
					SurveyValidationResult result = new SurveyValidationResult(path, messageKey);
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
				String messageKey = "survey.validation.error.enumerating_code_list_changed.code_removed";
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
		List<CollectTaxonomy> taxonomies;
		if (survey.isWork()) {
			taxonomies = speciesManager.loadTaxonomiesBySurveyWork(survey.getId());
		} else {
			taxonomies = speciesManager.loadTaxonomiesBySurvey(survey.getId());
		}
		for (CollectTaxonomy taxonomy : taxonomies) {
			if (taxonomy.getName().equals(taxonomyName)) {
				return taxonomy;
			}
		}
		return null;
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
		
		public void addResults(Collection<SurveyValidationResult> reults) {
			for (SurveyValidationResult result : reults) {
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
			return org.apache.commons.collections.CollectionUtils.isNotEmpty(errors);
		}

		public boolean hasWarnings() {
			return org.apache.commons.collections.CollectionUtils.isNotEmpty(warnings);
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
	
	public void setCodeListManager(CodeListManager codeListManager) {
		this.codeListManager = codeListManager;
	}
	
	public void setExpressionValidator(ExpressionValidator expressionValidator) {
		this.expressionValidator = expressionValidator;
	}
	
}
