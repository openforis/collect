package org.openforis.collect.datacleansing;

import java.util.Date;
import java.util.UUID;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author A. Modragon
 *
 */
public class DataCleansingReport extends DataCleansingItem {
	
	private static final long serialVersionUID = 1L;
	
	private int cleansingChainId;
	private Step recordStep = Step.ENTRY;
	private int datasetSize = 0;
	private Date lastRecordModifiedDate;
	private int cleansedRecords = 0;
	private int cleansedNodes = 0;
	
	public DataCleansingReport(CollectSurvey survey) {
		super(survey);
	}
	
	public DataCleansingReport(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
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
