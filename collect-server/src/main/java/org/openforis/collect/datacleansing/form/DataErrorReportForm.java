package org.openforis.collect.datacleansing.form;

import java.util.Date;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.collect.datacleansing.DataErrorReport;
import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.commons.web.PersistedObjectForm;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorReportForm extends PersistedObjectForm<DataErrorReport> {

	private Date creationDate;	
	
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

	public Date getCreationDate() {
		return creationDate;
	}
	
}
