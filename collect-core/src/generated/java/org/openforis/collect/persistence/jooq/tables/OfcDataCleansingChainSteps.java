/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables;


import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import org.openforis.collect.persistence.jooq.Collect;
import org.openforis.collect.persistence.jooq.Keys;
import org.openforis.collect.persistence.jooq.tables.records.OfcDataCleansingChainStepsRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcDataCleansingChainSteps extends TableImpl<OfcDataCleansingChainStepsRecord> {

	private static final long serialVersionUID = 1286730475;

	/**
	 * The reference instance of <code>collect.ofc_data_cleansing_chain_steps</code>
	 */
	public static final OfcDataCleansingChainSteps OFC_DATA_CLEANSING_CHAIN_STEPS = new OfcDataCleansingChainSteps();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<OfcDataCleansingChainStepsRecord> getRecordType() {
		return OfcDataCleansingChainStepsRecord.class;
	}

	/**
	 * The column <code>collect.ofc_data_cleansing_chain_steps.chain_id</code>.
	 */
	public final TableField<OfcDataCleansingChainStepsRecord, Integer> CHAIN_ID = createField("chain_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_data_cleansing_chain_steps.step_id</code>.
	 */
	public final TableField<OfcDataCleansingChainStepsRecord, Integer> STEP_ID = createField("step_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>collect.ofc_data_cleansing_chain_steps.pos</code>.
	 */
	public final TableField<OfcDataCleansingChainStepsRecord, Integer> POS = createField("pos", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>collect.ofc_data_cleansing_chain_steps</code> table reference
	 */
	public OfcDataCleansingChainSteps() {
		this("ofc_data_cleansing_chain_steps", null);
	}

	/**
	 * Create an aliased <code>collect.ofc_data_cleansing_chain_steps</code> table reference
	 */
	public OfcDataCleansingChainSteps(String alias) {
		this(alias, OFC_DATA_CLEANSING_CHAIN_STEPS);
	}

	private OfcDataCleansingChainSteps(String alias, Table<OfcDataCleansingChainStepsRecord> aliased) {
		this(alias, aliased, null);
	}

	private OfcDataCleansingChainSteps(String alias, Table<OfcDataCleansingChainStepsRecord> aliased, Field<?>[] parameters) {
		super(alias, Collect.COLLECT, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<OfcDataCleansingChainStepsRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<OfcDataCleansingChainStepsRecord, ?>>asList(Keys.OFC_DATA_CLEANSING_CHAIN_STEPS__OFC_DATA_CLEANSING_CHAIN_STEPS_CHAIN_FKEY, Keys.OFC_DATA_CLEANSING_CHAIN_STEPS__OFC_DATA_CLEANSING_CHAIN_STEPS_STEP_FKEY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingChainSteps as(String alias) {
		return new OfcDataCleansingChainSteps(alias, this);
	}

	/**
	 * Rename this table
	 */
	public OfcDataCleansingChainSteps rename(String name) {
		return new OfcDataCleansingChainSteps(name, null);
	}
}
