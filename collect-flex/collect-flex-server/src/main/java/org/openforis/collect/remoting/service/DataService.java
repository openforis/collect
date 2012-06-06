/**
 * 
 */
package org.openforis.collect.remoting.service;


import java.util.ArrayList;
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
import org.openforis.collect.persistence.RecordUnlockedException;
import org.openforis.collect.remoting.service.UpdateRequestOperation.Method;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
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
import org.openforis.idm.model.Record;
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
	private RecordManager recordManager;

	@Transactional
	@Secured("ROLE_ENTRY")
	public RecordProxy loadRecord(int id, int step, boolean forceUnlock) throws RecordPersistenceException {
		SessionState sessionState = sessionManager.getSessionState();
		if ( sessionState.isActiveRecordBeingEdited() ) {
			throw new MultipleEditException();
		}
		CollectSurvey survey = sessionState.getActiveSurvey();
		User user = sessionState.getUser();
		CollectRecord record = recordManager.checkout(survey, user, id, step, sessionState.getSessionId(), forceUnlock);
		Entity rootEntity = record.getRootEntity();
		recordManager.addEmptyNodes(rootEntity);
		sessionManager.setActiveRecord(record);
		return new RecordProxy(record);
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
		List<RecordProxy> proxies = new ArrayList<RecordProxy>();
		for (CollectRecord summary : summaries) {
			proxies.add(new RecordProxy(summary));
		}
		result.put("count", count);
		result.put("records", proxies);
		return result;
	}

	@Transactional
	@Secured("ROLE_ENTRY")
	public RecordProxy createRecord(String rootEntityName, String versionName) throws RecordPersistenceException {
		SessionState sessionState = sessionManager.getSessionState();
		if ( sessionState.isActiveRecordBeingEdited() ) {
			throw new MultipleEditException();
		}
		String sessionId = sessionState.getSessionId();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		User user = sessionState.getUser();
		ModelVersion version = activeSurvey.getVersion(versionName);
		Schema schema = activeSurvey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		CollectRecord record = recordManager.create(activeSurvey, rootEntityDefinition, user, version.getName(), sessionId);
		Entity rootEntity = record.getRootEntity();
		recordManager.addEmptyNodes(rootEntity);
		sessionManager.setActiveRecord(record);
		RecordProxy recordProxy = new RecordProxy(record);
		return recordProxy;
	}
	
	@Transactional
	@Secured("ROLE_ENTRY")
	public void deleteRecord(int id) throws RecordPersistenceException {
		recordManager.delete(id);
		sessionManager.clearActiveRecord();
	}
	
	@Transactional
	@Secured("ROLE_ENTRY")
	public void saveActiveRecord() throws RecordPersistenceException {
		sessionManager.checkIsActiveRecordLocked();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		User user = sessionState.getUser();
		record.setModifiedDate(new Date());
		record.setModifiedBy(user);
		recordManager.save(record, sessionState.getSessionId());
	}

	@Transactional
	@Secured("ROLE_ENTRY")
	public List<UpdateResponse> updateActiveRecord(UpdateRequest request) throws RecordUnlockedException {
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
		}
		return updateResponses;
	}
	
	@SuppressWarnings("unchecked")
	private Collection<UpdateResponse> processUpdateRequestOperation(UpdateRequestOperation operation) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();

		Integer parentEntityId = operation.getParentEntityId();
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		Integer nodeId = operation.getNodeId();
		Integer fieldIndex = operation.getFieldIndex();
		String nodeName = operation.getNodeName();
		
		Node<?> node = null;
		if(nodeId != null) {
			node = record.getNodeByInternalId(nodeId);
		}
		NodeDefinition nodeDef = ((EntityDefinition) parentEntity.getDefinition()).getChildDefinition(nodeName);
		String requestValue = operation.getValue();
		String remarks = operation.getRemarks();
		
		FieldSymbol symbol = operation.getSymbol();
		Method method = operation.getMethod();
		Map<Integer, UpdateResponse> responseMap = new HashMap<Integer, UpdateResponse>();
		Set<NodePointer> relReqDependencies = null;
		Set<Attribute<?,?>> checkDependencies = null;
		List<NodePointer> cardinalityNodePointers = null;
		
		Attribute<? extends AttributeDefinition, ?> attribute = null;
		switch (method) {
			case CONFIRM_ERROR:
				attribute = (Attribute<AttributeDefinition, ?>) node;
				record.setErrorConfirmed(attribute, true);
				//checkDependencies = recordManager.clearValidationResults(attribute);
				checkDependencies = new HashSet<Attribute<?,?>>();
				attribute.clearValidationResults();
				checkDependencies.add(attribute);
				
				UpdateResponse response = getUpdateResponse(responseMap, attribute);
				break;
			case APPROVE_MISSING:
				record.setMissingApproved(parentEntity, nodeName, true);
				cardinalityNodePointers = getCardinalityNodePointers(parentEntity);
				cardinalityNodePointers.add(new NodePointer(parentEntity, nodeName));
				break;
			case UPDATE_REMARKS:
				attribute = (Attribute<AttributeDefinition, ?>) node;
				Field<?> fld = attribute.getField(fieldIndex);
				fld.setRemarks(remarks);
				getUpdateResponse(responseMap, attribute);
				break;
			case ADD :
				Node<?> createdNode = addNode(parentEntity, nodeDef, requestValue, symbol, remarks);
				record.setMissingApproved(parentEntity, nodeName, false);
				response = getUpdateResponse(responseMap, createdNode);
				response.setCreatedNode(NodeProxy.fromNode(createdNode));
				relReqDependencies = recordManager.clearRelevanceRequiredStates(createdNode);
				if(createdNode instanceof Attribute){
					attribute = (Attribute<? extends AttributeDefinition, ?>) createdNode;
					checkDependencies = recordManager.clearValidationResults(attribute);
					checkDependencies.add(attribute);
				}
				relReqDependencies.add(new NodePointer(createdNode.getParent(), createdNode.getName()));
				cardinalityNodePointers = getCardinalityNodePointers(createdNode);
				break;
			case UPDATE:
				attribute = (Attribute<AttributeDefinition, ?>) node;
				record.setErrorConfirmed(attribute, false);
				record.setMissingApproved(parentEntity, node.getName(), false);
				record.setDefaultValueApplied(attribute, false);
				
				cardinalityNodePointers = getCardinalityNodePointers(attribute);
				response = getUpdateResponse(responseMap, attribute);
				Map<Integer, Object> updatedFieldValues = new HashMap<Integer, Object>();
				if (fieldIndex < 0) {
					Object value = null;
					if (requestValue != null) {
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
					Object value = parseFieldValue(parentEntity, attribute.getDefinition(), requestValue, fieldIndex);
					recordManager.setFieldValue(attribute, value, remarks, symbol, fieldIndex);
					Field<?> field = attribute.getField(fieldIndex);
					updatedFieldValues.put(fieldIndex, field.getValue());
				}
				response.setUpdatedFieldValues(updatedFieldValues);
				relReqDependencies = recordManager.clearRelevanceRequiredStates(attribute);
				checkDependencies = recordManager.clearValidationResults(attribute);
				relReqDependencies.add(new NodePointer(attribute.getParent(), attribute.getName()));
				checkDependencies.add(attribute);
				break;
			case APPLY_DEFAULT_VALUE: 
				if ( node instanceof Attribute ) {
					attribute = (Attribute<AttributeDefinition, ?>) node;
					recordManager.applyDefaultValue(attribute);
					Map<Integer, Object> fieldValues = new HashMap<Integer, Object>();
					int fieldCount = attribute.getFieldCount();
					for (int idx = 0; idx < fieldCount; idx ++) {
						Field<?> field = attribute.getField(idx);
						fieldValues.put(idx, field.getValue());
					}
					response = getUpdateResponse(responseMap, attribute);
					response.setUpdatedFieldValues(fieldValues);
					cardinalityNodePointers = getCardinalityNodePointers(attribute);
					relReqDependencies = recordManager.clearRelevanceRequiredStates(attribute);
					checkDependencies = recordManager.clearValidationResults(attribute);
					relReqDependencies.add(new NodePointer(attribute.getParent(), attribute.getName()));
					checkDependencies.add(attribute);
				} else {
					throw new IllegalArgumentException("This method is applicable only to attributes");
				}
				break; 
			case DELETE:
				relReqDependencies = new HashSet<NodePointer>();
				checkDependencies = new HashSet<Attribute<?,?>>();
				cardinalityNodePointers = getCardinalityNodePointers(node);
				deleteNode(node, relReqDependencies, checkDependencies, responseMap);
				break;
		}
		prepareUpdateResponse(responseMap, relReqDependencies, checkDependencies, cardinalityNodePointers);
		return responseMap.values();
	}
	
	private List<NodePointer> getCardinalityNodePointers(Node<?> node){
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

	private void deleteNode(Node<?> node,Set<NodePointer> relevanceRequiredDependencies, Set<Attribute<?,?>> checkDependencies, Map<Integer, UpdateResponse> responseMap){
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
			
			UpdateResponse resp = getUpdateResponse(responseMap, node);
			resp.setDeletedNodeId(node.getInternalId());
		}
		
		//clear dependencies
		recordManager.clearRelevantDependencies(relevantDependencies);
		requiredDependencies.addAll(relevantDependencies);
		recordManager.clearRequiredDependencies(requiredDependencies);
		recordManager.clearValidationResults(checkDependencies);
		
		relevanceRequiredDependencies.addAll(requiredDependencies);
	}
	
	
	private void prepareUpdateResponse(Map<Integer, UpdateResponse> responseMap, Set<NodePointer> relevanceRequiredDependencies, Set<Attribute<?, ?>> validtionResultsDependencies, List<NodePointer> cardinalityNodePointers) {
		if (cardinalityNodePointers != null) {
			for (NodePointer nodePointer : cardinalityNodePointers) {
				// entity could be root definition
				Entity parent = nodePointer.getEntity();
				if (parent != null && !parent.isDetached()) {
					String childName = nodePointer.getChildName();
					UpdateResponse response = getUpdateResponse(responseMap, parent);
					response.setRelevant(childName, parent.isRelevant(childName));
					response.setRequired(childName, parent.isRequired(childName));
					response.setMinCountValid(childName, parent.validateMinCount(childName));
					response.setMaxCountValid(childName, parent.validateMaxCount(childName));
				}
			}
		}
		if (relevanceRequiredDependencies != null) {
			for (NodePointer nodePointer : relevanceRequiredDependencies) {
				Entity entity = nodePointer.getEntity();
				if (!entity.isDetached()) {
					String childName = nodePointer.getChildName();
					UpdateResponse response = getUpdateResponse(responseMap, entity);
					response.setRelevant(childName, entity.isRelevant(childName));
					response.setRequired(childName, entity.isRequired(childName));
					response.setMinCountValid(childName, entity.validateMinCount(childName));
					response.setMaxCountValid(childName, entity.validateMaxCount(childName));
					
					List<Node<? extends NodeDefinition>> list = entity.getAll(childName);
					for ( Node<? extends NodeDefinition> node : list ) {
						if ( node instanceof Attribute ){
							Attribute<?, ?> attribute = (Attribute<?, ?>) node;
							attribute.clearValidationResults();
							ValidationResults results = attribute.validateValue();
							UpdateResponse resp = getUpdateResponse(responseMap, attribute);
							resp.setAttributeValidationResults(results);
						}
					}
					
				}
			}
		}
		if (validtionResultsDependencies != null) {
			for (Attribute<?, ?> checkDepAttr : validtionResultsDependencies) {
				if (!checkDepAttr.isDetached()) {
					checkDepAttr.clearValidationResults();
					ValidationResults results = checkDepAttr.validateValue();
					UpdateResponse response = getUpdateResponse(responseMap, checkDepAttr);
					response.setAttributeValidationResults(results);
				}
			}
		}
	}

	private UpdateResponse getUpdateResponse(Map<Integer, UpdateResponse> responseMap, Node<?> node){
		Integer nodeId = node.getInternalId();
		UpdateResponse response = responseMap.get(nodeId);
		if(response == null){
			response = new UpdateResponse(node);
			responseMap.put(nodeId, response);
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	private Node<?> addNode(Entity parentEntity, NodeDefinition nodeDef, String requestValue, FieldSymbol symbol, String remarks) {
		if(nodeDef instanceof AttributeDefinition) {
			AttributeDefinition def = (AttributeDefinition) nodeDef;
			Attribute<?, ?> attribute = (Attribute<?, ?>) def.createNode();
			parentEntity.add(attribute);
			if(StringUtils.isNotBlank(requestValue)) {
				Value value = parseCompositeAttributeValue(parentEntity, (AttributeDefinition) nodeDef, requestValue);
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
			Entity e = recordManager.addEntity(parentEntity, nodeDef.getName());
			return e;
		}
	}
	
	private Object parseFieldValue(Entity parentEntity, AttributeDefinition def, String value, Integer fieldIndex) {
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
			if(fieldIndex != null && fieldIndex == 1) {
				//unit name
				fieldValue = value;
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
			if(fieldIndex != null && fieldIndex == 2) {
				//unit name
				fieldValue = value;
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
	
	private Value parseCompositeAttributeValue(Entity parentEntity, AttributeDefinition defn, String value) {
		Value result;
		if(defn instanceof CodeAttributeDefinition) {
			Record record = parentEntity.getRecord();
			ModelVersion version = record .getVersion();
			result = parseCode(parentEntity, (CodeAttributeDefinition) defn, value, version );
		} else if(defn instanceof RangeAttributeDefinition) {
			RangeAttributeDefinition rangeDef = (RangeAttributeDefinition) defn;
			RangeAttributeDefinition.Type type = rangeDef.getType();
			NumericRange<?> range = null;
			Unit unit = null; //todo check if unit is required here or is set later by the client
			switch(type) {
				case INTEGER:
					range = IntegerRange.parseIntegerRange(value, unit);
					break;
				case REAL:
					range = RealRange.parseRealRange(value, unit);
					break;
			}
			result = range;
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
	private void promote(Step to) throws RecordPersistenceException, RecordPromoteException  {
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
		} else {
			throw new IllegalStateException("The active record cannot be submitted: it is not in the exptected phase: " + exptectedStep);
		}
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
	private void demote(Step to) throws RecordPersistenceException {
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
	 */
	@Secured("ROLE_ENTRY")
	public void clearActiveRecord() throws RecordPersistenceException {
		sessionManager.checkIsActiveRecordLocked();
		SessionState sessionState = this.sessionManager.getSessionState();
		CollectRecord activeRecord = sessionState.getActiveRecord();
		if ( activeRecord != null && activeRecord.getId() != null ) {
			this.recordManager.releaseLock(activeRecord.getId());
		}
		this.sessionManager.clearActiveRecord();
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
	private List<CodeListItem> getAssignableCodeListItems(Entity parent, CodeAttributeDefinition def) {
		CollectRecord record = getActiveRecord();
		ModelVersion version = record.getVersion();
		
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
			for (CodeListItem item : items) {
				if(version.isApplicable(item)) {
					result.add(item);
				}
			}
		}
		return result;
	}
	
	private CodeAttribute getCodeParent(Entity context, CodeAttributeDefinition def) {
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

	private CodeListItem findCodeListItem(List<CodeListItem> siblings, String code) {
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
	
	private Code parseCode(Entity parent, CodeAttributeDefinition def, String value, ModelVersion version) {
		List<CodeListItem> items = getAssignableCodeListItems(parent, def);
		Code code = parseCode(value, items, version);
		return code;
	}
	
	private Code parseCode(String value, List<CodeListItem> codeList, ModelVersion version) {
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
