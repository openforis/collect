package org.openforis.collect.datacleansing.form;

import java.util.Date;

import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.json.CollectDateSerializer;
import org.openforis.collect.model.CollectRecord.Step;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportForm extends DataCleansingItemForm<DataErrorReport> {

	private Step recordStep;
	private int datasetSize;
	private Date lastRecordModifiedDate;
	private int itemCount;
	private int affectedRecordsCount;
	
	//calculated members
	private String queryGroupTitle;
	private DataErrorQueryGroupForm errorQueryGroup;
	
	public DataErrorReportForm() {
		super();
	}

	public DataErrorReportForm(DataErrorReport obj) {
		super(obj);
		DataErrorQueryGroup dataErrorQueryGroup = obj.getQueryGroup();
		this.errorQueryGroup = new DataErrorQueryGroupForm(dataErrorQueryGroup);
		this.queryGroupTitle = this.errorQueryGroup.getTitle();
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
	
	public DataErrorQueryGroupForm getErrorQueryGroup() {
		return errorQueryGroup;
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
