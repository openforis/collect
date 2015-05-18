package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.DataErrorType;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportForm extends DataCleansingItemForm<DataErrorReport> {

	//calculated members
	private String typeCode;
	private String queryTitle;
	private DataErrorQueryForm errorQuery;
	
	public DataErrorReportForm() {
		super();
	}

	public DataErrorReportForm(DataErrorReport obj) {
		super(obj);
		DataErrorQuery dataErrorQuery = obj.getQuery();
		this.errorQuery = new DataErrorQueryForm(dataErrorQuery);
		this.queryTitle = this.errorQuery.getQueryTitle();
		DataErrorType type = dataErrorQuery.getType();
		this.typeCode = type.getCode();
	}

	public String getTypeCode() {
		return typeCode;
	}
	
	public String getQueryTitle() {
		return queryTitle;
	}
	
	public DataErrorQueryForm getErrorQuery() {
		return errorQuery;
	}
	
}
