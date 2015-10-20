package org.openforis.collect.datacleansing.form;

import java.util.Date;

import org.openforis.collect.datacleansing.DataCleansingReport;
import org.openforis.collect.datacleansing.json.CollectDateSerializer;
import org.openforis.collect.model.CollectRecord.Step;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingReportForm extends DataCleansingItemForm<DataCleansingReport> {

	private int cleansingChainId;
	private Step recordStep;
	private int datasetSize;
	private Date lastRecordModifiedDate;
	private int cleansedRecords;
	private int cleansedNodes;
	
	public DataCleansingReportForm() {
		super();
	}

	public DataCleansingReportForm(DataCleansingReport obj) {
		super(obj);
	}

	public int getCleansingChainId() {
		return cleansingChainId;
	}

	public void setCleansingChainId(int cleansingChainId) {
		this.cleansingChainId = cleansingChainId;
	}

	public Step getRecordStep() {
		return recordStep;
	}

	public void setRecordStep(Step recordStep) {
		this.recordStep = recordStep;
	}

	public int getDatasetSize() {
		return datasetSize;
	}

	public void setDatasetSize(int datasetSize) {
		this.datasetSize = datasetSize;
	}

	@JsonSerialize(using = CollectDateSerializer.class)
	public Date getLastRecordModifiedDate() {
		return lastRecordModifiedDate;
	}

	public void setLastRecordModifiedDate(Date lastRecordModifiedDate) {
		this.lastRecordModifiedDate = lastRecordModifiedDate;
	}

	public int getCleansedRecords() {
		return cleansedRecords;
	}

	public void setCleansedRecords(int cleansedRecords) {
		this.cleansedRecords = cleansedRecords;
	}

	public int getCleansedNodes() {
		return cleansedNodes;
	}

	public void setCleansedNodes(int cleansedNodes) {
		this.cleansedNodes = cleansedNodes;
	}
	
}
