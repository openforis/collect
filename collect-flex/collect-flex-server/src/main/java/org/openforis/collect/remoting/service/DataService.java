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
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.persistence.AccessDeniedException;
import org.openforis.collect.persistence.InvalidIdException;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.NonexistentIdException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.remoting.service.UpdateRequestOperation.Method;
import org.openforis.collect.session.SessionState;
import org.openforis.collect.session.SessionState.RecordState;
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
import org.openforis.idm.metamodel.NumberAttributeDefinition.Type;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
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
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.springframework.beans.factory.annotation.Autowired;
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
	public RecordProxy loadRecord(int id) throws RecordLockedException, MultipleEditException, NonexistentIdException, AccessDeniedException {
		CollectSurvey survey = getActiveSurvey();
		User user = getUserInSession();
		CollectRecord record = recordManager.checkout(survey, user, id);
		//record.updateNodeStates();
		
		Entity rootEntity = record.getRootEntity();
		recordManager.addEmptyNodes(rootEntity);
		SessionState sessionState = sessionManager.getSessionState();
		sessionState.setActiveRecord(record);
		sessionState.setActiveRecordState(RecordState.SAVED);
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
	public Map<String, Object> getRecordSummaries(String rootEntityName, int offset, int maxNumberOfRows, String orderByFieldName, String filter) {
		Map<String, Object> result = new HashMap<String, Object>();
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		Schema schema = activeSurvey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		String rootEntityDefinitionName = rootEntityDefinition.getName();
		int count = recordManager.getCountRecords(rootEntityDefinition);
		List<CollectRecord> summaries = recordManager.getSummaries(activeSurvey, rootEntityDefinitionName, offset, maxNumberOfRows, orderByFieldName, filter);
		List<RecordProxy> proxies = new ArrayList<RecordProxy>();
		for (CollectRecord summary : summaries) {
			proxies.add(new RecordProxy(summary));
		}
		result.put("count", count);
		result.put("records", proxies);
		return result;
	}

	@Transactional
	public RecordProxy createRecord(String rootEntityName, String versionName) throws MultipleEditException, AccessDeniedException, RecordLockedException {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		ModelVersion version = activeSurvey.getVersion(versionName);
		Schema schema = activeSurvey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		CollectRecord record = recordManager.create(activeSurvey, rootEntityDefinition, user, version.getName());
		Entity rootEntity = record.getRootEntity();
		recordManager.addEmptyNodes(rootEntity);
		sessionState.setActiveRecord((CollectRecord) record);
		sessionState.setActiveRecordState(RecordState.NEW);
		RecordProxy recordProxy = new RecordProxy(record);
		return recordProxy;
	}
	
	@Transactional
	public void deleteRecord(int id) throws RecordLockedException, AccessDeniedException, MultipleEditException {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		recordManager.delete(id, user);
		sessionManager.clearActiveRecord();
	}
	
	@Transactional
	public void saveActiveRecord() {
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		record.setModifiedDate(new Date());
		record.setModifiedBy(sessionState.getUser());
		recordManager.save(record);
		sessionState.setActiveRecordState(RecordState.SAVED);
	}

	@Transactional
	public void deleteActiveRecord() throws RecordLockedException, AccessDeniedException, MultipleEditException {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		Record record = sessionState.getActiveRecord();
		recordManager.delete(record.getId(), user);
		sessionManager.clearActiveRecord();
	}

	public List<UpdateResponse> updateActiveRecord(UpdateRequest request) {
		List<UpdateRequestOperation> operations = request.getOperations();
		List<UpdateResponse> updateResponses = new ArrayList<UpdateResponse>();
		for (UpdateRequestOperation operation : operations) {
			Collection<UpdateResponse> responses = processUpdateRequestOperation(operation);
			updateResponses.addAll(responses);
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
		List<Entity> ancestors = null;
		Attribute<? extends AttributeDefinition, ?> attribute = null;
		switch (method) {
			case ADD :
				Node<?> createdNode = addNode(parentEntity, nodeDef, requestValue, symbol, remarks);
				UpdateResponse response = getUpdateResponse(responseMap, createdNode.getInternalId());
				response.setCreatedNode(createdNode);
				relReqDependencies = recordManager. clearRelevanceRequiredStates(createdNode);
				if(createdNode instanceof Attribute){
					attribute = (Attribute<? extends AttributeDefinition, ?>) createdNode;
					checkDependencies = recordManager.clearValidationResults(attribute);
					checkDependencies.add(attribute);
				}
				relReqDependencies.add(new NodePointer(createdNode.getParent(), createdNode.getName()));
				ancestors = createdNode.getAncestors();
				break;
			case UPDATE:
				attribute  = (Attribute<AttributeDefinition, ?>) node;
				ancestors = attribute.getAncestors();
				response = getUpdateResponse(responseMap, attribute.getInternalId());
				response.setUpdatedFieldValues(new HashMap<Integer, Object>());
				if(fieldIndex < 0){
					Object value = null;
					if(requestValue != null) {
						value = parseCompositeAttributeValue(parentEntity, attribute.getDefinition(), requestValue);
					}
					recordManager.setAttributeValue(attribute, value, remarks);
					for (int idx = 0; idx < attribute.getFieldCount(); idx ++) {
						Field<?> field = attribute.getField(idx);
						Object fieldValue = field.getValue();
						response.getUpdatedFieldValues().put(idx, fieldValue);
						recordManager.setFieldValue(attribute, fieldValue, remarks, symbol, idx);
					}
				} else {
					Object value = parseFieldValue(parentEntity, attribute.getDefinition(), requestValue, fieldIndex);
					recordManager.setFieldValue(attribute, value, remarks, symbol, fieldIndex);
					response.getUpdatedFieldValues().put(fieldIndex, attribute.getField(fieldIndex).getValue());
				}
				relReqDependencies = recordManager. clearRelevanceRequiredStates(attribute);
				checkDependencies = recordManager.clearValidationResults(attribute);
				relReqDependencies.add(new NodePointer(attribute.getParent(), attribute.getName()));
				checkDependencies.add(attribute);
				break;
			case DELETE:
				relReqDependencies = new HashSet<NodePointer>();
				checkDependencies = new HashSet<Attribute<?,?>>();
				deleteNode(node, relReqDependencies, checkDependencies, responseMap);
				break;
		}
		prepareUpdateResponse(responseMap, relReqDependencies, checkDependencies, ancestors);
		return responseMap.values();
	}

	private void deleteNode(Node<?> node,Set<NodePointer> relevanceRequiredDependencies, Set<Attribute<?,?>> checkDependencies, Map<Integer, UpdateResponse> responseMap){
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
			recordManager.deleteNode(n);
			
			UpdateResponse resp = getUpdateResponse(responseMap, node.getInternalId());
			resp.setDeletedNodeId(node.getInternalId());
		}
		
		//clear dependencies
		recordManager.clearRelevantDependencies(relevantDependencies);
		requiredDependencies.addAll(relevantDependencies);
		recordManager.clearRequiredDependencies(requiredDependencies);
		recordManager.clearValidationResults(checkDependencies);
		
		relevanceRequiredDependencies.addAll(requiredDependencies);
	}
	
	
	private void prepareUpdateResponse(Map<Integer, UpdateResponse> responseMap, Set<NodePointer> relevanceReqquiredDependencies, Set<Attribute<?, ?>> validtionResultsDependencies, List<Entity> ancestors) {
		if (ancestors != null) {
			for (Entity entity : ancestors) {
				// entity could be root definition
				Entity parent = entity.getParent();
				if (parent != null && !parent.isDetached()) {
					UpdateResponse response = getUpdateResponse(responseMap, parent.getInternalId());
					String childName = entity.getName();
					response.setMinCountValid(childName, parent.validateMinCount(childName));
					response.setRelevant(childName, parent.isRelevant(childName));
					response.setRequired(childName, parent.isRequired(childName));
				}
			}
		}
		if (relevanceReqquiredDependencies != null) {
			for (NodePointer nodePointer : relevanceReqquiredDependencies) {
				Entity entity = nodePointer.getEntity();
				if (!entity.isDetached()) {
					String childName = nodePointer.getChildName();
					UpdateResponse response = getUpdateResponse(responseMap, entity.getInternalId());
					response.setRelevant(childName, entity.isRelevant(childName));
					response.setRequired(childName, entity.isRequired(childName));
					response.setMinCountValid(childName, entity.validateMinCount(childName));
				}
			}
		}
		if (validtionResultsDependencies != null) {
			for (Attribute<?, ?> checkDepAttr : validtionResultsDependencies) {
				if (!checkDepAttr.isDetached()) {
					checkDepAttr.clearValidationResults();
					ValidationResults results = checkDepAttr.validateValue();
					UpdateResponse response = getUpdateResponse(responseMap, checkDepAttr.getInternalId());
					response.setAttributeValidationResults(results);
				}
			}
		}
	}

	private UpdateResponse getUpdateResponse(Map<Integer, UpdateResponse> responseMap, int nodeId){
		UpdateResponse response = responseMap.get(nodeId);
		if(response == null){
			response = new UpdateResponse(nodeId);
			responseMap.put(nodeId, response);
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	private Node<?> addNode(Entity parentEntity, NodeDefinition nodeDef, String requestValue, FieldSymbol symbol, String remarks) {
		
		if(nodeDef instanceof AttributeDefinition) {
			AttributeDefinition def = (AttributeDefinition) nodeDef;
			Attribute<?, ?> attribute = (Attribute<?, ?>) def.createNode();
			if(StringUtils.isNotBlank(requestValue)) {
				Object value = parseCompositeAttributeValue(parentEntity, (AttributeDefinition) nodeDef, requestValue);
				((Attribute<?, Object>) attribute).setValue(value);
			}
			if(symbol != null || remarks != null) {
				Character symbolChar = null;
				if(symbol != null) {
					symbolChar = symbol.getCode();
				}
				Field<?> firstField = attribute.getField(0);
				firstField.setSymbol(symbolChar);
				firstField.setRemarks(remarks);
			}
			parentEntity.add(attribute);
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
			NumberAttributeDefinition numberDef = (NumberAttributeDefinition) def;
			Type type = numberDef.getType();
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
		} else if(def instanceof RangeAttributeDefinition) {
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
		} else if(def instanceof TimeAttributeDefinition) {
			fieldValue = Integer.valueOf(value);
		} else {
			fieldValue = value;
		}
		return fieldValue;
	}
	
	private Object parseCompositeAttributeValue(Entity parentEntity, AttributeDefinition defn, String value) {
		Object result;
		if(defn instanceof CodeAttributeDefinition) {
			Record record = parentEntity.getRecord();
			ModelVersion version = record .getVersion();
			result = parseCode(parentEntity, (CodeAttributeDefinition) defn, value, version );
		} else if(defn instanceof RangeAttributeDefinition) {
			RangeAttributeDefinition rangeDef = (RangeAttributeDefinition) defn;
			RangeAttributeDefinition.Type type = rangeDef.getType();
			NumericRange<?> range = null;
			switch(type) {
				case INTEGER:
					range = IntegerRange.parseIntegerRange(value);
					break;
				case REAL:
					range = RealRange.parseRealRange(value);
					break;
			}
			result = range;
		} else {
			throw new IllegalArgumentException("Invalid AttributeDefinition: expected CodeAttributeDefinition or RangeAttributeDefinition");
		}
		return result;
	}
	
	
	
	@Transactional
	public int promoteRecord(int recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey survey = sessionState.getActiveSurvey();
		User user = sessionState.getUser();
		int promotedId = this.recordManager.promote(survey, recordId, user);
		sessionManager.clearActiveRecord();
		return promotedId;
	}

	@Transactional
	public void demoteRecord(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
		this.recordManager.demote(recordId);
	}

	public void updateNodeHierarchy(Node<? extends NodeDefinition> node, int newPosition) {
	}

	public List<String> find(String context, String query) {
		return null;
	}

	/**
	 * remove the active record from the current session
	 * @throws RecordLockedException 
	 * @throws AccessDeniedException 
	 * @throws MultipleEditException 
	 */
	public void clearActiveRecord() throws RecordLockedException, AccessDeniedException, MultipleEditException {
		CollectRecord activeRecord = getActiveRecord();
		User user = getUserInSession();
		this.recordManager.unlock(activeRecord, user);
		Integer recordId = activeRecord.getId();
		SessionState sessionState = this.sessionManager.getSessionState();
		if(RecordState.NEW == sessionState.getActiveRecordState()) {
			this.recordManager.delete(recordId, user);
		}
		this.sessionManager.clearActiveRecord();
	}
	
	/**
	 * Gets the code list items assignable to the specified attribute and matching the specified codes.
	 * 
	 * @param parentEntityId
	 * @param attrName
	 * @param codes
	 * @return
	 */
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
	
	private User getUserInSession() {
		SessionState sessionState = getSessionManager().getSessionState();
		User user = sessionState.getUser();
		return user;
	}

	private CollectSurvey getActiveSurvey() {
		SessionState sessionState = getSessionManager().getSessionState();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		return activeSurvey;
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
			Pattern pattern = Pattern.compile("^[0]*" + adaptedCode + "$");
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
