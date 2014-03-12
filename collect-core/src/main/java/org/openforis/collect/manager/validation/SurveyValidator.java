package org.openforis.collect.manager.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
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

	private static final String XML_XSD_FILE_NAME = "xml.xsd";
	private static final String IDML_XSD_FILE_NAME = "idml3.xsd";
	private static final String IDML_UI_XSD_FILE_NAME = "idml3-ui.xsd";

	private static final String[] SURVEY_XSD_FILE_NAMES = new String[] {
			XML_XSD_FILE_NAME, 
			IDML_XSD_FILE_NAME,
			IDML_UI_XSD_FILE_NAME 
	};
	
	@Autowired
	private CodeListManager codeListManager;
	
	@Autowired
	private ExpressionValidator expressionValidator;

	/**
	 * Verifies that the survey is compatible with an existing one and that replacing the old one
	 * will not break the inserted data (if any). 
	 * 
	 */
	public List<SurveyValidationResult> validateCompatibility(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) {
		List<SurveyValidationResult> results = validate(newSurvey);
		if ( oldPublishedSurvey != null ) {
			results.addAll(validateChanges(oldPublishedSurvey, newSurvey));
		}
		return results;
	}
	
	public void checkCompatibility(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) throws SurveyValidationException {
		List<SurveyValidationResult> result = validateCompatibility(oldPublishedSurvey, newSurvey);
		if ( ! result.isEmpty() ) {
			throw new SurveyValidationException("The survey is not compatible with the old published one");
		}
	}
	
	public List<SurveyValidationResult> validate(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		List<SurveyValidationResult> partialResults;
		//root entity key required
		partialResults = validateRootKeyAttributeSpecified(survey);
		results.addAll(partialResults);
		//empty entities not allowed
		partialResults = validateEntities(survey);
		results.addAll(partialResults);
		//validate expressions
		partialResults = validateExpressions(survey);
		results.addAll(partialResults);
		return results;
	}
	
	private List<SurveyValidationResult> validateRootKeyAttributeSpecified(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition rootEntity : rootEntityDefinitions) {
			List<KeyAttributeDefinition> keyAttributeDefinitions = schema.getKeyAttributeDefinitions(rootEntity);
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
	protected List<SurveyValidationResult> validateEntities(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		Schema schema = survey.getSchema();
		Stack<EntityDefinition> entitiesStack = new Stack<EntityDefinition>();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		entitiesStack.addAll(rootEntities);
		while ( ! entitiesStack.isEmpty() ) {
			EntityDefinition entity = entitiesStack.pop();
			List<NodeDefinition> childDefinitions = entity.getChildDefinitions();
			if ( childDefinitions.size() == 0 ) {
				SurveyValidationResult validationResult = new SurveyValidationResult(entity.getPath(), "survey.validation.error.empty_entity");
				results.add(validationResult);
			} else {
				for (NodeDefinition childDefn : childDefinitions) {
					if ( childDefn instanceof EntityDefinition ) {
						entitiesStack.push((EntityDefinition) childDefn);
					}
				}
			}
		}
		return results;
	}
	
	protected List<SurveyValidationResult> validateExpressions(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidationResult>();
		Schema schema = survey.getSchema();
		Stack<NodeDefinition> nodesStack = new Stack<NodeDefinition>();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		nodesStack.addAll(rootEntities);
		while ( ! nodesStack.isEmpty() ) {
			NodeDefinition node = nodesStack.pop();
			List<SurveyValidationResult> nodeValidationResults = validateExpressions(node);
			if ( ! nodeValidationResults.isEmpty() ) {
				results.addAll(nodeValidationResults);
			}
			if ( node instanceof EntityDefinition ) {
				List<NodeDefinition> childDefns = ((EntityDefinition) node).getChildDefinitions();
				if ( ! childDefns.isEmpty() ) {
					nodesStack.addAll(childDefns);
				}
			}
		}
		return results;
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
		//validate required expression
		addBooleanExpressionValidationResult(results, node, node.getRequiredExpression(), 
				"survey.validation.node.error.invalid_required_expression");
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
			//validate min distance
			addBooleanExpressionValidationResult(results, node, ((DistanceCheck) check).getMinDistanceExpression(),
					"survey.validation.check.distance.error.invalid_min_distance_expression");
			//validate min distance
			addBooleanExpressionValidationResult(results, node, ((DistanceCheck) check).getMaxDistanceExpression(),
					"survey.validation.check.distance.error.invalid_max_distance_expression");
		} else if ( check instanceof PatternCheck ) {
			String regEx = ((PatternCheck) check).getRegularExpression();
			if ( StringUtils.isNotBlank(regEx) && ! expressionValidator.validateRegularExpression(regEx) ) {
				results.add(new SurveyValidationResult(node.getPath(), "survey.validation.check.pattern.error.invalid_pattern_expression"));
			}
		} else if ( check instanceof UniquenessCheck ) {
			String expression = ((UniquenessCheck) check).getExpression();
			if ( StringUtils.isNotBlank(expression) && ! expressionValidator.validateUniquenessExpression(node, expression) ) {
				results.add(new SurveyValidationResult(node.getPath(), "survey.validation.check.uniqueness.error.invalid_uniqueness_expression"));
			}
		}
		return results;
	}

	private void addBooleanExpressionValidationResult(
			List<SurveyValidationResult> results, NodeDefinition node,
			String expression, String messageKey) {
		if ( StringUtils.isNotBlank(expression) && ! expressionValidator.validateBooleanExpression(node, expression) ) {
			results.add(new SurveyValidationResult(node.getPath(), messageKey));
		}
	}

	private void addValueExpressionValidationResult(
			List<SurveyValidationResult> results, NodeDefinition node,
			String expression, String messageKey) {
		if ( StringUtils.isNotBlank(expression) && ! expressionValidator.validateValueExpression(node, expression) ) {
			results.add(new SurveyValidationResult(node.getPath(), messageKey));
		}
	}

	private void addSchemaPathExpressionValidationResult(
			List<SurveyValidationResult> results, NodeDefinition node,
			String expression, String messageKey) {
		if ( StringUtils.isNotBlank(expression) && ! expressionValidator.validateSchemaPathExpression(node, expression) ) {
			results.add(new SurveyValidationResult(node.getPath(), messageKey));
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
						addValidationError(validationResult);
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
					SurveyValidationResult validationError = new SurveyValidationResult(path, messageKey);
					addValidationError(validationError);
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
					SurveyValidationResult validationError = new SurveyValidationResult(path, messageKey);
					addValidationError(validationError);
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
				String path = "codeList" + "/" + codeList.getName() + "/" + oldItem.getCode();
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
	    try {
	    	SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	    	Source[] schemas = getSourcesFromClassPath(SURVEY_XSD_FILE_NAMES);
			javax.xml.validation.Schema schema = factory.newSchema(schemas);
	        Validator validator = schema.newValidator();
	        validator.validate(new StreamSource(is));
	    } catch(SAXException e) {
	        throw new SurveyValidationException(e);
	    } catch (IOException e) {
	    	throw new SurveyValidationException(e.getMessage(), e);
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
	
	public static class SurveyValidationResult implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private String path;
		private String messageKey;

		public SurveyValidationResult(String path, String messageKey) {
			super();
			this.path = path;
			this.messageKey = messageKey;
		}

		public String getPath() {
			return path;
		}

		public String getMessageKey() {
			return messageKey;
		}

	}
	
	public static abstract class SurveyValidationNodeDefinitionVisitor implements NodeDefinitionVisitor {
		
		private List<SurveyValidationResult> results;
		
		public void addValidationError(SurveyValidationResult result) {
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
