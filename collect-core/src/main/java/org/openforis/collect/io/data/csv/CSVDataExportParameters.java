package org.openforis.collect.io.data.csv;

import org.openforis.collect.io.data.NodeFilter;
import org.openforis.collect.model.RecordFilter;

/**
 * 
 * @author S. Ricci
 *
 */
public class CSVDataExportParameters extends CSVDataExportParametersBase {
	
	private RecordFilter recordFilter;
	private NodeFilter nodeFilter;
		
	public RecordFilter getRecordFilter() {
		return recordFilter;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
	public NodeFilter getNodeFilter() {
		return nodeFilter;
	}
	
	public void setNodeFilter(NodeFilter nodeFilter) {
		this.nodeFilter = nodeFilter;
	}
}
