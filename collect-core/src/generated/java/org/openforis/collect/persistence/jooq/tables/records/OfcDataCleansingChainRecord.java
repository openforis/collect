/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.records;


import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;
import org.openforis.collect.persistence.jooq.tables.OfcDataCleansingChain;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcDataCleansingChainRecord extends UpdatableRecordImpl<OfcDataCleansingChainRecord> implements Record7<Integer, String, Integer, String, String, Timestamp, Timestamp> {

	private static final long serialVersionUID = -413604813;

	/**
	 * Setter for <code>collect.ofc_data_cleansing_chain.id</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_chain.id</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_chain.uuid</code>.
	 */
	public void setUuid(String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_chain.uuid</code>.
	 */
	public String getUuid() {
		return (String) getValue(1);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_chain.survey_id</code>.
	 */
	public void setSurveyId(Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_chain.survey_id</code>.
	 */
	public Integer getSurveyId() {
		return (Integer) getValue(2);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_chain.title</code>.
	 */
	public void setTitle(String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_chain.title</code>.
	 */
	public String getTitle() {
		return (String) getValue(3);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_chain.description</code>.
	 */
	public void setDescription(String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_chain.description</code>.
	 */
	public String getDescription() {
		return (String) getValue(4);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_chain.creation_date</code>.
	 */
	public void setCreationDate(Timestamp value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_chain.creation_date</code>.
	 */
	public Timestamp getCreationDate() {
		return (Timestamp) getValue(5);
	}

	/**
	 * Setter for <code>collect.ofc_data_cleansing_chain.modified_date</code>.
	 */
	public void setModifiedDate(Timestamp value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_cleansing_chain.modified_date</code>.
	 */
	public Timestamp getModifiedDate() {
		return (Timestamp) getValue(6);
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
	// Record7 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row7<Integer, String, Integer, String, String, Timestamp, Timestamp> fieldsRow() {
		return (Row7) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row7<Integer, String, Integer, String, String, Timestamp, Timestamp> valuesRow() {
		return (Row7) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field2() {
		return OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN.UUID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field3() {
		return OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN.SURVEY_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field4() {
		return OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN.TITLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field5() {
		return OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN.DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field6() {
		return OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN.CREATION_DATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field7() {
		return OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN.MODIFIED_DATE;
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
		return getSurveyId();
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
	public OfcDataCleansingChainRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingChainRecord value2(String value) {
		setUuid(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingChainRecord value3(Integer value) {
		setSurveyId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingChainRecord value4(String value) {
		setTitle(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingChainRecord value5(String value) {
		setDescription(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingChainRecord value6(Timestamp value) {
		setCreationDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingChainRecord value7(Timestamp value) {
		setModifiedDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataCleansingChainRecord values(Integer value1, String value2, Integer value3, String value4, String value5, Timestamp value6, Timestamp value7) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		value5(value5);
		value6(value6);
		value7(value7);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached OfcDataCleansingChainRecord
	 */
	public OfcDataCleansingChainRecord() {
		super(OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN);
	}

	/**
	 * Create a detached, initialised OfcDataCleansingChainRecord
	 */
	public OfcDataCleansingChainRecord(Integer id, String uuid, Integer surveyId, String title, String description, Timestamp creationDate, Timestamp modifiedDate) {
		super(OfcDataCleansingChain.OFC_DATA_CLEANSING_CHAIN);

		setValue(0, id);
		setValue(1, uuid);
		setValue(2, surveyId);
		setValue(3, title);
		setValue(4, description);
		setValue(5, creationDate);
		setValue(6, modifiedDate);
	}
}
