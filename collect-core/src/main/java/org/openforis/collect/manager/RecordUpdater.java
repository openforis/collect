package org.openforis.collect.manager;

import static org.openforis.collect.model.CollectRecord.APPROVED_MISSING_POSITION;
import static org.openforis.collect.model.CollectRecord.CONFIRMED_ERROR_POSITION;
import static org.openforis.collect.model.CollectRecord.DEFAULT_APPLIED_POSITION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.model.AttributeChange;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.EntityChange;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.NodeChangeMap;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.BooleanAttribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Value;
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
	public <V extends Value> NodeChangeSet updateAttribute(
			Attribute<? extends NodeDefinition, V> attribute,
			V value) {
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
	public NodeChangeSet updateAttribute(
			Attribute<?, ?> attribute,
			FieldSymbol symbol) {
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
	public <V> NodeChangeSet updateField(
			Field<V> field, V value) {
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
	public <V> NodeChangeSet updateField(
			Field<V> field, FieldSymbol symbol) {
		Attribute<?, ?> attribute = field.getAttribute();

		beforeAttributeUpdate(attribute);

		field.setValue(null);
		setFieldSymbol(field, symbol);
		
		return afterAttributeUpdate(attribute);
	}

	/**
	 * Adds a new entity to a the record.
	 * 
	 * @param parentEntity
	 * @param nodeName
	 * @return Changes applied to the record 
	 */
	public NodeChangeSet addEntity(Entity parentEntity, String nodeName) {
		Entity createdNode = performEntityAdd(parentEntity, nodeName);
		
		setMissingValueApproved(parentEntity, nodeName, false);

		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareAddEntityChange(createdNode);
		
		evaluateDependentCalculatedAttributes(changeMap, createdNode);
		
		Set<NodePointer> relevanceRequirenessDependencies = createdNode.getRelevantRequiredDependencies();
		relevanceRequirenessDependencies.add(new NodePointer(createdNode.getParent(), nodeName));
		Set<Attribute<?, ?>> checkDependencies = null;
		
		List<NodePointer> ancestorNodeDependencies = createAncestorNodeDependencies(createdNode);

		prepareChange(changeMap, relevanceRequirenessDependencies, checkDependencies, ancestorNodeDependencies);
		return new NodeChangeSet(changeMap.getChanges());
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
	public NodeChangeSet addAttribute(
			Entity parentEntity, String attributeName, Value value, FieldSymbol symbol, 
			String remarks) {
		Attribute<?, ?> attribute = performAttributeAdd(parentEntity, attributeName, value, symbol, remarks);
		
		setMissingValueApproved(parentEntity, attributeName, false);
		
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareAddAttributeChange(attribute);
		
		return afterAttributeInsertOrUpdate(changeMap, attribute);
	}

	/**
	 * Updates the remarks of a Field
	 * 
	 * @param field
	 * @param remarks
	 * @return
	 */
	public NodeChangeSet updateRemarks(
			Field<?> field, String remarks) {
		field.setRemarks(remarks);
		NodeChangeMap changeMap = new NodeChangeMap();
		Attribute<?, ?> attribute = field.getAttribute();
		changeMap.prepareAttributeChange(attribute);
		return new NodeChangeSet(changeMap.getChanges());
	}

	public NodeChangeSet approveMissingValue(
			Entity parentEntity, String nodeName) {
		setMissingValueApproved(parentEntity, nodeName, true);
		List<NodePointer> cardinalityNodePointers = createAncestorNodeDependencies(parentEntity);
		cardinalityNodePointers.add(new NodePointer(parentEntity, nodeName));
		NodeChangeMap changeMap = new NodeChangeMap();
		validateAll(changeMap, cardinalityNodePointers, false);
		return new NodeChangeSet(changeMap.getChanges());
	}

	public NodeChangeSet confirmError(Attribute<?, ?> attribute) {
		Set<Attribute<?,?>> checkDependencies = new HashSet<Attribute<?,?>>();
		setErrorConfirmed(attribute, true);
		attribute.clearValidationResults();
		checkDependencies.add(attribute);
		
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareAttributeChange(attribute);
		validateChecks(changeMap, checkDependencies);
		return new NodeChangeSet(changeMap.getChanges());
	}
	
	/**
	 * Applies the default value to an attribute, if any.
	 * The applied default value will be the first one having verified the "condition".
	 *  
	 * @param attribute
	 * @return
	 */
	public NodeChangeSet applyDefaultValue(
			Attribute<?, ?> attribute) {
		performDefaultValueApply(attribute);
		return afterAttributeUpdate(attribute);
	}

	protected <V extends Value> void beforeAttributeUpdate(
			Attribute<? extends NodeDefinition, V> attribute) {
		Entity parentEntity = attribute.getParent();
		setErrorConfirmed(attribute, false);
		setMissingValueApproved(parentEntity, attribute.getName(), false);
		setDefaultValueApplied(attribute, false);
	}

	protected <V extends Value> NodeChangeSet afterAttributeUpdate(
			Attribute<?, V> attribute) {
		NodeChangeMap changeMap = new NodeChangeMap();
		AttributeChange change = changeMap.prepareAttributeChange(attribute);
		Map<Integer, Object> updatedFieldValues = createFieldValuesMap(attribute);
		change.setUpdatedFieldValues(updatedFieldValues);
		return afterAttributeInsertOrUpdate(changeMap, attribute);
	}

	protected <V extends Value> NodeChangeSet afterAttributeInsertOrUpdate(
			NodeChangeMap changeMap, Attribute<? extends NodeDefinition, V> attribute) {
		evaluateDependentCalculatedAttributes(changeMap, attribute);
		
		Set<NodePointer> relevanceRequiredDependencies = attribute.getRelevantRequiredDependencies();
		relevanceRequiredDependencies.add(new NodePointer(attribute.getParent(), attribute.getName()));
		
		Set<Attribute<?, ?>> checkDependencies = attribute.getCheckDependencies();
		checkDependencies.add(attribute);
		
		List<NodePointer> ancestorNodeDependencies = createAncestorNodeDependencies(attribute);

		prepareChange(changeMap, relevanceRequiredDependencies, checkDependencies, ancestorNodeDependencies);

		return new NodeChangeSet(changeMap.getChanges());
	}

	/**
	 * Deletes a node from the record.
	 * 
	 * @param node
	 * @return
	 */
	public NodeChangeSet deleteNode(Node<?> node) {
		Set<NodePointer> relevanceDependencies = new HashSet<NodePointer>();
		Set<NodePointer> requirenessDependencies = new HashSet<NodePointer>();
		List<Attribute<?, ?>> dependentCalculatedAttributes = new ArrayList<Attribute<?,?>>();
		
		HashSet<Attribute<?, ?>> checkDependencies = new HashSet<Attribute<?,?>>();
		
		NodeChangeMap changeMap = new NodeChangeMap();
		changeMap.prepareDeleteNodeChange(node);

		List<NodePointer> ancestorNodeDependencies = createAncestorNodeDependencies(node);

		List<Node<?>> nodesToDelete = new ArrayList<Node<?>>();
		if ( node instanceof Entity ) {
			List<Node<?>> descendants = ((Entity) node).getDescendants();
			nodesToDelete.addAll(descendants);
		}
		nodesToDelete.add(node);
		
		for (Node<?> n : nodesToDelete) {
			relevanceDependencies.addAll(n.getRelevantDependencies());
			requirenessDependencies.addAll(n.getRequiredDependencies());
			if ( n instanceof Attribute ) {
				checkDependencies.addAll(((Attribute<?, ?>) n).getCheckDependencies());
			}
			node.clearDependencyStates();
			List<Attribute<?, ?>> nestedDependentCalculatedAttributes = node.getDependentCalculatedAttributes(true);
			for (Attribute<?, ?> calcAttr : nestedDependentCalculatedAttributes) {
				if ( dependentCalculatedAttributes.contains(calcAttr) ) {
					dependentCalculatedAttributes.add(calcAttr);
				}
			}
			performNodeDeletion(n);
		}

		// evaluate calculated attributes
		for (int i = dependentCalculatedAttributes.size() - 1; i >= 0; i--) {
			Attribute<?,?> calcAttr = dependentCalculatedAttributes.get(i);
			if ( ! calcAttr.isDetached() ) {
				evaluateCalculatedAttribute(changeMap, calcAttr);
			}
			
		}
		HashSet<NodePointer> relevanceAndRequirenessDependencies = new HashSet<NodePointer>();
		relevanceAndRequirenessDependencies.addAll(relevanceDependencies);
		relevanceAndRequirenessDependencies.addAll(requirenessDependencies);
		
		prepareChange(changeMap, relevanceAndRequirenessDependencies, checkDependencies, ancestorNodeDependencies);
		return new NodeChangeSet(changeMap.getChanges());
	}
	
	public void moveNode(CollectRecord record, int nodeId, int index) {
		Node<?> node = record.getNodeByInternalId(nodeId);
		Entity parent = node.getParent();
		String name = node.getName();
		List<Node<?>> siblings = parent.getAll(name);
		int oldIndex = siblings.indexOf(node);
		parent.move(name, oldIndex, index);
	}
	
	private void evaluateCalculatedAttribute(NodeChangeMap changeMap, Attribute<?, ?> calcAttr) {
		calcAttr.clearValue(false);
		//force evaluation of calculated value after it has been already cleaned
		calcAttr.getValue();
		AttributeChange change = changeMap.prepareAttributeChange(calcAttr);
		Map<Integer, Object> updatedFieldValues = createFieldValuesMap(calcAttr);
		change.setUpdatedFieldValues(updatedFieldValues);
	}
	
	private void evaluateDependentCalculatedAttributes(NodeChangeMap changeMap, Node<?> node) {
		List<Attribute<?, ?>> dependentCalculatedAttributes = node.getDependentCalculatedAttributes(true);
		for (Attribute<?, ?> calcAttr : dependentCalculatedAttributes) {
			evaluateCalculatedAttribute(changeMap, calcAttr);
		}
	}
	
	protected Node<?> performNodeDeletion(Node<?> node) {
		if(node.isDetached()) {
			throw new IllegalArgumentException("Unable to delete a node already detached");
		}
		Entity parentEntity = node.getParent();
		int index = node.getIndex();
		Node<?> deletedNode = parentEntity.remove(node.getName(), index);
		CollectRecord record = (CollectRecord) parentEntity.getRecord();
		record.removeValidationCache(deletedNode.getInternalId());
		return deletedNode;
	}

	/**
	 * Applies default values on each descendant attribute of a record in which empty nodes have already been added.
	 * Default values are applied only to "empty" attributes.
	 * @param record 
	 * 
	 * @throws InvalidExpressionException 
	 */
	protected void applyDefaultValues(CollectRecord record) {
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
	protected void applyDefaultValues(Entity entity) {
		List<Node<?>> children = entity.getChildren();
		for (Node<?> child: children) {
			if ( child instanceof Attribute ) {
				Attribute<?, ?> attribute = (Attribute<?, ?>) child;
				if ( attribute.isEmpty() ) {
					performDefaultValueApply(attribute);
				}
			} else if ( child instanceof Entity ) {
				applyDefaultValues((Entity) child);
			}
		}
	}
	
	protected <V> void setFieldSymbol(Field<V> field, FieldSymbol symbol){
		Character symbolChar = null;
		if (symbol != null) {
			symbolChar = symbol.getCode();
		}
		field.setSymbol(symbolChar);
	}
	
	protected Map<Integer, Object> createFieldValuesMap(
			Attribute<?, ?> attribute) {
		Map<Integer, Object> fieldValues = new HashMap<Integer, Object>();
		int fieldCount = attribute.getFieldCount();
		for (int idx = 0; idx < fieldCount; idx ++) {
			Field<?> field = attribute.getField(idx);
			fieldValues.put(idx, field.getValue());
		}
		return fieldValues;
	}

	protected <V extends Value> void setSymbolOnFields(
			Attribute<? extends NodeDefinition, V> attribute,
			FieldSymbol symbol) {
		for (Field<?> field : attribute.getFields()) {
			setFieldSymbol(field, symbol);
		}
	}
	
	protected <V extends Value> void setRemarksOnFirstField(
			Attribute<? extends NodeDefinition, V> attribute,
			String remarks) {
		Field<?> field = attribute.getField(0);
		field.setRemarks(remarks);
	}
	
	protected void setErrorConfirmed(Attribute<?,?> attribute, boolean confirmed){
		int fieldCount = attribute.getFieldCount();
		for( int i=0; i <fieldCount; i++ ){
			Field<?> field = attribute.getField(i);
			field.getState().set(CONFIRMED_ERROR_POSITION, confirmed);
		}
	}
	
	protected void setMissingValueApproved(Entity parentEntity, String childName, boolean approved) {
		org.openforis.idm.model.State childState = parentEntity.getChildState(childName);
		childState.set(APPROVED_MISSING_POSITION, approved);
	}
	
	protected void setDefaultValueApplied(Attribute<?, ?> attribute, boolean applied) {
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
	protected <V extends Value> void performDefaultValueApply(Attribute<?, V> attribute) {
		AttributeDefinition attributeDefn = (AttributeDefinition) attribute.getDefinition();
		List<AttributeDefault> defaults = attributeDefn.getAttributeDefaults();
		if ( defaults != null && defaults.size() > 0 ) {
			for (AttributeDefault attributeDefault : defaults) {
				try {
					if ( attributeDefault.evaluateCondition(attribute) ) {
						V value = attributeDefault.evaluate(attribute);
						if ( value != null ) {
							attribute.setValue(value);
							setDefaultValueApplied(attribute, true);
							break;
						}
					}
				} catch (InvalidExpressionException e) {
					throw new RuntimeException("Error applying default value for attribute " + attributeDefn.getPath());
				}
			}
		}
	}
	
	protected List<NodePointer> createAncestorNodeDependencies(Node<?> node){
		List<NodePointer> nodePointers = new ArrayList<NodePointer>();
		
		Entity parent = node.getParent();
		String childName = node.getName();
		while(parent != null){
			NodePointer nodePointer = new NodePointer(parent, childName);
			nodePointers.add(nodePointer);
			
			childName = parent.getName();
			parent = parent.getParent();
		}
		return nodePointers;
	}

	protected void prepareChange(NodeChangeMap changeMap, Set<NodePointer> relevanceRequiredDependencies, 
			Collection<Attribute<?, ?>> checkDependencies, List<NodePointer> ancestorNodeDependencies) {
		validateAll(changeMap, ancestorNodeDependencies, false);
		validateAll(changeMap, relevanceRequiredDependencies, true);
		validateChecks(changeMap, checkDependencies);
	}
	
	protected void validateChecks(NodeChangeMap changeMap,
			Collection<Attribute<?, ?>> attributes) {
		if (attributes != null) {
			for (Attribute<?, ?> attr : attributes) {
				validateAttribute(changeMap, attr);
			}
		}
	}

	protected void validateAttribute(NodeChangeMap changeMap,
			Attribute<?, ?> attr) {
		if ( !attr.isDetached() ) {
			attr.clearValidationResults();
			ValidationResults results = attr.validateValue();
			AttributeChange change = changeMap.prepareAttributeChange(attr);
			change.setValidationResults(results);
		}
	}

	protected void validateChecks(NodeChangeMap changeMap,
			Entity entity, String childName) {
		List<Node<?>> children = entity.getAll(childName);
		for ( Node<?> node : children ) {
			if ( node instanceof Attribute ){
				validateAttribute(changeMap, (Attribute<?, ?>) node);
			}
		}
	}
	
	protected void validateAll(NodeChangeMap changeMap,
			Collection<NodePointer> nodePointers, boolean validateChecks) {
		if (nodePointers != null) {
			for (NodePointer nodePointer : nodePointers) {
				Entity parent = nodePointer.getEntity();
				if ( parent != null && ! parent.isDetached()) {
					validateCardinality(changeMap, nodePointer);
					validateRelevanceState(changeMap, nodePointer);
					validateRequirenessState(changeMap, nodePointer);
					if ( validateChecks ) {
						validateChecks(changeMap, parent, nodePointer.getChildName());
					}
				}
			}
		}
	}

	protected void validateCardinality(NodeChangeMap changeMap, NodePointer nodePointer) {
		Entity entity = nodePointer.getEntity();
		String childName = nodePointer.getChildName();
		EntityChange change = changeMap.prepareEntityChange(entity);
		change.setChildrenMinCountValidation(childName, entity.validateMinCount(childName));
		change.setChildrenMaxCountValidation(childName, entity.validateMaxCount(childName));
	}

	protected void validateRelevanceState(
			NodeChangeMap changeMap, NodePointer nodePointer) {
		Entity entity = nodePointer.getEntity();
		String childName = nodePointer.getChildName();
		EntityChange change = changeMap.prepareEntityChange(entity);
		change.setChildrenRelevance(childName, entity.isRelevant(childName));
	}

	protected void validateRequirenessState(
			NodeChangeMap changeMap, NodePointer nodePointer) {
		Entity entity = nodePointer.getEntity();
		String childName = nodePointer.getChildName();
		EntityChange change = changeMap.prepareEntityChange(entity);
		change.setChildrenRequireness(childName, entity.isRequired(childName));
	}

	protected Attribute<?, ?> performAttributeAdd(Entity parentEntity, String nodeName, Value value, 
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
	
	protected Entity performEntityAdd(Entity parentEntity, String nodeName) {
		Entity entity = EntityBuilder.addEntity(parentEntity, nodeName);
		addEmptyNodes(entity);
		return entity;
	}

	protected Entity performEntityAdd(Entity parentEntity, String nodeName, int idx) {
		Entity entity = EntityBuilder.addEntity(parentEntity, nodeName, idx);
		addEmptyNodes(entity);
		return entity;
	}
	
	protected void addEmptyNodes(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		addEmptyNodes(rootEntity);
	}
	
	protected void addEmptyNodes(Entity entity) {
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

	protected int addEmptyChildren(Entity entity, NodeDefinition childDefn, int toBeInserted) {
		String childName = childDefn.getName();
		CollectSurvey survey = (CollectSurvey) entity.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
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
					RecordUpdater recordUpdater = new RecordUpdater();
					recordUpdater.performEntityAdd(entity, childName);
				}
				count ++;
			}
		}
		return count;
	}
	
	private void setInitialValue(Attribute<?, ?> attr) {
		if ( attr instanceof BooleanAttribute && ((BooleanAttributeDefinition) attr.getDefinition()).isAffirmativeOnly() ) {
			BooleanAttribute boolAttr = (BooleanAttribute) attr;
			boolAttr.setValue(new BooleanValue(false));
		}
	}

	protected void addEmptyEnumeratedEntities(Entity parentEntity) {
		Record record = parentEntity.getRecord();
		CollectSurvey survey = (CollectSurvey) parentEntity.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
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

	protected void addEmptyEnumeratedEntities(Entity parentEntity, EntityDefinition enumerableEntityDefn) {
		Record record = parentEntity.getRecord();
		ModelVersion version = record.getVersion();
		CodeAttributeDefinition enumeratingCodeDefn = enumerableEntityDefn.getEnumeratingKeyCodeAttribute(version);
		if(enumeratingCodeDefn != null) {
			String enumeratedEntityName = enumerableEntityDefn.getName();
			CodeList list = enumeratingCodeDefn.getList();
			Survey survey = record.getSurvey();
			CodeListService codeListService = survey.getContext().getCodeListService();
			List<CodeListItem> items = codeListService.loadRootItems(list);
			for (int i = 0; i < items.size(); i++) {
				CodeListItem item = items.get(i);
				if(version == null || version.isApplicable(item)) {
					String code = item.getCode();
					Entity enumeratedEntity = parentEntity.getEnumeratedEntity(enumerableEntityDefn, enumeratingCodeDefn, code);
					if( enumeratedEntity == null ) {
						Entity addedEntity = performEntityAdd(parentEntity, enumeratedEntityName, i);
						//set the value of the key CodeAttribute
						CodeAttribute addedCode = (CodeAttribute) addedEntity.get(enumeratingCodeDefn.getName(), 0);
						addedCode.setValue(new Code(code));
					} else {
						parentEntity.move(enumeratedEntityName, enumeratedEntity.getIndex(), i);
					}
				}
			}
		}
	}

}
