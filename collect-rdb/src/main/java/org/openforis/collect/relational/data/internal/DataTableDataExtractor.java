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
import org.openforis.collect.relational.model.DataAncestorFKColumn;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataParentKeyColumn;
import org.openforis.collect.relational.model.DataPrimaryKeyColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.idm.metamodel.EntityDefinition;
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
			return getTableArtificialPK((Entity) context);
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
			return getTableArtificialPK(nearestAncestorMultipleEntity);
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
			DataTableDataColumnValueExtractor valueExtractor = new DataTableDataColumnValueExtractor(table, (DataColumn) column);
			return valueExtractor.extractValue(context);
		} else {
			throw new IllegalArgumentException("Unsupported column type: " + column.getClass().getName());
		}
	}
	
	public static BigInteger getTableArtificialPK(Entity entity) {
		return getArtificialPK(entity.getRecord().getId(), entity.getDefinition(), entity.getInternalId());
	}

	public static BigInteger getArtificialPK(Integer recordId,
			EntityDefinition entityDef, Integer entityId) {
		if (entityDef.isRoot()) {
			return BigInteger.valueOf(recordId);
		} else {
			if ( entityId == null ) {
				throw new NullPointerException(String.format("Node id is null for node %d in record %d", entityId, recordId));
			}
			//result = id + recordId * NODE_ID_MAX_VALUE
			return BigInteger.valueOf(entityId).add(
					BigInteger.valueOf(recordId).multiply(BigInteger.valueOf(NODE_ID_MAX_VALUE))
			);
		}
	}
	
}
