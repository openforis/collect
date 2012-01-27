/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.model.User;
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
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
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
		int count = recordManager.getCountRecords(rootEntityDefinition);
		List<RecordSummary> list = recordManager.getSummaries(rootEntityDefinition, offset, maxNumberOfRows, orderByFieldName, filter);
		result.put("count", count);
		result.put("records", list);
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

	public List<Node<? extends NodeDefinition>> updateActiveRecord(UpdateRequest request) {
		Method method = request.getMethod();
		switch (method) {
			case ADD:

				break;
			case UPDATE:

				break;
			case DELETE:

				break;
		}
		return null;
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
		@SuppressWarnings("unchecked")
		Attribute<? extends CodeAttributeDefinition, ? extends Code> code = (Attribute<? extends CodeAttributeDefinition, ? extends Code>) this.getActiveRecord().getNodeById(id);
		return null;
	}

	public List<CodeListItem> findCodeList(Integer id) {
		CollectRecord activeRecord = this.getActiveRecord();
		@SuppressWarnings("unchecked")
		Attribute<CodeAttributeDefinition, ?> code = (Attribute<CodeAttributeDefinition, ?>) activeRecord.getNodeById(id);

		List<CodeListItem> items = new ArrayList<CodeListItem>();
		CodeListItem parent = findCodeListParent(code);
		List<CodeListItem> children = parent.getChildItems();

		ModelVersion recordVersion = activeRecord.getVersion();
		if (recordVersion != null) {
			for (CodeListItem codeListItem : children) {
				// TODO
				// if (VersioningUtils.hasValidVersion(codeListItem, recordVersion)) {
				// items.add(codeListItem);
				// }
			}
		} else {
			items.addAll(children);
		}
		return items;
	}

	/**
	 * Returns the code list item parent (see chooser popup of code list )
	 * 
	 * @param contextPath
	 * @return
	 */
	public CodeListItem findCodeListParent(Node<? extends NodeDefinition> node) {
		// Node<? extends NodeDefinition> node = record.getNodeById(id);
		if (node != null && node instanceof Attribute) {
			// TODO
		}
		return null;
	}

	private CodeListItem getCodeListItem(CodeList codeList, Object value) {
		List<CodeListItem> items = codeList.getItems();
		for (CodeListItem codeListItem : items) {
			String code = codeListItem.getCode();
			if (code.equals(value.toString())) {
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
