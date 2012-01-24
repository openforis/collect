package org.openforis.collect.persistence.jooq;

import static org.openforis.collect.persistence.jooq.tables.Record.RECORD;
import static org.openforis.collect.persistence.jooq.tables.UserAccount.USER_ACCOUNT;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Record;
import org.openforis.collect.model.RecordSummary;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
/**
 * 
 * @author S. Ricci
 *
 */
public class RecordSummaryParser {

	private static final String USER_TABLE_CREATED_BY_ALIAS = "user_created_by";
	private static final String USER_TABLE_MODIFIED_BY_ALIAS = "user_modified_by";
	private static final String COUNT_COLUMN_PREFIX = "count_";
	private static final String KEY_COLUMN_PREFIX = "key_";
	
	/**
	 * Parses the result of a select query into a list of RecordSummary objects
	 */
	public static List<RecordSummary> parseSelectResult(List<Record> records, List<AttributeDefinition> keyAttributeDefinitions, List<EntityDefinition> countEntityDefinitions) {
		List<RecordSummary> summaries = new ArrayList<RecordSummary>();
		for (Record r : records) {
			Integer id = r.getValueAsInteger(RECORD.ID);
			String createdBy = r.getValueAsString(USER_ACCOUNT.as(USER_TABLE_CREATED_BY_ALIAS).USERNAME);
			Date dateCreated = r.getValueAsDate(RECORD.DATE_CREATED);
			String modifiedBy = r.getValueAsString(USER_ACCOUNT.as(USER_TABLE_MODIFIED_BY_ALIAS).USERNAME);
			Date modifiedDate = r.getValueAsDate(RECORD.DATE_MODIFIED);
			Integer step = r.getValueAsInteger(RECORD.STEP);
			Integer warnings = r.getValueAsInteger(RECORD.WARNINGS);
			Integer errors = r.getValueAsInteger(RECORD.ERRORS);
			Integer skipped = r.getValueAsInteger(RECORD.SKIPPED);
			Integer missing = r.getValueAsInteger(RECORD.MISSING);
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
			RecordSummary recordSummary = new RecordSummary(id, keyAttributes, entityCounts, createdBy, dateCreated, modifiedBy, modifiedDate, step,
					skipped, missing, errors, warnings);
			summaries.add(recordSummary);
		}
		return summaries;
	}
	
}
