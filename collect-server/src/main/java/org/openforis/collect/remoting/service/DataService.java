/**
 * 
 */
package org.openforis.collect.remoting.service;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordIndexException;
import org.openforis.collect.manager.RecordIndexManager.SearchType;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordPromoteException;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeMap;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.NodeChangeSetProxy;
import org.openforis.collect.model.proxy.NodeUpdateRequestSetProxy;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.remoting.service.NodeUpdateRequest.AttributeAddRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.AttributeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.DefaultValueApplyRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.EntityAddRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.ErrorConfirmRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.FieldUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.MissingValueApproveRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.NodeDeleteRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.RemarksUpdateRequest;
import org.openforis.collect.remoting.service.recordindex.RecordIndexService;
import org.openforis.collect.web.session.SessionState;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
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
	private transient RecordManager recordManager;
	@Autowired
	private transient CodeListManager codeListManager;
	@Autowired
	private transient RecordFileManager fileManager;
	@Autowired
	private transient RecordIndexService recordIndexService;

	/**
	 * it's true when the root entity definition of the record in session has some nodes with the "collect:index" annotation
	 */
	private boolean hasActiveSurveyIndexedNodes;

	@Transactional
	@Secured("ROLE_ENTRY")
	public RecordProxy loadRecord(int id, Integer stepNumber, boolean forceUnlock) throws RecordPersistenceException, RecordIndexException {
		SessionState sessionState = sessionManager.getSessionState();
		if ( sessionState.isActiveRecordBeingEdited() ) {
			throw new MultipleEditException();
		}
		final CollectSurvey survey = sessionState.getActiveSurvey();
		User user = sessionState.getUser();
		Step step = stepNumber == null ? null: Step.valueOf(stepNumber);
		CollectRecord record = recordManager.checkout(survey, user, id, step, sessionState.getSessionId(), forceUnlock);
		sessionManager.setActiveRecord(record);
		fileManager.resetTempInfo();
		prepareRecordIndexing();
		Locale locale = sessionState.getLocale();
		return new RecordProxy(record, locale);
	}

	protected void prepareRecordIndexing() throws RecordIndexException {
		CollectRecord record = getActiveRecord();
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		hasActiveSurveyIndexedNodes = recordIndexService.hasIndexableNodes(rootEntityDefn);
		recordIndexService.cleanTemporaryIndex();
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
	@Secured("ROLE_ENTRY")
	public Map<String, Object> loadRecordSummaries(String rootEntityName, int offset, int maxNumberOfRows, List<RecordSummarySortField> sortFields, String[] keyValues) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		Schema schema = activeSurvey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		String rootEntityDefinitionName = rootEntityDefinition.getName();
		
		//load summaries
		List<CollectRecord> summaries = recordManager.loadSummaries(activeSurvey, rootEntityDefinitionName, offset, maxNumberOfRows, sortFields, keyValues);
		Locale locale = sessionState.getLocale();
		List<RecordProxy> proxies = RecordProxy.fromList(summaries, locale);
		
		result.put("records", proxies);
		
		//count total records
		RecordFilter filter = new RecordFilter(activeSurvey, rootEntityDefinition.getId());
		filter.setKeyValues(keyValues);
		
		int count = recordManager.countRecords(filter);
		result.put("count", count);
		
		return result;
	}

	@Transactional
	@Secured("ROLE_ENTRY")
	public RecordProxy createRecord(String rootEntityName, String versionName) throws RecordPersistenceException, RecordIndexException {
		SessionState sessionState = sessionManager.getSessionState();
		if ( sessionState.isActiveRecordBeingEdited() ) {
			throw new MultipleEditException();
		}
		String sessionId = sessionState.getSessionId();
		CollectSurvey activeSurvey = sessionState.getActiveSurvey();
		User user = sessionState.getUser();
		Schema schema = activeSurvey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntityName);
		CollectRecord record = recordManager.create(activeSurvey, rootEntityDefinition, user, versionName, sessionId);
		sessionManager.setActiveRecord(record);
		prepareRecordIndexing();
		Locale locale = sessionState.getLocale();
		RecordProxy recordProxy = new RecordProxy(record, locale);
		return recordProxy;
	}
	
	@Transactional
	@Secured("ROLE_ENTRY")
	public void deleteRecord(int id) throws RecordPersistenceException {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey survey = sessionState.getActiveSurvey();
		//TODO check that the record is in ENTRY phase: only delete in ENTRY phase is allowed
		CollectRecord record = recordManager.load(survey, id, Step.ENTRY);
		fileManager.deleteAllFiles(record);
		recordManager.delete(id);
	}
	
	@Transactional
	@Secured("ROLE_ENTRY")
	public void saveActiveRecord() throws RecordPersistenceException, RecordIndexException {
		sessionManager.checkIsActiveRecordLocked();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		User user = sessionState.getUser();
		record.setModifiedDate(new Date());
		record.setModifiedBy(user);
		record.setOwner(user);
		String sessionId = sessionState.getSessionId();
		recordManager.save(record, sessionId);
		if ( fileManager.commitChanges(sessionId, record) ) {
			recordManager.save(record, sessionId);
		}
		if ( isCurrentRecordIndexable() ) {
			recordIndexService.permanentlyIndex(record);
		}
	}

	@Transactional
	@Secured("ROLE_ENTRY")
	public NodeChangeSetProxy updateActiveRecord(NodeUpdateRequestSetProxy requestSet) throws RecordPersistenceException, RecordIndexException {
		sessionManager.checkIsActiveRecordLocked();
		CollectRecord activeRecord = getActiveRecord();
		NodeUpdateRequestSet reqSet = requestSet.toNodeUpdateRequestSet(codeListManager, fileManager, sessionManager, activeRecord);
		NodeChangeSet changeSet = updateRecord(activeRecord, reqSet);
		if ( ! changeSet.isEmpty() && isCurrentRecordIndexable() ) {
			recordIndexService.temporaryIndex(activeRecord);
		}
		Locale currentLocale = getCurrentLocale();
		NodeChangeSetProxy result = new NodeChangeSetProxy(activeRecord, changeSet, currentLocale);
		if ( requestSet.isAutoSave() ) {
			try {
				saveActiveRecord();
				result.setRecordSaved(true);
			} catch(Exception e) {
				result.setRecordSaved(false);
			}
		}
		return result;
	}

	protected NodeChangeSet updateRecord(CollectRecord record, NodeUpdateRequestSet nodeUpdateRequestSet) throws RecordPersistenceException, RecordIndexException {
		List<NodeUpdateRequest> opts = nodeUpdateRequestSet.getRequests();
		NodeChangeMap result = new NodeChangeMap();
		for (NodeUpdateRequest req : opts) {
			NodeChangeSet partialChangeSet = updateRecord(record, req);
			List<NodeChange<?>> changes = partialChangeSet.getChanges();
			for (NodeChange<?> change : changes) {
				result.addOrMergeChange(change);
			}
		}
		return new NodeChangeSet(result.getChanges());
	}
	
	@SuppressWarnings("unchecked")
	protected NodeChangeSet updateRecord(CollectRecord record, NodeUpdateRequest req) throws RecordPersistenceException {
		if ( req instanceof ErrorConfirmRequest ) {
			return recordManager.confirmError(((ErrorConfirmRequest) req).getAttribute());
		} else if ( req instanceof MissingValueApproveRequest ) {
			MissingValueApproveRequest r = (MissingValueApproveRequest) req;
			return recordManager.approveMissingValue(r.getParentEntity(), r.getNodeName());
		} else if ( req instanceof RemarksUpdateRequest ) {
			RemarksUpdateRequest r = (RemarksUpdateRequest) req;
			return recordManager.updateRemarks(r.getField(), r.getRemarks());
		} else if ( req instanceof AttributeAddRequest ) {
			AttributeAddRequest<Value> r = (AttributeAddRequest<Value>) req;
			return recordManager.addAttribute(r.getParentEntity(), r.getNodeName(), r.getValue(), 
					r.getSymbol(), r.getRemarks());
		} else if ( req instanceof EntityAddRequest ) {
			EntityAddRequest r = (EntityAddRequest) req;
			return recordManager.addEntity(r.getParentEntity(), r.getNodeName());
		} else if ( req instanceof AttributeUpdateRequest ) {
			AttributeUpdateRequest<Value> r = (AttributeUpdateRequest<Value>) req;
			Value value = r.getValue();
			FieldSymbol symbol = r.getSymbol();
			if ( value == null && symbol == null || value != null ) {
				return recordManager.updateAttribute(r.getAttribute(), value);
			} else if ( symbol != null ) {
				return recordManager.updateAttribute(r.getAttribute(), symbol);
			} else {
				throw new IllegalArgumentException("Cannot specify both value and symbol");
			}
		} else if ( req instanceof FieldUpdateRequest ) {
			return processUpdateFieldRequest((FieldUpdateRequest<?>) req);
		} else if ( req instanceof DefaultValueApplyRequest ) {
			return recordManager.applyDefaultValue(((DefaultValueApplyRequest) req).getAttribute());
		} else if ( req instanceof NodeDeleteRequest ) {
			return recordManager.deleteNode(((NodeDeleteRequest) req).getNode());
		} else {
			throw new IllegalArgumentException("NodeChange not supported: " + req.getClass().getSimpleName());
		}
	}

	protected <T> NodeChangeSet processUpdateFieldRequest(FieldUpdateRequest<T> r) {
		if ( StringUtils.equals(r.getField().getRemarks(), r.getRemarks()) ) {
			if ( r.getValue() == null && r.getSymbol() == null ) {
				return recordManager.updateField(r.getField(), (T) null);
			} else if ( r.getValue() != null ) {
				return recordManager.updateField(r.getField(), r.getValue());
			} else {
				return recordManager.updateField(r.getField(), r.getSymbol());
			}
		} else {
			return recordManager.updateRemarks(r.getField(), r.getRemarks());
		}
	}
	
	@Secured("ROLE_ENTRY")
	public void promoteToCleansing() throws RecordPersistenceException, RecordPromoteException  {
		promote(Step.CLEANSING);
	}

	@Secured("ROLE_CLEANSING")
	public void promoteToAnalysis() throws RecordPersistenceException, RecordPromoteException  {
		promote(Step.ANALYSIS);
	}
	
	@Transactional
	protected void promote(Step to) throws RecordPersistenceException, RecordPromoteException  {
		sessionManager.checkIsActiveRecordLocked();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		Step currentStep = record.getStep();
		Step exptectedStep = to.getPrevious();
		if ( exptectedStep == currentStep ) {
			User user = sessionState.getUser();
			recordManager.promote(record, user);
			recordManager.releaseLock(record.getId());
			sessionManager.clearActiveRecord();
			if ( isCurrentRecordIndexable() ) {
				recordIndexService.permanentlyIndex(record);
			}
		} else {
			throw new IllegalStateException("The active record cannot be submitted: it is not in the exptected phase: " + exptectedStep);
		}
	}
	
	protected boolean isRecordIndexEnabled() {
		return recordIndexService.isInited();
	}
	
	protected boolean isCurrentRecordIndexable() {
		return isRecordIndexEnabled() && hasActiveSurveyIndexedNodes;
	}

	@Secured("ROLE_ANALYSIS")
	public void demoteToCleansing() throws RecordPersistenceException {
		demote(Step.CLEANSING);
	}
	
	@Secured("ROLE_CLEANSING")
	public void demoteToEntry() throws RecordPersistenceException {
		demote(Step.ENTRY);
	}
		
	@Transactional
	protected void demote(Step to) throws RecordPersistenceException {
		sessionManager.checkIsActiveRecordLocked();
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		Step currentStep = record.getStep();
		Step exptectedStep = to.getNext();
		if ( exptectedStep == currentStep ) {
			CollectSurvey survey = sessionState.getActiveSurvey();
			User user = sessionState.getUser();
			Integer recordId = record.getId();
			recordManager.demote(survey, recordId, record.getStep(), user);
			recordManager.releaseLock(recordId);
			sessionManager.clearActiveRecord();
		} else {
			throw new IllegalStateException("The active record cannot be demoted: it is not in the exptected phase: " + exptectedStep);
		}
	}

	/**
	 * remove the active record from the current session
	 * @throws RecordPersistenceException 
	 * @throws RecordIndexException 
	 */
	@Secured("ROLE_ENTRY")
	public void clearActiveRecord() throws RecordPersistenceException, RecordIndexException {
		sessionManager.releaseRecord();
		if ( isCurrentRecordIndexable() ) {
			recordIndexService.cleanTemporaryIndex();
		}
	}
	
	@Secured("ROLE_ENTRY")
	public void moveNode(int nodeId, int index) {
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = sessionState.getActiveRecord();
		recordManager.moveNode(record, nodeId, index);
	}

	/**
	 * Gets the code list items assignable to the specified attribute and matching the specified codes.
	 * 
	 * @param parentEntityId
	 * @param attrName
	 * @param codes
	 * @return
	 */
	@Secured("ROLE_ENTRY")
	public List<CodeListItemProxy> getCodeListItems(int parentEntityId, String attrName, String[] codes){
		CollectRecord record = getActiveRecord();
		Entity parent = (Entity) record.getNodeByInternalId(parentEntityId);
		CodeAttributeDefinition def = (CodeAttributeDefinition) parent.getDefinition().getChildDefinition(attrName);
		List<CodeListItem> items = codeListManager.loadValidItems(parent, def);
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
	@Secured("ROLE_ENTRY")
	public List<CodeListItemProxy> findAssignableCodeListItems(int parentEntityId, String attrName){
		CollectRecord record = getActiveRecord();
		Entity parent = (Entity) record.getNodeByInternalId(parentEntityId);
		CodeAttributeDefinition def = (CodeAttributeDefinition) parent.getDefinition().getChildDefinition(attrName);
		List<CodeListItem> items = codeListManager.loadValidItems(parent, def);
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
	@Secured("ROLE_ENTRY")
	public List<CodeListItemProxy> findAssignableCodeListItems(int parentEntityId, String attributeName, String[] codes) {
		CollectRecord record = getActiveRecord();
		Entity parent = (Entity) record.getNodeByInternalId(parentEntityId);
		CodeAttributeDefinition def = (CodeAttributeDefinition) parent.getDefinition().getChildDefinition(attributeName);
		List<CodeListItem> items = codeListManager.findValidItems(parent, def, codes);
		List<CodeListItemProxy> result = new ArrayList<CodeListItemProxy>();
		for (CodeListItem item : items) {
			result.add(new CodeListItemProxy(item));
		}
		return result;
	}
	
	@Secured("ROLE_ENTRY")
	public List<String> searchAutoCompleteValues(int attributeDefnId, int fieldIndex, String searchText) throws Exception {
		SessionState sessionState = sessionManager.getSessionState();
		CollectSurvey survey = sessionState.getActiveSurvey();
		int maxResults = 10;
		List<String> result = recordIndexService.search(SearchType.STARTS_WITH, survey, attributeDefnId, fieldIndex, searchText, maxResults);
		return result;
	}
	
	@Secured("ROLE_CLEANSING")
	public void assignOwner(int recordId, Integer ownerId) throws RecordLockedException, MultipleEditException {
		SessionState sessionState = sessionManager.getSessionState();
		recordManager.assignOwner(sessionState.getActiveSurvey(), 
				recordId, ownerId, sessionState.getUser(), sessionState.getSessionId());
	}

	protected CollectRecord getActiveRecord() {
		SessionState sessionState = getSessionState();
		CollectRecord activeRecord = sessionState.getActiveRecord();
		return activeRecord;
	}
	
	protected Locale getCurrentLocale() {
		SessionState sessionState = getSessionState();
		Locale locale = sessionState.getLocale();
		return locale;
	}

	protected SessionState getSessionState() {
		SessionState sessionState = getSessionManager().getSessionState();
		return sessionState;
	}

	protected SessionManager getSessionManager() {
		return sessionManager;
	}

	protected RecordManager getRecordManager() {
		return recordManager;
	}

}
