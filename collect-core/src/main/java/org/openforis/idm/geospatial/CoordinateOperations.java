/**
 * 
 */
package org.openforis.idm.geospatial;

import java.util.List;
import java.util.Set;

import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;

/**
 * @author S. Ricci
 *
 */
public interface CoordinateOperations {

	void validateWKT(String wkt) throws Exception;
	
	void parseSRS(List<SpatialReferenceSystem> srss);

	void parseSRS(SpatialReferenceSystem srs);

	boolean validate(Coordinate coordinate);
	
	double orthodromicDistance(double startX, double startY, String startSRSId,
			double destX, double destY, String destSRSId);

	double orthodromicDistance(Coordinate from, Coordinate to);
	
	SpatialReferenceSystem fetchSRS(String code);

	SpatialReferenceSystem fetchSRS(String code, Set<String> labelLanguages);
	
	Set<String> getAvailableSRSs();
	
	Coordinate convertToWgs84(Coordinate coordinate);
}