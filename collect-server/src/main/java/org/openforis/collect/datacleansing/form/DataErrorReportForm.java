package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataErrorQueryGroup;
import org.openforis.collect.datacleansing.DataErrorReport;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportForm extends DataCleansingItemForm<DataErrorReport> {

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
