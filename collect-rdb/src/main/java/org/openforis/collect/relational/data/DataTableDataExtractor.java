/**
 * 
 */
package org.openforis.collect.relational.data;

import java.util.List;

import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class DataTableDataExtractor extends DataExtractor {

	private DataTable table;
	private Record record;
	private List<Node<?>> nodes;
	private int total;
	private int nextIndex;

	public DataTableDataExtractor(DataTable table, Record record) {
		this.table = table;
		this.record = record;
		init();
	}

	private void init() {
		this.nodes = record.findNodesByPath(table.getNodeDefinition().getPath());
		this.total = nodes.size();
		this.nextIndex = 0;
	}
	
	@Override
	public boolean hasNext() {
		return nextIndex < total;
	}

	@Override
	public Row next() {
		Node<?> source = nodes.get(nextIndex ++);
		Row row = extractRow(source);
		return row;
	}

	public Dataset extractData(Node<?> source) {
		Dataset data = new Dataset();
		extractDataInternal(table, source, data);
		return data;
	}

	private void extractDataInternal(DataTable table, Node<?> source, Dataset data) {
		// Extract data from this node
		Row row = extractRow(source);
		data.addRow(row);
		// Extract data from descendants
		for (DataTable childTable : table.getChildTables()) {
			Path path = childTable.getRelativePath();
			List<Node<?>> children = path.evaluate(source);
			for (Node<?> child : children) {
				extractDataInternal(childTable, child, data);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Row extractRow(Node<?> source) {
		List<Column<?>> columns = table.getColumns();
		Row row = new Row(table);
		for (int i=0; i < columns.size(); i++) {
			Column col = columns.get(i);
			Object val = col.extractValue(source);
			row.setValue(i, val);
		}
		return row;
	}

}
