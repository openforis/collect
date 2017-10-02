/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.pojos;


import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcRecordData implements Serializable {

	private static final long serialVersionUID = 1277160025;

	private Integer   recordId;
	private Integer   seqNum;
	private Integer   step;
	private String    state;
	private Timestamp dateCreated;
	private Integer   createdBy;
	private Timestamp dateModified;
	private Integer   modifiedBy;
	private Integer   skipped;
	private Integer   missing;
	private Integer   errors;
	private Integer   warnings;
	private Integer   count1;
	private Integer   count2;
	private Integer   count3;
	private Integer   count4;
	private Integer   count5;
	private byte[]    data;
	private String    appVersion;
	private String    key1;
	private String    key2;
	private String    key3;

	public OfcRecordData() {}

	public OfcRecordData(OfcRecordData value) {
		this.recordId = value.recordId;
		this.seqNum = value.seqNum;
		this.step = value.step;
		this.state = value.state;
		this.dateCreated = value.dateCreated;
		this.createdBy = value.createdBy;
		this.dateModified = value.dateModified;
		this.modifiedBy = value.modifiedBy;
		this.skipped = value.skipped;
		this.missing = value.missing;
		this.errors = value.errors;
		this.warnings = value.warnings;
		this.count1 = value.count1;
		this.count2 = value.count2;
		this.count3 = value.count3;
		this.count4 = value.count4;
		this.count5 = value.count5;
		this.data = value.data;
		this.appVersion = value.appVersion;
		this.key1 = value.key1;
		this.key2 = value.key2;
		this.key3 = value.key3;
	}

	public OfcRecordData(
		Integer   recordId,
		Integer   seqNum,
		Integer   step,
		String    state,
		Timestamp dateCreated,
		Integer   createdBy,
		Timestamp dateModified,
		Integer   modifiedBy,
		Integer   skipped,
		Integer   missing,
		Integer   errors,
		Integer   warnings,
		Integer   count1,
		Integer   count2,
		Integer   count3,
		Integer   count4,
		Integer   count5,
		byte[]    data,
		String    appVersion,
		String    key1,
		String    key2,
		String    key3
	) {
		this.recordId = recordId;
		this.seqNum = seqNum;
		this.step = step;
		this.state = state;
		this.dateCreated = dateCreated;
		this.createdBy = createdBy;
		this.dateModified = dateModified;
		this.modifiedBy = modifiedBy;
		this.skipped = skipped;
		this.missing = missing;
		this.errors = errors;
		this.warnings = warnings;
		this.count1 = count1;
		this.count2 = count2;
		this.count3 = count3;
		this.count4 = count4;
		this.count5 = count5;
		this.data = data;
		this.appVersion = appVersion;
		this.key1 = key1;
		this.key2 = key2;
		this.key3 = key3;
	}

	public Integer getRecordId() {
		return this.recordId;
	}

	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}

	public Integer getSeqNum() {
		return this.seqNum;
	}

	public void setSeqNum(Integer seqNum) {
		this.seqNum = seqNum;
	}

	public Integer getStep() {
		return this.step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Timestamp getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Integer getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getDateModified() {
		return this.dateModified;
	}

	public void setDateModified(Timestamp dateModified) {
		this.dateModified = dateModified;
	}

	public Integer getModifiedBy() {
		return this.modifiedBy;
	}

	public void setModifiedBy(Integer modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Integer getSkipped() {
		return this.skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getMissing() {
		return this.missing;
	}

	public void setMissing(Integer missing) {
		this.missing = missing;
	}

	public Integer getErrors() {
		return this.errors;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	public Integer getWarnings() {
		return this.warnings;
	}

	public void setWarnings(Integer warnings) {
		this.warnings = warnings;
	}

	public Integer getCount1() {
		return this.count1;
	}

	public void setCount1(Integer count1) {
		this.count1 = count1;
	}

	public Integer getCount2() {
		return this.count2;
	}

	public void setCount2(Integer count2) {
		this.count2 = count2;
	}

	public Integer getCount3() {
		return this.count3;
	}

	public void setCount3(Integer count3) {
		this.count3 = count3;
	}

	public Integer getCount4() {
		return this.count4;
	}

	public void setCount4(Integer count4) {
		this.count4 = count4;
	}

	public Integer getCount5() {
		return this.count5;
	}

	public void setCount5(Integer count5) {
		this.count5 = count5;
	}

	public byte[] getData() {
		return this.data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getAppVersion() {
		return this.appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public String getKey1() {
		return this.key1;
	}

	public void setKey1(String key1) {
		this.key1 = key1;
	}

	public String getKey2() {
		return this.key2;
	}

	public void setKey2(String key2) {
		this.key2 = key2;
	}

	public String getKey3() {
		return this.key3;
	}

	public void setKey3(String key3) {
		this.key3 = key3;
	}
}