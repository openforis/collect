package org.openforis.collect.relational;

import java.util.List;

import org.junit.Test;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.model.Table;

/**
 * 
 * @author G. Miceli
 *
 */
public class RelationalSchemaGeneratorTest extends CollectRelationalTest {

	@Test
	public void testGenerator() throws Exception {
		RelationalSchemaGenerator rsg = new RelationalSchemaGenerator();
		RelationalSchema rs = rsg.generateSchema(survey, "archenland1");
		List<Table<?>> tables = rs.getTables();
		// Debug
		for (Table<?> table : tables) {
			DataTable t = (DataTable) table;
			t.print(System.out);
		}
		
		// TODO proper integration test
	}

}
