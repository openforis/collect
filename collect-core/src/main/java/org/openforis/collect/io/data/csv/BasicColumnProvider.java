package org.openforis.collect.io.data.csv;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BasicColumnProvider implements ColumnProvider {

	private CSVExportConfiguration config;

	public BasicColumnProvider(CSVExportConfiguration config) {
		super();
		this.config = config;
	}
	
	public CSVExportConfiguration getConfig() {
		return config;
	}
	
}
