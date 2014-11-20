package org.openforis.collect.model;

import static org.openforis.collect.model.CollectRecord.APPROVED_MISSING_POSITION;
import static org.openforis.collect.model.CollectRecord.CONFIRMED_ERROR_POSITION;
import static org.openforis.collect.model.CollectRecord.DEFAULT_APPLIED_POSITION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.BooleanAttribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.BooleanExpression;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordUpdater {
	
	/**
	 * Updates an attribute with a new value
	 * 
	 * @param attribute
	 * @param value
	 * @return
	 */
	public <V extends Value> NodeChangeSet updateAttribute(Attribute<?, V> attribute, V value) {
		beforeAttributeUpdate(attribute);
		attribute.setValue(value);
		return afterAttributeUpdate(attribute);
	}
	
	/**
	 * Updates an attribute and sets the specified FieldSymbol on every field
	 * 
	 * @param attribute
	 * @param value
	 * @return
	 */
	public NodeChangeSet updateAttribute(Attribute<?, ?> attribute,	FieldSymbol symbol) {
		beforeAttributeUpdate(attribute);
		attribute.clearValue();
		setSymbolOnFields(attribute, symbol);
		return afterAttributeUpdate(attribute);
	}
	
	/**
	 * Updates a field with a new value.
	 * The value will be parsed according to field data type.
	 * 
	 * @param field
	 * @param value 
	 * @return
	 */
	public <V> NodeChangeSet updateField(Field<V> field, V value) {
		Attribute<?, ?> attribute = field.getAttribute();
		beforeAttributeUpdate(attribute);
		
		field.setValue(value);
		
		return afterAttributeUpdate(attribute);
	}

	/**
	 * Updates a field with a new symbol.
	 * 
	 * @param field
	 * @param symbol 
	 * @return
	 */
	public <V> NodeChangeSet updateField(Field<V> field, FieldSymbol symbol) {
		Attribute<?, ?> attribute = field.getAttribute();

		beforeAttributeUpdate(attribute);

		field.setValue(null);
		setFieldSymbol(field, symbol);
		
		return afterAttributeUpdate(attribute);
	}
	
	public NodeChangeSet addNode(Entity parentEntity, String nodeName) {
		NodeDefinition nodeDef = parentEntity.getDefinition().getChildDefinition(nodeName);
		if ( nodeDef instanceof EntityDefinition ) {
			return addEntity(parentEntity, nodeName);
		} else {
			return addAttribute(parentEntity, nodeName);
		}
	}

	/**
	 * Adds a new entity to a the record.
	 * 
	 * @param parentEntity
	 * @param entityName
	 * @return Changes applied to the record 
	 */
	public NodeChangeSet addEntity(Entity parentEntity, String entityName) {
		Entity entity = performEntityAdd(parentEntity, entityName);
		
		setMissingValueApproved(parentEntity, entityName, false);

		NodeChangeMap changeMap = initializeEntity(entity);
		return changeMap;
	}

	public NodeChangeSet addAttribute(Entity parentEntity, String attributeName) {
		return addAttribute(parentEntity, attributeName, null, null, null);
	}
	
	/**
	 * Adds a new attribute to a record.
	 * This attribute can be immediately populated with a value or with a FieldSymbol, and remarks.
	 * You cannot specify both value and symbol.
	 * 
	 * @param parentEntity Parent entity of the attribute
	 * @param attributeName Name of the attribute definition
	 * @param value Value to set on the attribute
	 * @param symbol FieldSymbol to set on each field of the attribute
	 * @param remarks Remarks to set on each field of the attribute
	 * @return Changes applied to the record 
	 */
	public NodeChangeSet addAttribute(Entity parentEntity, 
									  String attributeName, 
									  Value value, 
									  FieldSymbol symbol, 
									  String remarks) {
		Attribute<?, ?> attribute = performAttributeAdd(parentEntity, attributeName, value, symbol, remarks);
		
		setMissingValueApproved(parentEntity, attributeName, false);
		
		return afterAttributeInsert(attribute);
	}

	/**
	 * Updates the remarks of a Field
	 * 
	 * @param field
	 * @param remarks
	 * @return
	 */
	public NodeChangeSet updateRemarks(Field<?> field, String remarks) {
		field.setRemarks(remarks);
		NodeChangeMap changeMap = new NodeChangeMap();
		Attribute<?, ?> attribute = field.getAttribute();
		changeMap.prepareAttributeChange(attribute);
		return changeMap;
	}

	public NodeChangeSet approveMissingValue(Entity parentEntity, String nodeName) {
		setMissingValueApproved(parentEntity, nodeName, true);
		Set<NodePointer> cardinalityPointers = getAncestorPointers(parentEntity);
		cardinalityPointers.add(new NodePointer(parentEntity, nodeName));
		NodeChangeMap changeMap = new NodeChangeMap();
		validateCardinality(parentEntity.getRecord(), cardinalityPointers, changeMap);
		return changeMap;
	}

	public NodeChangeSet confirmError(Attribute<?, ?> attribute) {
		Set<Attribute<?,?>> checkDependencies = new HashSet<Attribute<?,?>>();
		setErrorConfirmed(attribute, true);
		checkDependencies.add(attribute);
		
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareAttributeChange(attribute);
		
		Set<Attribute<?, ?>> attributesToRevalidate = new HashSet<Attribute<?,?>>();
		attributesToRevalidate.add(attribute);
		
		validateAttributes(attribute.getRecord(), attributesToRevalidate, changeMap);
		return changeMap;
	}
	
	/**
	 * Applies the default value to an attribute, if any.
	 * The applied default value will be the first one having verified the "condition".
	 *  
	 * @param attribute
	 * @return
	 */
	public NodeChangeSet applyDefaultValue(Attribute<?, ?> attribute) {
		performDefaultValueApply(attribute);
		return afterAttributeUpdate(attribute);
	}

	private void beforeAttributeUpdate(Attribute<?, ?> attribute) {
		Entity parentEntity = attribute.getParent();
		setErrorConfirmed(attribute, false);
		setMissingValueApproved(parentEntity, attribute.getName(), false);
		setDefaultValueApplied(attribute, false);
	}

	private NodeChangeSet afterAttributeInsert(Attribute<?, ?> attribute) {
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.addAttributeAddChange(attribute);
		return afterAttributeInsertOrUpdate(changeMap, attribute);
	}

	private NodeChangeSet afterAttributeUpdate(Attribute<?, ?> attribute) {
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.addValueChange(attribute);
		return afterAttributeInsertOrUpdate(changeMap, attribute);
	}
	
	private NodeChangeSet afterAttributeInsertOrUpdate(NodeChangeMap changeMap, Attribute<?, ?> attribute) {
		attribute.updateSummaryInfo();
		
		Record record = attribute.getRecord();
		
		// calculated attributes
		List<Attribute<?, ?>> updatedCalculatedAttributes = recalculateDependentCalculatedAttributes(attribute);
		changeMap.addValueChanges(updatedCalculatedAttributes);
		
		// relevance
		Collection<Node<?>> nodesToCheckRelevanceFor = new ArrayList<Node<?>>(updatedCalculatedAttributes);
		nodesToCheckRelevanceFor.add(attribute);
		
		List<NodePointer> relevanceToUpdate = record.determineRelevanceDependentNodes(nodesToCheckRelevanceFor);
		RelevanceUpdater relevanceUpdater = new RelevanceUpdater(relevanceToUpdate);
		Set<NodePointer> updatedRelevancePointers = relevanceUpdater.update();
		changeMap.addRelevanceChanges(updatedRelevancePointers);
		
		// requireness
		Collection<NodePointer> pointersToCheckRequirenessFor = new HashSet<NodePointer>(updatedRelevancePointers);
		NodePointer attributeNodePointer = new NodePointer(attribute);
		pointersToCheckRequirenessFor.add(attributeNodePointer);
		pointersToCheckRequirenessFor.addAll(nodesToPointers(updatedCalculatedAttributes));
		
		Collection<NodePointer> requirenessToUpdate = record.determineRequirenessDependentNodes(pointersToCheckRequirenessFor);
		Set<NodePointer> updatedRequirenessPointers = updateRequireness(requirenessToUpdate);
		changeMap.addRequirenessChanges(updatedRequirenessPointers);
		
		// validate cardinality
		Set<NodePointer> pointersToValidateCardinalityFor = new HashSet<NodePointer>(updatedRequirenessPointers);
		// validate cardinality on ancestor node pointers because we are considering empty nodes as missing nodes
		pointersToValidateCardinalityFor.addAll(getAncestorsAndSelfPointers(attribute));
		validateCardinality(record, pointersToValidateCardinalityFor, changeMap);
		
		// validate attributes
		Set<Node<?>> nodesToCheckValidationFor = new HashSet<Node<?>>(updatedCalculatedAttributes);
		nodesToCheckValidationFor.add(attribute);
		nodesToCheckValidationFor.addAll(pointersToNodes(updatedRelevancePointers));
		
		Set<Attribute<?, ?>> attributesToRevalidate = record.determineValidationDependentNodes(nodesToCheckValidationFor);

		validateAttributes(record, attributesToRevalidate, changeMap);
		return changeMap;
	}

	private void validateAttributes(Record record, Set<Attribute<?, ?>> attributes, NodeChangeMap changeMap) {
		Validator validator = record.getSurveyContext().getValidator();
		
		for (Attribute<?, ?> a : attributes) {
			ValidationResults validationResults;
			if ( a.isRelevant() ) {
				validationResults = validator.validate(a);
			} else {
				validationResults = new ValidationResults(); 
			}
			a.setValidationResults(validationResults);
			changeMap.addValidationResultChange(a, validationResults);
		}
	}

	private List<Attribute<?, ?>> recalculateDependentCalculatedAttributes(Node<?> node) {
		Record record = node.getRecord();
		List<Attribute<?, ?>> attributesToRecalculate = record.determineCalculatedAttributes(node);
		return recalculateValues(attributesToRecalculate);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Attribute<?, ?>> recalculateValues(List<Attribute<?, ?>> attributesToRecalculate) {
		List<Attribute<?, ?>> updatedAttributes = new ArrayList<Attribute<?,?>>();
		for (Attribute calcAttr : attributesToRecalculate) {
			Value previousValue = calcAttr.getValue();
			Value newValue = recalculateValue(calcAttr);
			if ( ! ( (previousValue == newValue) || (previousValue != null && previousValue.equals(newValue)) ) ) {
				calcAttr.setValue(newValue);
				calcAttr.updateSummaryInfo();
				updatedAttributes.add(calcAttr);
			}
		}
		return updatedAttributes;
	}

	private Set<NodePointer> updateRequireness(Collection<NodePointer> nodePointers) {
		Set<NodePointer> updatedPointers = new HashSet<NodePointer>();
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			String childName = nodePointer.getChildName();
			boolean oldRequireness = entity.isRequired(childName);
			boolean newRequireness = calculateRequireness(nodePointer);
			entity.setRequired(childName, newRequireness);
			if ( newRequireness != oldRequireness ) {
				updatedPointers.add(nodePointer);
			}
		}
		return updatedPointers;
	}
	
	private void validateCardinality(Record record, Set<NodePointer> pointers, NodeChangeMap changeMap) {
		Validator validator = record.getSurveyContext().getValidator();
		for (NodePointer nodePointer : pointers) {
			Entity entity = nodePointer.getEntity();
			String childName = nodePointer.getChildName();
			
			ValidationResultFlag minCountResult, maxCountResult;
			
			if ( entity.isRelevant(childName) ) {
				minCountResult = validator.validateMinCount(entity, childName);
				maxCountResult = validator.validateMaxCount(entity, childName);
			} else {
				minCountResult = maxCountResult = ValidationResultFlag.OK;
			}
			if ( entity.getMinCountValidationResult(childName) != minCountResult ) {
				entity.setMinCountValidationResult(childName, minCountResult);
				changeMap.addMinCountValidationResultChange(nodePointer, minCountResult);
			}
			if ( entity.getMaxCountValidationResult(childName) != maxCountResult ) {
				entity.setMaxCountValidationResult(childName, maxCountResult);
				changeMap.addMaxCountValidationResultChange(nodePointer, maxCountResult);
			}
		}
	}

	/**
	 * Deletes a node from the record.
	 * 
	 * @param node
	 * @return
	 */
	public NodeChangeSet deleteNode(Node<?> node) {
		Record record = node.getRecord();
		
		NodeChangeMap changeMap = new NodeChangeMap();
		
		Set<Node<?>> nodesToBeDeleted = node.getDescendantsAndSelf();
		Set<NodePointer> pointersToBeDeleted = nodesToPointers(nodesToBeDeleted);

		NodePointer nodePointer = new NodePointer(node);
		Set<NodePointer> ancestorPointers = getAncestorPointers(node);
		
		// calculated attributes
		List<Attribute<?, ?>> dependentCalculatedAttributes = record.determineCalculatedAttributes(nodesToBeDeleted);
		dependentCalculatedAttributes.removeAll(nodesToBeDeleted);
		
		// relevance
		List<NodePointer> relevanceDependenciesToDeleted = record.determineRelevanceDependentNodes(nodesToBeDeleted);

		// requireness
		Collection<NodePointer> preDeletionRequirenessDependenciesToCheck = new HashSet<NodePointer>(pointersToBeDeleted);
		preDeletionRequirenessDependenciesToCheck.addAll(getAncestorsAndSelfPointers(node));
		Collection<NodePointer> requirenessDependenciesToDeleted = record.determineRequirenessDependentNodes(preDeletionRequirenessDependenciesToCheck);
		
		// validation
		Set<Attribute<?, ?>> validationDependenciesToDeleted = record.determineValidationDependentNodes(nodesToBeDeleted);
		validationDependenciesToDeleted.removeAll(nodesToBeDeleted);
		
		performNodeDeletion(node);

		changeMap.addNodeDeleteChange(node);

		// calculated attributes
		List<Attribute<?, ?>> updatedCalculatedAttributes = recalculateValues(dependentCalculatedAttributes);
		changeMap.addValueChanges(updatedCalculatedAttributes);
		
		// relevance
		
		Collection<Node<?>> nodesToCheckRelevanceFor = new ArrayList<Node<?>>(updatedCalculatedAttributes);
		nodesToCheckRelevanceFor.addAll(pointersToNodes(relevanceDependenciesToDeleted));
		
		List<NodePointer> relevanceToUpdate = record.determineRelevanceDependentNodes(nodesToCheckRelevanceFor);
		//check relevance update with detached entity in node pointer
		RelevanceUpdater relevanceUpdater = new RelevanceUpdater(relevanceToUpdate);
		Set<NodePointer> updatedRelevancePointers = relevanceUpdater.update();
		changeMap.addRelevanceChanges(updatedRelevancePointers);
		
		// requireness
		Collection<NodePointer> pointersToCheckRequirenessFor = new HashSet<NodePointer>(updatedRelevancePointers);
		pointersToCheckRequirenessFor.addAll(requirenessDependenciesToDeleted);
		pointersToCheckRequirenessFor.addAll(nodesToPointers(updatedCalculatedAttributes));
		
		Collection<NodePointer> requirenessToUpdate = record.determineRequirenessDependentNodes(pointersToCheckRequirenessFor);
		Set<NodePointer> updatedRequirenessPointers = updateRequireness(requirenessToUpdate);
		changeMap.addRequirenessChanges(updatedRequirenessPointers);
		
		// validate cardinality
		Set<NodePointer> pointersToValidateCardinalityFor = new HashSet<NodePointer>(updatedRequirenessPointers);
		// validate cardinality on ancestor node pointers because we are considering empty nodes as missing nodes
		pointersToValidateCardinalityFor.add(nodePointer);
		pointersToValidateCardinalityFor.addAll(ancestorPointers);
		validateCardinality(record, pointersToValidateCardinalityFor, changeMap);
		
		// validate attributes
		Set<Node<?>> nodesToCheckValidationFor = new HashSet<Node<?>>(validationDependenciesToDeleted);
		nodesToCheckValidationFor.addAll(updatedCalculatedAttributes);
		nodesToCheckValidationFor.addAll(pointersToNodes(updatedRelevancePointers));
		
		Set<Attribute<?, ?>> attributesToRevalidate = record.determineValidationDependentNodes(nodesToCheckValidationFor);

		validateAttributes(record, attributesToRevalidate, changeMap);
		return changeMap;
	}

	public void moveNode(CollectRecord record, int nodeId, int index) {
		Node<?> node = record.getNodeByInternalId(nodeId);
		Entity parent = node.getParent();
		String name = node.getName();
		List<Node<?>> siblings = parent.getAll(name);
		int oldIndex = siblings.indexOf(node);
		parent.move(name, oldIndex, index);
	}
	
	private Node<?> performNodeDeletion(Node<?> node) {
		if(node.isDetached()) {
			throw new IllegalArgumentException("Unable to delete a node already detached");
		}
		Entity parentEntity = node.getParent();
		int index = node.getIndex();
		Node<?> deletedNode = parentEntity.remove(node.getName(), index);
		return deletedNode;
	}

	/**
	 * Applies default values on each descendant attribute of a record in which empty nodes have already been added.
	 * Default values are applied only to "empty" attributes.
	 * @param record 
	 * 
	 * @throws InvalidExpressionException 
	 */
	public void applyDefaultValues(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		applyDefaultValues(rootEntity);
	}

	/**
	 * Applies default values on each descendant attribute of an Entity in which empty nodes have already been added.
	 * Default values are applied only to "empty" attributes.
	 * 
	 * @param entity
	 * @throws InvalidExpressionException 
	 */
	private void applyDefaultValues(Entity entity) {
		List<Node<?>> children = entity.getChildren();
		for (Node<?> child: children) {
			if ( child instanceof Attribute ) {
				Attribute<?, ?> attribute = (Attribute<?, ?>) child;
				if ( isDefaultValueToBeApplied(attribute) ) {
					performDefaultValueApply(attribute);
				}
			} else if ( child instanceof Entity ) {
				applyDefaultValues((Entity) child);
			}
		}
	}
	
	private void setFieldSymbol(Field<?> field, FieldSymbol symbol){
		Character symbolChar = null;
		if (symbol != null) {
			symbolChar = symbol.getCode();
		}
		field.setSymbol(symbolChar);
	}
	
	private void setSymbolOnFields(Attribute<?, ?> attribute, FieldSymbol symbol) {
		for (Field<?> field : attribute.getFields()) {
			setFieldSymbol(field, symbol);
		}
	}
	
	private void setRemarksOnFirstField(Attribute<?, ?> attribute, String remarks) {
		Field<?> field = attribute.getField(0);
		field.setRemarks(remarks);
	}
	
	private void setErrorConfirmed(Attribute<?,?> attribute, boolean confirmed){
		int fieldCount = attribute.getFieldCount();
		for( int i=0; i <fieldCount; i++ ){
			Field<?> field = attribute.getField(i);
			field.getState().set(CONFIRMED_ERROR_POSITION, confirmed);
		}
	}
	
	private void setMissingValueApproved(Entity parentEntity, String childName, boolean approved) {
		org.openforis.idm.model.State childState = parentEntity.getChildState(childName);
		childState.set(APPROVED_MISSING_POSITION, approved);
	}
	
	private void setDefaultValueApplied(Attribute<?, ?> attribute, boolean applied) {
		int fieldCount = attribute.getFieldCount();
		
		for( int i=0; i <fieldCount; i++ ){
			Field<?> field = attribute.getField(i);
			field.getState().set(DEFAULT_APPLIED_POSITION, applied);
		}
	}
	
	/**
	 * Applies the first default value (if any) that is applicable to the attribute.
	 * The condition of the corresponding DefaultValue will be verified.
	 *  
	 * @param attribute
	 */
	private <V extends Value> void performDefaultValueApply(Attribute<?, V> attribute) {
		AttributeDefinition attributeDefn = (AttributeDefinition) attribute.getDefinition();
		List<AttributeDefault> defaults = attributeDefn.getAttributeDefaults();
		for (AttributeDefault attributeDefault : defaults) {
			try {
				if ( attributeDefault.evaluateCondition(attribute) ) {
					V value = attributeDefault.evaluate(attribute);
					if ( value != null ) {
						attribute.setValue(value);
						setDefaultValueApplied(attribute, true);
						attribute.updateSummaryInfo();
						break;
					}
				}
			} catch (InvalidExpressionException e) {
				throw new RuntimeException("Error applying default value for attribute " + attributeDefn.getPath());
			}
		}
	}
	
	/**
	 * Validate the entire record validating the value of each attribute and 
	 * the min/max count of each child node of each entity
	 * 
	 * @return 
	 */
	public void validate(final CollectRecord record) {
		record.resetValidationInfo();
		initializeRecord(record);
	}

	private Attribute<?, ?> performAttributeAdd(Entity parentEntity, String nodeName, Value value, 
			FieldSymbol symbol, String remarks) {
		if ( value != null && symbol != null ) {
			throw new IllegalArgumentException("Cannot specify both value and symbol");
		}
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		AttributeDefinition def = (AttributeDefinition) parentEntityDefn.getChildDefinition(nodeName);
		@SuppressWarnings("unchecked")
		Attribute<?, Value> attribute = (Attribute<?, Value>) def.createNode();
		parentEntity.add(attribute);
		if ( value != null ) {
			attribute.setValue(value);
		} else if ( symbol != null ) {
			setSymbolOnFields(attribute, symbol);
		}
		if ( remarks != null ) {
			setRemarksOnFirstField(attribute, remarks);
		}
		return attribute;
	}
	
	private Entity performEntityAdd(Entity parentEntity, String nodeName) {
		return performEntityAdd(parentEntity, nodeName, null);
	}
	
	private Entity performEntityAdd(Entity parentEntity, String name, Integer idx) {
		EntityDefinition parentDefn = parentEntity.getDefinition();
		EntityDefinition defn = parentDefn.getChildDefinition(name, EntityDefinition.class);
		Entity entity = (Entity) defn.createNode();
		if ( idx != null ) {
			parentEntity.add(entity, idx);
		} else {
			parentEntity.add(entity);
		}
		return entity;
	}

	public void initializeRecord(Record record) {
		initializeEntity(record.getRootEntity());
	}

	protected NodeChangeMap initializeEntity(Entity entity) {
		List<Node<?>> entityAsList = new ArrayList<Node<?>>();
		entityAsList.add(entity);

		Set<NodePointer> entityDescendantPointers = getDescendantNodePointers(entity);

		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.addEntityAddChange(entity);
		
		addEmptyNodes(entity);
		
		Record record = entity.getRecord();
		
		//recalculate attributes
		List<Attribute<?, ?>> calculatedAttributes = recalculateDependentCalculatedAttributes(entity);
		changeMap.addValueChanges(calculatedAttributes);
		
		//relevance
		
		List<NodePointer> relevancePointers = getChildNodePointers(entity);
		Set<NodePointer> updatedRelevancePointers = new RelevanceUpdater(relevancePointers).update();
		changeMap.addRelevanceChanges(updatedRelevancePointers);
		
		//requireness
		
		//for root entity there is no node pointer so we iterate over its descendants
		Collection<NodePointer> requirenessDependentNodes = record.determineRequirenessDependentNodes(entityDescendantPointers);
		Set<NodePointer> updatedRequirenessPointers = updateRequireness(requirenessDependentNodes);
		changeMap.addRequirenessChanges(updatedRequirenessPointers);

		//cardinality
		
		Set<NodePointer> nodePointersToCheckCardinalityFor = new HashSet<NodePointer>(entityDescendantPointers);
		if ( entity.getParent() != null ) {
			nodePointersToCheckCardinalityFor.add(new NodePointer(entity));
		}
		validateCardinality(record, nodePointersToCheckCardinalityFor, changeMap);
		
		//validate attributes
		Set<Attribute<?, ?>> attributes = record.determineValidationDependentNodes(entityAsList);
		validateAttributes(record, attributes, changeMap);
		return changeMap;
	}

	private void addEmptyNodes(Entity entity) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		addEmptyEnumeratedEntities(entity);
		EntityDefinition entityDefn = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			if(version == null || version.isApplicable(childDefn)) {
				String childName = childDefn.getName();
				if(entity.getCount(childName) == 0) {
					int toBeInserted = entity.getEffectiveMinCount(childName);
					if ( toBeInserted <= 0 && childDefn instanceof AttributeDefinition || ! childDefn.isMultiple() ) {
						//insert at least one node
						toBeInserted = 1;
					}
					addEmptyChildren(entity, childDefn, toBeInserted);
				} else {
					List<Node<?>> children = entity.getAll(childName);
					for (Node<?> child : children) {
						if(child instanceof Entity) {
							addEmptyNodes((Entity) child);
						}
					}
				}
			}
		}
	}

	private int addEmptyChildren(Entity entity, NodeDefinition childDefn, int toBeInserted) {
		String childName = childDefn.getName();
		UIOptions uiOptions = getUIOptions(entity.getSurvey());
		int count = 0;
		boolean multipleEntityFormLayout = childDefn instanceof EntityDefinition && childDefn.isMultiple() && 
				uiOptions != null && uiOptions.getLayout((EntityDefinition) childDefn) == Layout.FORM;
		if ( ! multipleEntityFormLayout ) {
			while(count < toBeInserted) {
				if(childDefn instanceof AttributeDefinition) {
					Node<?> createdNode = childDefn.createNode();
					entity.add(createdNode);
					setInitialValue((Attribute<?, ?>) createdNode);
				} else if(childDefn instanceof EntityDefinition ) {
					Entity childEntity = performEntityAdd(entity, childName);
					addEmptyNodes(childEntity);
				}
				count ++;
			}
		}
		return count;
	}
	
	private void setInitialValue(Attribute<?, ?> attr) {
		if (! attr.getDefinition().isCalculated()) {
			if(isDefaultValueToBeApplied(attr)) {
				performDefaultValueApply(attr);
			}
			if(attr instanceof BooleanAttribute && ((BooleanAttributeDefinition) attr.getDefinition()).isAffirmativeOnly() && attr.isEmpty()) {
				BooleanAttribute boolAttr = (BooleanAttribute) attr;
				boolAttr.setValue(new BooleanValue(false));
				boolAttr.updateSummaryInfo();
			}
		}
	}

	private boolean isDefaultValueToBeApplied(Attribute<?, ?> attr) {
		Survey survey = attr.getSurvey();
		if(survey instanceof CollectSurvey) {
			CollectAnnotations annotations = ((CollectSurvey) survey).getAnnotations();
			Step step = ((CollectRecord) attr.getRecord()).getStep();
			AttributeDefinition def = attr.getDefinition();
			Step phaseToApplyDefaultValue = annotations.getPhaseToApplyDefaultValue(def);
			return step.compareTo(phaseToApplyDefaultValue) <= 0;
		} else {
			return false;
		}
	}

	private void addEmptyEnumeratedEntities(Entity parentEntity) {
		Record record = parentEntity.getRecord();
		UIOptions uiOptions = getUIOptions(parentEntity.getSurvey());
		ModelVersion version = record.getVersion();
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		List<NodeDefinition> childDefinitions = parentEntityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			if ( childDefn instanceof EntityDefinition && (version == null || version.isApplicable(childDefn)) ) {
				EntityDefinition childEntityDefn = (EntityDefinition) childDefn;
				boolean tableLayout = uiOptions == null || uiOptions.getLayout(childEntityDefn) == Layout.TABLE;
				if(childEntityDefn.isMultiple() && childEntityDefn.isEnumerable() && tableLayout) {
					addEmptyEnumeratedEntities(parentEntity, childEntityDefn);
				}
			}
		}
	}

	private void addEmptyEnumeratedEntities(Entity parentEntity, EntityDefinition enumerableEntityDefn) {
		Record record = parentEntity.getRecord();
		ModelVersion version = record.getVersion();
		CodeAttributeDefinition enumeratingCodeDefn = enumerableEntityDefn.getEnumeratingKeyCodeAttribute(version);
		if(enumeratingCodeDefn != null) {
			String enumeratedEntityName = enumerableEntityDefn.getName();
			CodeList list = enumeratingCodeDefn.getList();
			Survey survey = record.getSurvey();
			CodeListService codeListService = survey.getContext().getCodeListService();
			List<CodeListItem> items = codeListService.loadRootItems(list);
			int i = 0;
			for (CodeListItem item : items) {
				if(version == null || version.isApplicable(item)) {
					String code = item.getCode();
					Entity enumeratedEntity = parentEntity.getEnumeratedEntity(enumerableEntityDefn, enumeratingCodeDefn, code);
					if( enumeratedEntity == null ) {
						Entity addedEntity = performEntityAdd(parentEntity, enumeratedEntityName, i);
						addEmptyNodes(addedEntity);
						//set the value of the key CodeAttribute
						CodeAttribute addedCode = (CodeAttribute) addedEntity.get(enumeratingCodeDefn.getName(), 0);
						addedCode.setValue(new Code(code));
					} else if (enumeratedEntity.getIndex() != i) {
						parentEntity.move(enumeratedEntityName, enumeratedEntity.getIndex(), i);
					}
					i++;
				}
			}
		}
	}
	
	private boolean calculateRequireness(NodePointer nodePointer) {
		Entity entity = nodePointer.getEntity();
		if ( ! entity.isRelevant(nodePointer.getChildName()) ) {
			return false;
		}
		NodeDefinition defn = nodePointer.getChildDefinition();
		if ( defn.hasMinCount() ) {
			return true;
		}
		String requiredExpression = defn.getRequiredExpression();
		if ( StringUtils.isBlank(requiredExpression)) {
			throw new IllegalStateException(String.format("Expected required expression or min count on node pointer %s", nodePointer.toString()));
		}
		try {
			SurveyContext context = defn.getSurvey().getContext();
			ExpressionFactory expressionFactory = context.getExpressionFactory();
			BooleanExpression expr = expressionFactory.createBooleanExpression(requiredExpression);
			return expr.evaluate(entity, null);
		} catch (InvalidExpressionException e) {
			throw new IdmInterpretationError("Error evaluating required expression", e);
		}
	}

	private Value recalculateValue(Attribute<?, ?> attribute) {
		try {
			AttributeDefinition defn = attribute.getDefinition();
			List<AttributeDefault> attributeDefaults = defn.getAttributeDefaults();
			for (AttributeDefault attributeDefault : attributeDefaults) {
				if ( attributeDefault.evaluateCondition(attribute) ) {
					Value value = attributeDefault.evaluate(attribute);
					return value;
				}
			}
			return null;
		} catch (InvalidExpressionException e) {
			throw new IllegalStateException(String.format("Invalid expression for calculated attribute %s", attribute.getPath()));
		}
	}

	private UIOptions getUIOptions(Survey survey) {
		UIOptions uiOptions = survey instanceof CollectSurvey ? ((CollectSurvey) survey).getUIOptions(): null;
		return uiOptions;
	}

	private List<NodePointer> getChildNodePointers(Entity entity) {
		List<NodePointer> pointers = new ArrayList<NodePointer>();
		EntityDefinition definition = entity.getDefinition();
		for (NodeDefinition childDef : definition.getChildDefinitions()) {
			pointers.add(new NodePointer(entity, childDef));
		}
		return pointers;
	}
	
	private Set<NodePointer> getDescendantNodePointers(Entity entity) {
		Set<NodePointer> pointers = new LinkedHashSet<NodePointer>();
		EntityDefinition definition = entity.getDefinition();
		for (NodeDefinition childDef : definition.getChildDefinitions()) {
			pointers.add(new NodePointer(entity, childDef));
			if ( childDef instanceof EntityDefinition ) {
				for (Node<?> childEntity : entity.getAll(childDef.getName())) {
					pointers.addAll(getDescendantNodePointers((Entity) childEntity));
				}
			}
		}
		return pointers;
	}
	
	private Set<NodePointer> getAncestorPointers(Node<?> node) {
		Set<NodePointer> pointers = new HashSet<NodePointer>();
		Entity parent = node.getParent();
		if ( parent != null && parent.getParent() != null ) {
			pointers.add(new NodePointer(parent));
			pointers.addAll(getAncestorPointers(parent));
		}
		return pointers;
	}
		
	private Set<NodePointer> getAncestorsAndSelfPointers(Node<?> node) {
		Set<NodePointer> pointers = getAncestorPointers(node);
		pointers.add(new NodePointer(node));
		return pointers;
	}

	private Collection<Node<?>> pointersToNodes(Collection<NodePointer> nodePointers) {
		Set<Node<?>> result = new HashSet<Node<?>>();
		for (NodePointer nodePointer : nodePointers) {
			result.addAll(nodePointer.getNodes());
		}
		return result;
	}
	
	private Set<NodePointer> nodesToPointers(Collection<? extends Node<?>> nodes) {
		Set<NodePointer> result = new HashSet<NodePointer>();
		for (Node<?> n : nodes) {
			result.add(new NodePointer(n));
		}
		return result;
	}

	private static class RelevanceUpdater {
		private final List<NodePointer> pointersToUpdate;
		private final Set<NodePointer> updatedNodePointers;
		
		RelevanceUpdater(List<NodePointer> pointersToUpdate) {
			this.pointersToUpdate = pointersToUpdate;
			this.updatedNodePointers = new HashSet<NodePointer>();
		}
		
		Set<NodePointer> update() {
			for (NodePointer nodePointer : pointersToUpdate) {
				updatedNodePointers.addAll(update(nodePointer));
			}
			return updatedNodePointers;
		}
		
		private Collection<NodePointer> update(NodePointer nodePointer) {
			if (updatedNodePointers.contains(nodePointer)) {
				return Collections.emptySet();
			}

			boolean relevance = calculateRelevance(nodePointer);
			updatedNodePointers.addAll(setRelevance(nodePointer, relevance));
			return updatedNodePointers;
		}
	
		private Collection<NodePointer> setRelevance(NodePointer nodePointer, boolean relevant) {
			Collection<NodePointer> changedNodePointers = new HashSet<NodePointer>();
			
			Entity entity = nodePointer.getEntity();
			String childName = nodePointer.getChildName();
			EntityDefinition entityDef = entity.getDefinition();
			
			Boolean oldRelevance = entity.getRelevance(childName);
			if ( oldRelevance == null || oldRelevance.booleanValue() != relevant ) {
				entity.setRelevant(childName, relevant);
				changedNodePointers.add(nodePointer);
				
				NodeDefinition childDef = entityDef.getChildDefinition(childName);
				if ( childDef instanceof EntityDefinition ) {
					List<Node<?>> nodes = entity.getChildren(childName);
					for (Node<?> node : nodes) {
						Entity childEntity = (Entity) node;
						EntityDefinition childEntityDef = childEntity.getDefinition();
						for (NodeDefinition nextChildDef : childEntityDef.getChildDefinitions()) {
							NodePointer nextNodePointer = new NodePointer(childEntity, nextChildDef);
							if ( relevant ) {
								changedNodePointers.addAll(update(nextNodePointer));
							} else {
								changedNodePointers.addAll(setRelevance(nextNodePointer, false));
							}
						}
					}
				}
			}
			return changedNodePointers;	
		}
		
		private boolean calculateRelevance(NodePointer nodePointer) {
			NodeDefinition childDef = nodePointer.getChildDefinition();
			String expr = childDef.getRelevantExpression();
			if (StringUtils.isBlank(expr)) {
				return true;
			}
			try {
				Entity entity = nodePointer.getEntity();
				Survey survey = entity.getSurvey();
				ExpressionFactory expressionFactory = survey.getContext().getExpressionFactory();
				BooleanExpression relevanceExpr = expressionFactory.createBooleanExpression(expr);
				return relevanceExpr.evaluate(entity, null);
			} catch (InvalidExpressionException e) {
				throw new IdmInterpretationError(childDef.getPath() + " - Unable to evaluate expression: " + expr, e);
			}
		}
	}
	
}
