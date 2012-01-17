/**
 * 
 */
package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Data.DATA;
import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;
import static org.openforis.collect.persistence.jooq.tables.User.USER;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.persistence.jooq.tables.records.DataRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class RecordSummaryQueryBuilder {
	private final Log LOG = LogFactory.getLog(RecordSummaryQueryBuilder.class);
	
	private Factory jooqFactory;
	
	private static final String USER_TABLE_CREATED_BY_ALIAS = "user_created_by";
	private static final String USER_TABLE_MODIFIED_BY_ALIAS = "user_modified_by";
	private static final String COUNT_COLUMN_ALIAS_PREFIX = "count_";
	private static final String KEY_COLUMN_ALIAS_PREFIX = "count_";
	
	private static final String ORDER_BY_CREATED_BY_FIELD_NAME = "createdBy";
	private static final String ORDER_BY_MODIFIED_BY_FIELD_NAME = "createdBy";
	private static final String ORDER_BY_DATE_MODIFIED_FIELD_NAME = "modifiedDate";
	private static final String ORDER_BY_DATE_CREATED_FIELD_NAME = "creationDate";
	
	
	public RecordSummaryQueryBuilder(Factory jooqFactory) {
		super();
		this.jooqFactory = jooqFactory;
	}
	
	/**
	 * Build the select query to load record summaries.
	 * 
	 * @param rootEntityDefinition
	 * @param keyAttributeDefinitions
	 * @param countInSummaryListEntityDefs
	 * @param offset
	 * @param maxNumberOfRecords
	 * @param orderByFieldName
	 * @param filter
	 * @return 
	 * 
	 * the projection will contain:
	 * - key attribute columns
	 * - count of entities annotated with countInRecordSummary
	 * - record table fields
	 * - user created by and modified by info
	 *  
	 */
	public SelectQuery buildSelect(EntityDefinition rootEntityDefinition, List<AttributeDefinition> keyAttributeDefinitions, List<EntityDefinition> countInSummaryListEntityDefs, int offset, int maxNumberOfRecords, String orderByFieldName, String filter) {
		//CREATE SELECT QUERY
		SelectQuery selectQuery = jooqFactory.selectQuery();
		
		selectQuery.addSelect(RECORD.DATE_CREATED, RECORD.DATE_MODIFIED, RECORD.ID, RECORD.LOCKED_BY_ID, RECORD.MODEL_VERSION, RECORD.MODIFIED_BY_ID, RECORD.ROOT_ENTITY_ID, RECORD.STATE, RECORD.STEP,
				USER.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME, USER.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME);
		
		selectQuery.addFrom(RECORD);
		
		selectQuery.addJoin(USER.as(USER_TABLE_CREATED_BY_ALIAS), JoinType.LEFT_OUTER_JOIN, RECORD.CREATED_BY_ID.equal(USER.as(USER_TABLE_CREATED_BY_ALIAS).ID));
		selectQuery.addJoin(USER.as(USER_TABLE_MODIFIED_BY_ALIAS), JoinType.LEFT_OUTER_JOIN, RECORD.MODIFIED_BY_ID.equal(USER.as(USER_TABLE_MODIFIED_BY_ALIAS).ID));
		
		//add key attribute column(s)
		addKeyAttributeJoins(selectQuery, keyAttributeDefinitions, orderByFieldName);
		
		//add count of entities columns
		addCountColumns(selectQuery, countInSummaryListEntityDefs, orderByFieldName);

		//where conditions
		//TODO add filter
		selectQuery.addConditions(RECORD.ROOT_ENTITY_ID.equal(rootEntityDefinition.getId()));
		
		addOrderBy(selectQuery, keyAttributeDefinitions, orderByFieldName);
		
		//limit query results
		selectQuery.addLimit(offset, maxNumberOfRecords);
		
		return selectQuery;

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
	 * @param selectQuery
	 * @param countInListNodeDefinitions
	 * @param orderByFieldName
	 */
	private void addCountColumns(SelectQuery selectQuery, List<EntityDefinition> countInListNodeDefinitions, String orderByFieldName) {
		TableField<?, ?> orderByField = null;
		for (NodeDefinition nodeDefinition : countInListNodeDefinitions) {
			String alias = COUNT_COLUMN_ALIAS_PREFIX + nodeDefinition.getName();
			Field<Object> countField = createCountField(nodeDefinition, alias);
			selectQuery.addSelect(countField);
			if(orderByField == null && orderByFieldName != null && orderByFieldName.equals(alias)) {
				selectQuery.addOrderBy(countField);
			}
		}
	}
	
	/**
	 * Adds joins with DATA table to get key attribute values
	 * 
	 * @param selectQuery
	 * @param keyAttributeDefinitions
	 * @param orderByFieldName
	 */
	private void addKeyAttributeJoins(SelectQuery selectQuery, List<AttributeDefinition> keyAttributeDefinitions, String orderByFieldName) {
		//for each key attribute add a left join, a field in the projection and in the order by (if matches orderByFieldName)
		TableField<?, ?> orderByField = null;
		for (AttributeDefinition attributeDefinition : keyAttributeDefinitions) {
			String dataTableAlias = "data_" + attributeDefinition.getName();
			//left join with DATA table to get the key attribute
			selectQuery.addJoin(DATA.as(dataTableAlias), JoinType.LEFT_OUTER_JOIN, 
					DATA.as(dataTableAlias).RECORD_ID.equal(RECORD.ID), DATA.as(dataTableAlias).DEFINITION_ID.equal(attributeDefinition.getId()));
			
			TableField<DataRecord, ?> dataField = null;
			
			if(attributeDefinition instanceof CodeAttributeDefinition || attributeDefinition instanceof TextAttributeDefinition) {
				dataField = DATA.as(dataTableAlias).TEXT1;
			} else if(attributeDefinition instanceof NumberAttributeDefinition) {
				dataField = DATA.as(dataTableAlias).NUMBER1;
			}
			
			if(dataField != null) {
				String keyColumnAlias = KEY_COLUMN_ALIAS_PREFIX + attributeDefinition.getName();
				//add key field to the projection fields
				Field<?> fieldAlias = dataField.as(keyColumnAlias);
				selectQuery.addSelect(fieldAlias);

				//add field to order by conditions if matches orderByFieldName
				if(orderByField == null && orderByFieldName != null && orderByFieldName.equals(keyColumnAlias)) {
					selectQuery.addOrderBy(fieldAlias);
				}
			}
		}
	}
	
	/**
	 * Adds order by conditions to the select query
	 * 
	 * @param selectQuery
	 * @param keyAttributeDefinitions
	 * @param orderByFieldName
	 */
	private void addOrderBy(SelectQuery selectQuery, List<AttributeDefinition> keyAttributeDefinitions, String orderByFieldName) {
		TableField<?, ?> orderByField = null;
		//order by
		if (orderByFieldName != null) {
			if ("id".equals(orderByFieldName)) {
				orderByField = RECORD.ID;
			} else if (ORDER_BY_CREATED_BY_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = USER.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME;
			} else if (ORDER_BY_MODIFIED_BY_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = USER.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME;
			} else if (ORDER_BY_DATE_CREATED_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.DATE_CREATED;
			} else if (ORDER_BY_DATE_MODIFIED_FIELD_NAME.equals(orderByFieldName)) {
				orderByField = RECORD.DATE_MODIFIED;
			}
		}
		//default: order by ID
		if(orderByField == null) {
			orderByField = RECORD.ID;
		}
		selectQuery.addOrderBy(orderByField);
	}

	
	/**
	 * 
	 * @param result
	 * @param keyAttributeDefinitions
	 * @param countInSummaryListEntityDefs
	 * @return parses the result records into a list of RecordSummary objects
	 */
	public static List<RecordSummary> parseResult(List<Record> result, List<AttributeDefinition> keyAttributeDefinitions, List<EntityDefinition> countInSummaryListEntityDefs) {
		List<RecordSummary> summaries = new ArrayList<RecordSummary>();
		for (Record r : result) {
			Integer id = r.getValueAsInteger(RECORD.ID);
			String createdBy = r.getValueAsString(USER.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME);
			Date dateCreated = r.getValueAsDate(RECORD.DATE_CREATED);
			String modifiedBy = r.getValueAsString(USER.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME);
			Date modifiedDate = r.getValueAsDate(RECORD.DATE_MODIFIED);
			int step = r.getValueAsInteger(RECORD.STEP);
			//TODO add errors and warnings count
			int warningCount = 0;
			int errorCount = 0;
			//create key attributes map
			Map<String, String> keyAttributes = new HashMap<String, String>();
			for (AttributeDefinition attributeDefinition : keyAttributeDefinitions) {
				String keyValueProjectionAlias = KEY_COLUMN_ALIAS_PREFIX + attributeDefinition.getName();
				String key = keyValueProjectionAlias;
				Object value = r.getValue(keyValueProjectionAlias);
				String valueStr = value != null ? value.toString(): "";
				keyAttributes.put(key, valueStr);
			}
			//create entity counts map
			Map<String, Integer> entityCounts = new HashMap<String, Integer>();
			for (EntityDefinition entityDefinition : countInSummaryListEntityDefs) {
				String keyValueProjectionAlias = COUNT_COLUMN_ALIAS_PREFIX + entityDefinition.getName();
				String key = keyValueProjectionAlias;
				Integer value = r.getValueAsInteger(keyValueProjectionAlias);
				entityCounts.put(key, value);
			}
			RecordSummary recordSummary = new RecordSummary(id, keyAttributes, entityCounts, errorCount, warningCount, createdBy, dateCreated, modifiedBy, modifiedDate, step);
			summaries.add(recordSummary);
		}
		return summaries;
	}
	
}
