/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.daos;


import java.util.List;

import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;
import org.openforis.collect.persistence.jooq.tables.OfcUser;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcUserDao extends DAOImpl<OfcUserRecord, org.openforis.collect.persistence.jooq.tables.pojos.OfcUser, Integer> {

	/**
	 * Create a new OfcUserDao without any configuration
	 */
	public OfcUserDao() {
		super(OfcUser.OFC_USER, org.openforis.collect.persistence.jooq.tables.pojos.OfcUser.class);
	}

	/**
	 * Create a new OfcUserDao with an attached configuration
	 */
	public OfcUserDao(Configuration configuration) {
		super(OfcUser.OFC_USER, org.openforis.collect.persistence.jooq.tables.pojos.OfcUser.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Integer getId(org.openforis.collect.persistence.jooq.tables.pojos.OfcUser object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public List<org.openforis.collect.persistence.jooq.tables.pojos.OfcUser> fetchById(Integer... values) {
		return fetch(OfcUser.OFC_USER.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>id = value</code>
	 */
	public org.openforis.collect.persistence.jooq.tables.pojos.OfcUser fetchOneById(Integer value) {
		return fetchOne(OfcUser.OFC_USER.ID, value);
	}

	/**
	 * Fetch records that have <code>username IN (values)</code>
	 */
	public List<org.openforis.collect.persistence.jooq.tables.pojos.OfcUser> fetchByUsername(String... values) {
		return fetch(OfcUser.OFC_USER.USERNAME, values);
	}

	/**
	 * Fetch a unique record that has <code>username = value</code>
	 */
	public org.openforis.collect.persistence.jooq.tables.pojos.OfcUser fetchOneByUsername(String value) {
		return fetchOne(OfcUser.OFC_USER.USERNAME, value);
	}

	/**
	 * Fetch records that have <code>password IN (values)</code>
	 */
	public List<org.openforis.collect.persistence.jooq.tables.pojos.OfcUser> fetchByPassword(String... values) {
		return fetch(OfcUser.OFC_USER.PASSWORD, values);
	}

	/**
	 * Fetch records that have <code>enabled IN (values)</code>
	 */
	public List<org.openforis.collect.persistence.jooq.tables.pojos.OfcUser> fetchByEnabled(String... values) {
		return fetch(OfcUser.OFC_USER.ENABLED, values);
	}
}
