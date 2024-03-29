/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.records;


import java.sql.Timestamp;

import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.openforis.collect.persistence.jooq.tables.OfcRecord;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcRecordRecord extends UpdatableRecordImpl<OfcRecordRecord> {

	private static final long serialVersionUID = -1582584131;

	/**
	 * Setter for <code>collect.ofc_record.id</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.id</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>collect.ofc_record.survey_id</code>.
	 */
	public void setSurveyId(Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.survey_id</code>.
	 */
	public Integer getSurveyId() {
		return (Integer) getValue(1);
	}

	/**
	 * Setter for <code>collect.ofc_record.root_entity_definition_id</code>.
	 */
	public void setRootEntityDefinitionId(Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.root_entity_definition_id</code>.
	 */
	public Integer getRootEntityDefinitionId() {
		return (Integer) getValue(2);
	}

	/**
	 * Setter for <code>collect.ofc_record.date_created</code>.
	 */
	public void setDateCreated(Timestamp value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.date_created</code>.
	 */
	public Timestamp getDateCreated() {
		return (Timestamp) getValue(3);
	}

	/**
	 * Setter for <code>collect.ofc_record.created_by_id</code>.
	 */
	public void setCreatedById(Integer value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.created_by_id</code>.
	 */
	public Integer getCreatedById() {
		return (Integer) getValue(4);
	}

	/**
	 * Setter for <code>collect.ofc_record.date_modified</code>.
	 */
	public void setDateModified(Timestamp value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.date_modified</code>.
	 */
	public Timestamp getDateModified() {
		return (Timestamp) getValue(5);
	}

	/**
	 * Setter for <code>collect.ofc_record.modified_by_id</code>.
	 */
	public void setModifiedById(Integer value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.modified_by_id</code>.
	 */
	public Integer getModifiedById() {
		return (Integer) getValue(6);
	}

	/**
	 * Setter for <code>collect.ofc_record.model_version</code>.
	 */
	public void setModelVersion(String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.model_version</code>.
	 */
	public String getModelVersion() {
		return (String) getValue(7);
	}

	/**
	 * Setter for <code>collect.ofc_record.step</code>.
	 */
	public void setStep(Integer value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.step</code>.
	 */
	public Integer getStep() {
		return (Integer) getValue(8);
	}

	/**
	 * Setter for <code>collect.ofc_record.state</code>.
	 */
	public void setState(String value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.state</code>.
	 */
	public String getState() {
		return (String) getValue(9);
	}

	/**
	 * Setter for <code>collect.ofc_record.skipped</code>.
	 */
	public void setSkipped(Integer value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.skipped</code>.
	 */
	public Integer getSkipped() {
		return (Integer) getValue(10);
	}

	/**
	 * Setter for <code>collect.ofc_record.missing</code>.
	 */
	public void setMissing(Integer value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.missing</code>.
	 */
	public Integer getMissing() {
		return (Integer) getValue(11);
	}

	/**
	 * Setter for <code>collect.ofc_record.errors</code>.
	 */
	public void setErrors(Integer value) {
		setValue(12, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.errors</code>.
	 */
	public Integer getErrors() {
		return (Integer) getValue(12);
	}

	/**
	 * Setter for <code>collect.ofc_record.warnings</code>.
	 */
	public void setWarnings(Integer value) {
		setValue(13, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.warnings</code>.
	 */
	public Integer getWarnings() {
		return (Integer) getValue(13);
	}

	/**
	 * Setter for <code>collect.ofc_record.key1</code>.
	 */
	public void setKey1(String value) {
		setValue(14, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.key1</code>.
	 */
	public String getKey1() {
		return (String) getValue(14);
	}

	/**
	 * Setter for <code>collect.ofc_record.key2</code>.
	 */
	public void setKey2(String value) {
		setValue(15, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.key2</code>.
	 */
	public String getKey2() {
		return (String) getValue(15);
	}

	/**
	 * Setter for <code>collect.ofc_record.key3</code>.
	 */
	public void setKey3(String value) {
		setValue(16, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.key3</code>.
	 */
	public String getKey3() {
		return (String) getValue(16);
	}

	/**
	 * Setter for <code>collect.ofc_record.count1</code>.
	 */
	public void setCount1(Integer value) {
		setValue(17, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.count1</code>.
	 */
	public Integer getCount1() {
		return (Integer) getValue(17);
	}

	/**
	 * Setter for <code>collect.ofc_record.count2</code>.
	 */
	public void setCount2(Integer value) {
		setValue(18, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.count2</code>.
	 */
	public Integer getCount2() {
		return (Integer) getValue(18);
	}

	/**
	 * Setter for <code>collect.ofc_record.count3</code>.
	 */
	public void setCount3(Integer value) {
		setValue(19, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.count3</code>.
	 */
	public Integer getCount3() {
		return (Integer) getValue(19);
	}

	/**
	 * Setter for <code>collect.ofc_record.count4</code>.
	 */
	public void setCount4(Integer value) {
		setValue(20, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.count4</code>.
	 */
	public Integer getCount4() {
		return (Integer) getValue(20);
	}

	/**
	 * Setter for <code>collect.ofc_record.count5</code>.
	 */
	public void setCount5(Integer value) {
		setValue(21, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.count5</code>.
	 */
	public Integer getCount5() {
		return (Integer) getValue(21);
	}

	/**
	 * Setter for <code>collect.ofc_record.owner_id</code>.
	 */
	public void setOwnerId(Integer value) {
		setValue(22, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.owner_id</code>.
	 */
	public Integer getOwnerId() {
		return (Integer) getValue(22);
	}

	/**
	 * Setter for <code>collect.ofc_record.app_version</code>.
	 */
	public void setAppVersion(String value) {
		setValue(23, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.app_version</code>.
	 */
	public String getAppVersion() {
		return (String) getValue(23);
	}

	/**
	 * Setter for <code>collect.ofc_record.data_seq_num</code>.
	 */
	public void setDataSeqNum(Integer value) {
		setValue(24, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.data_seq_num</code>.
	 */
	public Integer getDataSeqNum() {
		return (Integer) getValue(24);
	}

	/**
	 * Setter for <code>collect.ofc_record.qualifier1</code>.
	 */
	public void setQualifier1(String value) {
		setValue(25, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.qualifier1</code>.
	 */
	public String getQualifier1() {
		return (String) getValue(25);
	}

	/**
	 * Setter for <code>collect.ofc_record.qualifier2</code>.
	 */
	public void setQualifier2(String value) {
		setValue(26, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.qualifier2</code>.
	 */
	public String getQualifier2() {
		return (String) getValue(26);
	}

	/**
	 * Setter for <code>collect.ofc_record.qualifier3</code>.
	 */
	public void setQualifier3(String value) {
		setValue(27, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.qualifier3</code>.
	 */
	public String getQualifier3() {
		return (String) getValue(27);
	}

	/**
	 * Setter for <code>collect.ofc_record.summary1</code>.
	 */
	public void setSummary1(String value) {
		setValue(28, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.summary1</code>.
	 */
	public String getSummary1() {
		return (String) getValue(28);
	}

	/**
	 * Setter for <code>collect.ofc_record.summary2</code>.
	 */
	public void setSummary2(String value) {
		setValue(29, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.summary2</code>.
	 */
	public String getSummary2() {
		return (String) getValue(29);
	}

	/**
	 * Setter for <code>collect.ofc_record.summary3</code>.
	 */
	public void setSummary3(String value) {
		setValue(30, value);
	}

	/**
	 * Getter for <code>collect.ofc_record.summary3</code>.
	 */
	public String getSummary3() {
		return (String) getValue(30);
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
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached OfcRecordRecord
	 */
	public OfcRecordRecord() {
		super(OfcRecord.OFC_RECORD);
	}

	/**
	 * Create a detached, initialised OfcRecordRecord
	 */
	public OfcRecordRecord(Integer id, Integer surveyId, Integer rootEntityDefinitionId, Timestamp dateCreated, Integer createdById, Timestamp dateModified, Integer modifiedById, String modelVersion, Integer step, String state, Integer skipped, Integer missing, Integer errors, Integer warnings, String key1, String key2, String key3, Integer count1, Integer count2, Integer count3, Integer count4, Integer count5, Integer ownerId, String appVersion, Integer dataSeqNum, String qualifier1, String qualifier2, String qualifier3, String summary1, String summary2, String summary3) {
		super(OfcRecord.OFC_RECORD);

		setValue(0, id);
		setValue(1, surveyId);
		setValue(2, rootEntityDefinitionId);
		setValue(3, dateCreated);
		setValue(4, createdById);
		setValue(5, dateModified);
		setValue(6, modifiedById);
		setValue(7, modelVersion);
		setValue(8, step);
		setValue(9, state);
		setValue(10, skipped);
		setValue(11, missing);
		setValue(12, errors);
		setValue(13, warnings);
		setValue(14, key1);
		setValue(15, key2);
		setValue(16, key3);
		setValue(17, count1);
		setValue(18, count2);
		setValue(19, count3);
		setValue(20, count4);
		setValue(21, count5);
		setValue(22, ownerId);
		setValue(23, appVersion);
		setValue(24, dataSeqNum);
		setValue(25, qualifier1);
		setValue(26, qualifier2);
		setValue(27, qualifier3);
		setValue(28, summary1);
		setValue(29, summary2);
		setValue(30, summary3);
	}
}
