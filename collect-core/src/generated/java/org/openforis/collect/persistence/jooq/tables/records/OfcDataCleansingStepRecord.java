/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.records;


import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;
import org.openforis.collect.persistence.jooq.tables.OfcDataCleansingStep;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcDataCleansingStepRecord extends UpdatableRecordImpl<OfcDataCleansingStepRecord> implements Record8<Integer, String, Integer, String, String, Timestamp, Timestamp, String> {

	private static final long serialVersionUID = 1565316226;

	/**
	 * Setter for <code>collect.ofc_data_cleansing_step.id</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_step.id</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_step.uuid</code>.
	 */
	public void setUuid(String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_step.uuid</code>.
	 */
	public String getUuid() {
		return (String) getValue(1);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_step.query_id</code>.
	 */
	public void setQueryId(Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_step.query_id</code>.
	 */
	public Integer getQueryId() {
		return (Integer) getValue(2);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_step.title</code>.
	 */
	public void setTitle(String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_step.title</code>.
	 */
	public String getTitle() {
		return (String) getValue(3);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_step.description</code>.
	 */
	public void setDescription(String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_step.description</code>.
	 */
	public String getDescription() {
		return (String) getValue(4);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_step.creation_date</code>.
	 */
	public void setCreationDate(Timestamp value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_step.creation_date</code>.
	 */
	public Timestamp getCreationDate() {
		return (Timestamp) getValue(5);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_step.modified_date</code>.
	 */
	public void setModifiedDate(Timestamp value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_step.modified_date</code>.
	 */
	public Timestamp getModifiedDate() {
		return (Timestamp) getValue(6);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_step.type</code>.
	 */
	public void setType(String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_step.type</code>.
	 */
	public String getType() {
		return (String) getValue(7);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Record1<Integer> key() {
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record8 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row8<Integer, String, Integer, String, String, Timestamp, Timestamp, String> fieldsRow() {
		return (Row8) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row8<Integer, String, Integer, String, String, Timestamp, Timestamp, String> valuesRow() {
		return (Row8) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field2() {
		return OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.UUID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field3() {
		return OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.QUERY_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field4() {
		return OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.TITLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field5() {
		return OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field6() {
		return OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.CREATION_DATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field7() {
		return OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.MODIFIED_DATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field8() {
		return OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP.TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value2() {
		return getUuid();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value3() {
		return getQueryId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value4() {
		return getTitle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value5() {
		return getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value6() {
		return getCreationDate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value7() {
		return getModifiedDate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value8() {
		return getType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingStepRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingStepRecord value2(String value) {
		setUuid(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingStepRecord value3(Integer value) {
		setQueryId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingStepRecord value4(String value) {
		setTitle(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingStepRecord value5(String value) {
		setDescription(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingStepRecord value6(Timestamp value) {
		setCreationDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingStepRecord value7(Timestamp value) {
		setModifiedDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingStepRecord value8(String value) {
		setType(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingStepRecord values(Integer value1, String value2, Integer value3, String value4, String value5, Timestamp value6, Timestamp value7, String value8) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		value5(value5);
		value6(value6);
		value7(value7);
		value8(value8);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached OfcDataCleansingStepRecord
	 */
	public OfcDataCleansingStepRecord() {
		super(OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP);
	}

	/**
	 * Create a detached, initialised OfcDataCleansingStepRecord
	 */
	public OfcDataCleansingStepRecord(Integer id, String uuid, Integer queryId, String title, String description, Timestamp creationDate, Timestamp modifiedDate, String type) {
		super(OfcDataCleansingStep.OFC_DATA_CLEANSING_STEP);

		setValue(0, id);
		setValue(1, uuid);
		setValue(2, queryId);
		setValue(3, title);
		setValue(4, description);
		setValue(5, creationDate);
		setValue(6, modifiedDate);
		setValue(7, type);
	}
}
