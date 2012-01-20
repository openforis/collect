package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;
import static org.openforis.collect.persistence.jooq.tables.User.USER;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.collect.model.RecordSummary;
import org.openforis.collect.persistence.jooq.tables.Data;
import org.openforis.collect.persistence.jooq.tables.records.DataRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
/**
 * 
 * @author S. Ricci
 *
 */
public class RecordDAOUtil {

	private static final String USER_TABLE_CREATED_BY_ALIAS = "user_created_by";
	private static final String USER_TABLE_MODIFIED_BY_ALIAS = "user_modified_by";
	private static final String COUNT_COLUMN_PREFIX = "count_";
	private static final String KEY_COLUMN_PREFIX = "key_";
	
	/**
	 * Parses the result of a select query into a list of RecordSummary objects
	 */
	public static List<RecordSummary> parseRecordSummariesSelectResult(List<Record> records, List<AttributeDefinition> keyAttributeDefinitions, List<EntityDefinition> countEntityDefinitions) {
		List<RecordSummary> summaries = new ArrayList<RecordSummary>();
		for (Record r : records) {
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
				String projectionAlias = KEY_COLUMN_PREFIX + attributeDefinition.getName();
				String key = attributeDefinition.getName();
				Object value = r.getValue(projectionAlias);
				String valueStr = value != null ? value.toString(): "";
				keyAttributes.put(key, valueStr);
			}
			//create entity counts map
			Map<String, Integer> entityCounts = new HashMap<String, Integer>();
			for (EntityDefinition entityDefinition : countEntityDefinitions) {
				String projectionAlias = COUNT_COLUMN_PREFIX + entityDefinition.getName();
				String key = entityDefinition.getName();
				Integer value = r.getValueAsInteger(projectionAlias);
				entityCounts.put(key, value);
			}
			RecordSummary recordSummary = new RecordSummary(id, keyAttributes, entityCounts, errorCount, warningCount, createdBy, dateCreated, modifiedBy, modifiedDate, step);
			summaries.add(recordSummary);
		}
		return summaries;
	}
	
	public static List<RecordSummary> parseRecordSummariesViewSelectResult(List<Record> records, List<AttributeDefinition> keyAttributeDefinitions, List<EntityDefinition> countEntityDefinitions) {
		List<RecordSummary> summaries = new ArrayList<RecordSummary>();
		return summaries;
	}
		
	
	public static TableField<DataRecord, ?> getKeyValueField(Data dataTable, AttributeDefinition attributeDefinition) {
		TableField<DataRecord, ?> dataField = null;
		
		if(attributeDefinition instanceof CodeAttributeDefinition || attributeDefinition instanceof TextAttributeDefinition) {
			dataField = dataTable.TEXT1;
		} else if(attributeDefinition instanceof NumberAttributeDefinition) {
			dataField = dataTable.NUMBER1;
		}
		
		return dataField;
	}
	
}
