package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.RECORD_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.Data.DATA;
import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;
import static org.openforis.collect.persistence.jooq.tables.User.USER;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordSummary;
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
		selectQuery.addSelect(RECORD.DATE_CREATED, RECORD.DATE_MODIFIED, RECORD.ID, RECORD.LOCKED_BY_ID, RECORD.MODEL_VERSION, RECORD.MODEL_VERSION, RECORD.MODIFIED_BY_ID, RECORD.ROOT_ENTITY_ID, RECORD.STATE, RECORD.STEP,
				USER.as(userTableCreatedByAlias).USERNAME, USER.as(userTableModifiedByAlias).USERNAME);
		selectQuery.addFrom(RECORD);
		selectQuery.addJoin(USER.as(userTableCreatedByAlias), JoinType.LEFT_OUTER_JOIN, RECORD.CREATED_BY_ID.equal(USER.as(userTableCreatedByAlias).ID));
		selectQuery.addJoin(USER.as(userTableModifiedByAlias), JoinType.LEFT_OUTER_JOIN, RECORD.MODIFIED_BY_ID.equal(USER.as(userTableModifiedByAlias).ID));
		List<AttributeDefinition> keyAttributeDefinitions = rootEntityDefinition.getKeyAttributeDefinitions();
		
		addKeyAttributesToSelectRecordSummariesQuery(selectQuery, keyAttributeDefinitions, orderByFieldName);
		
		//where conditions
		//TODO add filter
		selectQuery.addConditions(RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId()));
		
		addOrderByToSelectRecordSummariesQuery(selectQuery, keyAttributeDefinitions, orderByFieldName);
		
		//limit query results
		selectQuery.addLimit(offset, maxNumberOfRecords);
		
		List<Record> records = selectQuery.fetch();

		List<RecordSummary> result = parseLoadRecordSummariesResult(records, keyAttributeDefinitions);
		
		if(LOG.isDebugEnabled()) {
			String sql = selectQuery.getSQL();
			LOG.debug(sql);
		}
		return result;
	}
	
	private List<RecordSummary> parseLoadRecordSummariesResult(List<Record> result, List<AttributeDefinition> keyAttributeDefinitions) {
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
			Map<String, String> keyAttributes = new HashMap<String, String>();
			for (AttributeDefinition attributeDefinition : keyAttributeDefinitions) {
				String keyValueProjectionAlias = "key_" + attributeDefinition.getName();
				Object keyValue = r.getValue(keyValueProjectionAlias);
				keyAttributes.put(attributeDefinition.getName(), keyValue != null ? keyValue.toString(): "");
			}
			RecordSummary recordSummary = new RecordSummary(id, keyAttributes, errorCount, warningCount, createdBy, dateCreated, modifiedBy, modifiedDate, step);
			summaries.add(recordSummary);
		}
		return summaries;
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
				selectQuery.addSelect(dataField.as(dataValueProjectionAlias));
			}
		}
	}
	
	private void addOrderByToSelectRecordSummariesQuery(SelectQuery selectQuery, List<AttributeDefinition> keyAttributeDefinitions, String orderByFieldName) {
		TableField<?, ?> orderByField = null;
		for (AttributeDefinition attributeDefinition : keyAttributeDefinitions) {
			String dataTableAlias = "data_" + attributeDefinition.getName();
			String dataValueProjectionAlias = "key_" + attributeDefinition.getName();
			TableField<DataRecord, ?> dataField = null;
			if(attributeDefinition instanceof CodeAttributeDefinition || attributeDefinition instanceof TextAttributeDefinition) {
				dataField = DATA.as(dataTableAlias).TEXT1;
			} else if(attributeDefinition instanceof NumberAttributeDefinition) {
				dataField = DATA.as(dataTableAlias).NUMBER1;
			}
			if(dataField != null) {
				if(orderByField == null && orderByFieldName != null && orderByFieldName.equals(dataValueProjectionAlias)) {
					//add key field to order by conditions
					orderByField = dataField;
				}
			}
		}
		//order by
		if (orderByField != null && orderByFieldName != null) {
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
		//record.setCreatedBy(r.getValueAsString(RECORD.CREATED_BY));
		record.setModifiedDate(r.getValueAsDate(RECORD.DATE_MODIFIED));
		//record.setModifiedBy(r.getValueAsString(RECORD.MODIFIED_BY));

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
				//.set(RECORD.CREATED_BY, record.getCreatedBy())
				.set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
				//.set(RECORD.MODIFIED_BY, record.getModifiedBy())
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
				//.set(RECORD.CREATED_BY, record.getCreatedBy())
				.set(RECORD.DATE_MODIFIED, toTimestamp(record.getModifiedDate()))
				//.set(RECORD.MODIFIED_BY, record.getModifiedBy())
				.set(RECORD.MODEL_VERSION, record.getVersion().getName())
				.set(RECORD.STEP, record.getStep().getStepNumber()).where(RECORD.ID.equal(recordId)).execute();
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
