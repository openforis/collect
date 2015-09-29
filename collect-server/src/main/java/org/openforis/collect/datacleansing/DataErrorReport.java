package org.openforis.collect.datacleansing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.PersistedSurveyObject;

/**
 * 
 * @author A. Modragon
 *
 */
public class DataErrorReport extends PersistedSurveyObject {
	
	private static final long serialVersionUID = 1L;
	
	private int queryGroupId;
	private Step recordStep = Step.ENTRY;
	private int datasetSize = 0;
	private Date lastRecordModifiedDate;
	private DataErrorQueryGroup queryGroup;
	private List<DataErrorReportItem> items = new ArrayList<DataErrorReportItem>();
	
	public DataErrorReport(CollectSurvey survey) {
		super(survey);
	}
	
	public DataErrorReport(CollectSurvey survey, UUID uuid) {
		super(survey, uuid);
	}

	public void addItem(DataErrorReportItem item) {
		items.add(item);
	}
	
	public void removeItem(DataErrorReportItem item) {
		items.remove(item);
	}
	
	public int getQueryGroupId() {
		return queryGroupId;
	}
	
	public void setQueryGroupId(int queryGroupId) {
		this.queryGroupId = queryGroupId;
	}
	
	public DataErrorQueryGroup getQueryGroup() {
		return queryGroup;
	}
	
	public void setQueryGroup(DataErrorQueryGroup queryGroup) {
		this.queryGroup = queryGroup;
		this.queryGroupId = queryGroup.getId();
	}

	public List<DataErrorReportItem> getItems() {
		return items;
	}

	public void setItems(List<DataErrorReportItem> items) {
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
