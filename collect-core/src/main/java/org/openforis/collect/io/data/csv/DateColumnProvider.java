/**
 * 
 */
package org.openforis.collect.io.data.csv;

import org.openforis.collect.io.data.csv.Column.DataType;
import org.openforis.idm.metamodel.DateAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class DateColumnProvider extends CompositeAttributeColumnProvider<DateAttributeDefinition> {
	
	public DateColumnProvider(CSVDataExportParameters config, DateAttributeDefinition defn) {
		super(config, defn);
	}

	@Override
	protected String[] getFieldNames() {
		return new String[] {
				DateAttributeDefinition.YEAR_FIELD_NAME,
				DateAttributeDefinition.MONTH_FIELD_NAME,
				DateAttributeDefinition.DAY_FIELD_NAME
		};
	}
	
	@Override
	protected Column generateFieldColumn(String fieldName, String suffix) {
		Column column = super.generateFieldColumn(fieldName, suffix);
		column.setDataType(DataType.INTEGER);
		return column;
	}

}
