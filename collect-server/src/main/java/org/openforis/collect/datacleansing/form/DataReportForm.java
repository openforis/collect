package org.openforis.collect.datacleansing.form;

import java.util.Date;

import org.openforis.collect.datacleansing.DataQueryGroup;
import org.openforis.collect.datacleansing.DataReport;
import org.openforis.collect.datacleansing.json.CollectDateSerializer;
import org.openforis.collect.model.CollectRecord.Step;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataReportForm extends DataCleansingItemForm<DataReport> {

	private Step recordStep;
	private int datasetSize;
	private Date lastRecordModifiedDate;
	private int itemCount;
	private int affectedRecordsCount;
	
	//calculated members
	private String queryGroupTitle;
	private DataQueryGroupForm queryGroup;
	
	public DataReportForm() {
		super();
	}

	public DataReportForm(DataReport obj) {
		super(obj);
		DataQueryGroup dataQueryGroup = obj.getQueryGroup();
		this.queryGroup = new DataQueryGroupForm(dataQueryGroup);
		this.queryGroupTitle = this.queryGroup.getTitle();
	}
	
	public double getAffectedRecordsPercent() {
		if (affectedRecordsCount > 0) {
			return (double) (affectedRecordsCount * 100) / datasetSize;
		} else {
			return 0;
		}
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

	public String getQueryGroupTitle() {
		return queryGroupTitle;
	}
	
	public DataQueryGroupForm getQueryGroup() {
		return queryGroup;
	}
	
	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public int getAffectedRecordsCount() {
		return affectedRecordsCount;
	}
	
	public void setAffectedRecordsCount(int affectedRecordsCount) {
		this.affectedRecordsCount = affectedRecordsCount;
	}
	
}
