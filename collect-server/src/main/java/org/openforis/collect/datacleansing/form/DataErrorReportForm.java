package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.model.CollectRecord.Step;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportForm extends DataCleansingItemForm<DataErrorReport> {

	private Step recordStep;
	
	//calculated members
	private String queryGroupTitle;
	private DataErrorQueryGroupForm errorQueryGroup;
	private int itemCount;
	
	public DataErrorReportForm() {
		super();
	}

	public DataErrorReportForm(DataErrorReport obj) {
		super(obj);
		DataErrorQueryGroup dataErrorQueryGroup = obj.getQueryGroup();
		this.errorQueryGroup = new DataErrorQueryGroupForm(dataErrorQueryGroup);
		this.queryGroupTitle = this.errorQueryGroup.getTitle();
	}
	
	public Step getRecordStep() {
		return recordStep;
	}
	
	public void setRecordStep(Step recordStep) {
		this.recordStep = recordStep;
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

}
