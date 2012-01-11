/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.exception.AccessDeniedException;
import org.openforis.collect.exception.DuplicateIdException;
import org.openforis.collect.exception.InvalidIdException;
import org.openforis.collect.exception.MultipleEditException;
import org.openforis.collect.exception.NonexistentIdException;
import org.openforis.collect.exception.RecordLockedException;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.remoting.service.UpdateRequest.Method;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 */
public class DataService {

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private RecordManager recordManager;

	public Record loadRecord(String entityName, long id) throws RecordLockedException, MultipleEditException, NonexistentIdException, AccessDeniedException {
		Record record = recordManager.checkout(entityName, id);
		sessionManager.setActiveRecord((CollectRecord) record);
		return record;
	}

	public List<RecordSummary> getRecordSummaries() {
		List<RecordSummary> list = recordManager.getSummaries();
		return list;
	}

	public List<RecordSummary> getRecordSummaries(int fromIndex, int toIndex, String orderByFieldName) {
		List<RecordSummary> list = recordManager.getSummaries(fromIndex, toIndex, orderByFieldName);
		return list;
	}

	public int getCountRecords() {
		int count = recordManager.getCountRecords();
		return count;
	}

	public Record newRecord(String name, Survey survey, String rootEntityId) throws MultipleEditException, DuplicateIdException, InvalidIdException, DuplicateIdException, AccessDeniedException,
			RecordLockedException {

		Record record = recordManager.create(name, survey, rootEntityId);
		return record;
	}

	public void saveActiveRecord() {
		Record record = this.sessionManager.getSessionState().getActiveRecord();
		recordManager.save(record);
	}

	public void deleteActiveRecord() {
		Record record = this.sessionManager.getSessionState().getActiveRecord();
		recordManager.delete(record.getRootEntity().getName(), record.getId());
		this.sessionManager.clearActiveRecord();
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

	public void promote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
		this.recordManager.promote(recordId);
	}

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
	 */
	public void clearActiveRecord() {
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
		Attribute<? extends CodeAttributeDefinition, ? extends Code<?>> code = (Attribute<? extends CodeAttributeDefinition, ? extends Code<?>>) this.getActiveRecord().getNodeById(id);
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

	protected CollectRecord getActiveRecord() {
		return this.sessionManager.getSessionState().getActiveRecord();
	}

	protected SessionManager getSessionManager() {
		return sessionManager;
	}

	protected RecordManager getRecordManager() {
		return recordManager;
	}
}
