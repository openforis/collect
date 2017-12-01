/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

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
	
	public CoordinateColumnProvider(CSVDataExportParameters config,CoordinateAttributeDefinition defn) {
		this(config, defn, KML_POINT_FORMAT);
	}
	
	public CoordinateColumnProvider(CSVDataExportParameters config,CoordinateAttributeDefinition defn, String kmlFormat) {
		super(config, defn);
		this.kmlFormat = kmlFormat;
	}
	
	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		result.add(CoordinateAttributeDefinition.SRS_FIELD_NAME); 
		result.add(CoordinateAttributeDefinition.X_FIELD_NAME);
		result.add(CoordinateAttributeDefinition.Y_FIELD_NAME);
		if ( getConfig().isIncludeKMLColumnForCoordinates() ) {
			result.add(KML_FIELD_NAME);
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	protected String generateFieldHeading(String fieldName) {
		if ( KML_FIELD_NAME.equals(fieldName) ) {
			return "_" + ColumnProviders.generateHeadingPrefix(attributeDefinition, config) + 
					getConfig().getFieldHeadingSeparator() + KML_COLUMN_SUFFIX;
		} else {
			return super.generateFieldHeading(fieldName);
		}
	}
	
	@Override
	protected String extractValue(Attribute<?, ?> attr, String fieldName) {
		if ( KML_FIELD_NAME.equals(fieldName) ) {
			if ( attr.isEmpty() ) {
				return "";
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
