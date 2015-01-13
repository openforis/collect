package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataErrorReportItem;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportItemForm extends DataQueryResultItemForm {

	public DataErrorReportItemForm() {
		super();
	}

	public DataErrorReportItemForm(DataErrorReportItem obj) {
		super(obj);
//		DataErrorReport report = obj.getReport();
//		CollectRecord record = obj.getRecord();
//		DataErrorQuery query = report.getQuery();
//		DataErrorType type = query.getType();
	}

}