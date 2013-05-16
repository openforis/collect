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
	
	DataTable(String prefix, String name, DataTable parent, NodeDefinition defn, Path relativePath) throws CollectRdbException {
		super(prefix, name);
		this.definition = defn;
		this.parent = parent;
		this.relativePath = relativePath;
		this.childTables = new ArrayList<DataTable>();
	}

	public NodeDefinition getNodeDefinition() {
		return definition;
	}

	public void print(PrintStream out) {
		out.printf("%-43s%s\n", getName()+":", getRelativePath());
		for (Column<?> col : getColumns()) {
			String name = col.getName();
			int type = col.getType();
			Integer length = col.getLength();
			String path = "";
			if ( col instanceof DataColumn ) {
				DataColumn dcol = (DataColumn) col;
				path = dcol.getRelativePath()+"";
			}
			out.printf("\t%-35s%-8s%-8s%s\n", name, type, length==null?"":length, path);
		}
		out.flush();
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
}