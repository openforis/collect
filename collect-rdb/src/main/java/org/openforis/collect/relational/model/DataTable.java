package org.openforis.collect.relational.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.relational.CollectRdbException;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataTable extends AbstractTable<Node<?>> {

	private NodeDefinition definition;
	private Path relativePath;
	private DataTable parent;
	private List<DataTable> childTables;
	
	DataTable(String prefix, String name, String suffix, DataTable parent, 
			NodeDefinition defn, Path relativePath) throws CollectRdbException {
		super(prefix, name, suffix);
		this.definition = defn;
		this.parent = parent;
		this.relativePath = relativePath;
		this.childTables = new ArrayList<DataTable>();
	}

	public NodeDefinition getNodeDefinition() {
		return definition;
	}

	public DataTable getParent() {
		return parent;
	}
	
	public Path getRelativePath() {
		return relativePath;
	}
	
	void addChildTable(DataTable table) {
		childTables.add(table);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Row extractRow(Node<?> source) {
		List<Column<?>> columns = getColumns();
		Row row = new Row(this);
		for (int i=0; i < columns.size(); i++) {
			Column col = columns.get(i);
			Object val = col.extractValue(source);
			row.setValue(i, val);
		}
		return row;
	}

	@Override
	public Dataset extractData(Node<?> source) {
		Dataset data = new Dataset();
		extractDataInternal(source, data);
		return data;
	}

	private void extractDataInternal(Node<?> source, Dataset data) {
		// Extract data from this node
		Row row = extractRow(source);
		data.addRow(row);
		// Extract data from descendants
		for (DataTable childTable : childTables) {
			Path path = childTable.getRelativePath();
			List<Node<?>> children = path.evaluate(source);
			for (Node<?> child : children) {
				childTable.extractDataInternal(child, data);
			}
		}
	}

	@Override
	public void print(PrintStream out) {
		out.printf("%-43s%s\n", getName()+":", getRelativePath());
		printColumns(out);
	}
}