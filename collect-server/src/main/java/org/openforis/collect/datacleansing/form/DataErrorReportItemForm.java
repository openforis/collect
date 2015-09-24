package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorQuery.Severity;
import org.openforis.collect.datacleansing.DataErrorReportItem;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.commons.web.Forms;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportItemForm extends DataQueryResultItemForm {

	private String queryTitle;
	private Severity severity;
	private DataErrorTypeForm errorType;

	public DataErrorReportItemForm() {
		super();
	}

	public DataErrorReportItemForm(DataErrorReportItem obj) {
		super(obj);
		DataErrorQuery errorQuery = obj.getErrorQuery();
		DataQuery query = errorQuery.getQuery();
		this.queryTitle = query.getTitle();
		this.severity = errorQuery.getSeverity();
		this.errorType = Forms.toForm(errorQuery.getType(), DataErrorTypeForm.class);
	}
	
	public Severity getSeverity() {
		return severity;
	}
	
	public String getQueryTitle() {
		return queryTitle;
	}

	public DataErrorTypeForm getErrorType() {
		return errorType;
	}
	
}