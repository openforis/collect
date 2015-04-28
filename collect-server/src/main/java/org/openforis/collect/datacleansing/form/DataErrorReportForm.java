package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.DataQuery;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportForm extends DataCleansingItemForm<DataErrorReport> {

	//calculated members
	private String typeCode;
	private String queryTitle;
	
	public DataErrorReportForm() {
		super();
	}

	public DataErrorReportForm(DataErrorReport obj) {
		super(obj);
		DataErrorQuery dataErrorQuery = obj.getQuery();
		DataQuery query = dataErrorQuery.getQuery();
		this.queryTitle = query.getTitle();
		DataErrorType type = dataErrorQuery.getType();
		this.typeCode = type.getCode();
	}

	public String getTypeCode() {
		return typeCode;
	}
	
	public String getQueryTitle() {
		return queryTitle;
	}

}
