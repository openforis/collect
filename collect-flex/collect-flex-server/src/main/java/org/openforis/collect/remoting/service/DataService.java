/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.AttributeSymbol;
import org.openforis.collect.model.proxy.NodeProxy;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.persistence.AccessDeniedException;
import org.openforis.collect.persistence.InvalidIdException;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.NonexistentIdException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.remoting.service.UpdateRequest.Method;
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
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
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
		Survey survey = getActiveSurvey();
		User user = getUserInSession();
		CollectRecord record = recordManager.checkout(survey, user, id);
		Entity rootEntity = record.getRootEntity();
		ModelVersion version = record.getVersion();
		recordManager.addEmptyAttributes(rootEntity, version);
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
		Survey activeSurvey = sessionState.getActiveSurvey();
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
		Survey activeSurvey = sessionState.getActiveSurvey();
		ModelVersion version = activeSurvey.getVersion(versionName);
		Schema schema = activeSurvey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		CollectRecord record = recordManager.create(activeSurvey, rootEntityDefinition, user, version.getName());
		Entity rootEntity = record.getRootEntity();
		recordManager.addEmptyAttributes(rootEntity, version);
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

	public List<NodeProxy> updateActiveRecord(UpdateRequest request) {
		List<Node<?>> updatedNodes = new ArrayList<Node<?>>();
		List<Node<?>> removedNodes = new ArrayList<Node<?>>();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		ModelVersion version = record.getVersion();
		Integer parentEntityId = request.getParentEntityId();
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		Integer nodeId = request.getNodeId();
		Integer fieldIndex = request.getFieldIndex();
		String nodeName = request.getNodeName();
		
		Node<?> node = null;
		if(nodeId != null) {
			node = record.getNodeByInternalId(nodeId);
		}
		NodeDefinition nodeDef = ((EntityDefinition) parentEntity.getDefinition()).getChildDefinition(nodeName);
		String requestValue = request.getValue();
		String remarks = request.getRemarks();
		//parse request values into a list of attribute value objects (for example Code, Date, Time...)
		Object value = null;
		if(requestValue != null && nodeDef instanceof AttributeDefinition) {
			value = parseFieldValue(parentEntity, (AttributeDefinition) nodeDef, requestValue, fieldIndex);
		}
		AttributeSymbol symbol = request.getSymbol();
		if(symbol == null && AttributeSymbol.isShortKeyForBlank(requestValue)) {
			 symbol = AttributeSymbol.fromShortKey(requestValue);
		}
		Character symbolChar = symbol != null ? symbol.getCode(): null;
		
		Method method = request.getMethod();
		switch (method) {
			case ADD:
				updatedNodes = addNode(version, parentEntity, nodeDef, value, fieldIndex, symbol, remarks);
				break;
			case UPDATE: 
				if(node instanceof CodeAttribute) {
					removedNodes = removeNodes(parentEntity, nodeName);
				}
				updatedNodes = updateNode(parentEntity, node, fieldIndex, value, symbol, remarks);
				break;
			case UPDATE_SYMBOL:
				//update attribute value
				if(nodeDef instanceof AttributeDefinition) {
					Attribute<?, ?> a = (Attribute<?, ?>) node;
					Field<?> field = a.getField(fieldIndex);
					field.setSymbol(symbolChar);
					field.setRemarks(remarks);
					updatedNodes.add(a);
				} else if(node instanceof Entity) {
					//update only the symbol in entity's attributes
					Entity entity = (Entity) node;
					recordManager.addEmptyAttributes(entity, version);
					List<NodeDefinition> childDefinitions = ((EntityDefinition) nodeDef).getChildDefinitions();
					for (NodeDefinition def : childDefinitions) {
						if(def instanceof AttributeDefinition) {
							String name = def.getName();
							Attribute<?, ?> a = (Attribute<?, ?>) entity.get(name, 0);
							setSymbolInAllFields(a, symbol);
							updatedNodes.add(a);
						}
					}
				}
				break;
			case DELETE: 
				Node<?> deleted = recordManager.deleteNode(parentEntity, node);
				removedNodes.add(deleted);
				break;
			
		}
		//convert nodes to proxies
		List<NodeProxy> result = NodeProxy.fromList((List<Node<?>>) updatedNodes);
		List<NodeProxy> removed = NodeProxy.fromList((List<Node<?>>) removedNodes);
		for (NodeProxy nodeProxy : removed) {
			nodeProxy.setDeleted(true);
		}
		result.addAll(removed);
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Node<?>> updateNode(Entity parentEntity, Node<?> node, Integer fieldIndex, 
			Object value, AttributeSymbol symbol,	String remarks) {
		if(node instanceof Attribute) {
			List<Node<?>> updatedNodes = new ArrayList<Node<?>>();
			Attribute<?, Object> attribute = (Attribute<?, Object>) node;
			CollectRecord activeRecord = getActiveRecord();
			ModelVersion version = activeRecord.getVersion();
			AttributeDefinition def = attribute.getDefinition();
			if(def instanceof CodeAttributeDefinition) {
				CodeAttributeDefinition codeDef = (CodeAttributeDefinition) def;
				String codesString = value != null ? value.toString(): null;
				List<Node<?>> list = insertCodeAttributes(version, parentEntity, codeDef, codesString, symbol, remarks);
				updatedNodes.addAll(list);
			} else {
				if(fieldIndex != null) {
					@SuppressWarnings("rawtypes")
					Field field = attribute.getField(fieldIndex);
					field.setRemarks(remarks);
					field.setSymbol(symbol != null ? symbol.getCode(): null);
					field.setValue(value);
				} else {
					attribute.setValue(value);
				}
				updatedNodes.add(attribute);
			}
			return updatedNodes;
		} else {
			throw new UnsupportedOperationException("Cannot update an entity");
		}
	}

	private List<Node<?>> addNode(ModelVersion version, Entity parentEntity, NodeDefinition nodeDef, Object value, Integer fieldIndex, 
			AttributeSymbol symbol, String remarks) {
		List<Node<?>> addedNodes = new ArrayList<Node<?>>();
		if(nodeDef instanceof AttributeDefinition) {
			AttributeDefinition def = (AttributeDefinition) nodeDef;
			if(def instanceof CodeAttributeDefinition) {
				CodeAttributeDefinition codeDef = (CodeAttributeDefinition) def;
				String codesString = value != null ? value.toString(): null;
				List<Node<?>> list = insertCodeAttributes(version, parentEntity, codeDef, codesString, symbol, remarks);
				addedNodes.addAll(list);
			} else {
				Attribute<?,?> attribute = recordManager.addAttribute(parentEntity, def, null);
				if(fieldIndex != null) {
					@SuppressWarnings("unchecked")
					Field<Object> field = (Field<Object>) attribute.getField(fieldIndex);
					field.setRemarks(remarks);
					field.setSymbol(symbol != null ? symbol.getCode(): null);
					field.setValue(value);
				}
				addedNodes.add(attribute);
			}
		} else {
			Entity e = recordManager.addEntity(parentEntity, nodeDef.getName(), version);
			addedNodes.add(e);
		}
		return addedNodes;
	}
	
	private List<Node<?>> insertCodeAttributes(ModelVersion version, Entity parentEntity, CodeAttributeDefinition def, String codesString, 
			AttributeSymbol symbol, String remarks) {
		List<Node<?>> addedNodes = new ArrayList<Node<?>>();
		List<Code> codes = codesString != null ? parseCodes(parentEntity, def, codesString, version): null;
		if(codes != null) {
			for (Code c : codes) {
				Attribute<?, ?> attribute = recordManager.addAttribute(parentEntity, def, c);
				//set symbol and remarks in first field
				Field<?> field = attribute.getField(0);
				if(symbol != null) {
					field.setSymbol(symbol.getCode());
				}
				field.setRemarks(remarks);
				addedNodes.add(attribute);
			}
		} else {
			Attribute<?,?> attribute = recordManager.addAttribute(parentEntity, def, null);
			Field<?> field = attribute.getField(0);
			field.setRemarks(remarks);
			field.setSymbol(symbol != null ? symbol.getCode(): null);
			addedNodes.add(attribute);
		}
		return addedNodes;
	}
	
	private List<Node<?>> removeNodes(Entity parentEntity, String nodeName) {
		List<Node<?>> deletedNodes = new ArrayList<Node<?>>();
		int count = parentEntity.getCount(nodeName);
		for (int i = count - 1; i >= 0; i--) {
			Node<?> removed = parentEntity.remove(nodeName, i);
			deletedNodes.add(removed);
		}
		return deletedNodes;
	}
	
	private void setSymbolInAllFields(Attribute<?, ?> attribute, AttributeSymbol symbol) {
		Character s = symbol != null ? symbol.getCode(): null;
		int count = attribute.getFieldCount();
		for (int i = 0; i < count; i++) {
			Field<?> field = attribute.getField(i);
			if(s == null || (symbol.isReasonBlank() && field.isEmpty())) {
				field.setSymbol(s);
			}
		}
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
					Long val = Long.valueOf(value);
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
	
	@Transactional
	public void promote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
		this.recordManager.promote(recordId);
	}

	@Transactional
	public void demote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
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

	private Survey getActiveSurvey() {
		SessionState sessionState = getSessionManager().getSessionState();
		Survey activeSurvey = sessionState.getActiveSurvey();
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
				//TODO exception if parent not specified
				items = parentCodeListItem.getChildItems(); 
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
			ExpressionFactory expressionFactory = context.getRecord().getContext().getExpressionFactory();
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
	
	public List<Code> parseCodes(Entity parent, CodeAttributeDefinition def, String value, ModelVersion version) {
		List<Code> result = new ArrayList<Code>();
		List<CodeListItem> items = getAssignableCodeListItems(parent, def);
		StringTokenizer st = new StringTokenizer(value, ",");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			Code code = parseCode(token, items, version);
			if(code != null) {
				result.add(code);
			} else {
				//TODO throw exception
			}
		}
		return result;
	}
}
