/**
 * 
 */
package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.exception.DuplicateIdException;
import org.openforis.collect.exception.InvalidIdException;
import org.openforis.collect.exception.MultipleEditException;
import org.openforis.collect.exception.NonexistentIdException;
import org.openforis.collect.exception.RecordLockedException;
import org.openforis.collect.model.RecordListItem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.persistence.RecordManager;
import org.springframework.security.access.AccessDeniedException;

/**
 * @author M. Togna
 * 
 */
public class CollectRecordManager implements RecordManager {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.persistence.RecordManager#getSurvey()
	 */
	@Override
	public Survey getSurvey() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.persistence.RecordManager#create(java.lang.String)
	 */
	@Override
	public Record create(String entityName) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.persistence.RecordManager#load(java.lang.String, long)
	 */
	@Override
	public Record load(String entityName, long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.persistence.RecordManager#save(org.openforis.idm.model.Record)
	 */
	@Override
	public void save(Record record) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openforis.idm.model.persistence.RecordManager#delete(java.lang.String, long)
	 */
	@Override
	public void delete(String entityName, long id) {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns a record and lock it
	 * 
	 * @param entityName
	 * @param id
	 * @return
	 */
	public Record checkout(String entityName, long id) throws RecordLockedException, MultipleEditException, NonexistentIdException, AccessDeniedException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<RecordListItem> getRecordsSummary() {
		// TODO
		return null;
	}

	public Record create(String name, Survey survey, String rootEntityId) throws MultipleEditException, DuplicateIdException, InvalidIdException, DuplicateIdException, AccessDeniedException,
			RecordLockedException {
		// TODO
		return null;
	}

	public void lock(Record record) {

	}

	public void unlock(Record record) {

	}

	public void updateRootEntityKey(String recordId, String newRootEntityKey) throws DuplicateIdException, InvalidIdException, NonexistentIdException, AccessDeniedException, RecordLockedException {

	}

	public void promote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
	}

	public void demote(String recordId) throws InvalidIdException, MultipleEditException, NonexistentIdException, AccessDeniedException, RecordLockedException {
	}
}
