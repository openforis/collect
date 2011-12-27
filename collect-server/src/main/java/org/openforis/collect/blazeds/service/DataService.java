/**
 * 
 */
package org.openforis.collect.blazeds.service;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.blazeds.service.UpdateRequest.Method;
import org.openforis.collect.exception.DuplicateIdException;
import org.openforis.collect.exception.InvalidIdException;
import org.openforis.collect.exception.MultipleEditException;
import org.openforis.collect.exception.NonexistentIdException;
import org.openforis.collect.exception.RecordLockedException;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectAttribute;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.util.VersioningUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.SchemaObjectDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.ModelObject;
import org.openforis.idm.model.Record;
import org.openforis.idm.model.Value;
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
	private RecordManager recordManager;

	@RemotingInclude
	public Record loadRecord(String entityName, long id) throws RecordLockedException, MultipleEditException, NonexistentIdException, AccessDeniedException {
		Record record = recordManager.checkout(entityName, id);
		sessionManager.setActiveRecord((CollectRecord) record);
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
	public List<ModelObject<? extends SchemaObjectDefinition>> updateActiveRecord(UpdateRequest request) {
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
	public void updateModelObjectHierarchy(ModelObject<? extends SchemaObjectDefinition> modelObject, int newPosition) {
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

	/**
	 * Returns all code list items that matches the comma separated ids
	 * 
	 * @param context
	 * @param ids
	 * @return
	 */
	@RemotingInclude
	public List<CodeListItem> findCodeListItemsById(Long id, String ids) {
		@SuppressWarnings("unchecked")
		CollectAttribute<? extends CodeAttributeDefinition,? extends Code<?>> code = (CollectAttribute<? extends CodeAttributeDefinition, ? extends Code<?>>) this.getActiveRecord().getModelObjectById(id);
		return null;
	}
	
	@RemotingInclude
	public List<CodeListItem> findCodeList(Long id) {
		CollectRecord activeRecord = this.getActiveRecord();
		@SuppressWarnings("unchecked")
		Attribute<CodeAttributeDefinition, ? extends Value> code = (Attribute<CodeAttributeDefinition, ? extends Value>) activeRecord.getModelObjectById(id);

		List<CodeListItem> items = new ArrayList<CodeListItem>();
		CodeListItem parent = findCodeListParent(code);
		List<CodeListItem> children = parent.getChildItems();

		ModelVersion recordVersion = activeRecord.getVersion();
		if (recordVersion != null) {
			for (CodeListItem codeListItem : children) {
				if (VersioningUtils.hasValidVersion(codeListItem, recordVersion)) {
					items.add(codeListItem);
				}
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
	@RemotingInclude
	public CodeListItem findCodeListParent(ModelObject<? extends SchemaObjectDefinition> modelObject) {
//		ModelObject<? extends SchemaObjectDefinition> modelObject = record.getModelObjectById(id);
		if (modelObject != null && modelObject instanceof CollectAttribute) {
			//TODO
		}
		return null;
	}

	private CodeListItem getCodeListItem(CodeList codeList, Object value){
				List<CodeListItem> items = codeList.getItems();
				for (CodeListItem codeListItem : items) {
					List<CodeDefinition> codes = codeListItem.getCodes();
					for (CodeDefinition codeDefinition : codes) {
						if(codeDefinition.getCode().equals(value.toString())){
							return codeListItem;
						}
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
