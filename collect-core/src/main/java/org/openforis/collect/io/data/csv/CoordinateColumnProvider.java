/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.io.data.csv.Column.DataType;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CoordinateAttribute;

/**
 * @author M. Togna
 * 
 */
public class CoordinateColumnProvider extends CompositeAttributeColumnProvider<CoordinateAttributeDefinition> {

	private static final String KML_POINT_FORMAT = "<Point><coordinates>%f,%f,0.0</coordinates></Point>";
	private static final String KML_FIELD_NAME = "kml";
	private static final String KML_COLUMN_SUFFIX = "kml";

	private String kmlFormat;
	
	public CoordinateColumnProvider(CSVDataExportParameters config, CoordinateAttributeDefinition defn) {
		this(config, defn, KML_POINT_FORMAT);
	}
	
	public CoordinateColumnProvider(CSVDataExportParameters config, CoordinateAttributeDefinition defn, String kmlFormat) {
		super(config, defn);
		this.kmlFormat = kmlFormat;
	}
	
	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		result.add(CoordinateAttributeDefinition.SRS_FIELD_NAME); 
		result.add(CoordinateAttributeDefinition.X_FIELD_NAME);
		result.add(CoordinateAttributeDefinition.Y_FIELD_NAME);
		CollectAnnotations annotations = attributeDefinition.<CollectSurvey>getSurvey().getAnnotations();
		if (annotations.isIncludeCoordinateAccuracy(attributeDefinition)) {
			result.add(CoordinateAttributeDefinition.ACCURACY_FIELD_NAME);
		}
		if (annotations.isIncludeCoordinateAltitude(attributeDefinition)) {
			result.add(CoordinateAttributeDefinition.ALTITUDE_FIELD_NAME);
		}
		if ( getConfig().isIncludeKMLColumnForCoordinates() ) {
			result.add(KML_FIELD_NAME);
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	protected Column generateFieldColumn(String fieldName, String suffix) {
		if ( KML_FIELD_NAME.equals(fieldName) ) {
			return new Column("_" + ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + 
					getConfig().getFieldHeadingSeparator() + KML_COLUMN_SUFFIX + suffix);
		} else if (Arrays.asList(
					CoordinateAttributeDefinition.X_FIELD_NAME, 
					CoordinateAttributeDefinition.Y_FIELD_NAME,
					CoordinateAttributeDefinition.ALTITUDE_FIELD_NAME,
					CoordinateAttributeDefinition.ACCURACY_FIELD_NAME
				).contains(fieldName)) {
			Column column = super.generateFieldColumn(fieldName, suffix);
			column.setDataType(DataType.DECIMAL);
			return column;
		} else {
			return super.generateFieldColumn(fieldName, suffix);
		}
	}
	
	@Override
	protected Object extractValue(Attribute<?, ?> attr, String fieldName) {
		if ( KML_FIELD_NAME.equals(fieldName) ) {
			if ( attr.isEmpty() ) {
				return null;
			} else {
				CoordinateAttribute coordAttr = (CoordinateAttribute) attr;
				String kml = String.format(kmlFormat, coordAttr.getXField().getValue(), coordAttr.getYField().getValue());
				return kml;
			}
		} else {
			return super.extractValue(attr, fieldName);
		}
	}
	
}
