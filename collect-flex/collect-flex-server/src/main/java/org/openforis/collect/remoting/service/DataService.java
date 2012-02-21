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
		int count = recordManager.getRecordCount(rootEntityDefinition);
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
		recordManager.addEmptyEnumeratedEntities(rootEntity, version);
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
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		ModelVersion version = record.getVersion();
		Integer parentEntityId = request.getParentEntityId();
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		EntityDefinition parentDef = parentEntity.getDefinition();
		Integer nodeId = request.getNodeId();
		Integer fieldIndex = request.getFieldIndex();
		String nodeName = request.getNodeName();
		Object fieldVal = null;
		Node<?> node = null;
		if(nodeId != null) {
			node = record.getNodeByInternalId(nodeId);
		}
		NodeDefinition nodeDef = ((EntityDefinition) parentEntity.getDefinition()).getChildDefinition(nodeName);
		String requestValue = request.getValue();
		String remarks = request.getRemarks();
		//parse request values into a list of attribute value objects (for example Code, Date, Time...)
		List<?> values = null;
		if(requestValue != null && nodeDef instanceof AttributeDefinition) {
			values = parseValues(parentEntity, (AttributeDefinition) nodeDef, requestValue, fieldIndex);
		}
		AttributeSymbol symbol = request.getSymbol();
		if(symbol == null && AttributeSymbol.isShortKeyForBlank(requestValue)) {
			 symbol = AttributeSymbol.fromShortKey(requestValue);
		}
		Character symbolChar = symbol != null ? symbol.getCode(): null;
		
		Method method = request.getMethod();
		switch (method) {
			case ADD:
				if(nodeDef instanceof AttributeDefinition) {
					AttributeDefinition def = (AttributeDefinition) nodeDef;
					List<Attribute<?, ?>> attributes = recordManager.addAttributes(parentEntity, def, values, symbolChar, remarks);
					updatedNodes.addAll(attributes);
				} else {
					Entity e = recordManager.addEntity(parentEntity, nodeName, version);
					updatedNodes.add(e);
				}
				break;
			case UPDATE: 
				//update attribute value
				if(node instanceof Attribute) {
					@SuppressWarnings("unchecked")
					Attribute<?, Object> attribute = (Attribute<?, Object>) node;
					AttributeDefinition def = attribute.getDefinition();
					if(def.isMultiple() && def instanceof CodeAttributeDefinition) {
						List<Attribute<?, ?>> attributes = recordManager.replaceAttributes(parentEntity, def, values, remarks);
						updatedNodes.addAll(attributes);
					} else {
						Object val = null;
						if(values != null && values.size() == 1) {
							val = values.get(0);
						}
						if(fieldIndex != null) {
							@SuppressWarnings("rawtypes")
							Field field = attribute.getField(fieldIndex);
							field.setRemarks(remarks);
							field.setSymbol(symbolChar);
							field.setValue(fieldVal);
						} else {
							attribute.setValue(val);
						}
						updatedNodes.add(attribute);
					}
				}
				break;
			case UPDATE_SYMBOL:
				//update attribute value
				if(nodeDef instanceof AttributeDefinition) {
					List<Node<?>> nodes = parentEntity.getAll(nodeName);
					if(nodes != null) {
						for (Node<?> n : nodes) {
							Attribute<?, ?> a = (Attribute<?, ?>) n;
							//a.setRemarks(remarks);
							if((symbol != null && ! symbol.isReasonBlank()) || a.isEmpty()) {
								//a.setSymbol(symbolChar);
							}
							updatedNodes.add(a);
						}
					}
				} else if(node instanceof Entity) {
					//update only the symbol in entity's attributes
					Entity entity = (Entity) node;
					recordManager.addEmptyAttributes(entity, version);
					List<NodeDefinition> childDefinitions = ((EntityDefinition) nodeDef).getChildDefinitions();
					for (NodeDefinition def : childDefinitions) {
						if(def instanceof AttributeDefinition) {
							String name = def.getName();
							Attribute<?, ?> a = (Attribute<?, ?>) entity.get(name, 0);
							if(! symbol.isReasonBlank() || a.isEmpty()) {
								//a.setSymbol(symbolChar);
							}
							updatedNodes.add(a);
						}
					}
				}
				break;
			case DELETE: 
				Node<?> deleted = recordManager.deleteNode(parentEntity, node);
				updatedNodes.add(deleted);
				break;
			
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

	@SuppressWarnings("unchecked")
	private List<?> parseValues(Entity parentEntity, AttributeDefinition def, String value, int fieldIndex) {
		List<Object> result = new ArrayList<Object>();
		CollectRecord activeRecord = getActiveRecord();
		ModelVersion version = activeRecord.getVersion();
		
		if(def instanceof BooleanAttributeDefinition) {
			result.add(Boolean.parseBoolean(value));
		} else if(def instanceof CodeAttributeDefinition) {
			List<?> codes = parseCodes(parentEntity, (CodeAttributeDefinition) def, value, version);
			if(codes.size() > 0) {
				result = (List<Object>) codes;
			}
		} else if(def instanceof CoordinateAttributeDefinition) {
			//TODO
			/*
			Object val = null;
			if(fieldIndex == 0 || fieldIndex == 1) {
				val = Long.parseLong(value);
			} else {
				val = value;
			}
			result.add(val);
			*/
		} else if(def instanceof DateAttributeDefinition) {
			int val = Integer.parseInt(value);
			result.add(val);
		} else if(def instanceof NumberAttributeDefinition) {
			NumberAttributeDefinition numberDef = (NumberAttributeDefinition) def;
			Type type = numberDef.getType();
			Number number = null;
			switch(type) {
				case INTEGER:
					number = Integer.parseInt(value);
					break;
				case REAL:
					number = Double.parseDouble(value);
					break;
			}
			if(number != null) {
				result.add(number);
			}
		} else if(def instanceof RangeAttributeDefinition) {
			org.openforis.idm.metamodel.RangeAttributeDefinition.Type type = ((RangeAttributeDefinition) def).getType();
			Number number = null;
			switch(type) {
				case INTEGER:
					number = Integer.parseInt(value);
					break;
				case REAL:
					number =  Double.parseDouble(value);
					break;
			}
			if(number != null) {
				result.add(number);
			}
		} else if(def instanceof TaxonAttributeDefinition) {
			/*
			int taxonId = Integer.parseInt(value);
			int vernacularNameId = Integer.parseInt(value);
			*/
			//result.add(o);
		} else if(def instanceof TimeAttributeDefinition) {
			int val = Integer.parseInt(value);
			result.add(val);
		} else {
			result.add(value);
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
