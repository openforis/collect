package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorQuery.Severity;
import org.openforis.collect.datacleansing.DataErrorReportItem;
import org.openforis.collect.datacleansing.DataQuery;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportItemForm extends DataQueryResultItemForm {

	private String queryTitle;
	private Severity severity;
	private String errorTypeCode;

	public DataErrorReportItemForm() {
		super();
	}

	public DataErrorReportItemForm(DataErrorReportItem obj) {
		super(obj);
		DataErrorQuery errorQuery = obj.getErrorQuery();
		DataQuery query = errorQuery.getQuery();
		this.queryTitle = query.getTitle();
		this.severity = errorQuery.getSeverity();
		this.errorTypeCode = errorQuery.getType().getCode();
	}
	
	public Severity getSeverity() {
		return severity;
	}
	
	public String getQueryTitle() {
		return queryTitle;
	}

	public String getErrorTypeCode() {
		return errorTypeCode;
	}
}