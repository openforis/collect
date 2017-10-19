package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
class CETable extends CEComponent {

	private List<String> headings = new ArrayList<String>();
	private List<CETableRow> rows = new ArrayList<CETableRow>();
	
	public CETable(String name, String label, boolean multiple, String tooltip) {
		super(null, name, label, multiple, tooltip);
	}
	
	public void addHeading(String heading) {
		headings.add(heading);
	}
	
	public void addRow(CETableRow row) {
		rows.add(row);
	}
	
	public List<String> getHeadings() {
		return headings;
	}
	
	public List<CETableRow> getRows() {
		return rows;
	}
	
}

