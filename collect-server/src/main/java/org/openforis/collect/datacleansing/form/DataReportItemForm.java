package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQuery.ErrorSeverity;
import org.openforis.collect.datacleansing.DataReportItem;
import org.openforis.commons.web.Forms;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataReportItemForm extends DataQueryResultItemForm {

	//calculated members
	private String queryTitle;
	private ErrorSeverity severity;
	private DataQueryTypeForm queryType;

	public DataReportItemForm() {
		super();
	}

	public DataReportItemForm(DataReportItem obj) {
		super(obj);
		DataQuery query = obj.getQuery();
		this.queryTitle = query.getTitle();
		this.severity = query.getErrorSeverity();
		this.queryType = Forms.toForm(query.getType(), DataQueryTypeForm.class);
	}
	
	public ErrorSeverity getSeverity() {
		return severity;
	}
	
	public String getQueryTitle() {
		return queryTitle;
	}

	public DataQueryTypeForm getQueryType() {
		return queryType;
	}
	
}