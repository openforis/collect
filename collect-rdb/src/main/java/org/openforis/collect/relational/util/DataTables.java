package org.openforis.collect.relational.util;

import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.idm.metamodel.CodeAttributeDefinition;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataTables {

	public static String getCodeFKColumnName(RelationalSchemaConfig config, DataTable dataTable, CodeAttributeDefinition attrDefn) {
		DataColumn codeValueColumn = dataTable.getDataColumn(attrDefn.getFieldDefinition(CodeAttributeDefinition.CODE_FIELD));
		return codeValueColumn.getName() + config.getCodeListTableSuffix() + config.getIdColumnSuffix();
	}
	
}
