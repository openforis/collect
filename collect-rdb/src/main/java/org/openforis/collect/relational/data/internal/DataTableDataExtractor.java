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
import org.openforis.collect.relational.model.CoordinateLatLonColumn;
import org.openforis.collect.relational.model.DataAncestorFKColumn;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataPrimaryKeyColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.Table;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Record;
import org.openforis.idm.path.Path;

/**
 * @author S. Ricci
 *
 */
public class DataTableDataExtractor extends DataExtractor {

	private static final int NODE_ID_MAX_VALUE = Integer.MAX_VALUE;

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

	@Override
	public Table<?> getTable() {
		return table;
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
			return getTableArtificialPK(context);
		} else if (column instanceof CodeValueFKColumn) {
			CodeValueFKColumnValueExtractor valueExtractor = new CodeValueFKColumnValueExtractor(table, (CodeValueFKColumn) column);
			return valueExtractor.extractValue(context);
		} else if (column instanceof CoordinateLatLonColumn) {
			return new CoordinateLatLonColumnValueExtractor(table, (CoordinateLatLonColumn) column).extractValue(context);
		} else if (column instanceof DataAncestorFKColumn) {
			EntityDefinition referencedEntityDefinition = table.getReferencedEntityDefinition((DataAncestorFKColumn) column);
			Entity ancestor = context.getAncestorByDefinition(referencedEntityDefinition);
			if (ancestor == null) {
				throw new IllegalArgumentException(String.format("Referenced ancestor entity not found for column %s inside table %s",
						column.getName(), table.getName()));
			} else {
				return getTableArtificialPK(ancestor);
			}
		} else if (column instanceof DataColumn) {
			DataTableDataColumnValueExtractor<DataColumn> valueExtractor = 
					new DataTableDataColumnValueExtractor<DataColumn>(table, (DataColumn) column);
			return valueExtractor.extractValue(context);
		} else {
			throw new IllegalArgumentException("Unsupported column type: " + column.getClass().getName());
		}
	}
	
	public static BigInteger getTableArtificialPK(Node<?> node) {
		return getTableArtificialPK(node.getRecord().getId(), node.getDefinition(), node.getInternalId());
	}

	public static BigInteger getTableArtificialPK(int recordId,
			NodeDefinition nodeDef, int nodeId) {
		if (nodeDef instanceof EntityDefinition && ((EntityDefinition) nodeDef).isRoot()) {
			return BigInteger.valueOf(recordId);
		} else {
			//result = recordId * NODE_ID_MAX_VALUE + node_id 
			return BigInteger.valueOf(nodeId).add(
					BigInteger.valueOf(recordId).multiply(BigInteger.valueOf(NODE_ID_MAX_VALUE))
			);
		}
	}
	
}
