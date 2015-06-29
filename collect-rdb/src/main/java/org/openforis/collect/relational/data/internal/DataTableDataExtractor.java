/**
 * 
 */
package org.openforis.collect.relational.data.internal;

import java.math.BigInteger;
import java.util.List;

import org.openforis.collect.relational.data.DataExtractor;
import org.openforis.collect.relational.data.Dataset;
import org.openforis.collect.relational.data.Row;
import org.openforis.collect.relational.model.CodeValueFKColumn;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataParentKeyColumn;
import org.openforis.collect.relational.model.DataPrimaryKeyColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class DataTableDataExtractor extends DataExtractor {

	private static final int NODE_ID_MAX_VALUE = 1000000;

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

	private Row extractRow(Node<?> source) {
		List<Column<?>> columns = table.getColumns();
		Row row = new Row(table);
		for (int i=0; i < columns.size(); i++) {
			Column<?> col = columns.get(i);
			Object val = extractColumnValue(source, col);
			row.setValue(i, val);
		}
		return row;
	}

	private Object extractColumnValue(Node<?> context, Column<?> column) {
		if (column instanceof DataPrimaryKeyColumn) {
			return generateArtificialId(context);
		} else if (column instanceof CodeValueFKColumn) {
			CodeValueFKColumnValueExtractor valueExtractor = new CodeValueFKColumnValueExtractor(table, (CodeValueFKColumn) column);
			return valueExtractor.extractValue(context);
		} else if (column instanceof DataParentKeyColumn) {
			Entity nearestAncestorMultipleEntity = context.getNearestMultipleEntityAncestor();
			if ( nearestAncestorMultipleEntity == null ) {
				throw new NullPointerException(String.format(
						"Cannot find neareast multiple entity ancestor for node %s in record %d", 
						context.getPath(), context.getRecord().getId()));
			}
			return generateArtificialId(nearestAncestorMultipleEntity);
		} else if (column instanceof DataColumn) {
			DataTableDataColumnValueExtractor valueExtractor = new DataTableDataColumnValueExtractor(table, (DataColumn) column);
			return valueExtractor.extractValue(context);
		} else {
			throw new IllegalArgumentException("Unsupported column type: " + column.getClass().getName());
		}
	}
	
	private BigInteger generateArtificialId(Node<?> node) {
		Integer id = node.getInternalId();
		if ( id == null ) {
			throw new NullPointerException(String.format("Node id is null for node %s in record %d", node.getPath(), node.getRecord().getId()));
		}
		Record record = node.getRecord();
		//result = id + recordId * NODE_ID_MAX_VALUE
		BigInteger result = BigInteger.valueOf(id).add(
				BigInteger.valueOf(record.getId()).multiply(BigInteger.valueOf(NODE_ID_MAX_VALUE))
		);
		return result;
	}
	
}
