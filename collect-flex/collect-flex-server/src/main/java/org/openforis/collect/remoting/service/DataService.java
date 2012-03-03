/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openforis.collect.model.proxy.NodeProxy;
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
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.Node;
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
		ModelVersion version = record.getVersion();
		recordManager.addEmptyNodes(rootEntity, version);
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
		recordManager.addEmptyNodes(rootEntity, version);
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
		record.setModifiedDate(new java.util.Date());
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

	public UpdateResponse updateActiveRecord(UpdateRequest request) {
		List<Node<?>> addedNodes = new ArrayList<Node<?>>();
		List<Node<?>> updatedNodes = new ArrayList<Node<?>>();
		List<Integer> deletedNodeIds = new ArrayList<Integer>();
		List<UpdateRequestOperation> operations = request.getOperations();
		for (UpdateRequestOperation operation : operations) {
			processUpdateRequestOperation(operation, addedNodes, updatedNodes, deletedNodeIds);
		}
		//convert nodes to proxies
		UpdateResponse response = new UpdateResponse();
		response.setAddedNodes(NodeProxy.fromList((List<Node<?>>) addedNodes));
		response.setUpdatedNodes(NodeProxy.fromList((List<Node<?>>) updatedNodes));
		response.setDeletedNodeIds(deletedNodeIds.toArray(new Integer[0]));
		return response;
	}
		
	
	@SuppressWarnings("unchecked")
	private void processUpdateRequestOperation(UpdateRequestOperation operation, List<Node<?>> addedNodes, List<Node<?>> updatedNodes, List<Integer> deletedNodeIds) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		ModelVersion version = record.getVersion();
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
		switch (method) {
			case ADD:
				Node<?> addedNode = addNode(version, parentEntity, nodeDef, requestValue, symbol, remarks);
				//nodeStates = record.updateNodeState(addedNode);
				addedNodes.add(addedNode);
				break;
			case UPDATE:
				updateAttribute(record, parentEntity, (Attribute<AttributeDefinition, ?>) node, fieldIndex, requestValue, symbol, remarks);
				//nodeStates = record.updateNodeState(node);
				if(! updatedNodes.contains(node)) {
					updatedNodes.add(node);
				}
				break;
			case DELETE: 
				Node<?> deletedNode = recordManager.deleteNode(parentEntity, node);
				deletedNodeIds.add(deletedNode.getInternalId());
				break;
		}
	}

	@SuppressWarnings("unchecked")
	private Node<?> addNode(ModelVersion version, Entity parentEntity, NodeDefinition nodeDef, String requestValue, FieldSymbol symbol, String remarks) {
		if(nodeDef instanceof AttributeDefinition) {
			AttributeDefinition def = (AttributeDefinition) nodeDef;
			Attribute<?, ?> attribute = (Attribute<?, ?>) def.createNode();
			if(StringUtils.isNotBlank(requestValue)) {
				Object value = parseValue(parentEntity, (AttributeDefinition) nodeDef, requestValue, version);
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
			Entity e = recordManager.addEntity(parentEntity, nodeDef.getName(), version);
			return e;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updateAttribute(CollectRecord record, Entity parentEntity, Attribute<AttributeDefinition, ?> attribute,
			Integer fieldIndex, String requestValue, FieldSymbol symbol, String remarks) {
		ModelVersion version = record.getVersion();
		AttributeDefinition defn = attribute.getDefinition();
		@SuppressWarnings("rawtypes")
		Field field;
		if(fieldIndex >= 0) {
			Object fieldValue = null;
			if(StringUtils.isNotBlank(requestValue)) {
				fieldValue = parseFieldValue(parentEntity, defn, requestValue, fieldIndex);
			}
			field = attribute.getField(fieldIndex);
			field.setValue(fieldValue);
		} else {
			Object value = null;
			if((symbol == null || ! symbol.isReasonBlank()) && StringUtils.isNotBlank(requestValue)) {
				value = parseValue(parentEntity, defn, requestValue, version);
			}
			((Attribute<AttributeDefinition, Object>) attribute).setValue(value);
			field = attribute.getField(0);
		}
		field.setRemarks(remarks);
		Character symbolChar = null;
		if(symbol != null) {
			symbolChar = symbol.getCode();
		}
		field.setSymbol(symbolChar);
	}
	
	private Object parseFieldValue(Entity parentEntity, AttributeDefinition def, String value, Integer fieldIndex) {
		Object result = null;
		if(StringUtils.isBlank(value)) {
			return null;
		}
		if(def instanceof BooleanAttributeDefinition) {
			result = Boolean.parseBoolean(value);
		} else if(def instanceof CodeAttributeDefinition) {
			result = value;
		} else if(def instanceof CoordinateAttributeDefinition) {
			if(fieldIndex != null) {
				if(fieldIndex == 2) {
					//srsId
					result = value;
				} else {
					Double val = Double.valueOf(value);
					result = val;
				}
			}
		} else if(def instanceof DateAttributeDefinition) {
			Integer val = Integer.valueOf(value);
			result = val;
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
				result = number;
			}
		} else if(def instanceof RangeAttributeDefinition) {
			org.openforis.idm.metamodel.RangeAttributeDefinition.Type type = ((RangeAttributeDefinition) def).getType();
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
				result = number;
			}
		} else if(def instanceof TaxonAttributeDefinition) {
			result = value;
		} else if(def instanceof TimeAttributeDefinition) {
			Integer val = Integer.valueOf(value);
			result = val;
		} else {
			result = value;
		}
		return result;
	}
	
	private Object parseValue(Entity parentEntity, AttributeDefinition def, String value, ModelVersion version) {
		Object result;
		if(def instanceof CodeAttributeDefinition) {
			result = parseCode(parentEntity, (CodeAttributeDefinition) def, value, version);
		} else if(def instanceof RangeAttributeDefinition) {
			RangeAttributeDefinition rangeDef = (RangeAttributeDefinition) def;
			org.openforis.idm.metamodel.RangeAttributeDefinition.Type type = rangeDef.getType();
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
