package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Data.DATA;
import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.jooq.DataLoader;
import org.openforis.collect.persistence.jooq.DataPersister;
import org.openforis.collect.persistence.jooq.RecordSummaryQueryBuilder;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodeVisitor;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class RecordDAO extends CollectDAO {
	private final Log LOG = LogFactory.getLog(RecordDAO.class);
	
	public RecordDAO() {
	}

	@Transactional
	public CollectRecord load(Survey survey, int recordId) throws DataInconsistencyException, NonexistentIdException {
		CollectRecord record = loadRecord(survey, recordId);
		loadData(record);

		return record;
	}

	@Transactional
	public void saveOrUpdate(CollectRecord record) {
		if (record.getId() == null) {
			insertRecord(record);
		} else {
			updateRecord(record);
			deleteData(record.getId());
		}
		insertData(record);
	}

	@Transactional
	public void delete(CollectRecord record) {
		Integer id = record.getId();
		delete(id);
	}
	
	@Transactional
	public void delete(Integer id) {
		deleteData(id);
		deleteRecord(id);
	}
	
	@Transactional
	public int getCountRecords(EntityDefinition rootEntityDefinition, String filter) {
		Factory jf = getJooqFactory();
		Record r = jf.select(Factory.count()).from(RECORD).where(RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId())).fetchOne();
		return r.getValueAsInteger(0);
	}

	@Transactional
	public List<RecordSummary> loadRecordSummaries(EntityDefinition rootEntityDefinition, List<EntityDefinition> countEntityDefinitions, int offset, int maxNumberOfRecords, String orderByFieldName, String filter) {
		Factory jf = getJooqFactory();
		
		RecordSummaryQueryBuilder recordSummaryQueryBuilder = new RecordSummaryQueryBuilder(jf);

		//set root entity definition to filter the records 
		recordSummaryQueryBuilder.setRootEntityDefinition(rootEntityDefinition);
		
		//set key attribute definitions
		List<AttributeDefinition> keyAttributeDefinitions = rootEntityDefinition.getKeyAttributeDefinitions();
		recordSummaryQueryBuilder.setKeyAttributes(keyAttributeDefinitions);
		
		//set entities to count
		recordSummaryQueryBuilder.setCountEntityDefinitions(countEntityDefinitions);

		//set order by
		recordSummaryQueryBuilder.setOrderBy(orderByFieldName);
		
		//set limit
		recordSummaryQueryBuilder.setLimit(offset, maxNumberOfRecords);
		
		//build select
		SelectQuery selectQuery = recordSummaryQueryBuilder.buildSelect();
		
		//execute query
		List<Record> records = selectQuery.fetch();

		//parse the result
		List<RecordSummary> result = RecordDAOUtil.parseRecordSummariesSelectResult(records, keyAttributeDefinitions, countEntityDefinitions);
		
		return result;
	}
	
	@Transactional
	public void lock(Integer recordId, User user) throws RecordLockedException, AccessDeniedException, MultipleEditException {
		Factory jf = getJooqFactory();
		//check if user has already locked another record
		checkCanLock(user);
		Record selectResult = jf.select(RECORD.LOCKED_BY_ID, org.openforis.collect.persistence.jooq.tables.User.USER.USERNAME).from(RECORD)
				.leftOuterJoin(org.openforis.collect.persistence.jooq.tables.User.USER).on(RECORD.LOCKED_BY_ID.equal(org.openforis.collect.persistence.jooq.tables.User.USER.ID))
				.where(RECORD.ID.equal(recordId)).fetchOne();
		Integer lockedById = selectResult.getValueAsInteger(RECORD.LOCKED_BY_ID);
		if (lockedById == null || lockedById.equals(user.getId())) {
			jf.update(RECORD).set(RECORD.LOCKED_BY_ID, user.getId()).where(RECORD.ID.equal(recordId)).execute();
		} else {
			String userName = selectResult.getValueAsString(org.openforis.collect.persistence.jooq.tables.User.USER.USERNAME);
			throw new RecordLockedException("Record already locked", userName);
		}
	}

	public void checkCanLock(User user) throws MultipleEditException  {
		Factory jf = getJooqFactory();
		Integer lockedRecordId = getLockedRecordId(jf, user);
		if(lockedRecordId != null) {
			throw new MultipleEditException("User has locked another record " + lockedRecordId);
		}
	}
	
	private Integer getLockedRecordId(Factory jf, User user) {
		Record r = jf.select(RECORD.ID).from(RECORD).where(RECORD.LOCKED_BY_ID.equal(user.getId())).fetchAny();
		if(r != null){
			return r.getValueAsInteger(RECORD.ID);
		} else {
			return null;
		}
	}
	
	@Transactional
	public void unlock(Integer recordId, User user) throws RecordLockedException {
		Factory jf = getJooqFactory();
		Record selectResult = jf.select(RECORD.LOCKED_BY_ID, org.openforis.collect.persistence.jooq.tables.User.USER.USERNAME).from(RECORD)
				.leftOuterJoin(org.openforis.collect.persistence.jooq.tables.User.USER).on(RECORD.LOCKED_BY_ID.equal(org.openforis.collect.persistence.jooq.tables.User.USER.ID))
				.where(RECORD.ID.equal(recordId)).fetchOne();
		Integer lockedById = selectResult.getValueAsInteger(RECORD.LOCKED_BY_ID);
		if (lockedById != null && lockedById.equals(user.getId())) {
			jf.update(RECORD).set(RECORD.LOCKED_BY_ID, (Integer)null).where(RECORD.ID.equal(recordId)).execute();
		} else {
			String userName = selectResult.getValueAsString(org.openforis.collect.persistence.jooq.tables.User.USER.USERNAME);
			throw new RecordLockedException("Record locked by another user", userName);
		}
	}

	@Transactional
	public void unlockAll() {
		Factory jf = getJooqFactory();
		jf.update(RECORD).set(RECORD.LOCKED_BY_ID, (Integer)null).execute();
	}

	private CollectRecord loadRecord(Survey survey, int recordId) throws NonexistentIdException {
		Factory jf = getJooqFactory();
		Record r = jf.select().from(RECORD).where(RECORD.ID.equal(recordId)).fetchOne();
		int rootEntityId = r.getValueAsInteger(RECORD.ROOT_ENTITY_ID);
		String version = r.getValueAsString(RECORD.MODEL_VERSION);
		
		Integer id = r.getValueAsInteger(RECORD.ID);
		if(id==null){
			throw new NonexistentIdException();
		}
		Schema schema = survey.getSchema();
		NodeDefinition rootEntityDefn = schema.getById(rootEntityId);
		if (rootEntityDefn == null) {
			throw new NullPointerException("Unknown root entity id " + rootEntityId);
		}
		String rootEntityName = rootEntityDefn.getName();

		CollectRecord record = new CollectRecord(survey, rootEntityName, version);
		record.setId(recordId);
		record.setCreationDate(r.getValueAsDate(RECORD.DATE_CREATED));
		// record.setCreatedBy(r.getValueAsString(RECORD.CREATED_BY));
		record.setModifiedDate(r.getValueAsDate(RECORD.DATE_MODIFIED));
		// record.setModifiedBy(r.getValueAsString(RECORD.MODIFIED_BY));

		return record;
	}

	private void loadData(CollectRecord record) throws DataInconsistencyException {
		DataLoader loader = new DataLoader(getJooqFactory());
		loader.load(record);
	}

	private void insertRecord(CollectRecord record) {
		EntityDefinition rootEntityDefinition = record.getRootEntity().getDefinition();
		Integer rootEntityId = rootEntityDefinition.getId();
		if (rootEntityId == null) {
			throw new IllegalArgumentException("Null schema object definition id");
		}
		// Insert into SURVEY table
		Factory jf = getJooqFactory();
		int recordId = jf.nextval(RECORD_ID_SEQ).intValue();
		jf.insertInto(RECORD).set(RECORD.ID, recordId).set(RECORD.ROOT_ENTITY_ID, rootEntityId).set(RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()))
		// .set(RECORD.CREATED_BY, record.getCreatedBy())
				.set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
				// .set(RECORD.MODIFIED_BY, record.getModifiedBy())
				.set(RECORD.MODEL_VERSION, record.getVersion().getName()).set(RECORD.STEP, record.getStep().getStepNumber()).execute();
		record.setId(recordId);
	}

	private void updateRecord(CollectRecord record) {
		EntityDefinition rootEntityDefinition = record.getRootEntity().getDefinition();
		Integer recordId = record.getId();
		if (recordId == null) {
			throw new IllegalArgumentException("Cannot update unsaved record");
		}
		Integer rootEntityId = rootEntityDefinition.getId();
		if (rootEntityId == null) {
			throw new IllegalArgumentException("Null schema object definition id");
		}
		// Insert into SURVEY table
		Factory jf = getJooqFactory();
		jf.update(RECORD).set(RECORD.ROOT_ENTITY_ID, rootEntityId).set(RECORD.DATE_CREATED, toTimestamp(record.getCreationDate()))
		// .set(RECORD.CREATED_BY, record.getCreatedBy())
				.set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
				// .set(RECORD.MODIFIED_BY, record.getModifiedBy())
				.set(RECORD.MODEL_VERSION, record.getVersion().getName()).set(RECORD.STEP, record.getStep().getStepNumber()).where(RECORD.ID.equal(recordId)).execute();
	}
	
	private void deleteRecord(Integer recordId) {
		if (recordId == null) {
			throw new IllegalArgumentException("Cannot update unsaved record");
		}
		Factory jf = getJooqFactory();
		jf.delete(RECORD).where(RECORD.ID.equal(recordId)).execute();
	}
	
	private void deleteData(int recordId) {
		Factory jf = getJooqFactory();
		jf.delete(DATA).where(DATA.RECORD_ID.equal(recordId)).execute();
	}

	private void insertData(final CollectRecord record) {
		// N.B.: traversal order matters; dfs so that parent id's are assigned before children
		Entity root = record.getRootEntity();
		root.traverse(new NodeVisitor() {
			DataPersister persister = new DataPersister(getJooqFactory());

			@Override
			public void visit(Node<? extends NodeDefinition> node, int idx) {
				persister.persist(node, idx);
			}
		});
	}
	
}
