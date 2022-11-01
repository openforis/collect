package org.openforis.collect.io.data.csv.columnProviders;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.io.data.csv.CSVDataExportParameters;
import org.openforis.collect.io.data.csv.Column;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BasicColumnProvider implements ColumnProvider {

	protected CSVDataExportParameters config;
	protected ColumnProviderChain parentProvider;

	public BasicColumnProvider(CSVDataExportParameters config) {
		super();
		this.config = config;
	}
	
	/**
	 * Returns column headings including ancestors heading prefixes
	 */
	public List<Column> generateFinalColumns() {
		List<Column> columns = getColumns();
		ColumnProviderChain p = parentProvider;
		if (parentProvider != null) {
			StringBuilder ancestorPrefixSB = new StringBuilder();
			while (p != null) {
				if (StringUtils.isNotBlank(p.getHeadingPrefix())) {
					ancestorPrefixSB.insert(0, p.getHeadingPrefix());
				}
				p = p.getParentProvider();
			}
			String prefix = ancestorPrefixSB.toString();
			for (Column column : columns) {
				column.setHeader(prefix + column.getHeader());
			}
		}
		return columns;
	}
	
	protected abstract String generateHeadingPrefix();
	
	public CSVDataExportParameters getConfig() {
		return config;
	}
	
	public ColumnProviderChain getParentProvider() {
		return parentProvider;
	}
	
	public void setParentProvider(ColumnProviderChain p) {
		this.parentProvider = p;
	}
	
}
