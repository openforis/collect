package org.openforis.collect.model;

import static org.openforis.collect.model.CollectRecord.APPROVED_MISSING_POSITION;
import static org.openforis.collect.model.CollectRecord.CONFIRMED_ERROR_POSITION;
import static org.openforis.idm.model.NodePointers.nodesToPointers;
import static org.openforis.idm.model.NodePointers.pointersToDescendantPointers;
import static org.openforis.idm.model.NodePointers.pointersToNodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.recordUpdater.RecordDependentsUpdater;
import org.openforis.collect.model.recordUpdater.RecordDependentsUpdater.RecordDependentsUpdateResult;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
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
import org.openforis.idm.model.NodePointers;
import org.openforis.idm.model.NodeVisitor;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionEvaluator;
import org.openforis.idm.model.expression.InvalidExpressionException;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordUpdater {

	public static class RecordUpdateConfiguration {
		private boolean validateAfterUpdate = true;
		private boolean clearNotRelevantAttributes = false;
		private boolean clearDependentCodeAttributes = false;
		private boolean addEmptyMultipleEntitiesWhenAddingNewEntities = true;
		private boolean alwaysEvaluateCalculatedAttributes = false;

		public boolean isValidateAfterUpdate() {
			return validateAfterUpdate;
		}

		public void setValidateAfterUpdate(boolean validateAfterUpdate) {
			this.validateAfterUpdate = validateAfterUpdate;
		}

		public boolean isClearDependentCodeAttributes() {
			return clearDependentCodeAttributes;
		}

		public void setClearDependentCodeAttributes(boolean clearDependentCodeAttributes) {
			this.clearDependentCodeAttributes = clearDependentCodeAttributes;
		}

		public boolean isClearNotRelevantAttributes() {
			return clearNotRelevantAttributes;
		}

		public void setClearNotRelevantAttributes(boolean clearNotRelevantAttributes) {
			this.clearNotRelevantAttributes = clearNotRelevantAttributes;
		}

		public boolean isAddEmptyMultipleEntitiesWhenAddingNewEntities() {
			return addEmptyMultipleEntitiesWhenAddingNewEntities;
		}

		public void setAddEmptyMultipleEntitiesWhenAddingNewEntities(
				boolean addEmptyMultipleEntitiesWhenAddingNewEntities) {
			this.addEmptyMultipleEntitiesWhenAddingNewEntities = addEmptyMultipleEntitiesWhenAddingNewEntities;
		}

		public boolean isAlwaysEvaluateCalculatedAttributes() {
			return alwaysEvaluateCalculatedAttributes;
		}

		public void setAlwaysEvaluateCalculatedAttributes(boolean alwaysEvaluateCalculatedAttributes) {
			this.alwaysEvaluateCalculatedAttributes = alwaysEvaluateCalculatedAttributes;
		}
	}

	private RecordUpdateConfiguration configuration = new RecordUpdateConfiguration();

	/**
	 * Updates an attribute with a new value
	 */
	public <V extends Value> NodeChangeSet updateAttribute(Attribute<?, V> attribute, V value) {
		beforeAttributeUpdate(attribute);
		attribute.setValue(value);
		attribute.updateSummaryInfo();
		return afterAttributeUpdate(attribute);
	}

	/**
	 * Updates an attribute and sets the specified FieldSymbol on every field
	 */
	public NodeChangeSet updateAttribute(Attribute<?, ?> attribute, FieldSymbol symbol) {
		beforeAttributeUpdate(attribute);
		attribute.clearValue();
		setSymbolOnFields(attribute, symbol);
		attribute.updateSummaryInfo();
		return afterAttributeUpdate(attribute);
	}

	public NodeChangeSet updateMultipleAttribute(Entity parentEntity, AttributeDefinition attrDef, List<Value> values) {
		CollectRecord record = (CollectRecord) parentEntity.getRecord();
		NodePointer selfNodePointer = new NodePointer(parentEntity, attrDef);

		NodeChangeMap changeMap = new NodeChangeMap();

		List<Node<?>> children = parentEntity.getChildren(attrDef);
		Set<Node<?>> oldAttributes = new HashSet<Node<?>>(children);

		Set<NodePointer> dependentPointers = record.determineDependenciesThatRequireUpdates(oldAttributes);

		// delete old values
		for (Node<?> child : oldAttributes) {
			List<Integer> ancestorIds = child.getAncestorIds();
			performNodeDeletion(child);
			changeMap.addNodeDeleteChange(record.getId(), record.getStep(), ancestorIds, parentEntity.getPath(), child);
		}

		// add new values
		Collection<Attribute<?, ?>> newAttrs = new ArrayList<Attribute<?, ?>>(values.size());
		for (Value value : values) {
			Attribute<?, ?> a = performAttributeAdd(parentEntity, attrDef, value, null, null);
			newAttrs.add(a);
			changeMap.addAttributeAddChange(a);
		}
		dependentPointers.addAll(NodePointers.nodesToPointers(newAttrs));

		// re-calculate values and relevance
		RecordDependentsUpdateResult dependentsUpdateResult = new RecordDependentsUpdater(configuration)
				.updateDependents(record, dependentPointers);
		List<Attribute<?, ?>> dependentUpdatedAttributes = dependentsUpdateResult.getUpdatedAttributes();
		changeMap.addValueChanges(dependentUpdatedAttributes);

		Set<NodePointer> updatedRelevancePointers = dependentsUpdateResult.getUpdatedRelevancePointers();
		changeMap.addRelevanceChanges(updatedRelevancePointers);

		if (configuration.validateAfterUpdate) {
			List<Attribute<?, ?>> updatedAttributes = new ArrayList<Attribute<?, ?>>();
			updatedAttributes.addAll(newAttrs);
			updatedAttributes.addAll(dependentUpdatedAttributes);

			List<NodePointer> ancestorsAndSelfPointers = getAncestorsAndSelfPointers(selfNodePointer);

			performValidationAfterUpdate(selfNodePointer, ancestorsAndSelfPointers, updatedAttributes,
					updatedRelevancePointers, Collections.<NodePointer>emptySet(), Collections.<NodePointer>emptySet(),
					Collections.<Attribute<?, ?>>emptySet(), changeMap);
		}
		return changeMap;
	}

	/**
	 * Updates a field with a new value. The value will be parsed according to field
	 * data type.
	 */
	public <V> NodeChangeSet updateField(Field<V> field, V value) {
		Attribute<?, ?> attribute = field.getAttribute();
		beforeAttributeUpdate(attribute);

		field.setValue(value);
		attribute.updateSummaryInfo();

		return afterAttributeUpdate(attribute);
	}

	/**
	 * Updates a field with a new symbol.
	 * 
	 * @param clearChildCodeAttributes
	 */
	public <V> NodeChangeSet updateField(Field<V> field, FieldSymbol symbol) {
		Attribute<?, ?> attribute = field.getAttribute();

		beforeAttributeUpdate(attribute);

		field.setValue(null);
		setFieldSymbol(field, symbol);
		attribute.updateSummaryInfo();

		return afterAttributeUpdate(attribute);
	}

	public NodeChangeSet addNode(Entity parentEntity, String nodeName) {
		NodeDefinition nodeDef = parentEntity.getDefinition().getChildDefinition(nodeName);
		return addNode(parentEntity, nodeDef);
	}

	public NodeChangeSet addNode(Entity parentEntity, NodeDefinition nodeDef) {
		if (nodeDef instanceof EntityDefinition) {
			return addEntity(parentEntity, (EntityDefinition) nodeDef);
		} else {
			return addAttribute(parentEntity, (AttributeDefinition) nodeDef);
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
		EntityDefinition entityDef = parentEntity.getDefinition().getChildDefinition(entityName,
				EntityDefinition.class);
		return addEntity(parentEntity, entityDef);
	}

	public NodeChangeSet addEntity(Entity parentEntity, EntityDefinition entityDef) {
		Entity entity = performEntityAdd(parentEntity, entityDef, null);

		setMissingValueApproved(parentEntity, entityDef.getName(), false);

		NodeChangeMap changeMap = initializeEntity(entity, true);
		return changeMap;
	}

	public NodeChangeSet addEntity(Entity parentEntity, Entity entity) {
		performEntityAdd(parentEntity, entity);

		setMissingValueApproved(parentEntity, entity.getName(), false);

		NodeChangeMap changeMap = initializeEntity(entity, false);
		return changeMap;
	}

	public NodeChangeSet addAttribute(Entity parentEntity, String attributeName) {
		return addAttribute(parentEntity, attributeName, null, null, null);
	}

	public NodeChangeSet addAttribute(Entity parentEntity, String attributeName, Value value) {
		return addAttribute(parentEntity, attributeName, value, null, null);
	}

	public NodeChangeSet addAttribute(Entity parentEntity, AttributeDefinition attributeDef) {
		return addAttribute(parentEntity, attributeDef, null, null, null);
	}

	/**
	 * Adds a new attribute to a record. This attribute can be immediately populated
	 * with a value or with a FieldSymbol, and remarks. You cannot specify both
	 * value and symbol.
	 * 
	 * @param parentEntity  Parent entity of the attribute
	 * @param attributeName Name of the attribute definition
	 * @param value         Value to set on the attribute
	 * @param symbol        FieldSymbol to set on each field of the attribute
	 * @param remarks       Remarks to set on each field of the attribute
	 * @return Changes applied to the record
	 */
	public NodeChangeSet addAttribute(Entity parentEntity, String attributeName, Value value, FieldSymbol symbol,
			String remarks) {
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		AttributeDefinition attributeDef = (AttributeDefinition) parentEntityDefn.getChildDefinition(attributeName);
		return addAttribute(parentEntity, attributeDef, value, symbol, remarks);
	}

	public NodeChangeSet addAttribute(Entity parentEntity, AttributeDefinition attributeDef, Value value,
			FieldSymbol symbol, String remarks) {
		Attribute<?, ?> attribute = performAttributeAdd(parentEntity, attributeDef, value, symbol, remarks);

		setMissingValueApproved(parentEntity, attribute.getName(), false);

		if (value == null) {
			applyInitialValue(attribute);
		}

		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.addAttributeAddChange(attribute);

		return afterAttributeInsertOrUpdate(changeMap, attribute);
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
		List<NodePointer> cardinalityPointers = getAncestorPointers(parentEntity);
		cardinalityPointers.add(new NodePointer(parentEntity, nodeName));
		NodeChangeMap changeMap = new NodeChangeMap();
		validateCardinality(parentEntity.getRecord(), cardinalityPointers, changeMap);
		return changeMap;
	}

	public NodeChangeSet confirmError(Attribute<?, ?> attribute) {
		Set<Attribute<?, ?>> checkDependencies = new HashSet<Attribute<?, ?>>();
		setErrorConfirmed(attribute, true);
		checkDependencies.add(attribute);

		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareAttributeChange(attribute);

		Set<Attribute<?, ?>> attributesToRevalidate = new HashSet<Attribute<?, ?>>();
		attributesToRevalidate.add(attribute);

		validateAttributes(attribute.getRecord(), attributesToRevalidate, changeMap);
		return changeMap;
	}

	/**
	 * Applies the default value to an attribute, if any. The applied default value
	 * will be the first one having verified the "condition".
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
		attribute.setDefaultValueApplied(false);
	}

	private NodeChangeSet afterAttributeUpdate(Attribute<?, ?> attribute) {
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.addValueChange(attribute);
		return afterAttributeInsertOrUpdate(changeMap, attribute);
	}

	private NodeChangeSet afterAttributeInsertOrUpdate(final NodeChangeMap changeMap, Attribute<?, ?> attribute) {
		CollectRecord record = (CollectRecord) attribute.getRecord();
		CollectSurvey survey = (CollectSurvey) record.getSurvey();

		NodePointer selfPointer = new NodePointer(attribute);

		RecordDependentsUpdater recordDependentsUpdater = new RecordDependentsUpdater(configuration);
		RecordDependentsUpdateResult recordDependentsUpdateResult = recordDependentsUpdater.updateDependents(record,
				selfPointer, true);

		List<Attribute<?, ?>> updatedAttributes = new ArrayList<Attribute<?, ?>>();
		updatedAttributes.add(attribute);
		updatedAttributes.addAll(recordDependentsUpdateResult.getUpdatedAttributes());
		changeMap.addValueChanges(updatedAttributes);

		if (survey.isCollectEarth()) {
			// include dependent code attributes in change map (allows the re-rendering of
			// code list items in Collect Earth)
			Collection<CodeAttribute> updatedCodeAttributes = Nodes.filterCodeAttributes(updatedAttributes);
			for (CodeAttribute codeAttribute : updatedCodeAttributes) {
				Set<CodeAttribute> dependentCodeAttributes = record.determineDependentCodeAttributes(codeAttribute);
				changeMap.addValueChanges(dependentCodeAttributes);
			}
		}

		Set<NodePointer> updatedRelevancePointers = recordDependentsUpdateResult.getUpdatedRelevancePointers();
		changeMap.addRelevanceChanges(updatedRelevancePointers);

		List<NodePointer> ancestorsAndSelfPointers = getAncestorsAndSelfPointers(selfPointer);

		if (configuration.validateAfterUpdate) {
			Set<NodePointer> minCountDependenciesToSelf = new HashSet<NodePointer>();
			Set<NodePointer> maxCountDependenciesToSelf = new HashSet<NodePointer>();
			Set<Attribute<?, ?>> validationDependenciesToSelf = new HashSet<Attribute<?, ?>>();

			performValidationAfterUpdate(selfPointer, ancestorsAndSelfPointers, updatedAttributes,
					updatedRelevancePointers, minCountDependenciesToSelf, maxCountDependenciesToSelf,
					validationDependenciesToSelf, changeMap);
		}
		return changeMap;
	}

	private void validateAttributes(Record record, Set<Attribute<?, ?>> attributes, NodeChangeMap changeMap) {
		Validator validator = record.getSurveyContext().getValidator();

		for (Attribute<?, ?> a : attributes) {
			ValidationResults validationResultsNew = a.isRelevantInsideAncestors() ? validator.validate(a)
					: new ValidationResults();
			ValidationResults validationResultsOld = a.getValidationResults();
			if (validationResultsOld == null && !validationResultsNew.isEmpty()
					|| validationResultsOld != null && !validationResultsNew.equals(validationResultsOld)) {
				a.setValidationResults(validationResultsNew);
				changeMap.addValidationResultChange(a, validationResultsNew);
			}
		}
	}

	private Collection<NodePointer> updateMinCount(Collection<NodePointer> nodePointers) {
		List<NodePointer> updatedPointers = new ArrayList<NodePointer>();
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			NodeDefinition childDef = nodePointer.getChildDefinition();
			Integer oldCount = entity.getMinCount(childDef);
			int newCount = calculateMinCount(nodePointer);
			entity.setMinCount(childDef, newCount);
			if (oldCount == null || oldCount.intValue() != newCount) {
				updatedPointers.add(nodePointer);
			}
		}
		return updatedPointers;
	}

	@SuppressWarnings("deprecation")
	private Collection<NodePointer> updateMaxCount(Collection<NodePointer> nodePointers) {
		List<NodePointer> updatedPointers = new ArrayList<NodePointer>();
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			NodeDefinition childDef = nodePointer.getChildDefinition();
			Integer oldCount = entity.getMaxCount(childDef);
			int newCount = calculateMaxCount(nodePointer);
			entity.setMaxCount(childDef, newCount);
			if (!ObjectUtils.equals(oldCount, newCount)) {
				updatedPointers.add(nodePointer);
			}
		}
		return updatedPointers;
	}

	private Set<NodePointer> validateCardinality(Record record, Collection<NodePointer> pointers,
			NodeChangeMap changeMap) {
		Set<NodePointer> updatedPointers = new HashSet<NodePointer>();
		Validator validator = record.getSurveyContext().getValidator();
		for (NodePointer nodePointer : pointers) {
			Entity entity = nodePointer.getEntity();
			NodeDefinition childDef = nodePointer.getChildDefinition();

			ValidationResultFlag minCountResult, maxCountResult;

			if (entity.isRelevant() && entity.isRelevant(childDef)) {
				minCountResult = validator.validateMinCount(entity, childDef);
				maxCountResult = validator.validateMaxCount(entity, childDef);
			} else {
				minCountResult = maxCountResult = ValidationResultFlag.OK;
			}
			if (entity.getMinCountValidationResult(childDef) != minCountResult) {
				entity.setMinCountValidationResult(childDef, minCountResult);
				changeMap.addMinCountValidationResultChange(nodePointer, minCountResult);
				updatedPointers.add(nodePointer);
			}
			if (entity.getMaxCountValidationResult(childDef) != maxCountResult) {
				entity.setMaxCountValidationResult(childDef, maxCountResult);
				changeMap.addMaxCountValidationResultChange(nodePointer, maxCountResult);
				updatedPointers.add(nodePointer);
			}
		}
		return updatedPointers;
	}

	public NodeChangeSet deleteChildren(Entity entity, NodeDefinition childDef) {
		NodeChangeMap changeMap = new NodeChangeMap();
		while (entity.getCount(childDef) > 0) {
			changeMap.addMergeChanges(deleteNode(entity.getLastChild(childDef)));
		}
		return changeMap;
	}

	/**
	 * Deletes a node from the record.
	 * 
	 * @param node
	 * @return
	 */
	public NodeChangeSet deleteNode(Node<?> node) {
		CollectRecord record = (CollectRecord) node.getRecord();

		NodeChangeMap changeMap = new NodeChangeMap();

		final Set<Node<?>> nodesToBeDeleted = new HashSet<Node<?>>();
		nodesToBeDeleted.add(node);
		if (node instanceof Entity) {
			nodesToBeDeleted.addAll(((Entity) node).getDescendants());
		}

		Set<NodePointer> pointersToBeDeleted = nodesToPointers(nodesToBeDeleted);

		NodePointer nodePointer = new NodePointer(node);

		List<NodePointer> ancestorsAndSelfPointers = getAncestorsAndSelfPointers(nodePointer);

		// calculated attributes and default values

		Collection<NodePointer> dependentPointers = record.determineDependenciesThatRequireUpdates(nodesToBeDeleted);
		CollectionUtils.filter(dependentPointers, new Predicate<NodePointer>() {
			public boolean evaluate(NodePointer nodePointer) {
				return !nodesToBeDeleted.contains(nodePointer.getEntity());
			}
		});

		Set<NodePointer> minCountDependenciesToSelf = new HashSet<NodePointer>();
		Set<NodePointer> maxCountDependenciesToSelf = new HashSet<NodePointer>();
		Set<Attribute<?, ?>> validationDependenciesToSelf = new HashSet<Attribute<?, ?>>();

		if (configuration.validateAfterUpdate) {
			// min/max
			Collection<NodePointer> preDeletionMinMaxDependenciesToCheck = new HashSet<NodePointer>(
					pointersToBeDeleted);
			preDeletionMinMaxDependenciesToCheck.addAll(getAncestorsAndSelfPointers(node));
			minCountDependenciesToSelf
					.addAll(record.determineMinCountDependentNodes(preDeletionMinMaxDependenciesToCheck));
			maxCountDependenciesToSelf
					.addAll(record.determineMaxCountDependentNodes(preDeletionMinMaxDependenciesToCheck));

			// validation
			validationDependenciesToSelf = record.determineValidationDependentNodes(nodesToBeDeleted);
			validationDependenciesToSelf.removeAll(nodesToBeDeleted);
		}

		// perform node deletion
		List<Integer> ancestorIds = node.getAncestorIds();
		Entity parentEntity = node.getParent();
		performNodeDeletion(node);
		changeMap.addNodeDeleteChange(record.getId(), record.getStep(), ancestorIds, parentEntity.getPath(), node);

		// update dependents
		RecordDependentsUpdateResult dependentsUpdateResult = new RecordDependentsUpdater(configuration)
				.updateDependents(record, dependentPointers);
		List<Attribute<?, ?>> updatedAttributes = dependentsUpdateResult.getUpdatedAttributes();
		changeMap.addValueChanges(updatedAttributes);
		Set<NodePointer> updatedRelevancePointers = dependentsUpdateResult.getUpdatedRelevancePointers();
		changeMap.addRelevanceChanges(updatedRelevancePointers);

		if (configuration.validateAfterUpdate) {
			performValidationAfterUpdate(nodePointer, ancestorsAndSelfPointers, updatedAttributes,
					updatedRelevancePointers, minCountDependenciesToSelf, maxCountDependenciesToSelf,
					validationDependenciesToSelf, changeMap);
		}
		return changeMap;
	}

	private void performValidationAfterUpdate(NodePointer nodePointer, List<NodePointer> ancestorsAndSelfPointers,
			List<Attribute<?, ?>> updatedAttributes, Set<NodePointer> updatedRelevancePointers,
			Set<NodePointer> minCountDependenciesToSelf, Set<NodePointer> maxCountDependenciesToSelf,
			Set<Attribute<?, ?>> validationDependenciesToSelf, NodeChangeMap changeMap) {
		Record record = nodePointer.getRecord();

		Set<NodePointer> updatedAttributePointers = nodesToPointers(updatedAttributes);

		// min count
		Collection<NodePointer> pointersToCheckMinCountFor = new HashSet<NodePointer>();
		pointersToCheckMinCountFor.addAll(ancestorsAndSelfPointers);
		pointersToCheckMinCountFor.addAll(updatedRelevancePointers);
		pointersToCheckMinCountFor.addAll(minCountDependenciesToSelf);
		pointersToCheckMinCountFor.addAll(updatedAttributePointers);

		Collection<NodePointer> minCountPointersToUpdate = record
				.determineMinCountDependentNodes(pointersToCheckMinCountFor);
		Collection<NodePointer> updatedMinCountPointers = updateMinCount(minCountPointersToUpdate);
		changeMap.addMinCountChanges(updatedMinCountPointers);

		// max count
		Collection<NodePointer> pointersToCheckMaxCountFor = new HashSet<NodePointer>();
		pointersToCheckMaxCountFor.addAll(ancestorsAndSelfPointers);
		pointersToCheckMaxCountFor.addAll(updatedRelevancePointers);
		pointersToCheckMaxCountFor.addAll(maxCountDependenciesToSelf);
		pointersToCheckMaxCountFor.addAll(updatedAttributePointers);

		Collection<NodePointer> maxCountPointersToUpdate = record
				.determineMaxCountDependentNodes(pointersToCheckMaxCountFor);
		Collection<NodePointer> updatedMaxCountPointers = updateMaxCount(maxCountPointersToUpdate);
		changeMap.addMaxCountChanges(updatedMaxCountPointers);

		Set<NodePointer> updatedCardinalityPointers = new HashSet<NodePointer>(updatedMinCountPointers);
		updatedCardinalityPointers.addAll(updatedMaxCountPointers);

		// validate cardinality
		Set<NodePointer> updatedAttributePointersAndSelf = new HashSet<NodePointer>();
		updatedAttributePointersAndSelf.addAll(updatedAttributePointers);
		updatedAttributePointersAndSelf.add(nodePointer);
		// determine dependent attributes (hierarchical code attributes with
		// parent/child relation)
		Set<NodePointer> dependentCodeAttributesPointers = determineDependentCodeAttributes(
				updatedAttributePointersAndSelf);

		Set<NodePointer> updatedRelevanceDescendantPointers = pointersToDescendantPointers(updatedRelevancePointers);

		Set<NodePointer> pointersToValidateCardinalityFor = new HashSet<NodePointer>();
		pointersToValidateCardinalityFor.addAll(updatedAttributePointers);
		pointersToValidateCardinalityFor.addAll(updatedMinCountPointers);
		pointersToValidateCardinalityFor.addAll(updatedMaxCountPointers);
		pointersToValidateCardinalityFor.addAll(updatedRelevancePointers);
		pointersToValidateCardinalityFor.addAll(updatedRelevanceDescendantPointers);
		pointersToValidateCardinalityFor.addAll(dependentCodeAttributesPointers);
		// validate cardinality on ancestor node pointers because we are considering
		// empty nodes as missing nodes
		pointersToValidateCardinalityFor.addAll(ancestorsAndSelfPointers);

		validateCardinality(record, pointersToValidateCardinalityFor, changeMap);

		// validate attributes
		Set<Node<?>> nodesToCheckValidationFor = new HashSet<Node<?>>();
		if (nodePointer.getChildDefinition() instanceof AttributeDefinition) {
			nodesToCheckValidationFor.addAll(nodePointer.getNodes());
		}
		nodesToCheckValidationFor.addAll(updatedAttributes);
		nodesToCheckValidationFor.addAll(validationDependenciesToSelf);
		nodesToCheckValidationFor.addAll(pointersToNodes(updatedRelevancePointers));
		nodesToCheckValidationFor.addAll(pointersToNodes(updatedRelevanceDescendantPointers));
		nodesToCheckValidationFor.addAll(pointersToNodes(updatedCardinalityPointers));

		Set<Attribute<?, ?>> attributesToRevalidate = filterAttributes(nodesToCheckValidationFor);
		attributesToRevalidate.addAll(record.determineValidationDependentNodes(nodesToCheckValidationFor));

		validateAttributes(record, attributesToRevalidate, changeMap);
	}

	public void moveNode(CollectRecord record, int nodeId, int index) {
		Node<?> node = record.getNodeByInternalId(nodeId);
		Entity parent = node.getParent();
		List<Node<?>> siblings = parent.getChildren(node.getDefinition());
		int oldIndex = siblings.indexOf(node);
		parent.move(node.getDefinition(), oldIndex, index);
	}

	private Node<?> performNodeDeletion(Node<?> node) {
		if (node.isDetached()) {
			throw new IllegalArgumentException("Unable to delete a node already detached");
		}
		Entity parentEntity = node.getParent();
		int index = node.getIndex();
		Node<?> deletedNode = parentEntity.remove(node.getDefinition(), index);
		return deletedNode;
	}

	private void setFieldSymbol(Field<?> field, FieldSymbol symbol) {
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

	private void setErrorConfirmed(Attribute<?, ?> attribute, boolean confirmed) {
		for (Field<?> field : attribute.getFields()) {
			field.getState().set(CONFIRMED_ERROR_POSITION, confirmed);
		}
	}

	private void setMissingValueApproved(Entity parentEntity, String childName, boolean approved) {
		org.openforis.idm.model.State childState = parentEntity.getChildState(childName);
		childState.set(APPROVED_MISSING_POSITION, approved);
	}

	/**
	 * Applies the first default value (if any) that is applicable to the attribute.
	 * The condition of the corresponding DefaultValue will be verified.
	 * 
	 * @param attribute
	 * @return
	 */
	private <V extends Value> V performDefaultValueApply(Attribute<?, V> attribute) {
		AttributeDefinition attributeDefn = (AttributeDefinition) attribute.getDefinition();
		List<AttributeDefault> defaults = attributeDefn.getAttributeDefaults();
		for (AttributeDefault attributeDefault : defaults) {
			try {
				if (attributeDefault.evaluateCondition(attribute)) {
					V value = attributeDefault.evaluate(attribute);
					if (value != null) {
						attribute.setValue(value);
						attribute.setDefaultValueApplied(true);
						attribute.updateSummaryInfo();
						return value;
					}
				}
			} catch (InvalidExpressionException e) {
				throw new RuntimeException("Error applying default value for attribute " + attributeDefn.getPath());
			}
		}
		return null;
	}

	/**
	 * Validate the entire record validating the value of each attribute and the
	 * min/max count of each child node of each entity
	 * 
	 * @return
	 */
	public void validate(final CollectRecord record) {
		record.resetValidationInfo();
		initializeRecord(record);
	}

	private Attribute<?, ?> performAttributeAdd(Entity parentEntity, AttributeDefinition attributeDef, Value value,
			FieldSymbol symbol, String remarks) {
		if (value != null && symbol != null) {
			throw new IllegalArgumentException("Cannot specify both value and symbol");
		}
		@SuppressWarnings("unchecked")
		Attribute<?, Value> attribute = (Attribute<?, Value>) attributeDef.createNode();
		parentEntity.add(attribute);
		if (value != null) {
			attribute.setValue(value);
		} else if (symbol != null) {
			setSymbolOnFields(attribute, symbol);
		}
		if (remarks != null) {
			setRemarksOnFirstField(attribute, remarks);
		}
		attribute.updateSummaryInfo();
		return attribute;
	}

	private Entity performEntityAdd(Entity parentEntity, EntityDefinition defn) {
		return performEntityAdd(parentEntity, defn, null);
	}

	private Entity performEntityAdd(Entity parentEntity, EntityDefinition defn, Integer idx) {
		return performEntityAdd(parentEntity, (Entity) defn.createNode(), idx);
	}

	private Entity performEntityAdd(Entity parentEntity, Entity entity) {
		return performEntityAdd(parentEntity, entity, null);
	}

	private Entity performEntityAdd(Entity parentEntity, Entity entity, Integer idx) {
		if (idx == null) {
			parentEntity.add(entity);
		} else {
			parentEntity.add(entity, idx);
		}
		ModelVersion version = parentEntity.getRecord().getVersion();
		for (NodeDefinition childDef : entity.getDefinition().getChildDefinitionsInVersion(version)) {
			entity.setMinCount(childDef, calculateMinCount(entity, childDef));
			entity.setMaxCount(childDef, calculateMaxCount(entity, childDef));
		}
		return entity;
	}

	public NodeChangeSet initializeRecord(Record record) {
		NodeChangeMap result = initializeEntity(record.getRootEntity(), false);
		result.addMergeChanges(new VirtualEntityPopuplator(this).populateVirtualEntitites(record));
		return result;
	}

	public NodeChangeSet initializeNewRecord(Record record) {
		return initializeEntity(record.getRootEntity(), true);
	}

	protected NodeChangeMap initializeEntity(Entity entity) {
		return initializeEntity(entity, false);
	}

	protected NodeChangeMap initializeEntity(Entity entity, boolean newEntity) {
		CollectRecord record = (CollectRecord) entity.getRecord();

		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.addEntityAddChange(entity);

		List<NodePointer> entityDescendantAndSelfPointers = getDescendantAndSelfNodePointers(entity);

		updateMinCount(entityDescendantAndSelfPointers);
		updateMaxCount(entityDescendantAndSelfPointers);

		addEmptyNodes(entity);

		// descendant pointers could have been changed
		entityDescendantAndSelfPointers = getDescendantAndSelfNodePointers(entity);

		RecordDependentsUpdater recordDependentsUpdater = new RecordDependentsUpdater(configuration);
		// recalculate attributes
		// TODO exclude this when exporting for backup (not for Calc)
		RecordDependentsUpdateResult recordDependentsUpdateResult = recordDependentsUpdater
				.updateDependentsAndSelf(record, entityDescendantAndSelfPointers);

		// calculated attributes and default values
		List<Attribute<?, ?>> updatedAttributes = recordDependentsUpdateResult.getUpdatedAttributes();
		changeMap.addValueChanges(updatedAttributes);

		// relevance
		Set<NodePointer> updatedRelevancePointers = recordDependentsUpdateResult.getUpdatedRelevancePointers();
		changeMap.addRelevanceChanges(updatedRelevancePointers);

		if (configuration.validateAfterUpdate) {
			// recalculate descendant pointers after empty nodes have been added
			entityDescendantAndSelfPointers = getDescendantAndSelfNodePointers(entity);
			// min/max count
			// for root entity there is no node pointer so we iterate over its descendants
			Collection<NodePointer> minCountDependentNodes = record
					.determineMinCountDependentNodes(entityDescendantAndSelfPointers);
			Collection<NodePointer> updatedMinCountPointers = updateMinCount(minCountDependentNodes);
			changeMap.addMinCountChanges(updatedMinCountPointers);

			Collection<NodePointer> maxCountDependentNodes = record
					.determineMaxCountDependentNodes(entityDescendantAndSelfPointers);
			Collection<NodePointer> updatedMaxCountPointers = updateMaxCount(maxCountDependentNodes);
			changeMap.addMaxCountChanges(updatedMaxCountPointers);

			Set<NodePointer> updatedCardinalityPointers = new HashSet<NodePointer>();
			updatedCardinalityPointers.addAll(updatedMinCountPointers);
			updatedCardinalityPointers.addAll(updatedMaxCountPointers);

			// cardinality
			Collection<NodePointer> nodePointersToCheckCardinalityFor = new HashSet<NodePointer>(
					entityDescendantAndSelfPointers);
			nodePointersToCheckCardinalityFor.addAll(updatedCardinalityPointers);
			validateCardinality(record, nodePointersToCheckCardinalityFor, changeMap);

			// validate attributes
			Set<Node<?>> nodesToCheckValidationFor = new HashSet<Node<?>>();
			nodesToCheckValidationFor.addAll(pointersToNodes(entityDescendantAndSelfPointers));
			nodesToCheckValidationFor.addAll(pointersToNodes(updatedCardinalityPointers));

			Set<Attribute<?, ?>> attributesToValidate = filterAttributes(nodesToCheckValidationFor);
			attributesToValidate.addAll(record.determineValidationDependentNodes(nodesToCheckValidationFor));
			validateAttributes(record, attributesToValidate, changeMap);
		}
		return changeMap;
	}

	private void addEmptyNodes(Entity entity) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		addEmptyEnumeratedEntities(entity);
		EntityDefinition entityDefn = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitionsInVersion(version);
		for (NodeDefinition childDefn : childDefinitions) {
			if (entity.getCount(childDefn) == 0) {
				boolean isMultipleEntity = childDefn instanceof EntityDefinition && childDefn.isMultiple();
				if (configuration.addEmptyMultipleEntitiesWhenAddingNewEntities || !isMultipleEntity) {
					int toBeInserted = entity.getMinCount(childDefn);
					if (toBeInserted <= 0 && childDefn instanceof AttributeDefinition || !childDefn.isMultiple()) {
						// insert at least one node
						toBeInserted = 1;
					}
					addEmptyChildren(entity, childDefn, toBeInserted);
				}
			} else {
				entity.visitChildren(childDefn, new NodeVisitor() {
					public void visit(Node<? extends NodeDefinition> child, int idx) {
						if (child instanceof Entity) {
							addEmptyNodes((Entity) child);
						}
					}
				});
			}
		}
	}

	private int addEmptyChildren(Entity entity, NodeDefinition childDefn, int toBeInserted) {
		CollectSurvey survey = (CollectSurvey) entity.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		int count = 0;
		if (!childDefn.isMultiple() || annotations.isAutoGenerateMinItems(childDefn) || survey.isCollectEarth()) {
			while (count < toBeInserted) {
				if (childDefn instanceof AttributeDefinition) {
					Node<?> createdNode = childDefn.createNode();
					entity.add(createdNode);
				} else if (childDefn instanceof EntityDefinition) {
					Entity childEntity = performEntityAdd(entity, (EntityDefinition) childDefn);
					addEmptyNodes(childEntity);
				}
				count++;
			}
		}
		return count;
	}

	private Value applyInitialValue(Attribute<?, ?> attr) {
		return applyInitialValue(attr, false);
	}

	private Value applyInitialValue(Attribute<?, ?> attr, boolean onlyIfEmpty) {
		if (!attr.getDefinition().isCalculated() && attr.isRelevant()
				&& (attr.isEmpty() || (!onlyIfEmpty && attr.isDefaultValueApplied()))) {
			if (canApplyDefaultValueInCurrentPhase(attr)) {
				// check if default value can be applied
				Value value = performDefaultValueApply(attr);
				if (value != null) {
					return value;
				}
			}
			if (attr instanceof BooleanAttribute
					&& ((BooleanAttributeDefinition) attr.getDefinition()).isAffirmativeOnly()) {
				// apply value "false" to boolean attributes with "checkbox" layout
				Value value = new BooleanValue(false);
				BooleanAttribute boolAttr = (BooleanAttribute) attr;
				boolAttr.setValue((BooleanValue) value);
				boolAttr.updateSummaryInfo();
				return value;
			}
		}
		return null;
	}

	private boolean canApplyDefaultValueInCurrentPhase(Attribute<?, ?> attr) {
		Survey survey = attr.getSurvey();
		if (survey instanceof CollectSurvey) {
			CollectAnnotations annotations = ((CollectSurvey) survey).getAnnotations();
			Step recordStep = ((CollectRecord) attr.getRecord()).getStep();
			AttributeDefinition def = attr.getDefinition();
			Step stepToApplyDefaultValue = annotations.getPhaseToApplyDefaultValue(def);
			return recordStep.compareTo(stepToApplyDefaultValue) >= 0;
		} else {
			return false;
		}
	}

	private void addEmptyEnumeratedEntities(Entity parentEntity) {
		Record record = parentEntity.getRecord();
		ModelVersion version = record.getVersion();
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		List<NodeDefinition> childDefinitions = parentEntityDefn.getChildDefinitionsInVersion(version);
		for (NodeDefinition childDefn : childDefinitions) {
			if (childDefn instanceof EntityDefinition) {
				EntityDefinition childEntityDefn = (EntityDefinition) childDefn;
				if (childEntityDefn.isMultiple() && childEntityDefn.isEnumerable() && childEntityDefn.isEnumerate()) {
					addEmptyEnumeratedEntities(parentEntity, childEntityDefn);
				}
			}
		}
	}

	private void addEmptyEnumeratedEntities(Entity parentEntity, EntityDefinition enumerableEntityDefn) {
		Record record = parentEntity.getRecord();
		ModelVersion version = record.getVersion();
		CodeAttributeDefinition enumeratingCodeDefn = enumerableEntityDefn.getEnumeratingKeyCodeAttribute(version);
		if (enumeratingCodeDefn != null) {
			CodeList list = enumeratingCodeDefn.getList();
			Survey survey = record.getSurvey();
			CodeListService codeListService = survey.getContext().getCodeListService();
			List<CodeListItem> items = codeListService.loadRootItems(list);
			int i = 0;
			for (CodeListItem item : items) {
				if (version == null || version.isApplicable(item)) {
					String code = item.getCode();
					Entity enumeratedEntity = parentEntity.getEnumeratedEntity(enumerableEntityDefn,
							enumeratingCodeDefn, code);
					if (enumeratedEntity == null) {
						Entity addedEntity = performEntityAdd(parentEntity, enumerableEntityDefn, i);
						addEmptyNodes(addedEntity);
						// set the value of the key CodeAttribute
						CodeAttribute addedCode = (CodeAttribute) addedEntity.getChild(enumeratingCodeDefn, 0);
						addedCode.setValue(new Code(code));
						addedCode.updateSummaryInfo();
					} else if (enumeratedEntity.getIndex() != i) {
						parentEntity.move(enumerableEntityDefn, enumeratedEntity.getIndex(), i);
					}
					i++;
				}
			}
		}
	}

	private int calculateMinCount(NodePointer nodePointer) {
		return calculateMinCount(nodePointer.getEntity(), nodePointer.getChildDefinition());
	}

	private int calculateMinCount(Entity entity, NodeDefinition defn) {
		String expression = defn.getMinCountExpression();
		if (!entity.isRelevant(defn) || StringUtils.isBlank(expression)) {
			return 0;
		}
		if (defn.getFixedMinCount() != null) {
			return defn.getFixedMinCount();
		}
		try {
			SurveyContext<?> surveyContext = defn.getSurvey().getContext();
			ExpressionEvaluator expressionEvaluator = surveyContext.getExpressionEvaluator();
			Number value = expressionEvaluator.evaluateNumericValue(entity, null, expression);
			return value == null ? 0 : value.intValue();
		} catch (InvalidExpressionException e) {
			throw new IdmInterpretationError("Error evaluating required expression", e);
		}
	}

	private int calculateMaxCount(NodePointer nodePointer) {
		return calculateMaxCount(nodePointer.getEntity(), nodePointer.getChildDefinition());
	}

	private int calculateMaxCount(Entity entity, NodeDefinition defn) {
		String expression = defn.getMaxCountExpression();
		if (!entity.isRelevant(defn) || StringUtils.isBlank(expression)) {
			return defn.isMultiple() ? Integer.MAX_VALUE : 1;
		}
		if (defn.getFixedMaxCount() != null) {
			return defn.getFixedMaxCount();
		}
		try {
			SurveyContext<?> surveyContext = defn.getSurvey().getContext();
			ExpressionEvaluator expressionEvaluator = surveyContext.getExpressionEvaluator();
			Number value = expressionEvaluator.evaluateNumericValue(entity, null, expression);
			return value == null ? Integer.MAX_VALUE : value.intValue();
		} catch (InvalidExpressionException e) {
			throw new IdmInterpretationError("Error evaluating required expression", e);
		}
	}

	private List<NodePointer> getDescendantNodePointers(Entity entity) {
		ModelVersion version = entity.getRecord().getVersion();
		List<NodePointer> pointers = new ArrayList<NodePointer>();
		EntityDefinition definition = entity.getDefinition();
		for (NodeDefinition childDef : definition.getChildDefinitionsInVersion(version)) {
			pointers.add(new NodePointer(entity, childDef));
			if (childDef instanceof EntityDefinition) {
				for (Node<?> childEntity : entity.getChildren(childDef)) {
					pointers.addAll(getDescendantNodePointers((Entity) childEntity));
				}
			}
		}
		return pointers;
	}

	private List<NodePointer> getDescendantAndSelfNodePointers(Entity entity) {
		List<NodePointer> descendantNodePointers = getDescendantNodePointers(entity);

		List<NodePointer> pointers = new ArrayList<NodePointer>(descendantNodePointers);

		if (!entity.isRoot()) {
			NodePointer selfPointer = new NodePointer(entity);
			pointers.add(0, selfPointer);
		}
		return pointers;
	}

	private List<NodePointer> getAncestorPointers(Node<?> node) {
		if (node.getParent() == null) {
			return new ArrayList<NodePointer>();
		} else {
			return getAncestorPointers(new NodePointer(node));
		}
	}

	private List<NodePointer> getAncestorPointers(NodePointer nodePointer) {
		List<NodePointer> pointers = new ArrayList<NodePointer>();
		Entity currentEntity = nodePointer.getEntity();
		while (currentEntity.getParent() != null) {
			pointers.add(new NodePointer(currentEntity));
			currentEntity = currentEntity.getParent();
		}
		return pointers;
	}

	private List<NodePointer> getAncestorsAndSelfPointers(Node<?> node) {
		List<NodePointer> pointers = getAncestorPointers(node);
		pointers.add(new NodePointer(node));
		return pointers;
	}

	private List<NodePointer> getAncestorsAndSelfPointers(NodePointer nodePointer) {
		List<NodePointer> pointers = getAncestorPointers(nodePointer);
		pointers.add(nodePointer);
		return pointers;
	}

	private Set<NodePointer> determineDependentCodeAttributes(Collection<NodePointer> attributePointers) {
		Set<CodeAttribute> dependentAttributes = new HashSet<CodeAttribute>();
		for (NodePointer updatedAttributePointer : attributePointers) {
			if (updatedAttributePointer.getChildDefinition() instanceof CodeAttributeDefinition) {
				dependentAttributes.addAll(
						updatedAttributePointer.getRecord().determineDependentCodeAttributes(updatedAttributePointer));
			}
		}
		Set<NodePointer> dependentAttributesPointers = nodesToPointers(dependentAttributes);
		return dependentAttributesPointers;
	}

	private Set<Attribute<?, ?>> filterAttributes(Collection<Node<?>> nodes) {
		Set<Attribute<?, ?>> attributes = new HashSet<Attribute<?, ?>>();
		for (Node<?> node : nodes) {
			if (node instanceof Attribute) {
				attributes.add((Attribute<?, ?>) node);
			}
		}
		return attributes;
	}

	public void setValidateAfterUpdate(boolean validateAfterUpdate) {
		this.configuration.validateAfterUpdate = validateAfterUpdate;
	}

	public void setClearNotRelevantAttributes(boolean clearNotRelevantAttributes) {
		this.configuration.clearNotRelevantAttributes = clearNotRelevantAttributes;
	}

	public void setClearDependentCodeAttributes(boolean clearDependentCodeAttributes) {
		this.configuration.clearDependentCodeAttributes = clearDependentCodeAttributes;
	}

	public void setAddEmptyMultipleEntitiesWhenAddingNewEntities(
			boolean addEmptyMultipleEntitiesWhenAddingNewEntities) {
		this.configuration.addEmptyMultipleEntitiesWhenAddingNewEntities = addEmptyMultipleEntitiesWhenAddingNewEntities;
	}

	public void setAlwaysEvaluateCalculatedAttributes(boolean alwaysEvaluateCalculatedAttributes) {
		this.configuration.alwaysEvaluateCalculatedAttributes = alwaysEvaluateCalculatedAttributes;
	}

	public RecordUpdateConfiguration getUpdateConfiguration() {
		return configuration;
	}

	public void setUpdateConfiguration(RecordUpdateConfiguration configuration) {
		this.configuration = configuration;
	}

	public static class VirtualEntityPopuplator {

		private RecordUpdater recordUpdater;

		public VirtualEntityPopuplator(RecordUpdater recordUpdater) {
			super();
			this.recordUpdater = recordUpdater;
		}

		public NodeChangeSet populateVirtualEntitites(final Record record) {
			final NodeChangeMap result = new NodeChangeMap();
			Entity rootEntity = record.getRootEntity();
			rootEntity.traverse(new NodeVisitor() {
				public void visit(Node<? extends NodeDefinition> node, int idx) {
					if (node instanceof Entity) {
						Entity parentEntity = (Entity) node;
						EntityDefinition parentEntityDef = parentEntity.getDefinition();
						List<NodeDefinition> childDefs = parentEntityDef.getChildDefinitions();
						for (NodeDefinition childDef : childDefs) {
							if (childDef instanceof EntityDefinition) {
								EntityDefinition childEntityDef = (EntityDefinition) childDef;
								if (childEntityDef.isVirtual()) {
									result.addMergeChanges(recordUpdater.deleteChildren(parentEntity, childEntityDef));
									result.addMergeChanges(populateVirtualEntity(parentEntity, childEntityDef));
								}
							}
						}
					}
				}
			});
			return result;
		}

		/**
		 * Populates a virtual entity cloning entities according to it's
		 * generatorExpression
		 */
		public NodeChangeSet populateVirtualEntity(Entity parentEntity, EntityDefinition entityDef) {
			NodeChangeMap result = new NodeChangeMap();
			String generatorExpression = entityDef.getGeneratorExpression();
			ExpressionEvaluator expressionEvaluator = parentEntity.getSurvey().getContext().getExpressionEvaluator();
			try {
				List<Node<?>> entities = expressionEvaluator.evaluateNodes(parentEntity, null, generatorExpression);
				for (Node<?> entity : entities) {
					result.addMergeChanges(duplicateEntity(parentEntity, entityDef, (Entity) entity));
				}
			} catch (InvalidExpressionException e) {
				throw new RuntimeException(e);
			}
			return result;
		}

		public NodeChangeSet duplicateEntity(Entity parentDestEntity, EntityDefinition destEntityDef,
				Entity sourceEntity) {
			NodeChangeMap result = new NodeChangeMap();
			result.addMergeChanges(recordUpdater.addEntity(parentDestEntity, destEntityDef.getName()));
			Entity newEntity = parentDestEntity.getLastChild(destEntityDef.getName());
			List<Node<?>> children = ((Entity) sourceEntity).getChildren();
			for (Node<?> child : children) {
				if (destEntityDef.containsChildDefinition(child.getName())
						&& destEntityDef.getChildDefinition(child.getName()).getClass()
								.isAssignableFrom(child.getDefinition().getClass())) {
					if (child instanceof Entity) {
						result.addMergeChanges(duplicateEntity(newEntity,
								(EntityDefinition) destEntityDef.getChildDefinition(child.getName()), (Entity) child));
					} else {
						// duplicate attribute
						if (child.getDefinition().isMultiple()) {
							result.addMergeChanges(recordUpdater.addAttribute(newEntity, child.getName()));
							Attribute<?, Value> newAttr = newEntity.getLastChild(child.getName());
							result.addMergeChanges(
									recordUpdater.updateAttribute(newAttr, ((Attribute<?, ?>) child).getValue()));
						} else {
							Attribute<?, Value> newAttr = newEntity.getChild(child.getName()); // node already inserted
																								// during entity
																								// creation
							result.addMergeChanges(
									recordUpdater.updateAttribute(newAttr, ((Attribute<?, ?>) child).getValue()));
						}
					}
				}
			}
			return result;
		}
	}
}
