package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataErrorType;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorTypeForm extends DataCleansingItemForm<DataErrorType> {

	private String code;
	private String label;
	private String description;
	
	public DataErrorTypeForm() {
		super();
	}

	public DataErrorTypeForm(DataErrorType obj) {
		super(obj);
	}

	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

}
