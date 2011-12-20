/**
 * 
 */
package org.openforis.collect.blazeds.service;

import java.util.List;

import org.openforis.collect.blazeds.service.UpdateRequest.Method;
import org.openforis.collect.exception.DuplicateIdException;
import org.openforis.collect.exception.InvalidIdException;
import org.openforis.collect.exception.MultipleEditException;
import org.openforis.collect.exception.NonexistentIdException;
import org.openforis.collect.exception.RecordLockedException;
import org.openforis.collect.manager.CollectRecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ModelObjectDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.ModelObject;
import org.openforis.idm.model.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.remoting.RemotingInclude;
import org.springframework.security.access.AccessDeniedException;

/**
 * @author M. Togna
 */
public class DataService {

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private CollectRecordManager recordManager;

	@RemotingInclude
	public Record loadRecord(String entityName, long id) throws RecordLockedException, MultipleEditException, NonexistentIdException, AccessDeniedException {
		Record record = recordManager.checkout(entityName, id);
		sessionManager.setActiveRecord(record);
		return record;
	}

	@RemotingInclude
	public List<RecordListItem> getRecordsSummary() {
		List<RecordListItem> list = recordManager.getRecordsSummary();
		return list;
	}

	@RemotingInclude
	public Record newRecord(String name, Survey survey, String rootEntityId) throws MultipleEditException, DuplicateIdException, InvalidIdException, DuplicateIdException, AccessDeniedException,
			RecordLockedException {

		Record record = recordManager.create(name, survey, rootEntityId);
		return record;
	}

	@RemotingInclude
	public void saveActiveRecord() {
		Record record = this.sessionManager.getSessionState().getActiveRecord();
		recordManager.save(record);
	}

	@RemotingInclude
	public void deleteActiveRecord() {
		Record record = this.sessionManager.getSessionState().getActiveRecord();
		recordManager.delete(record.getRootEntity().getName(), record.getId());
		this.sessionManager.clearActiveRecord();
	}

	@RemotingInclude
	public void updateRootEntityKey(String recordId, String newRootEntityKey) throws DuplicateIdException, InvalidIdException, NonexistentIdException, AccessDeniedException, RecordLockedException {
	}

	@RemotingInclude
	public List<ModelObject<? extends ModelObjectDefinition>> updateActiveRecord(UpdateRequest request) {
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

	@RemotingInclude
	public void promote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
		this.recordManager.promote(recordId);
	}

	@RemotingInclude
	public void demote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
		this.recordManager.demote(recordId);
	}

	@RemotingInclude
	public void updateModelObjectHierarchy(ModelObject<? extends ModelObjectDefinition> modelObject, int newPosition) {
	}

	@RemotingInclude
	public List<String> find(String context, String query) {
		return null;
	}

	/**
	 * remove the active record from the current session
	 */
	@RemotingInclude
	public void clearActiveRecord() {
		this.sessionManager.clearActiveRecord();
	}

	@RemotingInclude
	public List<CodeListItem> findCodeListItemsById(String context, String ids) {
		return null;
	}

	@RemotingInclude
	public CodeListItem findCodeListParent(String contextPath) {
		return null;
	}

	protected SessionManager getSessionManager() {
		return sessionManager;
	}

	protected CollectRecordManager getRecordManager() {
		return recordManager;
	}
}
