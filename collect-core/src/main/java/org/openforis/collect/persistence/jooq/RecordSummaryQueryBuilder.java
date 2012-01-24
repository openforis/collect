/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;
import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;
import static org.openforis.collect.persistence.jooq.tables.User.USER;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.collect.persistence.jooq.tables.Data;
import org.openforis.collect.persistence.jooq.tables.records.DataRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class RecordSummaryQueryBuilder {
	private final Log LOG = LogFactory.getLog(RecordSummaryQueryBuilder.class);

	private static final String USER_TABLE_CREATED_BY_ALIAS = "user_created_by";
	private static final String USER_TABLE_MODIFIED_BY_ALIAS = "user_modified_by";
	private static final String KEY_DATA_TABLE_ALIAS_PREFIX = "data_key_";
	private static final String COUNT_DATA_TABLE_ALIAS_PREFIX = "data_count_";
	
	private static final String COUNT_COLUMN_PREFIX = "count_";
	private static final String KEY_COLUMN_PREFIX = "key_";
	
	private static final String ORDER_BY_CREATED_BY_FIELD_NAME = "createdBy";
	private static final String ORDER_BY_MODIFIED_BY_FIELD_NAME = "createdBy";
	private static final String ORDER_BY_DATE_MODIFIED_FIELD_NAME = "modifiedDate";
	private static final String ORDER_BY_DATE_CREATED_FIELD_NAME = "creationDate";
	
	private Factory jooqFactory;
	
	/**
	 * 
	 */
	private SelectQuery selectQuery;
	
	/**
	 * the root entity used to filter the records
	 */
	private EntityDefinition rootEntityDefinition;
	
	public RecordSummaryQueryBuilder(Factory jooqFactory) {
		super();
		this.jooqFactory = jooqFactory;
		
		init();
	}
	
	private void init() {
		//CREATE SELECT QUERY
		selectQuery = jooqFactory.selectQuery();
		
		selectQuery.addSelect(RECORD.DATE_CREATED, RECORD.DATE_MODIFIED, RECORD.ID, RECORD.LOCKED_BY_ID, RECORD.MODEL_VERSION, RECORD.MODIFIED_BY_ID, RECORD.ROOT_ENTITY_ID, RECORD.STATE, RECORD.STEP,
				USER.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME, USER.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME);
		
		selectQuery.addFrom(RECORD);
		
		//add join with user table
		selectQuery.addJoin(USER.as(USER_TABLE_CREATED_BY_ALIAS), JoinType.LEFT_OUTER_JOIN, RECORD.CREATED_BY_ID.equal(USER.as(USER_TABLE_CREATED_BY_ALIAS).ID));
		selectQuery.addJoin(USER.as(USER_TABLE_MODIFIED_BY_ALIAS), JoinType.LEFT_OUTER_JOIN, RECORD.MODIFIED_BY_ID.equal(USER.as(USER_TABLE_MODIFIED_BY_ALIAS).ID));
		
		//required when using count of entities
		selectQuery.addGroupBy(RECORD.DATE_CREATED, RECORD.DATE_MODIFIED, RECORD.ID, RECORD.LOCKED_BY_ID, RECORD.MODEL_VERSION, RECORD.MODIFIED_BY_ID, RECORD.ROOT_ENTITY_ID, RECORD.STATE, RECORD.STEP, USER.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME, USER.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME);
		
		//always order by id to avoid pagination problems
		selectQuery.addOrderBy(RECORD.ID);
	}
	
	/**
	 * Build the select query to load record summaries.
	 * 
	 * the projection will contain:
	 * - key attribute columns
	 * - count of entities annotated with countInRecordSummary
	 * - record table fields
	 * - user created by and modified by info
	 *  
	 */
	public SelectQuery toQuery() {
		//where conditions
		if(rootEntityDefinition != null) {
			selectQuery.addConditions(RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId()));
		}
		
		if(LOG.isDebugEnabled()) {
			String sql = selectQuery.toString();
			LOG.debug(sql);
		}
		
		return selectQuery;

	}
	
	public void setRootEntityDefinition(EntityDefinition rootEntityDefinition) {
		this.rootEntityDefinition = rootEntityDefinition;
	}
	
	public void addLimit(int offset, int maxNumberOfRecords) {
		selectQuery.addLimit(offset, maxNumberOfRecords);
	}
	
	/**
	 * Utility method to create a single count field that can be added to the selection
	 * 
	 * @param nodeDefinition
	 * @param alias
	 * @return
	 */
	private Field<Integer> createCountField(Field<?> field, String alias) {
		//Field<Object> countField = jooqFactory.selectCount().from(DATA).where(DATA.RECORD_ID.equal(RECORD.ID).and(DATA.DEFINITION_ID.equal(nodeDefinition.getId()))).asField(alias);
		Field<Integer> countField = Factory.count(field).as(alias);
		return countField;
	}
	
	/**
	 * Adds count columns to the selection to get the count of entities annotated with counInSummaryList
	 * 
	 */
	public void addCountColumn(EntityDefinition entityDefinition) {
		String dataTableAliasName = COUNT_DATA_TABLE_ALIAS_PREFIX + entityDefinition.getName();
		//left join with DATA table
		Data dataTableAlias = DATA.as(dataTableAliasName);
		selectQuery.addJoin(dataTableAlias, JoinType.LEFT_OUTER_JOIN, 
				dataTableAlias.RECORD_ID.equal(RECORD.ID), 
				dataTableAlias.DEFINITION_ID.equal(entityDefinition.getId()));
		
		String alias = COUNT_COLUMN_PREFIX + entityDefinition.getName();
		Field<Integer> countField = createCountField(dataTableAlias.ID, alias);
		selectQuery.addSelect(countField);
	}
	
	/**
	 * Adds joins with DATA table to get key attribute values
	 * 
	 */
	public void addKeyAttribute(AttributeDefinition keyAttributeDefinition) {
		//for each key attribute add a left join and a field in the projection
		String dataTableAliasName = KEY_DATA_TABLE_ALIAS_PREFIX + keyAttributeDefinition.getName();
		//left join with DATA table to get the key attribute
		Data dataTableAlias = DATA.as(dataTableAliasName);
		selectQuery.addJoin(dataTableAlias, JoinType.LEFT_OUTER_JOIN, 
				dataTableAlias.RECORD_ID.equal(RECORD.ID), 
				dataTableAlias.DEFINITION_ID.equal(keyAttributeDefinition.getId()));
		
		TableField<DataRecord, ?> dataField = getKeyValueField(dataTableAlias, keyAttributeDefinition);
		
		if(dataField != null) {
			//add key field to the projection fields
			Field<?> fieldAlias = dataField.as(KEY_COLUMN_PREFIX + keyAttributeDefinition.getName());
			selectQuery.addSelect(fieldAlias);
			//necessary due to the count of entities in the select
			selectQuery.addGroupBy(fieldAlias);
		}
	}
	
	/**
	 * Adds order by condition to the select query
	 * 
	 */
	public void addOrderBy(String orderByFieldName) {
		Field<?> orderByField = null;
		if (orderByFieldName != null) {
			if (ORDER_BY_CREATED_BY_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = USER.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME;
			} else if (ORDER_BY_MODIFIED_BY_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = USER.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME;
			} else if (ORDER_BY_DATE_CREATED_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.DATE_CREATED;
			} else if (ORDER_BY_DATE_MODIFIED_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.DATE_MODIFIED;
			} else {
				List<Field<?>> selectFields = selectQuery.getSelect();
				for (Field<?> field : selectFields) {
					if(orderByFieldName.equals(field.getName())) {
						orderByField = field;
						break;
					}
				}
			}
		}
		if(orderByField != null) {
			selectQuery.addOrderBy(orderByField);
		}
	}
	
	private TableField<DataRecord, ?> getKeyValueField(Data dataTable, AttributeDefinition attributeDefinition) {
		TableField<DataRecord, ?> dataField = null;
		
		if(attributeDefinition instanceof CodeAttributeDefinition || attributeDefinition instanceof TextAttributeDefinition) {
			dataField = dataTable.TEXT1;
		} else if(attributeDefinition instanceof NumberAttributeDefinition) {
			dataField = dataTable.NUMBER1;
		}
		
		return dataField;
	}
}
