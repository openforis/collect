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
import org.openforis.collect.persistence.RecordDAOUtil;
import org.openforis.collect.persistence.jooq.tables.Data;
import org.openforis.collect.persistence.jooq.tables.records.DataRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author S. Ricci
 *
 */
public class RecordSummaryQueryBuilder {
	private final Log LOG = LogFactory.getLog(RecordSummaryQueryBuilder.class);

	private static final String USER_TABLE_CREATED_BY_ALIAS = "user_created_by";
	private static final String USER_TABLE_MODIFIED_BY_ALIAS = "user_modified_by";
	private static final String KEY_DATA_TABLE_ALIAS_PREFIX = "data_";
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
	
	/**
	 * key attribute definitions used to build the key columns of the select
	 */
	private List<AttributeDefinition> keyAttributeDefinitions; 
	
	/**
	 * entity definitions of the entities to count in the select
	 */
	private List<EntityDefinition> countEntityDefinitions;
	
	/**
	 * Index of record to start from
	 */
	private Integer offset;
	
	/**
	 * Maximum number of records returned.
	 */
	private Integer maxNumberOfRecords;
	
	/**
	 * Field name used in the order by condition
	 */
	private String orderByFieldName;

	
	public RecordSummaryQueryBuilder(Factory jooqFactory) {
		super();
		this.jooqFactory = jooqFactory;
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
	public SelectQuery buildSelect() {
		//CREATE SELECT QUERY
		selectQuery = jooqFactory.selectQuery();
		
		selectQuery.addSelect(RECORD.DATE_CREATED, RECORD.DATE_MODIFIED, RECORD.ID, RECORD.LOCKED_BY_ID, RECORD.MODEL_VERSION, RECORD.MODIFIED_BY_ID, RECORD.ROOT_ENTITY_ID, RECORD.STATE, RECORD.STEP,
				USER.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME, USER.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME);
		
		selectQuery.addFrom(RECORD);
		
		//add join with user table
		selectQuery.addJoin(USER.as(USER_TABLE_CREATED_BY_ALIAS), JoinType.LEFT_OUTER_JOIN, RECORD.CREATED_BY_ID.equal(USER.as(USER_TABLE_CREATED_BY_ALIAS).ID));
		selectQuery.addJoin(USER.as(USER_TABLE_MODIFIED_BY_ALIAS), JoinType.LEFT_OUTER_JOIN, RECORD.MODIFIED_BY_ID.equal(USER.as(USER_TABLE_MODIFIED_BY_ALIAS).ID));
		
		
		//add key attribute column(s)
		if(keyAttributeDefinitions != null) {
			addKeyAttributeJoinsToQuery();
		}
		
		//add count of entities columns
		addCountColumnsToQuery();

		//where conditions
		if(rootEntityDefinition != null) {
			selectQuery.addConditions(RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId()));
		}
		
		//TODO add filter on key attributes
		
		
		if(orderByFieldName != null) {
			addOrderByToQuery();
		}
		
		//limit query results
		if(offset != null && maxNumberOfRecords != null) {
			selectQuery.addLimit(offset, maxNumberOfRecords);
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
	
	public void addKeyAttributes(List<AttributeDefinition> keyAttributeDefinitions) {
		this.keyAttributeDefinitions = keyAttributeDefinitions;
	}
	
	public void addCountEntityDefinitions(List<EntityDefinition> entityDefinitions) {
		this.countEntityDefinitions = entityDefinitions;
	}
	
	public void addOrderBy(String fieldName) {
		this.orderByFieldName = fieldName;
	}
	
	public void addLimit(int offset, int maxNumberOfRecords) {
		this.offset = offset;
		this.maxNumberOfRecords = maxNumberOfRecords;
	}
	
	/**
	 * Utility method to create a single count field that can be added to the selection
	 * 
	 * @param nodeDefinition
	 * @param alias
	 * @return
	 */
	private Field<Object> createCountField(NodeDefinition nodeDefinition, String alias) {
		Field<Object> countField = jooqFactory.selectCount().from(DATA).where(DATA.RECORD_ID.equal(RECORD.ID).and(DATA.DEFINITION_ID.equal(nodeDefinition.getId()))).asField(alias);
		return countField;
	}
	
	/**
	 * Adds count columns to the selection to get the count of entities annotated with counInSummaryList
	 * 
	 */
	private void addCountColumnsToQuery() {
		TableField<?, ?> orderByField = null;
		for (NodeDefinition nodeDefinition : countEntityDefinitions) {
			String alias = COUNT_COLUMN_PREFIX + nodeDefinition.getName();
			Field<Object> countField = createCountField(nodeDefinition, alias);
			selectQuery.addSelect(countField);
			
			//add order by condition
			if(orderByField == null && orderByFieldName != null && orderByFieldName.equals(alias)) {
				selectQuery.addOrderBy(countField);
			}
		}
	}
	
	/**
	 * Adds joins with DATA table to get key attribute values
	 * 
	 */
	private void addKeyAttributeJoinsToQuery() {
		//for each key attribute add a left join and a field in the projection
		TableField<?, ?> orderByField = null;
		for (AttributeDefinition attributeDefinition : keyAttributeDefinitions) {
			String dataTableAliasName = KEY_DATA_TABLE_ALIAS_PREFIX + attributeDefinition.getName();
			//left join with DATA table to get the key attribute
			Data dataTableAlias = DATA.as(dataTableAliasName);
			selectQuery.addJoin(dataTableAlias, JoinType.LEFT_OUTER_JOIN, 
					dataTableAlias.RECORD_ID.equal(RECORD.ID), 
					dataTableAlias.DEFINITION_ID.equal(attributeDefinition.getId()));
			
			TableField<DataRecord, ?> dataField = RecordDAOUtil.getKeyValueField(dataTableAlias, attributeDefinition);
			
			if(dataField != null) {
				//add key field to the projection fields
				Field<?> fieldAlias = dataField.as(KEY_COLUMN_PREFIX + attributeDefinition.getName());
				selectQuery.addSelect(fieldAlias);
				
				if(orderByField == null && orderByFieldName != null && orderByFieldName.equals(attributeDefinition.getName())) {
					selectQuery.addOrderBy(fieldAlias);
				}
			}
		}
	}
	
	/**
	 * Adds order by conditions to the select query
	 * 
	 */
	private void addOrderByToQuery() {
		TableField<?, ?> orderByField = null;
		if (orderByFieldName != null) {
			if (ORDER_BY_CREATED_BY_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = USER.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME;
			} else if (ORDER_BY_MODIFIED_BY_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = USER.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME;
			} else if (ORDER_BY_DATE_CREATED_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.DATE_CREATED;
			} else if (ORDER_BY_DATE_MODIFIED_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.DATE_MODIFIED;
			}
		}
		if(orderByField != null) {
			selectQuery.addOrderBy(orderByField);
		}
	}
	
}
