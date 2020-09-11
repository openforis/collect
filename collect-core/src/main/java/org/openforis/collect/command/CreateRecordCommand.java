package org.openforis.collect.command;

import java.util.ArrayList;
import java.util.List;

public class CreateRecordCommand extends RecordCommand {
	
	private static final long serialVersionUID = 1L;
	
	private String formVersion;
	private List<String> keyValues = new ArrayList<String>();
	
	public String getFormVersion() {
		return formVersion;
	}
	
	public void setFormVersion(String formVersion) {
		this.formVersion = formVersion;
	}
	
	public List<String> getKeyValues() {
		return keyValues;
	}

	public void setKeyValues(List<String> keyValues) {
		this.keyValues = keyValues;
	}
	
}
