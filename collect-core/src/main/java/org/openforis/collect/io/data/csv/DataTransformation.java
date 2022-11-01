package org.openforis.collect.io.data.csv;

import org.openforis.collect.io.data.csv.columnProviders.ColumnProvider;

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
