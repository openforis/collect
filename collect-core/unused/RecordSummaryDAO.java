package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.OfcRecord.RECORD;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.collect.model.RecordSummary;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
public class RecordSummaryDao extends CollectDao {
	
	public static final String DATE_CREATED_ALIAS = "creationDate";
	public static final String DATE_MODIFIED_ALIAS = "modifiedDate";
	
	public static final String KEY_ALIAS_PREFIX = "key_";
	public static final String COUNT_ALIAS_PREFIX = "count_";
	
	public RecordSummaryDao() {
	}

	
	@Transactional
	public List<RecordSummary> load(EntityDefinition rootEntityDefinition, List<EntityDefinition> countable, int offset, int maxRecords, String orderByField, String filter) {
		List<AttributeDefinition> keyDefs = rootEntityDefinition.getKeyAttributeDefinitions();
		
		Factory jf = getJooqFactory();
		org.openforis.collect.persistence.jooq.tables.Record r = RECORD.as("r");
		
		SelectQuery q = jf.selectQuery();
		
		q.addFrom(r);
		
		q.addSelect(
				r.DATE_CREATED.as(DATE_CREATED_ALIAS), 
				r.DATE_MODIFIED.as(DATE_MODIFIED_ALIAS), 
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
				r.WARNINGS
				);
		{
			//add keys to select with an alias like KEY_ALIAS_PREFIX + ATTRIBUTE_NAME
			int position = 1;
			for (AttributeDefinition def : keyDefs) {
				String alias = KEY_ALIAS_PREFIX + def.getName();
				Field<?> field = RecordDaoUtil.getKeyField(r, def, position).as(alias);
				q.addSelect(field);
				position ++;
			}
		}
		{
			//add count columns to select with an alias like COUNT_ALIAS_PREFIX + ENTITY_NAME
			int position = 1;
			for (EntityDefinition def : countable) {
				String alias = COUNT_ALIAS_PREFIX + def.getName();
				Field<?> field = RecordDaoUtil.getCountField(r, position).as(alias);
				q.addSelect(field);
				position ++;
			}
		}
		//add order by condition
		Field<?> orderBy = null;
		if(orderByField != null) {
			List<Field<?>> selectFields = q.getSelect();
			for (Field<?> field : selectFields) {
				if(orderByField.equals(field.getName())) {
					orderBy = field;
					break;
				}
			}
		}
		if(orderBy != null) {
			q.addOrderBy(orderBy);
		}
		//always order by ID to avoid pagination issues
		q.addOrderBy(r.ID);
		
		//add limit
		q.addLimit(offset, maxRecords);
		
		//fetch results
		Result<Record> records = q.fetch();
		
		List<RecordSummary> result = mapRecordsToSummaries(records, keyDefs, countable);
		return result;
	}

	private List<RecordSummary> mapRecordsToSummaries(List<Record> records, List<AttributeDefinition> keyDefs, List<EntityDefinition> countable) {
		List<RecordSummary> result = new ArrayList<RecordSummary>();
		
		for (Record record : records) {
			Integer id = record.getValueAsInteger(RECORD.ID);
			//String createdBy = r.getValueAsString(USER_MODIFIED_BY_ALIAS);
			String createdBy = null;
			Date dateCreated = record.getValueAsDate(DATE_CREATED_ALIAS);
			//String modifiedBy = r.getValueAsString(USER_CREATED_BY_ALIAS);
			String modifiedBy = null;
			Date modifiedDate = record.getValueAsDate(DATE_MODIFIED_ALIAS);
			Integer step = record.getValueAsInteger(RECORD.STEP);
			Integer warnings = record.getValueAsInteger(RECORD.WARNINGS);
			Integer errors = record.getValueAsInteger(RECORD.ERRORS);
			Integer skipped = record.getValueAsInteger(RECORD.SKIPPED);
			Integer missing = record.getValueAsInteger(RECORD.MISSING);
			//create count map
			Map<String, Integer> entityCounts = new HashMap<String, Integer>();
			for (EntityDefinition def : countable) {
				String alias = COUNT_ALIAS_PREFIX + def.getName();
				String key = def.getName();
				Integer value = record.getValueAsInteger(alias);
				entityCounts.put(key, value);
			}
			//create key attributes map
			Map<String, String> keyAttributes = new HashMap<String, String>();
			for (AttributeDefinition attributeDefinition : keyDefs) {
				String projectionAlias = KEY_ALIAS_PREFIX + attributeDefinition.getName();
				String key = attributeDefinition.getName();
				Object value = record.getValue(projectionAlias);
				String valueStr = value != null ? value.toString(): "";
				keyAttributes.put(key, valueStr);
			}
			RecordSummary recordSummary = new RecordSummary(id, keyAttributes, entityCounts, createdBy, dateCreated, modifiedBy, modifiedDate, step,
					skipped, missing, errors, warnings);
			result.add(recordSummary);
		}
		return result;
	}
	
}
