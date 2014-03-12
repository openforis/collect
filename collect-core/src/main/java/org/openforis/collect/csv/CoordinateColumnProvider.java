/**
 * 
 */
package org.openforis.collect.csv;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CoordinateAttribute;

/**
 * @author M. Togna
 * 
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class CoordinateColumnProvider extends CompositeAttributeColumnProvider<CoordinateAttributeDefinition> {

	private static final String KML_POINT_FORMAT = "<Point><coordinates>%f,%f,0.0</coordinates></Point>";
	private static final String KML_FIELD_NAME = "kml";
	
	private static final String KML_COLUMN_SUFFIX = "kml";

	private boolean includeKMLColumn;
	private String kmlFormat;
	
	public CoordinateColumnProvider(CoordinateAttributeDefinition defn) {
		this(defn, false);
	}

	public CoordinateColumnProvider(CoordinateAttributeDefinition defn, boolean includeKMLColumn) {
		this(defn, includeKMLColumn, KML_POINT_FORMAT);
	}
	
	public CoordinateColumnProvider(CoordinateAttributeDefinition defn, boolean includeKMLColumn, String kmlFormat) {
		super(defn);
		this.includeKMLColumn = includeKMLColumn;
		this.kmlFormat = kmlFormat;
	}
	
	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		result.add(CoordinateAttributeDefinition.SRS_FIELD_NAME); 
		result.add(CoordinateAttributeDefinition.X_FIELD_NAME);
		result.add(CoordinateAttributeDefinition.Y_FIELD_NAME);
		if ( includeKMLColumn ) {
			result.add(KML_FIELD_NAME);
		}
		return result.toArray(new String[]{});
	}

	@Override
	protected String getFieldHeading(String fieldName) {
		if ( KML_FIELD_NAME.equals(fieldName) ) {
			return "_" + getAttributeName() + FIELD_SEPARATOR + KML_COLUMN_SUFFIX;
		} else {
			return super.getFieldHeading(fieldName);
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
