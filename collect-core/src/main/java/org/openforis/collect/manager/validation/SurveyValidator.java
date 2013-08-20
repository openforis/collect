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
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyValidator {

	@Autowired
	private CodeListManager codeListManager;

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
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		List<SurveyValidationResult> partialResults = validateEnties(survey);
		results.addAll(partialResults);
//		partialResults = validateExpressions(survey);
//		results.addAll(partialResults);
		return results;
	}
	
	public List<SurveyValidationResult> validateChanges(CollectSurvey oldPublishedSurvey, CollectSurvey newSurvey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
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
	protected List<SurveyValidationResult> validateEnties(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		Schema schema = survey.getSchema();
		Stack<EntityDefinition> entitiesStack = new Stack<EntityDefinition>();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		entitiesStack.addAll(rootEntities);
		while ( ! entitiesStack.isEmpty() ) {
			EntityDefinition entity = entitiesStack.pop();
			List<NodeDefinition> childDefinitions = entity.getChildDefinitions();
			if ( childDefinitions.size() == 0 ) {
				String messageKey = "survey.validation.error.empty_entity";
				String path = entity.getPath();
				SurveyValidationResult validationResult = new SurveyValidationResult(path, messageKey);
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
	
	/*
	protected List<SurveyValidationResult> validateExpressions(CollectSurvey survey) {
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
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
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
		NodeDefinition parentDefn = node.getParentDefinition();
		String path = node.getPath();
		String expression = node.getRelevantExpression();
		if ( StringUtils.isNotBlank(expression) ) {
		}
		if ( node.getMinCount() > 0 ) {
			expression = node.getRequiredExpression();
			if ( StringUtils.isNotBlank(expression) ) {
			}
		}
		if ( node instanceof CodeAttributeDefinition ) {
			CodeAttributeDefinition codeDefn = (CodeAttributeDefinition) node;
			String expr = codeDefn.getParentExpression();
			if ( StringUtils.isNotBlank(expr) && ! surveyManager.validatePathExpression(parentDefn, expr) ) {
				String message = Labels.getLabel("survey.schema.attribute.code.validation.error.invalid_parent_expression");
				SurveyValidationResult surveyValidationResult = new SurveyValidationResult(path, message);
				results.add(surveyValidationResult);
			}
		} else if ( node instanceof TaxonAttributeDefinition ) {
			List<String> qualifiers = ((TaxonAttributeDefinition) node).getQualifiers();
			if ( qualifiers != null ) {
				for (String expr : qualifiers) {
					if ( StringUtils.isNotBlank(expr) && ! surveyManager.validatePathExpression(parentDefn, expr) ) {
						String message = Labels.getLabel("survey.schema.attribute.taxon.validation.error.invalid_qualifier_expression");
						SurveyValidationResult surveyValidationResult = new SurveyValidationResult(path, message);
						results.add(surveyValidationResult);
						break;
					}
				}
			}
		}
		return results;
	}
	*/
	
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
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
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
		List<SurveyValidationResult> results = new ArrayList<SurveyValidator.SurveyValidationResult>();
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
	    	Source[] schemas = getSourcesFromClassPath("xml.xsd", "idml3.xsd", "idml3-ui.xsd");
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
				results = new ArrayList<SurveyValidator.SurveyValidationResult>();
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
	
}
