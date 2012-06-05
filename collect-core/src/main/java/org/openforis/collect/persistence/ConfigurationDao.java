package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.tables.OfcConfig.OFC_CONFIG;

import java.util.Set;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class ConfigurationDao extends JooqDaoSupport {
	
	public ConfigurationDao() {
		super();
	}

	public Configuration load() {
		Configuration c = new Configuration();
		Factory jf = getJooqFactory();
		Result<Record> fetch = jf.select()
				.from(OFC_CONFIG)
				.fetch();
		for (Record record : fetch) {
			String name = record.getValue(OFC_CONFIG.NAME);
			String value = record.getValue(OFC_CONFIG.VALUE);
			c.put(name, value);
		}
		return c;
	}
	
	public void save(Configuration config) {
		Factory jf = getJooqFactory();
		//delete old records
		jf.delete(OFC_CONFIG).execute();
		//insert new records
		Set<String> keySet = config.getProperties();
		for (String name : keySet) {
			String value = config.get(name);
			jf.insertInto(OFC_CONFIG)
				.set(OFC_CONFIG.NAME, name)
				.set(OFC_CONFIG.VALUE, value)
				.execute();
		}
	}
	
}
