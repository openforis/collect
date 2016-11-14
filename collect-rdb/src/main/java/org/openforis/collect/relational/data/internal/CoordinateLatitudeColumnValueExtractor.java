package org.openforis.collect.relational.data.internal;

import org.openforis.collect.relational.model.CoordinateLatitudeColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public class CoordinateLatitudeColumnValueExtractor extends DataTableDataColumnValueExtractor {

	public CoordinateLatitudeColumnValueExtractor(DataTable table, CoordinateLatitudeColumn column) {
		super(table, column);
	}
	
	@Override
	public Object extractValue(Node<?> context) {
		Node<?> valNode = super.extractValueNode(context);
		if ( valNode != null && valNode instanceof CoordinateAttribute ) {
			Coordinate coordinate = ((CoordinateAttribute) valNode).getValue();
			Coordinate latLongCoordinate = valNode.getSurveyContext().getCoordinateOperations().convertToWgs84(coordinate);
			return latLongCoordinate.getY();
		} else {
			return null;
		}
	}
	
}
