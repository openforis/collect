package org.openforis.collect.model.transform;

/**
 * @author G. Miceli
 */
public class DataTransformation {
	private String axisPath;
	private ColumnProvider provider;
	
	public DataTransformation(String axisPath, ColumnProvider provider) {
		this.axisPath = axisPath;
		this.provider = provider;
	}
	
	public String getAxisPath() {
		return axisPath;
	}
	
	public ColumnProvider getColumnProvider() {
		return provider;
	}
}
