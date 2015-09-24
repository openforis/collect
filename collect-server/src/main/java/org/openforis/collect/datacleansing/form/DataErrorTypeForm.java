package org.openforis.collect.datacleansing.form;

import java.util.Arrays;

import org.openforis.collect.datacleansing.DataErrorType;
import org.openforis.commons.lang.Strings;

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

	/**
	 * Calculated property: returns a pretty representation of the object
	 */
	public String getPrettyLabel() {
		return Strings.joinNotBlank(Arrays.asList(code, label), " - ");
	}
	
	/**
	 * Void: avoids deserialization issues
	 */
	public void setPrettyLabel(String label) {
		return;
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
