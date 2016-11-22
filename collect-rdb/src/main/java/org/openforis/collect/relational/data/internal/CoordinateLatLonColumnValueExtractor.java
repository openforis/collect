package org.openforis.collect.relational.data.internal;

import org.openforis.collect.relational.model.CoordinateLatLonColumn;
import org.openforis.collect.relational.model.CoordinateLatitudeColumn;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Node;

public class CoordinateLatLonColumnValueExtractor extends DataTableDataColumnValueExtractor<CoordinateLatLonColumn> {

	public CoordinateLatLonColumnValueExtractor(DataTable table, CoordinateLatLonColumn column) {
		super(table, column);
	}
	
	@Override
	public Object extractValue(Node<?> context) {
		Node<?> valNode = super.extractValueNode(context);
		if ( valNode == null || ! (valNode instanceof CoordinateAttribute)) {
			return null;
		} else {
			Coordinate coordinate = ((CoordinateAttribute) valNode).getValue();
			if (coordinate == null || ! coordinate.isComplete()) {
				return null;
			} else {
				Coordinate latLongCoordinate = valNode.getSurveyContext().getCoordinateOperations().convertToWgs84(coordinate);
				if (column instanceof CoordinateLatitudeColumn) {
					return latLongCoordinate.getY();
				} else {
					return latLongCoordinate.getX();
				}
			}
		}
	}
	
}
