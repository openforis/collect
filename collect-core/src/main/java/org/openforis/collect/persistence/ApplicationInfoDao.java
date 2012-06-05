package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.tables.OfcApplicationInfo.OFC_APPLICATION_INFO;

import org.jooq.Record;
import org.jooq.impl.Factory;
import org.openforis.collect.model.ApplicationInfo;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional
public class ApplicationInfoDao extends JooqDaoSupport {
	
	public ApplicationInfoDao() {
		super();
	}

	public ApplicationInfo load() {
		Factory jf = getJooqFactory();
		Record record = jf.select()
				.from(OFC_APPLICATION_INFO)
				.fetchOne();
		if ( record != null ) {
			ApplicationInfo i = new ApplicationInfo();
			String version = record.getValue(OFC_APPLICATION_INFO.VERSION);
			i.setVersion(version);
			return i;
		} else {
			return null;
		}
	}

	public void save(ApplicationInfo info) {
		Factory jf = getJooqFactory();
		//delete old record
		jf.delete(OFC_APPLICATION_INFO).execute();
		//insert new record
		String version = info.getVersion();
		jf.insertInto(OFC_APPLICATION_INFO)
			.set(OFC_APPLICATION_INFO.VERSION, version)
			.execute();
	}
	
}
