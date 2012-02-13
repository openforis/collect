/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.AttributeProxy;
import org.openforis.collect.model.proxy.AttributeSymbol;
import org.openforis.collect.model.proxy.EntityProxy;
import org.openforis.collect.model.proxy.NodeProxy;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.persistence.AccessDeniedException;
import org.openforis.collect.persistence.DuplicateIdException;
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
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.IdmInterpretationError;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.openforis.idm.model.expression.InvalidPathException;
import org.openforis.idm.model.expression.ModelPathExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 */
public class DataService {
	
	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private RecordManager recordManager;

	@Autowired
	private ExpressionFactory expressionFactory;

	@Transactional
	public RecordProxy loadRecord(int id) throws RecordLockedException, MultipleEditException, NonexistentIdException, AccessDeniedException {
		Survey survey = getActiveSurvey();
		User user = getUserInSession();
		CollectRecord record = recordManager.checkout(survey, user, id);
		SessionState sessionState = sessionManager.getSessionState();
		sessionState.setActiveRecord((CollectRecord) record);
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
	public RecordProxy createNewRecord(String rootEntityName, String versionName) throws MultipleEditException, AccessDeniedException, RecordLockedException {
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		Survey activeSurvey = sessionState.getActiveSurvey();
		ModelVersion version = activeSurvey.getVersion(versionName);
		Schema schema = activeSurvey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		CollectRecord record = recordManager.create(activeSurvey, rootEntityDefinition, user, version.getName());
		Entity rootEntity = record.getRootEntity();
		addEmptyAttributes(rootEntity, version);
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

	public void updateRootEntityKey(String recordId, String newRootEntityKey) throws DuplicateIdException, InvalidIdException, NonexistentIdException, AccessDeniedException, RecordLockedException {
	}

	@SuppressWarnings("unchecked")
	public List<NodeProxy> updateActiveRecord(UpdateRequest request) {
		List<NodeProxy> result = new ArrayList<NodeProxy>();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		Integer parentNodeId = request.getParentEntityId();
		Integer nodeId = request.getNodeId();
		Entity parentEntity = (Entity) record.getNodeById(parentNodeId);
		EntityDefinition parentDef = parentEntity.getDefinition();
		String nodeName = request.getNodeName();
		String value = request.getValue();
		String remarks = request.getRemarks();
		AttributeSymbol symbol = request.getSymbol();
		NodeDefinition nodeDef = ((EntityDefinition) parentDef).getChildDefinition(nodeName);
		
		Method method = request.getMethod();
		switch (method) {
		case ADD:
			if(nodeDef instanceof AttributeDefinition) {
				AttributeDefinition def = (AttributeDefinition) nodeDef;
				List<Attribute<?, ?>> attributes = addAttributes(parentEntity, (AttributeDefinition) def, value, symbol, remarks);
				for (Attribute<?, ?> attribute : attributes) {
					NodeProxy proxy = new AttributeProxy(attribute);
					result.add(proxy);
				}
			} else {
				Entity node = addEntity(parentEntity, nodeName, record.getVersion());
				EntityProxy proxy = new EntityProxy(node);
				result.add(proxy);
			}
			break;
		case UPDATE:
			Node<? extends NodeDefinition> node = record.getNodeById(nodeId);
			//update attribute value
			if(node instanceof Attribute) {
				Attribute<?, ?> attribute = (Attribute<?, ?>) node;
				if(node instanceof CodeAttribute && node.getDefinition().isMultiple()) {
					AttributeDefinition def = attribute.getDefinition();
					String name = def.getName();
					//remove old attributes
					int count = parentEntity.getCount(def.getName());
					for (int i = count - 1; i >= 0; i--) {
						parentEntity.remove(name, i);
					}
					//add new attributes
					List<Attribute<?, ?>> attributes = addAttributes(parentEntity, def, value, symbol, remarks);
					for (Attribute<?, ?> a : attributes) {
						NodeProxy proxy = new AttributeProxy(a);
						result.add(proxy);
					}
				} else {
					update(parentEntity, (Attribute<?, Object>) attribute, value, symbol, remarks);
					NodeProxy proxy = new AttributeProxy(attribute);
					result.add(proxy);
				}
			}
			break;
		case DELETE:
			Node<? extends NodeDefinition> nodeToDel = record.getNodeById(nodeId);
			NodeDefinition def = nodeToDel.getDefinition();
			String name = def.getName();
			List<Node<?>> children = parentEntity.getAll(name);
			int index = children.indexOf(nodeToDel);
			Node<?> deleted = parentEntity.remove(name, index);
			NodeProxy proxy;
			if(nodeToDel instanceof Entity) {
				proxy = new EntityProxy((Entity) deleted);
			} else {
				proxy = new AttributeProxy((Attribute<?, ?>) deleted);
			}
			proxy.setDeleted(true);
			result.add(proxy);
			break;
		}
		return result;
	}
	
	private Entity addEntity(Entity parentEntity, String nodeName, ModelVersion version) {
		Entity node = parentEntity.addEntity(nodeName);
		addEmptyAttributes(node, version);
		return node;
	}
	
	private List<Attribute<?, ?>> addAttributes(Entity parentEntity, AttributeDefinition def, String value, AttributeSymbol symbol, String remarks) {
		List<Attribute<?, ?>> result = new ArrayList<Attribute<?, ?>>();
		if(symbol == null && AttributeSymbol.isShortKeyForBlank(value)) {
			 symbol = AttributeSymbol.fromShortKey(value);
		}
		Object val = null;
		if(symbol == null || !symbol.isReasonBlank()) {
			val = parseValue(parentEntity, (AttributeDefinition) def, value);
		}
		if(val instanceof List) {
			List<?> values = (List<?>) val;
			for (Object v : values) {
				Attribute<?, ?> attribute = addAttribute(parentEntity, def, v, symbol, remarks);
				result.add(attribute);
			}
		} else {
			Attribute<?, ?> attribute = addAttribute(parentEntity,  def, val, symbol, remarks);
			result.add(attribute);
		}
		return result;
	}
	
	private Attribute<?, ?> addAttribute(Entity parentEntity, AttributeDefinition def, Object value, AttributeSymbol symbol, String remarks) {
		String name = def.getName();
		Attribute<?, ?> result = null;
		if(def instanceof BooleanAttributeDefinition) {
			result = parentEntity.addValue(name, (Boolean) value);
		} else if(def instanceof CodeAttributeDefinition) {
			result = parentEntity.addValue(name, (Code) value);
		} else if(def instanceof CoordinateAttributeDefinition) {
			result = parentEntity.addValue(name, (Coordinate) value);
		} else if(def instanceof DateAttributeDefinition) {
			result = parentEntity.addValue(name, (Date) value);
		} else if(def instanceof NumericAttributeDefinition) {
			Type type = ((NumericAttributeDefinition) def).getType();
			switch(type) {
				case INTEGER:
					result = parentEntity.addValue(name, (Integer) value);
					break;
				case REAL:
					result = parentEntity.addValue(name, (Double) value);
					break;
			}
		} else if(def instanceof TextAttributeDefinition) {
			result = parentEntity.addValue(name, (String) value);
		} else if(def instanceof TimeAttributeDefinition) {
			result = parentEntity.addValue(name, (Time) value);
		}
		if(symbol != null) {
			result.setSymbol(symbol.getCode());
		} else {
			result.setSymbol(null);
		}
		result.setRemarks(remarks);
		return result;
	}
	
	private Attribute<?, ?> update(Entity parentEntity, Attribute<?, Object> attribute, String value, AttributeSymbol symbol, String remarks) {
		if(value != null && AttributeSymbol.isShortKeyForBlank(value)) {
			symbol = AttributeSymbol.fromShortKey(value);
		}
		Object val = null;
		if(symbol != null) {
			attribute.setSymbol(symbol.getCode());
		} else {
			attribute.setSymbol(null);
		}
		if(symbol == null || ! symbol.isReasonBlank()) {
			AttributeDefinition def = (AttributeDefinition) attribute.getDefinition();
			val = parseValue(parentEntity, def, value);
		}
		attribute.setRemarks(remarks);
		attribute.setValue(val);
		return attribute;
	}

	private Object parseValue(Entity parentEntity, AttributeDefinition def, String value) {
		Object result = null;
		if(def instanceof BooleanAttributeDefinition) {
			result = Boolean.parseBoolean(value);
		} else if(def instanceof CodeAttributeDefinition) {
			StringTokenizer st = new StringTokenizer(value, ",");
			List<Code> codes = new ArrayList<Code>();
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				List<CodeListItem> codeList = null;
				if(((CodeAttributeDefinition) def).getList() != null) {
					codeList = findCodeList(parentEntity, (CodeAttributeDefinition) def);
				}
				Code code = parseCode(token, codeList);
				if(code != null) {
					codes.add(code);
				} else {
					//TODO throw exception
				}
			}
			if(codes.size() > 1) {
				result = codes;
			} else if(codes.size() == 1) {
				result = codes.get(0);
			}
		} else if(def instanceof CoordinateAttributeDefinition) {
			//TODO
			result = null;
		} else if(def instanceof DateAttributeDefinition) {
			//parse date string
			String[] parts = value.split("/");
			if(parts.length == 3) {
				String date = parts[0];
				String month = parts[1];
				String year = parts[2];
				result = new Date(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(date));
			} else {
				//TODO error parsing date
				throw new RuntimeException("Error parsing date");
			}
		} else if(def instanceof NumericAttributeDefinition) {
			NumberAttributeDefinition numberDef = (NumberAttributeDefinition) def;
			Type type = numberDef.getType();
			switch(type) {
				case INTEGER:
					result = Integer.parseInt(value);
					break;
				case REAL:
					result = Double.parseDouble(value);
					break;
			}
		} else if(def instanceof TimeAttributeDefinition) {
			//parse date string
			String[] parts = value.split(":");
			if(parts.length == 2) {
				String hours = parts[0];
				String minutes = parts[1];
				result = new Time(Integer.parseInt(hours), Integer.parseInt(minutes));
			} else {
				//TODO error parsing time
				throw new RuntimeException("Error parsing time");
			}
		} else if(def instanceof TextAttributeDefinition) {
			result = value;
		} else {
			result = value;
		}
		return result;
	}
	
	private Code parseCode(String value, List<CodeListItem> codeList) {
		Code code = null;
		String[] strings = value.split(":");
		String codeStr = strings[0].trim();
		String qualifier = null;
		if(strings.length == 2) {
			qualifier = strings[1].trim();
		}
		if(codeList != null) {
			CodeListItem codeListItem = getCodeListItem(codeList, codeStr);
			if(codeListItem != null) {
				code = new Code(codeListItem.getCode(), qualifier);
				return code;
			}
		}
		if (code == null) {
			code = new Code(codeStr, qualifier);
		}
		return code;
	}
	
	private void addEmptyAttributes(Entity entity, ModelVersion version) {
		EntityDefinition entityDef = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDef.getChildDefinitions();
		for (NodeDefinition nodeDef : childDefinitions) {
			if(ModelVersionUtil.isInVersion(nodeDef, version)) {
				if(nodeDef instanceof AttributeDefinition) {
					addAttribute(entity, (AttributeDefinition) nodeDef, null, null, null);
				} else if(nodeDef instanceof EntityDefinition && ! nodeDef.isMultiple()) {
					addEntity(entity, nodeDef.getName(), version);
				}
			}
		}
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
	 * Returns all code list items that matches the comma separated ids
	 * 
	 * @param context
	 * @param ids
	 * @return
	 */
	public List<CodeListItem> findCodeListItemsById(Integer id, String ids) {
		/*
		@SuppressWarnings("unchecked")
		CodeAttribute code = (CodeAttribute) this.getActiveRecord().getNodeById(id);
		List<CodeListItem> codeList = findCodeList(code);
		for (CodeListItem item : codeList) {
			//TODO
		}
		*/
		return null;
	}
	
	/**
	 * Called from client.
	 * 
	 * @param parentEntityId
	 * @param attributeName
	 * @return
	 */
	public List<CodeListItemProxy> findCodeList(int parentEntityId, String attributeName) {
		CollectRecord activeRecord = this.getActiveRecord();
		Entity parentEntity = (Entity) activeRecord.getNodeById(parentEntityId);
		EntityDefinition parentEntityDef = parentEntity.getDefinition();
		NodeDefinition childDefinition = parentEntityDef.getChildDefinition(attributeName);
		if(childDefinition instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeDef = (CodeAttributeDefinition) childDefinition;
			List<CodeListItem> items = findCodeList(parentEntity, codeDef );
			List<CodeListItemProxy> proxies = CodeListItemProxy.fromList(items);
			List<Node<?>> codes = parentEntity.getAll(attributeName);
			if(codes != null) {
				CodeListItemProxy.setSelectedItems(proxies, codes);
			}
			return proxies;
		} else {
			return Collections.emptyList();
		}
	}
	
	private List<CodeListItem> findCodeList(Entity parentEntity, CodeAttributeDefinition def) {
		CodeAttribute parent = findParent(parentEntity, def);
		List<CodeListItem> items;
		if(parent == null) {
			//node is root
			CodeList list = def.getList();
			items = list.getItems();
		} else {
			Entity ancestorEntity = parent.getParent();
			CodeAttributeDefinition parentDefinition = parent.getDefinition();
			List<CodeListItem> codeList = findCodeList(ancestorEntity, parentDefinition);
			Code parentCode = parent.getValue();
			String parentCodeValue = parentCode.getCode();
			CodeListItem parentItem = getCodeListItem(codeList, parentCodeValue);
			items = parentItem.getChildItems();
		}
		List<CodeListItem> itemsInVersion = new ArrayList<CodeListItem>();
		CollectRecord activeRecord = this.getActiveRecord();
		ModelVersion version = activeRecord.getVersion();
		for (CodeListItem codeListItem : items) {
			if (ModelVersionUtil.isInVersion(codeListItem, version)) {
				itemsInVersion.add(codeListItem);
			}
		}
		return itemsInVersion;
	}
	
	
	/**
	 * Apply the parentExpression in the attribute definition to the parentEntity specified
	 * 
	 * @param parentEntity
	 * @param def
	 * @return
	 */
	private CodeAttribute findParent(Entity parentEntity, CodeAttributeDefinition def) {
		String parentExpression = def.getParentExpression();
		if(StringUtils.isNotBlank(parentExpression)) {
			ModelPathExpression expression = expressionFactory.createModelPathExpression(parentExpression);
			Object result;
			try {
				result = expression.evaluate(parentEntity);
				if(result instanceof CodeAttribute) {
					return (CodeAttribute) result;
				} else {
					throw new RuntimeException("Result is not a code attribute");
				}
			} catch (InvalidPathException e) {
				throw new IdmInterpretationError("Error retrieving parent code", e);
			}
		} else {
			return null;
		}
	}
	
	private CodeListItem getCodeListItem(List<CodeListItem> siblings, String code) {
		for (CodeListItem item : siblings) {
			String itemCode = item.getCode();
			String paddedCode;
			if (itemCode.length() > code.length()) {
				//try to left pad the code with '0'
				paddedCode = StringUtils.leftPad(code, itemCode.length(), '0');
			} else {
				paddedCode = code;
			}
			if (itemCode.equalsIgnoreCase(paddedCode)) {
				return item;
			}
		}
		return null;
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

}
