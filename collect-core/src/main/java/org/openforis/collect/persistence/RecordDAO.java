package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Data.DATA;
import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;
import static org.openforis.collect.persistence.jooq.tables.User.USER;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.jooq.DataLoader;
import org.openforis.collect.persistence.jooq.DataPersister;
import org.openforis.collect.persistence.jooq.tables.records.DataRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.TextAttributeDefinition;
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
	public int getCountRecords(EntityDefinition rootEntityDefinition, String filter) {
		Factory jf = getJooqFactory();
		Record r = jf.select(Factory.count()).from(RECORD).where(RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId())).fetchOne();
		return r.getValueAsInteger(0);
	}

	@Transactional
	public List<RecordSummary> loadRecordSummaries(EntityDefinition rootEntityDefinition, int offset, int maxNumberOfRecords, String orderByFieldName, String filter) {
		Factory jf = getJooqFactory();

		//CREATE SELECT QUERY
		SelectQuery selectQuery = jf.selectQuery();
		String userTableCreatedByAlias = "user_createdBy";
		String userTableModifiedByAlias = "user_modifiedBy";
		selectQuery.addSelect(RECORD.DATE_CREATED, RECORD.DATE_MODIFIED, RECORD.ID, RECORD.LOCKED_BY_ID, RECORD.MODEL_VERSION, RECORD.MODIFIED_BY_ID, RECORD.ROOT_ENTITY_ID, RECORD.STATE, RECORD.STEP,
				USER.as(userTableCreatedByAlias).USERNAME, USER.as(userTableModifiedByAlias).USERNAME);
		selectQuery.addFrom(RECORD);
		selectQuery.addJoin(USER.as(userTableCreatedByAlias), JoinType.LEFT_OUTER_JOIN, RECORD.CREATED_BY_ID.equal(USER.as(userTableCreatedByAlias).ID));
		selectQuery.addJoin(USER.as(userTableModifiedByAlias), JoinType.LEFT_OUTER_JOIN, RECORD.MODIFIED_BY_ID.equal(USER.as(userTableModifiedByAlias).ID));
		
		//add key attribute column(s)
		List<AttributeDefinition> keyAttributeDefinitions = rootEntityDefinition.getKeyAttributeDefinitions();
		addKeyAttributesToSelectRecordSummariesQuery(selectQuery, keyAttributeDefinitions, orderByFieldName);
		
		//add count of entities columns
		List<EntityDefinition> countInSummaryListEntityDefs = getCountInSummaryListEntityDefinitions(rootEntityDefinition);
		addCountEntityColumnsToSelectRecordSummariesQuery(jf, selectQuery, countInSummaryListEntityDefs, orderByFieldName);

		//where conditions
		//TODO add filter
		selectQuery.addConditions(RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId()));
		
		addOrderByToSelectRecordSummariesQuery(selectQuery, keyAttributeDefinitions, orderByFieldName);
		
		//limit query results
		selectQuery.addLimit(offset, maxNumberOfRecords);
		
		List<Record> records = selectQuery.fetch();

		List<RecordSummary> result = parseLoadRecordSummariesResult(records, keyAttributeDefinitions, countInSummaryListEntityDefs);
		
		if(LOG.isDebugEnabled()) {
			String sql = selectQuery.getSQL();
			LOG.debug(sql);
		}
		return result;
	}
	
	private List<RecordSummary> parseLoadRecordSummariesResult(List<Record> result, List<AttributeDefinition> keyAttributeDefinitions, List<EntityDefinition> countInSummaryListEntityDefs) {
		List<RecordSummary> summaries = new ArrayList<RecordSummary>();
		for (Record r : result) {
			Integer id = r.getValueAsInteger(RECORD.ID);
			String createdBy = r.getValueAsString(USER.as("user_createdBy").USERNAME);
			Date dateCreated = r.getValueAsDate(RECORD.DATE_CREATED);
			String modifiedBy = r.getValueAsString(USER.as("user_modifiedBy").USERNAME);
			Date modifiedDate = r.getValueAsDate(RECORD.DATE_MODIFIED);
			int step = r.getValueAsInteger(RECORD.STEP);
			//TODO add errors and warnings count
			int warningCount = 0;
			int errorCount = 0;
			//create key attributes map
			Map<String, String> keyAttributes = new HashMap<String, String>();
			for (AttributeDefinition attributeDefinition : keyAttributeDefinitions) {
				String keyValueProjectionAlias = "key_" + attributeDefinition.getName();
				String key = keyValueProjectionAlias;
				Object value = r.getValue(keyValueProjectionAlias);
				String valueStr = value != null ? value.toString(): "";
				keyAttributes.put(key, valueStr);
			}
			//create entity counts map
			Map<String, Integer> entityCounts = new HashMap<String, Integer>();
			for (EntityDefinition entityDefinition : countInSummaryListEntityDefs) {
				String keyValueProjectionAlias = "count_" + entityDefinition.getName();
				String key = keyValueProjectionAlias;
				Integer value = r.getValueAsInteger(keyValueProjectionAlias);
				entityCounts.put(key, value);
			}
			RecordSummary recordSummary = new RecordSummary(id, keyAttributes, entityCounts, errorCount, warningCount, createdBy, dateCreated, modifiedBy, modifiedDate, step);
			summaries.add(recordSummary);
		}
		return summaries;
	}
	
	private Field<Object> createCountField(Factory jf, NodeDefinition nodeDefinition, String alias) {
		Field<Object> countField = jf.selectCount().from(DATA).where(DATA.RECORD_ID.equal(RECORD.ID).and(DATA.DEFINITION_ID.equal(nodeDefinition.getId()))).asField(alias);
		return countField;
	}
	
	private void addCountEntityColumnsToSelectRecordSummariesQuery(Factory jf, SelectQuery selectQuery, List<EntityDefinition> countInListNodeDefinitions, String orderByFieldName) {
		TableField<?, ?> orderByField = null;
		for (NodeDefinition nodeDefinition : countInListNodeDefinitions) {
			String alias = "count_" + nodeDefinition.getName();
			Field<Object> countField = createCountField(jf, nodeDefinition, alias);
			selectQuery.addSelect(countField);
			if(orderByField == null && orderByFieldName != null && orderByFieldName.equals(alias)) {
				selectQuery.addOrderBy(countField);
			}
		}
	}
	
	private void addKeyAttributesToSelectRecordSummariesQuery(SelectQuery selectQuery, List<AttributeDefinition> keyAttributeDefinitions, String orderByFieldName) {
		//for each key attribute add a left join, a field in the projection and in the order by (if matches orderByFieldName)
		TableField<?, ?> orderByField = null;
		for (AttributeDefinition attributeDefinition : keyAttributeDefinitions) {
			String dataTableAlias = "data_" + attributeDefinition.getName();
			//left join with DATA table to get the key attribute
			selectQuery.addJoin(DATA.as(dataTableAlias), JoinType.LEFT_OUTER_JOIN, 
					DATA.as(dataTableAlias).RECORD_ID.equal(RECORD.ID), DATA.as(dataTableAlias).DEFINITION_ID.equal(attributeDefinition.getId()));
			String dataValueProjectionAlias = "key_" + attributeDefinition.getName();
			TableField<DataRecord, ?> dataField = null;
			if(attributeDefinition instanceof CodeAttributeDefinition || attributeDefinition instanceof TextAttributeDefinition) {
				dataField = DATA.as(dataTableAlias).TEXT1;
			} else if(attributeDefinition instanceof NumberAttributeDefinition) {
				dataField = DATA.as(dataTableAlias).NUMBER1;
			}
			if(dataField != null) {
				//add key field to the projection fields
				Field<?> fieldAlias = dataField.as(dataValueProjectionAlias);
				selectQuery.addSelect(fieldAlias);
				if(orderByField == null && orderByFieldName != null && orderByFieldName.equals(dataValueProjectionAlias)) {
					selectQuery.addOrderBy(fieldAlias);
				}
			}
		}
	}
	
	private void addOrderByToSelectRecordSummariesQuery(SelectQuery selectQuery, List<AttributeDefinition> keyAttributeDefinitions, String orderByFieldName) {
		TableField<?, ?> orderByField = null;
		//order by
		if (orderByFieldName != null) {
			if ("id".equals(orderByFieldName)) {
				orderByField = RECORD.ID;
			} else if ("createdBy".equals(orderByFieldName)) {
				orderByField = USER.as("user_createdBy").USERNAME;
			} else if ("modifiedBy".equals(orderByFieldName)) {
				orderByField = USER.as("user_modifiedBy").USERNAME;
			} else if ("creationDate".equals(orderByFieldName)) {
				orderByField = RECORD.DATE_CREATED;
			} else if ("modifiedDate".equals(orderByFieldName)) {
				orderByField = RECORD.DATE_MODIFIED;
			}
		}
		//default: order by ID
		if(orderByField == null) {
			orderByField = RECORD.ID;
		}
		selectQuery.addOrderBy(orderByField);
	}

	@Transactional
	public void lock(Integer recordId, User user) throws RecordLockedException {
		Factory jf = getJooqFactory();
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
	
	private List<EntityDefinition> getCountInSummaryListEntityDefinitions(EntityDefinition rootEntityDefinition) {
		List<EntityDefinition> result = new ArrayList<EntityDefinition>();
		List<NodeDefinition> childDefinitions = rootEntityDefinition.getChildDefinitions();
		QName countInSummaryListAnnotation = new QName("http://www.openforis.org/collect/3.0/ui", "countInSummaryList");
		for (NodeDefinition childDefinition : childDefinitions) {
			if(childDefinition instanceof EntityDefinition) {
				EntityDefinition entityDefinition = (EntityDefinition) childDefinition;
				String annotation = childDefinition.getAnnotation(countInSummaryListAnnotation);
				if(annotation != null && Boolean.parseBoolean(annotation)) {
					result.add(entityDefinition);
				}
			}
		}
		return result;
	}
	
}
