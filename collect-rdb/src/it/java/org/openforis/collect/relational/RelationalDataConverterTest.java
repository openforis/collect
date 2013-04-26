package org.openforis.collect.relational;

import java.util.List;

import org.junit.Test;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.relational.model.Dataset;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.model.Table;

/**
 * 
 * @author G. Miceli
 *
 */
public class RelationalDataConverterTest extends CollectRelationalTest {

	@Test
	public void testGenerator() throws Exception {
		RelationalSchemaGenerator rsg = new RelationalSchemaGenerator();
		RelationalSchema rs = rsg.generateSchema(survey, "archenland1");
		List<Table<?>> tables = rs.getTables();
		
		CollectRecord record = createTestRecord(survey, "123_456");
		Dataset data = rs.createDataset(record);
		data.print(System.out);
		// TODO proper integration test
	}

}
