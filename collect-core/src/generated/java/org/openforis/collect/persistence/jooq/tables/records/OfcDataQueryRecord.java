/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.records;


import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record12;
import org.jooq.Row12;
import org.jooq.impl.UpdatableRecordImpl;
import org.openforis.collect.persistence.jooq.tables.OfcDataQuery;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcDataQueryRecord extends UpdatableRecordImpl<OfcDataQueryRecord> implements Record12<Integer, String, Integer, String, String, Timestamp, Timestamp, Integer, Integer, String, Integer, String> {

	private static final long serialVersionUID = 540930151;

	/**
	 * Setter for <code>collect.ofc_data_query.id</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.id</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.uuid</code>.
	 */
	public void setUuid(String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.uuid</code>.
	 */
	public String getUuid() {
		return (String) getValue(1);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.survey_id</code>.
	 */
	public void setSurveyId(Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.survey_id</code>.
	 */
	public Integer getSurveyId() {
		return (Integer) getValue(2);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.title</code>.
	 */
	public void setTitle(String value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.title</code>.
	 */
	public String getTitle() {
		return (String) getValue(3);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.description</code>.
	 */
	public void setDescription(String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.description</code>.
	 */
	public String getDescription() {
		return (String) getValue(4);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.creation_date</code>.
	 */
	public void setCreationDate(Timestamp value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.creation_date</code>.
	 */
	public Timestamp getCreationDate() {
		return (Timestamp) getValue(5);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.modified_date</code>.
	 */
	public void setModifiedDate(Timestamp value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.modified_date</code>.
	 */
	public Timestamp getModifiedDate() {
		return (Timestamp) getValue(6);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.entity_id</code>.
	 */
	public void setEntityId(Integer value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.entity_id</code>.
	 */
	public Integer getEntityId() {
		return (Integer) getValue(7);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.attribute_id</code>.
	 */
	public void setAttributeId(Integer value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.attribute_id</code>.
	 */
	public Integer getAttributeId() {
		return (Integer) getValue(8);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.conditions</code>.
	 */
	public void setConditions(String value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.conditions</code>.
	 */
	public String getConditions() {
		return (String) getValue(9);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.type_id</code>.
	 */
	public void setTypeId(Integer value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.type_id</code>.
	 */
	public Integer getTypeId() {
		return (Integer) getValue(10);
	}

	/**
	 * Setter for <code>collect.ofc_data_query.severity</code>.
	 */
	public void setSeverity(String value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>collect.ofc_data_query.severity</code>.
	 */
	public String getSeverity() {
		return (String) getValue(11);
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
	// Record12 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row12<Integer, String, Integer, String, String, Timestamp, Timestamp, Integer, Integer, String, Integer, String> fieldsRow() {
		return (Row12) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row12<Integer, String, Integer, String, String, Timestamp, Timestamp, Integer, Integer, String, Integer, String> valuesRow() {
		return (Row12) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return OfcDataQuery.OFC_DATA_QUERY.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field2() {
		return OfcDataQuery.OFC_DATA_QUERY.UUID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field3() {
		return OfcDataQuery.OFC_DATA_QUERY.SURVEY_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field4() {
		return OfcDataQuery.OFC_DATA_QUERY.TITLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field5() {
		return OfcDataQuery.OFC_DATA_QUERY.DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field6() {
		return OfcDataQuery.OFC_DATA_QUERY.CREATION_DATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field7() {
		return OfcDataQuery.OFC_DATA_QUERY.MODIFIED_DATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field8() {
		return OfcDataQuery.OFC_DATA_QUERY.ENTITY_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field9() {
		return OfcDataQuery.OFC_DATA_QUERY.ATTRIBUTE_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field10() {
		return OfcDataQuery.OFC_DATA_QUERY.CONDITIONS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field11() {
		return OfcDataQuery.OFC_DATA_QUERY.TYPE_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field12() {
		return OfcDataQuery.OFC_DATA_QUERY.SEVERITY;
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
	public Integer value8() {
		return getEntityId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value9() {
		return getAttributeId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value10() {
		return getConditions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value11() {
		return getTypeId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value12() {
		return getSeverity();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value2(String value) {
		setUuid(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value3(Integer value) {
		setSurveyId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value4(String value) {
		setTitle(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value5(String value) {
		setDescription(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value6(Timestamp value) {
		setCreationDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value7(Timestamp value) {
		setModifiedDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value8(Integer value) {
		setEntityId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value9(Integer value) {
		setAttributeId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value10(String value) {
		setConditions(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value11(Integer value) {
		setTypeId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord value12(String value) {
		setSeverity(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OfcDataQueryRecord values(Integer value1, String value2, Integer value3, String value4, String value5, Timestamp value6, Timestamp value7, Integer value8, Integer value9, String value10, Integer value11, String value12) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		value5(value5);
		value6(value6);
		value7(value7);
		value8(value8);
		value9(value9);
		value10(value10);
		value11(value11);
		value12(value12);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached OfcDataQueryRecord
	 */
	public OfcDataQueryRecord() {
		super(OfcDataQuery.OFC_DATA_QUERY);
	}

	/**
	 * Create a detached, initialised OfcDataQueryRecord
	 */
	public OfcDataQueryRecord(Integer id, String uuid, Integer surveyId, String title, String description, Timestamp creationDate, Timestamp modifiedDate, Integer entityId, Integer attributeId, String conditions, Integer typeId, String severity) {
		super(OfcDataQuery.OFC_DATA_QUERY);

		setValue(0, id);
		setValue(1, uuid);
		setValue(2, surveyId);
		setValue(3, title);
		setValue(4, description);
		setValue(5, creationDate);
		setValue(6, modifiedDate);
		setValue(7, entityId);
		setValue(8, attributeId);
		setValue(9, conditions);
		setValue(10, typeId);
		setValue(11, severity);
	}
}
