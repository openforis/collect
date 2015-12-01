package org.openforis.collect.reporting;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.mondrian.Schema;
import org.openforis.collect.mondrian.Schema.Cube;
import org.openforis.collect.mondrian.Table;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;

public class MondrianSchemaGenerator2 {

	public Schema generate(CollectSurvey survey) {
		String schemaName = survey.getName();
		Schema schema = new Schema();
		schema.setName(schemaName);
		
		RelationalSchema rdbSchema = null;
		List<DataTable> dataTables = rdbSchema.getDataTables();
		for (DataTable dataTable : dataTables) {
			Cube cube = new Schema.Cube();
			cube.setName(dataTable.getBaseName());
			Table table = new Table();
			table.setName(dataTable.getName());
			cube.setTable(table);
//			
//			dataTable.getDataColumns(attributeDefinition)
//			
//			cube.getDimensionUsageOrDimension().add(dimension);
			
			schema.getCube().add(cube);
		}
		
		return schema;
	}
	
}
