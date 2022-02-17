/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables;


import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;
import org.openforis.collect.persistence.jooq.Collect;
import org.openforis.collect.persistence.jooq.Keys;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcUser extends TableImpl<OfcUserRecord> {

	private static final long serialVersionUID = -453376807;

	/**
	 * The reference instance of <code>collect.ofc_user</code>
	 */
	public static final OfcUser OFC_USER = new OfcUser();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<OfcUserRecord> getRecordType() {
		return OfcUserRecord.class;
	}

	/**
	 * The column <code>collect.ofc_user.id</code>.
	 */
	public final TableField<OfcUserRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_user.username</code>.
	 */
	public final TableField<OfcUserRecord, String> USERNAME = createField("username", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_user.password</code>.
	 */
	public final TableField<OfcUserRecord, String> PASSWORD = createField("password", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_user.enabled</code>.
	 */
	public final TableField<OfcUserRecord, String> ENABLED = createField("enabled", org.jooq.impl.SQLDataType.CHAR.length(1).nullable(false).defaulted(true), this, "");

	/**
	 * Create a <code>collect.ofc_user</code> table reference
	 */
	public OfcUser() {
		this("ofc_user", null);
	}

	/**
	 * Create an aliased <code>collect.ofc_user</code> table reference
	 */
	public OfcUser(String alias) {
		this(alias, OFC_USER);
	}

	private OfcUser(String alias, Table<OfcUserRecord> aliased) {
		this(alias, aliased, null);
	}

	private OfcUser(String alias, Table<OfcUserRecord> aliased, Field<?>[] parameters) {
		super(alias, Collect.COLLECT, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<OfcUserRecord> getPrimaryKey() {
		return Keys.OFC_USER_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<OfcUserRecord>> getKeys() {
		return Arrays.<UniqueKey<OfcUserRecord>>asList(Keys.OFC_USER_PKEY, Keys.OFC_USER_USERNAME_KEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcUser as(String alias) {
		return new OfcUser(alias, this);
	}

	/**
	 * Rename this table
	 */
	public OfcUser rename(String name) {
		return new OfcUser(name, null);
	}
}
