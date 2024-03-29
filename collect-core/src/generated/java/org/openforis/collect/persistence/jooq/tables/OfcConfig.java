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
import org.openforis.collect.persistence.jooq.tables.records.OfcConfigRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcConfig extends TableImpl<OfcConfigRecord> {

	private static final long serialVersionUID = -742589348;

	/**
	 * The reference instance of <code>collect.ofc_config</code>
	 */
	public static final OfcConfig OFC_CONFIG = new OfcConfig();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<OfcConfigRecord> getRecordType() {
		return OfcConfigRecord.class;
	}

	/**
	 * The column <code>collect.ofc_config.name</code>.
	 */
	public final TableField<OfcConfigRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(25).nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_config.value</code>.
	 */
	public final TableField<OfcConfigRecord, String> VALUE = createField("value", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * Create a <code>collect.ofc_config</code> table reference
	 */
	public OfcConfig() {
		this("ofc_config", null);
	}

	/**
	 * Create an aliased <code>collect.ofc_config</code> table reference
	 */
	public OfcConfig(String alias) {
		this(alias, OFC_CONFIG);
	}

	private OfcConfig(String alias, Table<OfcConfigRecord> aliased) {
		this(alias, aliased, null);
	}

	private OfcConfig(String alias, Table<OfcConfigRecord> aliased, Field<?>[] parameters) {
		super(alias, Collect.COLLECT, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<OfcConfigRecord> getPrimaryKey() {
		return Keys.OFC_CONFIG_PKEY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<OfcConfigRecord>> getKeys() {
		return Arrays.<UniqueKey<OfcConfigRecord>>asList(Keys.OFC_CONFIG_PKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcConfig as(String alias) {
		return new OfcConfig(alias, this);
	}

	/**
	 * Rename this table
	 */
	public OfcConfig rename(String name) {
		return new OfcConfig(name, null);
	}
}
