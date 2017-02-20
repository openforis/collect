package org.openforis.idm.geospatial;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;

/**
 * @author Daniel Wiell
 */
public class CoordinateOperations {

	private Map<String, SpatialReferenceSystem> spatialReferenceSystemsById = new ConcurrentHashMap<String, SpatialReferenceSystem>();

	public CoordinateOperations() {
		registerSRS(SpatialReferenceSystem.LAT_LON_SRS);
		registerSRS(SpatialReferenceSystem.WEB_MARCATOR_SRS);
	}
	
	public void registerSrs(List<SpatialReferenceSystem> list) {
		for (SpatialReferenceSystem srs : list)
			registerSRS(srs);
	}

	public void registerSRS(SpatialReferenceSystem srs) {
		if (!spatialReferenceSystemsById.containsKey(srs.getId()))
			spatialReferenceSystemsById.put(srs.getId(), srs);
	}

	public double orthodromicDistance(double startX, double startY,
			String startSRSId, double destX, double destY, String destSRSId) {
		return CoordinateUtils.distance(getSrs(startSRSId), new double[] {
				startX, startY }, getSrs(destSRSId), new double[] { destX, destY });
	}

	public double orthodromicDistance(Coordinate from, Coordinate to) {
		return CoordinateUtils.distance(getSrs(from.getSrsId()),
				toUiCoordinate(from), toSrs(to), toUiCoordinate(to));
	}

	private double[] toUiCoordinate(Coordinate coordinate) {
		return new double[] { coordinate.getX(), coordinate.getY() };
	}

	private SpatialReferenceSystem toSrs(Coordinate coordinate) {
		return spatialReferenceSystemsById.get(coordinate.getSrsId());
	}

	private SpatialReferenceSystem getSrs(String srsId) {
		return spatialReferenceSystemsById.get(srsId);
	}

	public Coordinate convertToWgs84(Coordinate coordinate) {
		SpatialReferenceSystem toSrs = SpatialReferenceSystem.LAT_LON_SRS;
		return convertTo(coordinate, toSrs);
	}

	public Coordinate convertTo(Coordinate coordinate, String toSrsId) {
		SpatialReferenceSystem toSrs = spatialReferenceSystemsById.get(toSrsId);
		return convertTo(coordinate, toSrs);
	}
	
	private Coordinate convertTo(Coordinate coordinate, SpatialReferenceSystem toSrs) {
		SpatialReferenceSystem fromSrs = toSrs(coordinate);
		if (fromSrs.equals(toSrs)) {
			return coordinate;
		} else {
			double[] uiCoordinate = toUiCoordinate(coordinate);
			double[] transformed = CoordinateUtils.transform(fromSrs, uiCoordinate, toSrs);
			return new Coordinate(transformed[0], transformed[1], toSrs.getId());
		}
	}
	
	public boolean validate(Coordinate coordinate) {
		return true; // TODO: Implement validation
	}

	public void validateWKT(String wkt) throws Exception {
	}
	
}