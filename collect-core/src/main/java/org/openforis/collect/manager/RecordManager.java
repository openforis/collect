/**
 * 
 */
package org.openforis.collect.manager;

import java.util.List;

import org.openforis.collect.exception.AccessDeniedException;
import org.openforis.collect.exception.DuplicateIdException;
import org.openforis.collect.exception.InvalidIdException;
import org.openforis.collect.exception.MultipleEditException;
import org.openforis.collect.exception.NonexistentIdException;
import org.openforis.collect.exception.RecordLockedException;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.persistence.RecordDAO;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Record;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
public class RecordManager {

	@Autowired
	private RecordDAO recordDAO;
	
	public Record create(Survey survey, String entityName) {
		// TODO Auto-generated method stub
		return null;
	}

	public Record load(String entityName, long id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(Record record) {
		// TODO Auto-generated method stub

	}

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

	public List<RecordSummary> getSummaries() {
		// TODO implement getRecordSummaries
		return null;
	}
	
	public List<RecordSummary> getSummaries(int fromIndex, int toIndex, String orderByFieldName) {
		List<RecordSummary> recordsSummary = recordDAO.getRecordsSummary(fromIndex, toIndex, orderByFieldName);
		return recordsSummary;
	}
	
	public int getCountRecords() {
		int count = recordDAO.getCountRecords();
		return count;
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
