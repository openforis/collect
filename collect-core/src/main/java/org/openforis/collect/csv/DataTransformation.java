package org.openforis.collect.csv;

/**
 * @author G. Miceli
 * @deprecated replaced with idm-transform api
 */
@Deprecated
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
