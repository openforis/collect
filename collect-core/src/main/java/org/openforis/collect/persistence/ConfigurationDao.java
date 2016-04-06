package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.tables.OfcConfig.OFC_CONFIG;

import java.util.Set;

import org.jooq.Result;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord;

/**
 * @author S. Ricci
 */
public class ConfigurationDao extends JooqDaoSupport {
	
	public ConfigurationDao() {
		super();
	}

	public Configuration load() {
		Configuration c = new Configuration();
		CollectDSLContext dsl = dsl();
		Result<OfcConfigRecord> result = dsl.selectFrom(OFC_CONFIG).fetch();
		for (OfcConfigRecord record : result) {
			String key = record.getValue(OFC_CONFIG.NAME);
			String value = record.getValue(OFC_CONFIG.VALUE);
			ConfigurationItem configurationItem = ConfigurationItem.fromKey(key);
			c.put(configurationItem, value);
		}
		return c;
	}
	
	public void save(Configuration config) {
		CollectDSLContext dsl = dsl();
		//delete old records
		dsl.delete(OFC_CONFIG).execute();
		//insert new records
		Set<ConfigurationItem> items = config.getProperties();
		for (ConfigurationItem item : items) {
			String value = config.get(item);
			dsl.insertInto(OFC_CONFIG)
				.set(OFC_CONFIG.NAME, item.getKey())
				.set(OFC_CONFIG.VALUE, value)
				.execute();
		}
	}
	
}
