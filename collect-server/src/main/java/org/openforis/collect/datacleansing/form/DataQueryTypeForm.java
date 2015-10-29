package org.openforis.collect.datacleansing.form;

import java.util.Arrays;

import org.openforis.collect.datacleansing.DataQueryType;
import org.openforis.commons.lang.Strings;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQueryTypeForm extends DataCleansingItemForm<DataQueryType> {

	private String code;
	private String label;
	private String description;
	
	public DataQueryTypeForm() {
		super();
	}

	public DataQueryTypeForm(DataQueryType obj) {
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
