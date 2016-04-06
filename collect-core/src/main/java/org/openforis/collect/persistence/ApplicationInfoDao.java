package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.tables.OfcApplicationInfo.OFC_APPLICATION_INFO;

import org.jooq.DSLContext;
import org.openforis.collect.model.ApplicationInfo;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@Transactional(propagation=Propagation.SUPPORTS)
public class ApplicationInfoDao extends JooqDaoSupport {
	
	public ApplicationInfoDao() {
		super();
	}

	public ApplicationInfo load() {
		DSLContext dsl = dsl();
		if ( dsl.select()
				.from(OFC_APPLICATION_INFO)
				.fetchOne() != null ) {
			ApplicationInfo i = new ApplicationInfo();
			String version = dsl.select()
					.from(OFC_APPLICATION_INFO)
					.fetchOne().getValue(OFC_APPLICATION_INFO.VERSION);
			i.setVersion(version);
			return i;
		} else {
			return null;
		}
	}

	public void save(ApplicationInfo info) {
		DSLContext dsl = dsl();
		//delete old record
		dsl.delete(OFC_APPLICATION_INFO).execute();
		//insert new record
		String version = info.getVersion();
		dsl.insertInto(OFC_APPLICATION_INFO)
			.set(OFC_APPLICATION_INFO.VERSION, version)
			.execute();
	}
	
}
