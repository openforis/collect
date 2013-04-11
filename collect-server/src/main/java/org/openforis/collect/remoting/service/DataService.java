/**
 * 
 */
package org.openforis.collect.remoting.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.RecordFileException;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordIndexException;
import org.openforis.collect.manager.RecordIndexManager.SearchType;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordPromoteException;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.NodeProxy;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.remoting.service.UpdateRequestOperation.Method;
import org.openforis.collect.remoting.service.recordindex.RecordIndexService;
import org.openforis.collect.spring.MessageContextHolder;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
import org.openforis.idm.model.NumericRange;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class DataService {
	
	@Autowired
	private SessionManager sessionManager;
	@Autowired
	private transient RecordManager recordManager;
	@Autowired
	private transient RecordFileManager fileManager;
	@Autowired
	private transient RecordIndexService recordIndexService;
	@Autowired
	private MessageContextHolder messageContextHolder;

	/**
	 * it's true when the root entity definition of the record in session has some nodes with the "collect:index" annotation
	 */
	private boolean hasActiveSurveyIndexedNodes;

	@Transactional
	@Secured("ROLE_ENTRY")
	public RecordProxy loadRecord(int id, int step, boolean forceUnlock) throws RecordPersistenceException, RecordIndexException {
		SessionState sessionState = sessionManager.getSessionState();
		if ( sessionState.isActiveRecordBeingEdited() ) {
			throw new MultipleEditException();
		}
		final CollectSurvey survey = sessionState.getActiveSurvey();
		User user = sessionState.getUser();
		CollectRecord record = recordManager.checkout(survey, user, id, step, sessionState.getSessionId(), forceUnlock);
		Entity rootEntity = record.getRootEntity();
		recordManager.addEmptyNodes(rootEntity);
		sessionManager.setActiveRecord(record);
		fileManager.reset();
		prepareRecordIndexing();
		return new RecordProxy(messageContextHolder, record);
	}

	protected void prepareRecordIndexing() throws RecordIndexException {
		CollectRecord record = getActiveRecord();
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		hasActiveSurveyIndexedNodes = recordIndexService.hasIndexableNodes(rootEntityDefn);
		recordIndexService.cleanTemporaryIndex();
	}
	
	/**
	 * 
	 * @param rootEntityName
	 * @param offset
	 * @param toIndex
	 * @param orderByFieldName
	 * @param filter
	 * 
	 * @return map with "count" and "records" items
	 */
	@Transactional
	@Secured("ROLE_ENTRY")
	public Map<String, Object> loadRecordSummaries(String rootEntityName, int offset, int maxNumberOfRows, List<RecordSummarySortField> sortFields, String[] keyValues) {
		Map<String, Object> result = new HashMap<String, Object>();
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		Schema schema = activeSurvey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		String rootEntityDefinitionName = rootEntityDefinition.getName();
		int count = recordManager.getRecordCount(activeSurvey, rootEntityDefinitionName, keyValues);
		List<CollectRecord> summaries = recordManager.loadSummaries(activeSurvey, rootEntityDefinitionName, offset, maxNumberOfRows, sortFields, keyValues);
		List<RecordProxy> proxies = RecordProxy.fromList(messageContextHolder, summaries);
		result.put("count", count);
		result.put("records", proxies);
		return result;
	}

	@Transactional
	@Secured("ROLE_ENTRY")
	public RecordProxy createRecord(String rootEntityName, String versionName) throws RecordPersistenceException, RecordIndexException {
		SessionState sessionState = sessionManager.getSessionState();
		if ( sessionState.isActiveRecordBeingEdited() ) {
			throw new MultipleEditException();
		}
		String sessionId = sessionState.getSessionId();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		User user = sessionState.getUser();
		Schema schema = activeSurvey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		CollectRecord record = recordManager.create(activeSurvey, rootEntityDefinition, user, versionName, sessionId);
		Entity rootEntity = record.getRootEntity();
		recordManager.addEmptyNodes(rootEntity);
		sessionManager.setActiveRecord(record);
		prepareRecordIndexing();
		RecordProxy recordProxy = new RecordProxy(messageContextHolder, record);
		return recordProxy;
	}
	
	@Transactional
	@Secured("ROLE_ENTRY")
	public void deleteRecord(int id) throws RecordPersistenceException {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey survey = sessionState.getActiveSurvey();
		//TODO check that the record is in ENTRY phase: only delete in ENTRY phase is allowed
		CollectRecord record = recordManager.load(survey, id, Step.ENTRY.getStepNumber());
		fileManager.deleteAllFiles(record);
		recordManager.delete(id);
	}
	
	@Transactional
	@Secured("ROLE_ENTRY")
	public void saveActiveRecord() throws RecordPersistenceException, RecordIndexException {
		sessionManager.checkIsActiveRecordLocked();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		User user = sessionState.getUser();
		record.setModifiedDate(new Date());
		record.setModifiedBy(user);
		String sessionId = sessionState.getSessionId();
		recordManager.save(record, sessionId);
		fileManager.commitChanges(sessionId, record);
		if ( isCurrentRecordIndexable() ) {
			recordIndexService.permanentlyIndex(record);
		}
	}

	@Transactional
	@Secured("ROLE_ENTRY")
	public List<UpdateResponse> updateActiveRecord(UpdateRequest request) throws RecordPersistenceException, RecordIndexException {
		sessionManager.checkIsActiveRecordLocked();
		List<UpdateRequestOperation> operations = request.getOperations();
		List<UpdateResponse> updateResponses = new ArrayList<UpdateResponse>();
		for (UpdateRequestOperation operation : operations) {
			Collection<UpdateResponse> responses = processUpdateRequestOperation(operation);
			updateResponses.addAll(responses);
		}
		if ( updateResponses.size() > 0 ) {
			CollectRecord activeRecord = getActiveRecord();
			UpdateResponse firstResp = updateResponses.get(0);
			firstResp.setErrors(activeRecord.getErrors());
			firstResp.setMissing(activeRecord.getMissing());
			firstResp.setMissingErrors(activeRecord.getMissingErrors());
			firstResp.setMissingWarnings(activeRecord.getMissingWarnings());
			firstResp.setSkipped(activeRecord.getSkipped());
			firstResp.setWarnings(activeRecord.getWarnings());
			if ( isCurrentRecordIndexable() ) {
				recordIndexService.temporaryIndex(activeRecord);
			}
		}
		return updateResponses;
	}
	
	protected Collection<UpdateResponse> processUpdateRequestOperation(UpdateRequestOperation operation) throws RecordPersistenceException {
		Method method = operation.getMethod();
		switch (method) {
			case CONFIRM_ERROR:
				return processConfirmError(operation);
			case APPROVE_MISSING:
				return processApproveMissingValue(operation);
			case UPDATE_REMARKS:
				return processUpdateRemarks(operation);
			case ADD:
				return processAddNode(operation);
			case UPDATE:
				return processUpdateAttribute(operation);
			case APPLY_DEFAULT_VALUE: 
				return processApplyDefaultValue(operation);
			case DELETE:
				return processDeleteNode(operation);
			default:
				throw new IllegalArgumentException("UpdateRequestOperation method not supported: " + method);
		}
	}

	protected Collection<UpdateResponse> processDeleteNode(
			UpdateRequestOperation operation) {
		CollectRecord record = getActiveRecord();
		Node<?> node = record.getNodeByInternalId(operation.getNodeId());
		Map<Integer, UpdateResponse> responseMap = new HashMap<Integer, UpdateResponse>();
		HashSet<NodePointer> relevanceRequiredDependencies = new HashSet<NodePointer>();
		HashSet<Attribute<?, ?>> checkDependencies = new HashSet<Attribute<?,?>>();
		List<NodePointer> cardinalityNodePointers = createCardinalityNodePointers(node);
		deleteNode(responseMap, node, relevanceRequiredDependencies, checkDependencies);
		prepareUpdateResponse(responseMap, relevanceRequiredDependencies, checkDependencies, cardinalityNodePointers);
		return responseMap.values();
	}

	protected Collection<UpdateResponse> processApplyDefaultValue(
			UpdateRequestOperation operation) {
		CollectRecord record = getActiveRecord();
		Integer nodeId = operation.getNodeId();
		Node<?> node = record.getNodeByInternalId(nodeId);
		if ( node instanceof Attribute ) {
			Attribute<?, ?> attribute = (Attribute<?, ?>) node;
			recordManager.applyDefaultValue(attribute);
			Map<Integer, Object> fieldValues = new HashMap<Integer, Object>();
			int fieldCount = attribute.getFieldCount();
			for (int idx = 0; idx < fieldCount; idx ++) {
				Field<?> field = attribute.getField(idx);
				fieldValues.put(idx, field.getValue());
			}
			Map<Integer, UpdateResponse> responseMap = new HashMap<Integer, UpdateResponse>();
			UpdateResponse response = getOrCreateUpdateResponse(responseMap, attribute);
			response.setUpdatedFieldValues(fieldValues);
			List<NodePointer> cardinalityNodePointers = createCardinalityNodePointers(attribute);
			Set<NodePointer> relevanceRequiredDependencies = recordManager.clearRelevanceRequiredDependencies(attribute);
			Set<Attribute<?, ?>> checkDependencies = recordManager.clearValidationResults(attribute);
			relevanceRequiredDependencies.add(new NodePointer(attribute.getParent(), attribute.getName()));
			checkDependencies.add(attribute);
			prepareUpdateResponse(responseMap, relevanceRequiredDependencies, checkDependencies, cardinalityNodePointers);
			return responseMap.values();
		} else {
			throw new IllegalArgumentException("This method is applicable only to attributes");
		}
	}

	protected Collection<UpdateResponse> processUpdateAttribute(
			UpdateRequestOperation operation) throws RecordFileException {
		CollectRecord record = getActiveRecord();
		Integer nodeId = operation.getNodeId();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(nodeId);
		NodeDefinition nodeDef = attribute.getDefinition();
		Integer parentEntityId = operation.getParentEntityId();
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);

		record.setErrorConfirmed(attribute, false);
		record.setMissingApproved(parentEntity, attribute.getName(), false);
		record.setDefaultValueApplied(attribute, false);
		
		Map<Integer, Object> updatedFieldValues = new HashMap<Integer, Object>();
		
		Integer fieldIndex = operation.getFieldIndex();
		Object requestValue = operation.getValue();
		String remarks = operation.getRemarks();
		FieldSymbol symbol = operation.getSymbol();

		if (fieldIndex < 0) {
			Object value = null;
			if ( nodeDef instanceof FileAttributeDefinition) {
				value = parseFileAttributeValue(nodeId, requestValue);
			} else if (requestValue != null) {
				value = parseCompositeAttributeValue(parentEntity, attribute.getDefinition(), requestValue);
			}
			recordManager.setAttributeValue(attribute, value, remarks);
			for (int idx = 0; idx < attribute.getFieldCount(); idx++) {
				Field<?> field = attribute.getField(idx);
				Object fieldValue = field.getValue();
				updatedFieldValues.put(idx, fieldValue);
				recordManager.setFieldValue(attribute, fieldValue, remarks, symbol, idx);
			}
		} else {
			Object value = parseFieldValue(parentEntity, attribute.getDefinition(), (String) requestValue, fieldIndex);
			recordManager.setFieldValue(attribute, value, remarks, symbol, fieldIndex);
			Field<?> field = attribute.getField(fieldIndex);
			updatedFieldValues.put(fieldIndex, field.getValue());
		}
		Map<Integer, UpdateResponse> responseMap = new HashMap<Integer, UpdateResponse>();
		UpdateResponse response = getOrCreateUpdateResponse(responseMap, attribute);
		response.setUpdatedFieldValues(updatedFieldValues);
		Set<NodePointer> relevanceRequiredDependencies = recordManager.clearRelevanceRequiredDependencies(attribute);
		Set<Attribute<?, ?>> checkDependencies = recordManager.clearValidationResults(attribute);
		relevanceRequiredDependencies.add(new NodePointer(attribute.getParent(), attribute.getName()));
		checkDependencies.add(attribute);
		List<NodePointer> cardinalityDependencies = createCardinalityNodePointers(attribute);
		prepareUpdateResponse(responseMap, relevanceRequiredDependencies, checkDependencies, cardinalityDependencies);
		return responseMap.values();
	}

	protected Collection<UpdateResponse> processAddNode(
			UpdateRequestOperation operation) {
		CollectRecord record = getActiveRecord();
		String nodeName = operation.getNodeName();

		Entity parentEntity = (Entity) record.getNodeByInternalId(operation.getParentEntityId());
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		NodeDefinition nodeDef = parentEntityDefn.getChildDefinition(nodeName);
		
		Node<?> createdNode = addNode(parentEntity, nodeDef, operation.getValue(), operation.getSymbol(), operation.getRemarks());
		record.setMissingApproved(parentEntity, nodeName, false);
		Map<Integer, UpdateResponse> responseMap = new HashMap<Integer, UpdateResponse>();
		UpdateResponse response = getOrCreateUpdateResponse(responseMap, createdNode);
		response.setCreatedNode(NodeProxy.fromNode(messageContextHolder, createdNode));
		Set<NodePointer> relevanceRequiredDependencies = recordManager.clearRelevanceRequiredDependencies(createdNode);
		Set<Attribute<?, ?>> checkDependencies = null;
		if(createdNode instanceof Attribute){
			Attribute<?, ?> attribute = (Attribute<?, ?>) createdNode;
			checkDependencies = recordManager.clearValidationResults(attribute);
			checkDependencies.add(attribute);
		}
		relevanceRequiredDependencies.add(new NodePointer(createdNode.getParent(), nodeName));
		List<NodePointer> cardinalityDependencies = createCardinalityNodePointers(createdNode);
		prepareUpdateResponse(responseMap, relevanceRequiredDependencies, checkDependencies, cardinalityDependencies);
		return responseMap.values();
	}

	protected Collection<UpdateResponse> processUpdateRemarks(
			UpdateRequestOperation operation) {
		CollectRecord record = getActiveRecord();
		Attribute<?, ?> attribute = (Attribute<?, ?>) record.getNodeByInternalId(operation.getNodeId());
		Field<?> fld = attribute.getField(operation.getFieldIndex());
		fld.setRemarks(operation.getRemarks());
		UpdateResponse response = createUpdateResponse(attribute);
		return Arrays.asList(response);
	}

	protected Collection<UpdateResponse> processApproveMissingValue(
			UpdateRequestOperation operation) {
		CollectRecord record = getActiveRecord();
		String nodeName = operation.getNodeName();
		Integer parentEntityId = operation.getParentEntityId();
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		record.setMissingApproved(parentEntity, nodeName, true);
		List<NodePointer> cardinalityNodePointers = createCardinalityNodePointers(parentEntity);
		cardinalityNodePointers.add(new NodePointer(parentEntity, nodeName));
		Map<Integer, UpdateResponse> responseMap = new HashMap<Integer, UpdateResponse>();
		validateCardinalityRelevanceAndRequiredState(responseMap, cardinalityNodePointers);
		return responseMap.values();
	}

	protected Collection<UpdateResponse> processConfirmError(UpdateRequestOperation op) {
		CollectRecord record = getActiveRecord();
		Integer nodeId = op.getNodeId();
		Node<?> node = null;
		if(nodeId != null) {
			node = record.getNodeByInternalId(nodeId);
		}
		Map<Integer, UpdateResponse> responseMap = new HashMap<Integer, UpdateResponse>();
		Set<Attribute<?,?>> checkDependencies = new HashSet<Attribute<?,?>>();
		Attribute<?, ?> attribute = (Attribute<?, ?>) node;
		record.setErrorConfirmed(attribute, true);
		attribute.clearValidationResults();
		checkDependencies.add(attribute);
		
		getOrCreateUpdateResponse(responseMap, attribute);
		validateChecks(responseMap, checkDependencies);
		return responseMap.values();
	}

	protected Object parseFileAttributeValue(Integer nodeId, Object requestValue) throws RecordFileException {
		Object result;
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		String sessionId = sessionState.getSessionId();
		if ( requestValue != null ) {
			if ( requestValue instanceof FileWrapper ) {
				FileWrapper fileWrapper = (FileWrapper) requestValue;
				result = fileManager.saveToTempFolder(fileWrapper.getData(), fileWrapper.getFileName(), sessionId, record, nodeId);
			} else {
				throw new IllegalArgumentException("Invalid value type: expected byte[]");
			}
		} else {
			fileManager.prepareDeleteFile(sessionId, record, nodeId);
			result = null;
		}
		return result;
	}
	
	protected List<NodePointer> createCardinalityNodePointers(Node<?> node){
		List<NodePointer> nodePointers = new ArrayList<NodePointer>();
		
		Entity parent = node.getParent();
		String childName = node.getName();
		while(parent != null){
			NodePointer nodePointer = new NodePointer(parent, childName );
			nodePointers.add(nodePointer);
			
			childName = parent.getName();
			parent = parent.getParent();
		}
		return nodePointers;
	}

	protected void deleteNode(Map<Integer, UpdateResponse> responseMap, Node<?> node, Set<NodePointer> relevanceRequiredDependencies, Set<Attribute<?,?>> checkDependencies){
		CollectRecord record = getActiveRecord();
		Stack<Node<?>> dependenciesStack = new Stack<Node<?>>();
		Stack<Node<?>> nodesToRemove = new Stack<Node<?>>();
		dependenciesStack.push(node);
		
		Set<NodePointer> relevantDependencies = new HashSet<NodePointer>();
		Set<NodePointer> requiredDependencies = new HashSet<NodePointer>();
		while(!dependenciesStack.isEmpty()){
			Node<?> n = dependenciesStack.pop();
			nodesToRemove.push(n);
			
			relevantDependencies.addAll(n.getRelevantDependencies());
			requiredDependencies.addAll(n.getRequiredDependencies());
			if(n instanceof Entity){
				Entity entity = (Entity) n;
				List<Node<? extends NodeDefinition>> children = entity.getChildren();
				for (Node<? extends NodeDefinition> child : children) {
					dependenciesStack.push(child);
				}
			} else {
				Attribute<?,?> attr = (Attribute<?, ?>) n;
				checkDependencies.addAll(attr.getCheckDependencies());
			}
		}
		
		while(!nodesToRemove.isEmpty()){
			Node<?> n = nodesToRemove.pop();
			record.deleteNode(n);
			
			UpdateResponse resp = getOrCreateUpdateResponse(responseMap, node);
			resp.setDeletedNodeId(node.getInternalId());
		}
		
		//clear dependencies
		recordManager.clearRelevantDependencies(relevantDependencies);
		requiredDependencies.addAll(relevantDependencies);
		recordManager.clearRequiredDependencies(requiredDependencies);
		recordManager.clearValidationResults(checkDependencies);
		
		relevanceRequiredDependencies.addAll(requiredDependencies);
	}
	
	protected void prepareUpdateResponse(Map<Integer, UpdateResponse> responseMap, Set<NodePointer> relevanceRequiredDependencies, 
				Set<Attribute<?, ?>> checkDependencies, List<NodePointer> cardinalityDependencies) {
		validateCardinalityRelevanceAndRequiredState(responseMap, cardinalityDependencies);
		validateCardinalityRelevanceRequiredStateAndChecks(responseMap, relevanceRequiredDependencies);
		validateChecks(responseMap, checkDependencies);
	}

	protected void validateChecks(Map<Integer, UpdateResponse> responseMap,
			Set<Attribute<?, ?>> attributes) {
		if (attributes != null) {
			for (Attribute<?, ?> attr : attributes) {
				validateAttribute(responseMap, attr);
			}
		}
	}

	protected void validateAttribute(Map<Integer, UpdateResponse> responseMap,
			Attribute<?, ?> attr) {
		if ( !attr.isDetached() ) {
			attr.clearValidationResults();
			ValidationResults results = attr.validateValue();
			UpdateResponse response = getOrCreateUpdateResponse(responseMap, attr);
			response.setAttributeValidationResults(results);
		}
	}

	protected void validateChecks(Map<Integer, UpdateResponse> responseMap,
			Entity entity, String childName) {
		List<Node<?>> children = entity.getAll(childName);
		for ( Node<?> node : children ) {
			if ( node instanceof Attribute ){
				validateAttribute(responseMap, (Attribute<?, ?>) node);
			}
		}
	}
	
	protected void validateCardinalityRelevanceRequiredStateAndChecks(
			Map<Integer, UpdateResponse> responseMap,
			Set<NodePointer> nodePointers) {
		if (nodePointers != null) {
			for (NodePointer nodePointer : nodePointers) {
				Entity entity = nodePointer.getEntity();
				if (!entity.isDetached()) {
					String childName = nodePointer.getChildName();
					validateCardinalityRelevanceAndRequiredState(
							responseMap, nodePointer);
					
					validateChecks(responseMap, entity, childName);
				}
			}
		}
	}

	protected String validateCardinalityRelevanceAndRequiredState(
			Map<Integer, UpdateResponse> responseMap, NodePointer nodePointer) {
		Entity entity = nodePointer.getEntity();
		String childName = nodePointer.getChildName();
		UpdateResponse response = getOrCreateUpdateResponse(responseMap, entity);
		response.setRelevant(childName, entity.isRelevant(childName));
		response.setRequired(childName, entity.isRequired(childName));
		response.setMinCountValid(childName, entity.validateMinCount(childName));
		response.setMaxCountValid(childName, entity.validateMaxCount(childName));
		return childName;
	}

	protected void validateCardinalityRelevanceAndRequiredState(Map<Integer, UpdateResponse> responseMap,
			List<NodePointer> nodePointers) {
		if (nodePointers != null) {
			for (NodePointer nodePointer : nodePointers) {
				// entity could be root definition
				Entity parent = nodePointer.getEntity();
				if (parent != null && !parent.isDetached()) {
					validateCardinalityRelevanceAndRequiredState(
							responseMap, nodePointer);
				}
			}
		}
	}

	protected UpdateResponse getOrCreateUpdateResponse(Map<Integer, UpdateResponse> responseMap, Node<?> node){
		Integer nodeId = node.getInternalId();
		UpdateResponse response = responseMap.get(nodeId);
		if(response == null){
			response = createUpdateResponse(node);
			responseMap.put(nodeId, response);
		}
		return response;
	}

	protected UpdateResponse createUpdateResponse(Node<?> node) {
		return new UpdateResponse(messageContextHolder, node);
	}
	
	@SuppressWarnings("unchecked")
	protected Node<?> addNode(Entity parentEntity, NodeDefinition nodeDefn, Object requestValue, FieldSymbol symbol, String remarks) {
		if(nodeDefn instanceof AttributeDefinition) {
			AttributeDefinition def = (AttributeDefinition) nodeDefn;
			Attribute<?, ?> attribute = (Attribute<?, ?>) def.createNode();
			parentEntity.add(attribute);
			if(requestValue != null) {
				Value value = parseCompositeAttributeValue(parentEntity, (AttributeDefinition) nodeDefn, requestValue);
				((Attribute<?, Value> ) attribute).setValue(value);
			}
			if(symbol != null || remarks != null) {
				Character symbolChar = null;
				if(symbol != null) {
					symbolChar = symbol.getCode();
				}
				int fieldCount = attribute.getFieldCount();
				for (int idx = 0; idx < fieldCount; idx++) {
					Field<?> field = attribute.getField(idx);
					field.setSymbol(symbolChar);
					field.setRemarks(remarks);
				}
			}
			return attribute;
		} else {
			Entity e = recordManager.addEntity(parentEntity, nodeDefn.getName());
			return e;
		}
	}
	
	protected Object parseFieldValue(Entity parentEntity, AttributeDefinition def, String value, Integer fieldIndex) {
		Object fieldValue = null;
		if(StringUtils.isBlank(value)) {
			return null;
		}
		if(def instanceof BooleanAttributeDefinition) {
			fieldValue = Boolean.parseBoolean(value);
		} else if(def instanceof CoordinateAttributeDefinition) {
			if(fieldIndex != null) {
				if(fieldIndex == 2) {
					fieldValue = value;
				} else {
					fieldValue = Double.valueOf(value);
				}
			}
		} else if(def instanceof DateAttributeDefinition) {
			Integer val = Integer.valueOf(value);
			fieldValue = val;
		} else if(def instanceof NumberAttributeDefinition) {
			NumericAttributeDefinition numberDef = (NumericAttributeDefinition) def;
			if(fieldIndex != null && fieldIndex == 2) {
				//unit id
				fieldValue = Integer.parseInt(value);
			} else {
				NumericAttributeDefinition.Type type = numberDef.getType();
				Number number = null;
				switch(type) {
					case INTEGER:
						number = Integer.valueOf(value);
						break;
					case REAL:
						number = Double.valueOf(value);
						break;
				}
				if(number != null) {
					fieldValue = number;
				}
			}
		} else if(def instanceof RangeAttributeDefinition) {
			if(fieldIndex != null && fieldIndex == 3) {
				//unit id
				fieldValue = Integer.parseInt(value);
			} else {
				RangeAttributeDefinition.Type type = ((RangeAttributeDefinition) def).getType();
				Number number = null;
				switch(type) {
					case INTEGER:
						number = Integer.valueOf(value);
						break;
					case REAL:
						number = Double.valueOf(value);
						break;
				}
				if(number != null) {
					fieldValue = number;
				}
			}
		} else if(def instanceof TimeAttributeDefinition) {
			fieldValue = Integer.valueOf(value);
		} else {
			fieldValue = value;
		}
		return fieldValue;
	}
	
	protected Value parseCompositeAttributeValue(Entity parentEntity, AttributeDefinition defn, Object value) {
		Value result;
		if(defn instanceof CodeAttributeDefinition) {
			if ( value instanceof String) {
				String stringVal = (String) value;
				result = parseCode(parentEntity, (CodeAttributeDefinition) defn, stringVal );
			} else {
				throw new IllegalArgumentException("Invalid value type: expected String");
			}
		} else if(defn instanceof RangeAttributeDefinition) {
			if ( value instanceof String) {
				String stringVal = (String) value;
				RangeAttributeDefinition rangeDef = (RangeAttributeDefinition) defn;
				RangeAttributeDefinition.Type type = rangeDef.getType();
				NumericRange<?> range = null;
				Unit unit = null; //todo check if unit is required here or is set later by the client
				switch(type) {
					case INTEGER:
						range = IntegerRange.parseIntegerRange(stringVal, unit);
						break;
					case REAL:
						range = RealRange.parseRealRange(stringVal, unit);
						break;
				}
				result = range;
			} else {
				throw new IllegalArgumentException("Invalid value type: expected String");
			}
		} else {
			throw new IllegalArgumentException("Invalid AttributeDefinition: expected CodeAttributeDefinition or RangeAttributeDefinition");
		}
		return result;
	}
	
	@Secured("ROLE_ENTRY")
	public void promoteToCleansing() throws RecordPersistenceException, RecordPromoteException  {
		promote(Step.CLEANSING);
	}

	@Secured("ROLE_CLEANSING")
	public void promoteToAnalysis() throws RecordPersistenceException, RecordPromoteException  {
		promote(Step.ANALYSIS);
	}
	
	@Transactional
	protected void promote(Step to) throws RecordPersistenceException, RecordPromoteException  {
		sessionManager.checkIsActiveRecordLocked();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		Step currentStep = record.getStep();
		Step exptectedStep = to.getPrevious();
		if ( exptectedStep == currentStep ) {
			User user = sessionState.getUser();
			recordManager.promote(record, user);
			recordManager.releaseLock(record.getId());
			sessionManager.clearActiveRecord();
			if ( isCurrentRecordIndexable() ) {
				recordIndexService.permanentlyIndex(record);
			}
		} else {
			throw new IllegalStateException("The active record cannot be submitted: it is not in the exptected phase: " + exptectedStep);
		}
	}
	
	protected boolean isRecordIndexEnabled() {
		return recordIndexService.isInited();
	}
	
	protected boolean isCurrentRecordIndexable() {
		return isRecordIndexEnabled() && hasActiveSurveyIndexedNodes;
	}

	@Secured("ROLE_ANALYSIS")
	public void demoteToCleansing() throws RecordPersistenceException {
		demote(Step.CLEANSING);
	}
	
	@Secured("ROLE_CLEANSING")
	public void demoteToEntry() throws RecordPersistenceException {
		demote(Step.ENTRY);
	}
		
	@Transactional
	protected void demote(Step to) throws RecordPersistenceException {
		sessionManager.checkIsActiveRecordLocked();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		Step currentStep = record.getStep();
		Step exptectedStep = to.getNext();
		if ( exptectedStep == currentStep ) {
			CollectSurvey survey = sessionState.getActiveSurvey();
			User user = sessionState.getUser();
			Integer recordId = record.getId();
			recordManager.demote(survey, recordId, record.getStep(), user);
			recordManager.releaseLock(recordId);
			sessionManager.clearActiveRecord();
		} else {
			throw new IllegalStateException("The active record cannot be demoted: it is not in the exptected phase: " + exptectedStep);
		}
	}

	/**
	 * remove the active record from the current session
	 * @throws RecordPersistenceException 
	 * @throws RecordIndexException 
	 */
	@Secured("ROLE_ENTRY")
	public void clearActiveRecord() throws RecordPersistenceException, RecordIndexException {
		sessionManager.releaseRecord();
		if ( isCurrentRecordIndexable() ) {
			recordIndexService.cleanTemporaryIndex();
		}
	}
	
	@Secured("ROLE_ENTRY")
	public void moveNode(int nodeId, int index) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		recordManager.moveNode(record, nodeId, index);
	}

	/**
	 * Gets the code list items assignable to the specified attribute and matching the specified codes.
	 * 
	 * @param parentEntityId
	 * @param attrName
	 * @param codes
	 * @return
	 */
	@Secured("ROLE_ENTRY")
	public List<CodeListItemProxy> getCodeListItems(int parentEntityId, String attrName, String[] codes){
		CollectRecord record = getActiveRecord();
		Entity parent = (Entity) record.getNodeByInternalId(parentEntityId);
		CodeAttributeDefinition def = (CodeAttributeDefinition) parent.getDefinition().getChildDefinition(attrName);
		List<CodeListItem> items = getAssignableCodeListItems(parent, def);
		List<CodeListItem> filteredItems = new ArrayList<CodeListItem>();
		if(codes != null && codes.length > 0) {
			//filter by specified codes
			for (CodeListItem item : items) {
				for (String code : codes) {
					if(item.getCode().equals(code)) {
						filteredItems.add(item);
					}
				}
			}
		}
		List<CodeListItemProxy> result = CodeListItemProxy.fromList(filteredItems);
		return result;
	} 
	
	/**
	 * Gets the code list items assignable to the specified attribute.
	 * 
	 * @param parentEntityId
	 * @param attrName
	 * @return
	 */
	@Secured("ROLE_ENTRY")
	public List<CodeListItemProxy> findAssignableCodeListItems(int parentEntityId, String attrName){
		CollectRecord record = getActiveRecord();
		Entity parent = (Entity) record.getNodeByInternalId(parentEntityId);
		CodeAttributeDefinition def = (CodeAttributeDefinition) parent.getDefinition().getChildDefinition(attrName);
		List<CodeListItem> items = getAssignableCodeListItems(parent, def);
		List<CodeListItemProxy> result = CodeListItemProxy.fromList(items);
		List<Node<?>> selectedCodes = parent.getAll(attrName);
		CodeListItemProxy.setSelectedItems(result, selectedCodes);
		return result;
	}
	
	/**
	 * Finds a list of code list items assignable to the specified attribute and matching the passed codes
	 * 
	 * @param parentEntityId
	 * @param attributeName
	 * @param codes
	 * @return
	 */
	@Secured("ROLE_ENTRY")
	public List<CodeListItemProxy> findAssignableCodeListItems(int parentEntityId, String attributeName, String[] codes) {
		CollectRecord record = getActiveRecord();
		Entity parent = (Entity) record.getNodeByInternalId(parentEntityId);
		CodeAttributeDefinition def = (CodeAttributeDefinition) parent.getDefinition().getChildDefinition(attributeName);
		List<CodeListItem> items = getAssignableCodeListItems(parent, def);
		List<CodeListItemProxy> result = new ArrayList<CodeListItemProxy>();
		for (String code : codes) {
			CodeListItem item = findCodeListItem(items, code);
			if(item != null) {
				CodeListItemProxy proxy = new CodeListItemProxy(item);
				result.add(proxy);
			}
		}
		return result;
	}
	
	@Secured("ROLE_ENTRY")
	public List<String> searchAutoCompleteValues(int attributeDefnId, int fieldIndex, String searchText) throws Exception {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey survey = sessionState.getActiveSurvey();
		int maxResults = 10;
		List<String> result = recordIndexService.search(SearchType.STARTS_WITH, survey, attributeDefnId, fieldIndex, searchText, maxResults);
		return result;
	}
	
	
	protected CollectRecord getActiveRecord() {
		SessionState sessionState = getSessionManager().getSessionState();
		CollectRecord activeRecord = sessionState.getActiveRecord();
		return activeRecord;
	}

	protected SessionManager getSessionManager() {
		return sessionManager;
	}

	protected RecordManager getRecordManager() {
		return recordManager;
	}

	/**
	 * Start of CodeList utility methods
	 * 
	 * TODO move them to a better location
	 */
	protected List<CodeListItem> getAssignableCodeListItems(Entity parent, CodeAttributeDefinition def) {
		CollectRecord record = getActiveRecord();
		List<CodeListItem> items = null;
		if(StringUtils.isEmpty(def.getParentExpression())){
			items = def.getList().getItems();
		} else {
			CodeAttribute parentCodeAttribute = getCodeParent(parent, def);
			if(parentCodeAttribute!=null){
				CodeListItem parentCodeListItem = parentCodeAttribute.getCodeListItem();
				if(parentCodeListItem != null) {
					//TODO exception if parent not specified
					items = parentCodeListItem.getChildItems();
				}
			}
		}
		List<CodeListItem> result = new ArrayList<CodeListItem>();
		if(items != null) {
			ModelVersion version = record.getVersion();
			for (CodeListItem item : items) {
				if (version == null || version.isApplicable(item)) {
					result.add(item);
				}
			}
		}
		return result;
	}
	
	protected CodeAttribute getCodeParent(Entity context, CodeAttributeDefinition def) {
		try {
			String parentExpr = def.getParentExpression();
			ExpressionFactory expressionFactory = context.getRecord().getSurveyContext().getExpressionFactory();
			ModelPathExpression expression = expressionFactory.createModelPathExpression(parentExpr);
			Node<?> parentNode = expression.evaluate(context, null);
			if (parentNode != null && parentNode instanceof CodeAttribute) {
				return (CodeAttribute) parentNode;
			}
		} catch (Exception e) {
			// return null;
		}
		return null;
	}

	protected CodeListItem findCodeListItem(List<CodeListItem> siblings, String code) {
		String adaptedCode = code.trim();
		adaptedCode = adaptedCode.toUpperCase();
		//remove initial zeros
		adaptedCode = adaptedCode.replaceFirst("^0+", "");
		adaptedCode = Pattern.quote(adaptedCode);

		for (CodeListItem item : siblings) {
			String itemCode = item.getCode();
			Pattern pattern = Pattern.compile("^[0]*" + adaptedCode + "$", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(itemCode);
			if(matcher.find()) {
				return item;
			}
		}
		return null;
	}
	
	protected Code parseCode(Entity parent, CodeAttributeDefinition def, String value) {
		List<CodeListItem> items = getAssignableCodeListItems(parent, def);
		Code code = parseCode(value, items);
		return code;
	}
	
	protected Code parseCode(String value, List<CodeListItem> codeList) {
		Code code = null;
		String[] strings = value.split(":");
		String codeStr = null;
		String qualifier = null;
		switch(strings.length) {
			case 2:
				qualifier = strings[1].trim();
			case 1:
				codeStr = strings[0].trim();
				break;
			default:
				//TODO throw error: invalid parameter
		}
		CodeListItem codeListItem = findCodeListItem(codeList, codeStr);
		if(codeListItem != null) {
			code = new Code(codeListItem.getCode(), qualifier);
		}
		if (code == null) {
			code = new Code(codeStr, qualifier);
		}
		return code;
	}
	
}
