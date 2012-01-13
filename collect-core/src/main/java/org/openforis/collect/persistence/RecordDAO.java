package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Data.DATA;
import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.persistence.jooq.DataLoader;
import org.openforis.collect.persistence.jooq.DataPersister;
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

	public RecordDAO() {
	}

	@Transactional
	public CollectRecord load(Survey survey, int recordId) throws DataInconsistencyException {
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
	public int getCountRecords(int rootEntityId, String filter) {
		Factory jf = getJooqFactory();
		Record r = jf.select(Factory.count()).from(RECORD).where(RECORD.ROOT_ENTITY_ID.equal(rootEntityId)).fetchOne();
		return r.getValueAsInteger(0);
	}

	@Transactional
	public List<RecordSummary> getRecordSummaries(int rootEntityId, int offset, int maxNumberOfRecords, String orderByFieldName, String filter) {
		Factory jf = getJooqFactory();

		// default: order by ID
		TableField<?, ?> orderByField = RECORD.ID;
		if (orderByFieldName != null) {
			if ("id".equals(orderByFieldName)) {
				orderByField = RECORD.ID;
			} else if ("createdBy".equals(orderByFieldName)) {
				orderByField = RECORD.CREATED_BY;
			} else if ("modifiedByBy".equals(orderByFieldName)) {
				orderByField = RECORD.MODIFIED_BY;
			} else if ("creationDate".equals(orderByFieldName)) {
				orderByField = RECORD.DATE_CREATED;
			} else if ("modifiedDate".equals(orderByFieldName)) {
				orderByField = RECORD.DATE_MODIFIED;
			}
		}

		List<Record> records = jf.select().from(RECORD).where(RECORD.ROOT_ENTITY_ID.equal(rootEntityId)).orderBy(orderByField).limit(offset, maxNumberOfRecords).fetch();
		List<RecordSummary> result = new ArrayList<RecordSummary>();
		for (Record r : records) {
			String id = r.getValueAsString(RECORD.ID);
			String createdBy = r.getValueAsString(RECORD.CREATED_BY);
			Date dateCreated = r.getValueAsDate(RECORD.DATE_CREATED);
			String modifiedBy = r.getValueAsString(RECORD.MODIFIED_BY);
			Date modifiedDate = r.getValueAsDate(RECORD.DATE_MODIFIED);
			int step = r.getValueAsInteger(RECORD.STEP);
			int warningCount = 0;
			int errorCount = 0;
			RecordSummary recordSummary = new RecordSummary(id, errorCount, warningCount, createdBy, dateCreated, modifiedBy, modifiedDate, step);
			result.add(recordSummary);
		}
		return result;
	}

	private CollectRecord loadRecord(Survey survey, int recordId) {
		Factory jf = getJooqFactory();
		Record r = jf.select().from(RECORD).where(RECORD.ID.equal(recordId)).fetchOne();
		int rootEntityId = r.getValueAsInteger(RECORD.ROOT_ENTITY_ID);
		String version = r.getValueAsString(RECORD.MODEL_VERSION);

		Schema schema = survey.getSchema();
		NodeDefinition rootEntityDefn = schema.getById(rootEntityId);
		if (rootEntityDefn == null) {
			throw new NullPointerException("Unknown root entity id " + rootEntityId);
		}
		String rootEntityName = rootEntityDefn.getName();

		CollectRecord record = new CollectRecord(survey, rootEntityName, version);
		record.setId(recordId);
		record.setCreationDate(r.getValueAsDate(RECORD.DATE_CREATED));
		record.setCreatedBy(r.getValueAsString(RECORD.CREATED_BY));
		record.setModifiedDate(r.getValueAsDate(RECORD.DATE_MODIFIED));
		record.setModifiedBy(r.getValueAsString(RECORD.MODIFIED_BY));

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
				.set(RECORD.CREATED_BY, record.getCreatedBy()).set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate())).set(RECORD.MODIFIED_BY, record.getModifiedBy())
				.set(RECORD.MODEL_VERSION, record.getVersion().getName()).execute();
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
		jf.update(RECORD).set(RECORD.ROOT_ENTITY_ID, rootEntityId).set(RECORD.DATE_CREATED, toTimestamp(record.getCreationDate())).set(RECORD.CREATED_BY, record.getCreatedBy())
				.set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate())).set(RECORD.MODIFIED_BY, record.getModifiedBy()).set(RECORD.MODEL_VERSION, record.getVersion().getName())
				.where(RECORD.ID.equal(recordId)).execute();
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
