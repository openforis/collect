package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataCleansingChain;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingChainForm extends DataCleansingItemForm<DataCleansingChain> {

	private String title;
	private String description;

	public DataCleansingChainForm() {
		super();
	}
	
	public DataCleansingChainForm(DataCleansingChain chain) {
		super(chain);
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
}
