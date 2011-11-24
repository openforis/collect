/**
 * 
 */
package org.openforis.collect.blazeds.service;

import java.util.List;

import org.openforis.collect.exception.DuplicateIdException;
import org.openforis.collect.exception.InvalidIdException;
import org.openforis.collect.exception.MultipleEditException;
import org.openforis.collect.exception.NonexistentIdException;
import org.openforis.collect.exception.RecordLockedException;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.RecordListItem;
import org.openforis.collect.model.UpdateRequest;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.model.ModelObject;
import org.openforis.idm.model.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.flex.remoting.RemotingInclude;
import org.springframework.security.access.AccessDeniedException;

/**
 * @author Mino Togna
 */
public class DataService {

	@Autowired
	protected SessionManager sessionManager;

	@RemotingInclude
	public Record loadRecord(String recordId) throws RecordLockedException, MultipleEditException, NonexistentIdException, AccessDeniedException {
		return null;
	}

	@RemotingInclude
	public List<RecordListItem> getRecordsSummary() {
		return null;
	}

	@RemotingInclude
	public void newRecord(Record record) throws MultipleEditException, DuplicateIdException, InvalidIdException, DuplicateIdException, AccessDeniedException, RecordLockedException {
	}

	@RemotingInclude
	public void saveActiveRecord() {
	}

	@RemotingInclude
	public void deleteActiveRecord() {
	}

	@RemotingInclude
	public void updateRootEntityKey(String recordId, String newRootEntityKey) throws DuplicateIdException, InvalidIdException, NonexistentIdException, AccessDeniedException,
			RecordLockedException {
	}

	@RemotingInclude
	public List<ModelObject<?, ?>> updateActiveRecord(UpdateRequest request) {
		return null;
	}

	@RemotingInclude
	public void promote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
	}

	@RemotingInclude
	public void demote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
	}

	@RemotingInclude
	public void updateModelObjectHierarchy(ModelObject<?, ?> modelObject, int newPosition) {
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
	}

	@RemotingInclude
	public List<CodeListItem> getRelevantCodeListItemsById(String context, String ids) {
		return null;
	}

	@RemotingInclude
	public CodeListItem getRelevantCodeListParent(String contextPath) {
		return null;
	}
}
