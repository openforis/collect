/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.OfcData.DATA;
import static org.openforis.collect.persistence.jooq.tables.OfcNodeCountView.NODE_COUNT_VIEW;
import static org.openforis.collect.persistence.jooq.tables.OfcRecord.RECORD;
import static org.openforis.collect.persistence.jooq.tables.OfcUserAccount.USER_ACCOUNT;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.collect.persistence.jooq.tables.Data;
import org.openforis.collect.persistence.jooq.tables.NodeCountView;
import org.openforis.collect.persistence.jooq.tables.Record;
import org.openforis.collect.persistence.jooq.tables.UserAccount;
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
	private static final String COUNT_VIEW_ALIAS_PREFIX = "view_count_";
	
	private static final String COUNT_COLUMN_PREFIX = "count_";
	private static final String KEY_COLUMN_PREFIX = "key_";
	
	private static final String ORDER_BY_CREATED_BY_FIELD_NAME = "createdBy";
	private static final String ORDER_BY_MODIFIED_BY_FIELD_NAME = "createdBy";
	private static final String ORDER_BY_DATE_MODIFIED_FIELD_NAME = "modifiedDate";
	private static final String ORDER_BY_DATE_CREATED_FIELD_NAME = "creationDate";
	private static final String ORDER_BY_SKIPPED_FIELD_NAME = "skipped";
	private static final String ORDER_BY_MISSING_FIELD_NAME = "missing";
	private static final String ORDER_BY_ERRORS_FIELD_NAME = "errors";
	private static final String ORDER_BY_WARNINGS_FIELD_NAME = "warnings";
	
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
		// Aliases used in query
		private static final Record r = RECORD.as("r");
		private static final UserAccount u1 = USER_ACCOUNT.as("u1");
		private static final UserAccount u2 = USER_ACCOUNT.as("u2");
		
		//CREATE SELECT QUERY
		selectQuery = jooqFactory.selectQuery();
		//select
		selectQuery.addSelect(
				r.DATE_CREATED, 
				r.DATE_MODIFIED, 
				r.ERRORS, 
				r.ID, 
				r.LOCKED_BY_ID, 
				r.MISSING,  
				r.MODEL_VERSION, 
				r.MODIFIED_BY_ID, 
				r.ROOT_ENTITY_ID, 
				r.SKIPPED, 
				r.STATE, 
				r.STEP, 
				r.WARNINGS, 
				u1.USERNAME, 
				u2.USERNAME);
		//from
		selectQuery.addFrom(r);
		//add join with user table
		selectQuery.addJoin(u1, JoinType.LEFT_OUTER_JOIN, r.CREATED_BY_ID.equal(u1.ID));
		selectQuery.addJoin(u2, JoinType.LEFT_OUTER_JOIN, r.MODIFIED_BY_ID.equal(u2.ID));
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
		//always order by id to avoid pagination problems
		selectQuery.addOrderBy(RECORD.ID);

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
	 * Adds count columns to the selection to get the count of entities annotated with counInSummaryList
	 * 
	 */
	public void addCountColumn(EntityDefinition entityDefinition) {
		String viewAliasName = COUNT_VIEW_ALIAS_PREFIX + entityDefinition.getName();
		//left join with ENTITY_COUNT_VIEW
		NodeCountView viewAlias = NODE_COUNT_VIEW.as(viewAliasName);
		selectQuery.addJoin(viewAlias, JoinType.LEFT_OUTER_JOIN, 
				viewAlias.RECORD_ID.equal(RECORD.ID), 
				viewAlias.DEFINITION_ID.equal(entityDefinition.getId()));
		
		String alias = COUNT_COLUMN_PREFIX + entityDefinition.getName();
		Field<?> countField = viewAlias.NODE_COUNT.as(alias);
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
				orderByField = USER_ACCOUNT.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME;
			} else if (ORDER_BY_MODIFIED_BY_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = USER_ACCOUNT.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME;
			} else if (ORDER_BY_DATE_CREATED_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.DATE_CREATED;
			} else if (ORDER_BY_DATE_MODIFIED_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.DATE_MODIFIED;
			} else if (ORDER_BY_SKIPPED_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.SKIPPED;
			} else if (ORDER_BY_MISSING_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.MISSING;
			} else if (ORDER_BY_ERRORS_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.ERRORS;
			} else if (ORDER_BY_WARNINGS_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.WARNINGS;
			} else if (ORDER_BY_MODIFIED_BY_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = USER_ACCOUNT.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME;
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
