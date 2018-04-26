package org.openforis.collect.relational.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.relational.CollectRdbException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
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
	private Map<Integer, CodeValueFKColumn> foreignKeyCodeColumns;
	private Map<Integer, DataAncestorFKColumn> ancestorFKColumnsByDefinitionId;
	private DataAncestorFKColumn parentFKColumn;
	
	DataTable(String prefix, String name, DataTable parent, NodeDefinition defn, Path relativePath) throws CollectRdbException {
		super(prefix, name);
		this.definition = defn;
		this.parent = parent;
		this.relativePath = relativePath;
		this.childTables = new ArrayList<DataTable>();
		this.foreignKeyCodeColumns = new HashMap<Integer, CodeValueFKColumn>();
		this.ancestorFKColumnsByDefinitionId = new HashMap<Integer, DataAncestorFKColumn>();
	}

	@Override
	void addColumn(Column<?> column) {
		super.addColumn(column);
		if ( column instanceof CodeValueFKColumn ) {
			int attrDefnId = ((CodeValueFKColumn) column).getAttributeDefinition().getId();
			foreignKeyCodeColumns.put(attrDefnId, (CodeValueFKColumn) column);
		} if (column instanceof DataAncestorFKColumn) {
			DataAncestorFKColumn ancestorFKColumn = (DataAncestorFKColumn) column;
			ancestorFKColumnsByDefinitionId.put(ancestorFKColumn.getAncestorDefinitionId(), ancestorFKColumn);
			if (ancestorFKColumn.isParentFKColumn()) {
				parentFKColumn = ancestorFKColumn;
			}
		}
	}
	
	public DataAncestorFKColumn getRecordIdColumn() {
		EntityDefinition rootEntityDef = definition.getRootEntity();
		return getAncestorFKColumn(rootEntityDef.getId());
	}
	
	public Collection<DataAncestorFKColumn> getAncestorFKColumns() {
		return ancestorFKColumnsByDefinitionId.values();
	}
	
	public DataAncestorFKColumn getAncestorFKColumn(int definitionId) {
		DataAncestorFKColumn column = ancestorFKColumnsByDefinitionId.get(definitionId);
		if (column == null) {
			throw new IllegalStateException("No ancestor id column found in table " + getName() + " for definition id " + definitionId);
		}
		return column;
	}
	
	public CodeValueFKColumn getForeignKeyCodeColumn(CodeAttributeDefinition defn) {
		return foreignKeyCodeColumns.get(defn.getId());
	}
	
	public NodeDefinition getNodeDefinition() {
		return definition;
	}

	public void print(PrintStream out) {
		out.printf("%-43s%s\n", getName()+":", getRelativePath());
		for (Column<?> col : getColumns()) {
			Integer length = col.getLength();
			String path = "";
			if ( col instanceof DataColumn ) {
				DataColumn dcol = (DataColumn) col;
				path = dcol.getRelativePath()+"";
			}
			out.printf("\t%-35s%-8s%-8s%s\n", col.getName(), col.getType().getCode(), length == null ? "" : length, path);
		}
		out.flush();
	}
	
	public List<DataColumn> getDataColumns(AttributeDefinition attributeDefinition) {
		List<DataColumn> result = new ArrayList<DataColumn>();
		int attributeDefinitionId = attributeDefinition.getId();
		for ( Column<?> column : getColumns() ) {
			if ( column instanceof DataColumn ) {
				DataColumn dataCol = (DataColumn) column;
				AttributeDefinition columnAttrDefn = dataCol.getAttributeDefinition();
				if ( columnAttrDefn.getId() == attributeDefinitionId 
						&& ! ( dataCol instanceof CodeValueFKColumn )) {
					result.add(dataCol); 
				}
			}
		}
		return result;
	}
	
	public DataColumn getDataColumn(FieldDefinition<?> fieldDefinition) {
		AttributeDefinition attributeDefinition = (AttributeDefinition) fieldDefinition.getParentDefinition();
		List<DataColumn> attributeDataColumns = getDataColumns(attributeDefinition);
		String fieldDefinitionName = fieldDefinition.getName();
		for ( DataColumn column : attributeDataColumns ) {
			NodeDefinition columnNodeDefn = column.getNodeDefinition();
			if ( columnNodeDefn instanceof FieldDefinition && 
					columnNodeDefn.getName().equals(fieldDefinitionName)) {
				return column; 
			}
		}
		return null;
	}
	
	public List<DataTable> getAncestors() {
		List<DataTable> result = new ArrayList<DataTable>();
		DataTable ancestor = getParent();
		while (ancestor != null) {
			result.add(ancestor);
			ancestor = ancestor.getParent();
		}
		return result;
	}
	
	public DataTable getRootAncestor() {
		List<DataTable> ancestors = getAncestors();
		return ancestors.isEmpty() ? this : ancestors.get(ancestors.size() - 1);
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
	
	public List<DataTable> getChildTables() {
		return childTables;
	}

	public DataPrimaryKeyColumn getPrimaryKeyColumn() {
		PrimaryKeyConstraint pkConstraint = getPrimaryKeyConstraint();
		return (DataPrimaryKeyColumn) pkConstraint.getPrimaryKeyColumn();
	}
	
//	public DataTable getParentDataTable() {
//		if (parentFKColumn == null) {
//			return null;
//		}
//		DataTable parentTable = getReferencedTable(parentFKColumn);
//		return parentTable;
//	}
	
	public DataAncestorFKColumn getParentFKColumn() {
		return parentFKColumn;
	}
	
	public void setParentFKColumn(DataAncestorFKColumn parentFKColumn) {
		this.parentFKColumn = parentFKColumn;
	}
	
}