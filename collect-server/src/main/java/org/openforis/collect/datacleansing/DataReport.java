package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;

/**
 * 
 * @author A. Modragon
 *
 */
public class DataReport extends DataCleansingItem {
	
	private static final long serialVersionUID = 1L;
	
	private int queryGroupId;
	private Step recordStep = Step.ENTRY;
	private int datasetSize = 0;
	private Date lastRecordModifiedDate;
	private DataQueryGroup queryGroup;
	private List<DataReportItem> items = new ArrayList<DataReportItem>();
	
	public DataReport(CollectSurvey survey) {
		super(survey);
	}
	
	public DataReport(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}

	public void addItem(DataReportItem item) {
		items.add(item);
	}
	
	public void removeItem(DataReportItem item) {
		items.remove(item);
	}
	
	public int getQueryGroupId() {
		return queryGroupId;
	}
	
	public void setQueryGroupId(int queryGroupId) {
		this.queryGroupId = queryGroupId;
	}
	
	public DataQueryGroup getQueryGroup() {
		return queryGroup;
	}
	
	public void setQueryGroup(DataQueryGroup queryGroup) {
		this.queryGroup = queryGroup;
		this.queryGroupId = queryGroup.getId();
	}

	public List<DataReportItem> getItems() {
		return items;
	}

	public void setItems(List<DataReportItem> items) {
		this.items = items;
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

}
