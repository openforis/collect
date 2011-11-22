/**
 * 
 */
package org.openforis.collect.blazeds.service;

import java.util.List;

import org.openforis.collect.exception.InvalidIdException;
import org.openforis.collect.exception.OperationNotPermittedException;
import org.openforis.collect.model.RecordListItem;
import org.openforis.collect.model.UpdateRequest;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.model.ModelObject;
import org.openforis.idm.model.Record;

/**
 * @author Mino Togna
 */
public class DataService {

	/**
		 */
	public Record loadRecord(String recordId) throws OperationNotPermittedException, InvalidIdException {
		return null;
	}

	/**
			 */
	public List<RecordListItem> getRecordsSummary() {
		return null;
	}

	/**
		 */
	public Record newRecord(String recordId) throws OperationNotPermittedException, InvalidIdException {
		return null;
	}

	/**
			 */
	public void saveActiveRecord() {
	}

	/**
				 */
	public void deleteActiveRecord() {
	}

	/**
					 */
	public void updateRootEntityKey(String recordId, String newRootEntityKey) throws OperationNotPermittedException, InvalidIdException {
	}

	/**
		 */
	public List<ModelObject<?, ?>> updateActiveRecord(UpdateRequest request) {
		return null;
	}

	/**
		 */
	public void promote(String recordId) throws OperationNotPermittedException, InvalidIdException {
	}

	/**
			 */
	public void demote(String recordId) throws OperationNotPermittedException, InvalidIdException {
	}

	/**
		 */
	public void updateModelObjectHierarchy(ModelObject<?, ?> modelObject, int newPosition) {
	}

	/**
		 */
	public List<String> find(String context, String query) {
		return null;
	}

	/**
	 * remove the active record from the current session
	 */
	public void clearActiveRecord() {
	}

	public List<CodeListItem> getRelevantCodeListItemsById(String context, String ids) {
		return null;
	}

	public CodeListItem getRelevantCodeListParent(String contextPath) {
		return null;
	}
}
