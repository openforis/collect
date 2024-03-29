/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables.pojos;


import java.io.Serializable;
import java.sql.Timestamp;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OfcDataReport implements Serializable {

	private static final long serialVersionUID = -2131503306;

	private Integer   id;
	private String    uuid;
	private Integer   queryGroupId;
	private Integer   recordStep;
	private Integer   datasetSize;
	private Timestamp lastRecordModifiedDate;
	private Timestamp creationDate;

	public OfcDataReport() {}

	public OfcDataReport(OfcDataReport value) {
		this.id = value.id;
		this.uuid = value.uuid;
		this.queryGroupId = value.queryGroupId;
		this.recordStep = value.recordStep;
		this.datasetSize = value.datasetSize;
		this.lastRecordModifiedDate = value.lastRecordModifiedDate;
		this.creationDate = value.creationDate;
	}

	public OfcDataReport(
		Integer   id,
		String    uuid,
		Integer   queryGroupId,
		Integer   recordStep,
		Integer   datasetSize,
		Timestamp lastRecordModifiedDate,
		Timestamp creationDate
	) {
		this.id = id;
		this.uuid = uuid;
		this.queryGroupId = queryGroupId;
		this.recordStep = recordStep;
		this.datasetSize = datasetSize;
		this.lastRecordModifiedDate = lastRecordModifiedDate;
		this.creationDate = creationDate;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Integer getQueryGroupId() {
		return this.queryGroupId;
	}

	public void setQueryGroupId(Integer queryGroupId) {
		this.queryGroupId = queryGroupId;
	}

	public Integer getRecordStep() {
		return this.recordStep;
	}

	public void setRecordStep(Integer recordStep) {
		this.recordStep = recordStep;
	}

	public Integer getDatasetSize() {
		return this.datasetSize;
	}

	public void setDatasetSize(Integer datasetSize) {
		this.datasetSize = datasetSize;
	}

	public Timestamp getLastRecordModifiedDate() {
		return this.lastRecordModifiedDate;
	}

	public void setLastRecordModifiedDate(Timestamp lastRecordModifiedDate) {
		this.lastRecordModifiedDate = lastRecordModifiedDate;
	}

	public Timestamp getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
}
