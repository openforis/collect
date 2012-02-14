/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.AttributeSymbol;
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
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Time;
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

	@Autowired
	private CodeListManager codeListManager;


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

	public List<NodeProxy> updateActiveRecord(UpdateRequest request) {
		List<Node<?>> updatedNodes = new ArrayList<Node<?>>();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		ModelVersion version = record.getVersion();
		Integer parentEntityId = request.getParentEntityId();
		Entity parentEntity = (Entity) record.getNodeById(parentEntityId);
		EntityDefinition parentDef = parentEntity.getDefinition();
		Integer nodeId = request.getNodeId();
		String nodeName = request.getNodeName();
		Node<?> node = null;
		if(nodeId != null) {
			node = record.getNodeById(nodeId);
		}
		NodeDefinition nodeDef = ((EntityDefinition) parentDef).getChildDefinition(nodeName);
		String requestValue = request.getValue();
		String remarks = request.getRemarks();
		//parse request value into a model value (for example Code, Date, Time...)
		Object value = null;
		if(nodeDef instanceof AttributeDefinition) {
			value = parseValue(parentEntity, (AttributeDefinition) nodeDef, requestValue);
		}
		AttributeSymbol symbol = request.getSymbol();
		if(symbol == null && AttributeSymbol.isShortKeyForBlank(requestValue)) {
			 symbol = AttributeSymbol.fromShortKey(requestValue);
		}
		Character symbolChar = symbol != null ? symbol.getCode(): null;
		
		Method method = request.getMethod();
		switch (method) {
			case ADD: 
			{
				if(nodeDef instanceof AttributeDefinition) {
					AttributeDefinition def = (AttributeDefinition) nodeDef;
					List<Attribute<?, ?>> attributes = recordManager.addAttributes(parentEntity, def, value, symbolChar, remarks);
					updatedNodes.addAll(attributes);
				} else {
					Entity e = recordManager.addEntity(parentEntity, nodeName, version);
					updatedNodes.add(e);
				}
				break;
			}
			case UPDATE:
			{
				//update attribute value
				if(node instanceof Attribute) {
					List<Attribute<?, ?>> attributes = recordManager.updateAttributes(parentEntity, (Attribute<?, ?>) node, value, symbolChar, remarks);
					updatedNodes.addAll(attributes);
				} else if(node instanceof Entity) {
					//update symbol in entity's attributes
					Entity entity = (Entity) node;
					recordManager.addEmptyAttributes(entity, version);
					EntityDefinition entityDef = (EntityDefinition) nodeDef;
					List<NodeDefinition> childDefinitions = entityDef.getChildDefinitions();
					for (NodeDefinition def : childDefinitions) {
						if(def instanceof AttributeDefinition) {
							String name = def.getName();
							Attribute<?, ?> attribute = (Attribute<?, ?>) entity.get(name, 0);
							List<Attribute<?,?>> attributes = recordManager.updateAttributes(parentEntity, attribute, value, symbolChar, remarks);
							updatedNodes.addAll(attributes);
						}
					}
				}
				break;
			}
			case DELETE: 
			{
				Node<?> deleted = recordManager.deleteNode(parentEntity, node);
				updatedNodes.add(deleted);
				break;
			}
		}
		//convert nodes to proxies
		List<NodeProxy> result = NodeProxy.fromList((List<Node<?>>) updatedNodes);
		if(method == Method.DELETE) {
			for (NodeProxy nodeProxy : result) {
				nodeProxy.setDeleted(true);
			}
		}
		return result;
	}

	private Object parseValue(Entity parentEntity, AttributeDefinition def, String value) {
		CollectRecord activeRecord = getActiveRecord();
		ModelVersion version = activeRecord.getVersion();
		
		Object result = null;
		if(def instanceof BooleanAttributeDefinition) {
			result = Boolean.parseBoolean(value);
		} else if(def instanceof CodeAttributeDefinition) {
			List<Code> codes = codeListManager.parseCodes(parentEntity, (CodeAttributeDefinition) def, value, version);
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
		CollectRecord activeRecord = getActiveRecord();
		ModelVersion version = activeRecord.getVersion();
		Entity parentEntity = (Entity) activeRecord.getNodeById(parentEntityId);
		EntityDefinition parentEntityDef = parentEntity.getDefinition();
		NodeDefinition childDefinition = parentEntityDef.getChildDefinition(attributeName);
		if(childDefinition instanceof CodeAttributeDefinition) {
			CodeAttributeDefinition codeDef = (CodeAttributeDefinition) childDefinition;
			List<CodeListItem> items = codeListManager.findCodeList(parentEntity, codeDef, version);
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
