/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.AttributeProxy;
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
		addEmptyAttributes(rootEntity);
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
		int parentNodeId = request.getParentNodeId();
		Node<? extends NodeDefinition> parentNode = record.getNodeById(parentNodeId);
		NodeDefinition parentDef = parentNode.getDefinition();
		if(parentDef instanceof EntityDefinition) {
			String nodeName = request.getNodeName();
			String value = request.getValue();
			NodeDefinition nodeDef = ((EntityDefinition) parentDef).getChildDefinition(nodeName);
			Entity parentEntity = (Entity) parentNode;
			
			Method method = request.getMethod();
			switch (method) {
			case ADD:
				if(nodeDef instanceof AttributeDefinition) {
					AttributeDefinition attributeDef = (AttributeDefinition) nodeDef;
					Object val = parseValue(attributeDef, value);
					Attribute<?, ?> attribute = addAttribute(parentEntity, (AttributeDefinition) nodeDef, val);
					AttributeProxy proxy = new AttributeProxy(attribute);
					result.add(proxy);
				} else {
					Entity node = parentEntity.addEntity(nodeName);
					addEmptyAttributes(node);
					EntityProxy proxy = new EntityProxy(node);
					result.add(proxy);
				}
				break;
			case UPDATE:
				Integer nodeId = request.getNodeId();
				Node<? extends NodeDefinition> node = record.getNodeById(nodeId);
				if(node instanceof Attribute) {
					update((Attribute<?, Object>) node, value);
				}
				@SuppressWarnings("rawtypes")
				AttributeProxy proxy = new AttributeProxy((Attribute) node);
				result.add(proxy);
				break;
			case DELETE:
				break;
			}
			return result;
		} else {
			throw new RuntimeException("Parent node is not an entity");
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Attribute addAttribute(Entity parentEntity, AttributeDefinition def, Object value) {
		String name = def.getName();
		Attribute result = null;
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
		return result;
	}
	
	private void update(Attribute<?, Object> attribute, String value) {
		AttributeDefinition def = (AttributeDefinition) attribute.getDefinition();
		Object val = parseValue(def, value);
		attribute.setValue(val);
	}

	private Object parseValue(AttributeDefinition def, String value) {
		Object result = null;
		if(def instanceof BooleanAttributeDefinition) {
			result = Boolean.parseBoolean(value);
		} else if(def instanceof CodeAttributeDefinition) {
			Code code = new Code(value);
			result = code;
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
				throw new RuntimeException("invalid date format");
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
				throw new RuntimeException("invalid date format");
			}
		} else if(def instanceof TextAttributeDefinition) {
			result = value;
		} else {
			result = value;
		}
		return result;
	}
	
	private void addEmptyAttributes(Entity entity) {
		EntityDefinition entityDef = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDef.getChildDefinitions();
		for (NodeDefinition nodeDef : childDefinitions) {
			if(nodeDef instanceof AttributeDefinition) {
				//TODO verify if is in version
				addAttribute(entity, (AttributeDefinition) nodeDef, null);
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

	public List<CodeListItemProxy> findCodeList(int parentEntityId, String attributeName) {
		CollectRecord activeRecord = this.getActiveRecord();
		Entity parentEntity = (Entity) activeRecord.getNodeById(parentEntityId);
		EntityDefinition parentEntityDef = parentEntity.getDefinition();
		NodeDefinition childDefinition = parentEntityDef.getChildDefinition(attributeName);
		if(childDefinition instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeDef = (CodeAttributeDefinition) childDefinition;
			List<CodeListItem> items = findCodeList(parentEntity, codeDef );
			List<CodeListItemProxy> proxies = CodeListItemProxy.fromList(items);
			List<Node<? extends NodeDefinition>> codes = parentEntity.getAll(attributeName);
			if(codes != null) {
				CodeListItemProxy.setSelectedItems(proxies, codes);
			}
			return proxies;
		} else {
			throw new RuntimeException("CodeAttributeDefinition expected");
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
		/*
		CollectRecord activeRecord = this.getActiveRecord();
		ModelVersion recordVersion = activeRecord.getVersion();
		if (recordVersion != null) {
			for (CodeListItem codeListItem : items) {
				// TODO
				// if (VersioningUtils.hasValidVersion(codeListItem, recordVersion)) {
				// itemsInVersion.add(codeListItem);
				// }
			}
		} else {
			itemsInVersion.addAll(items);
		}
		*/
		itemsInVersion.addAll(items);
		
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
				throw new RuntimeException("error while retrieving parent code list item");
			}
		} else {
			return null;
		}
	}
	
	private CodeListItem getCodeListItem(List<CodeListItem> siblings, String code) {
		for (CodeListItem codeListItem : siblings) {
			String itemCode = codeListItem.getCode();
			if (itemCode.equals(code)) {
				return codeListItem;
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
