/**
 * 
 */
package org.openforis.collect.geospatial;


import static org.geotools.referencing.CRS.parseWKT;
import static org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.AbstractSingleCRS;
import org.openforis.idm.geospatial.CoordinateOperationException;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.coordinate.Position;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.GenericName;

/**
 * @author M. Togna
 * 
 */
public class GeoToolsCoordinateOperations implements CoordinateOperations {

	private static final String WGS84_ID = "EPSG:4326";

	private static final Log LOG = LogFactory.getLog(GeoToolsCoordinateOperations.class);

	// private static CoordinateOperationFactory CO_FACTORY;
	// private static Map<String, CoordinateReferenceSystem> SYSTEMS;
	private static Map<String, MathTransform> TO_WGS84_TRANSFORMS;

	static {
		init();
	}

	private static void init() {
		try {
			// SYSTEMS = new HashMap<String, CoordinateReferenceSystem>();
			TO_WGS84_TRANSFORMS = new HashMap<String, MathTransform>();

			MathTransform wgs84Transform = CRS.findMathTransform(WGS84, WGS84);
			TO_WGS84_TRANSFORMS.put(WGS84_ID, wgs84Transform);
		} catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error while initializing CoordinateOperations", e);
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the orthodromic distance between two points
	 * 
	 * @param startingPosition
	 * @param destinationPosition
	 * @return
	 * @throws TransformException
	 */
	public double orthodromicDistance(Position startingPosition, Position destinationPosition) throws CoordinateOperationException {
		try {
			GeodeticCalculator calculator = new GeodeticCalculator();
			calculator.setStartingPosition(startingPosition);
			calculator.setDestinationPosition(destinationPosition);
			double result = calculator.getOrthodromicDistance();
			return result;
		} catch (Exception e) {
			throw new CoordinateOperationException("Failed to determine distance from " + startingPosition + " to " + destinationPosition, e);
		}
	}
	
	@Override
	public boolean validate(Coordinate coordinate) {
		try {
			double x = coordinate.getX();
			double y = coordinate.getY();
			String srsId = coordinate.getSrsId();
			Position position = toWgs84(x, y, srsId);
			GeodeticCalculator calculator = new GeodeticCalculator();
			//this will call methods checkLatidude and checkLongitude inside of GeodeticCalculator
			calculator.setStartingPosition(position);
			return true;
		} catch ( Exception e ) {
			return false;
		}
	}

	@Override
	public void validateWKT(String wkt) throws Exception {
		parseWKT(wkt);
	}
	
	@Override
	public double orthodromicDistance(double startX, double startY, String startSRSId, double destX, double destY, String destSRSId) throws CoordinateOperationException {
		Position startingPosition = toWgs84(startX, startY, startSRSId);
		Position destinationPosition = toWgs84(destX, destY, destSRSId);
		return orthodromicDistance(startingPosition, destinationPosition);
	}

	@Override
	public double orthodromicDistance(Coordinate startingCoordinate, Coordinate destinationCoordinate) throws CoordinateOperationException {
		double startX = startingCoordinate.getX();
		double startY = startingCoordinate.getY();
		String startSRSId = startingCoordinate.getSrsId();

		double destX = destinationCoordinate.getX();
		double destY = destinationCoordinate.getY();
		String destSRSId = destinationCoordinate.getSrsId();

		return orthodromicDistance(startX, startY, startSRSId, destX, destY, destSRSId);
	}

	@Override
	public SpatialReferenceSystem fetchSRS(String code) {
		return fetchSRS(code, new HashSet<String>(Arrays.asList("en")));
	}

	@Override
	public SpatialReferenceSystem fetchSRS(String code, Set<String> labelLanguages) {
		try {
			CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);
			CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem(code);
			SpatialReferenceSystem result = new SpatialReferenceSystem();
			result.setId(code);
			result.setWellKnownText(crs.toWKT());
			String description = getDescription(crs);
			for (String lang : labelLanguages) {
				result.setLabel(lang, code);
				result.setDescription(lang, description);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Error fetching SRS with code: " + code, e);
		}
	}
	
	/**
	 * It returns a concatenation of datum, aliases and scope
	 */
	private String getDescription(CoordinateReferenceSystem crs) {
		List<String> parts = new ArrayList<String>();
		//datum
		if ( crs instanceof AbstractSingleCRS ) {
			Datum datum = ((AbstractSingleCRS) crs).getDatum();
			String datumName = datum.getName().toString();
			parts.add(datumName);
		}
		//aliases
		for (GenericName genericName : crs.getAlias()) {
			parts.add(genericName.toString());
		}
		//scope
		String scope = crs.getScope().toString();
		if ( StringUtils.isNotBlank(scope) ) {
			parts.add(scope);
		}
		String result = StringUtils.join(parts, "\n");
		return result;
	}
	
	@Override
	public Set<String> getAvailableSRSs() {
		Set<String> result = new HashSet<String>();
		String authorityCode = "EPSG";
		String codePrefix = authorityCode + ":";
		Set<String> supportedCodes = CRS.getSupportedCodes(authorityCode);
		for (String code : supportedCodes) {
			if ( code.startsWith(codePrefix) ) {
				result.add(code);
			} else {
				result.add(codePrefix + code);
			}
		}
		return result;
	}
	
	@Override
	public void parseSRS(List<SpatialReferenceSystem> srss) {
		for (SpatialReferenceSystem srs : srss) {
			parseSRS(srs);
		}
	}
	
	@Override
	public void parseSRS(SpatialReferenceSystem srs) {
		String srsId = srs.getId();
		MathTransform transform = TO_WGS84_TRANSFORMS.get(srsId);
		if (transform == null) {
			String wkt = srs.getWellKnownText();
			try {
				transform = findMathTransform(wkt);
				TO_WGS84_TRANSFORMS.put(srsId, transform);
			} catch (Exception e) {
				//TODO throw exception
				//throw new CoordinateOperationException(String.format("Error parsing SpatialRefernceSystem with id %s and Well Known Text %s", srsId, wkt), e);
				if (LOG.isErrorEnabled()) {
					LOG.error(String.format("Error parsing SpatialRefernceSystem with id %s and Well Known Text %s", srsId, wkt), e);
				}
			}
		}
	}

	private static Position toWgs84(double x, double y, String srsId) {
		try {
			DirectPosition src = new DirectPosition2D(x, y);
			MathTransform transform = TO_WGS84_TRANSFORMS.get(srsId);
			if (transform == null) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Unknown CRS: " + srsId);
				}
				return new DirectPosition2D(0, 0);
			}

			DirectPosition directPosition = transform.transform(src, null);
			return directPosition;
		} catch (Throwable t) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error converting: x=" + x + " y=" + y + " srs=" + srsId, t);
				throw new RuntimeException(t);
			}
			return new DirectPosition2D(0, 0);
		}
	}

	private static MathTransform findMathTransform(String wkt) throws Exception {
		try {
			CoordinateReferenceSystem crs = parseWKT(wkt);
			MathTransform mathTransform = CRS.findMathTransform(crs, WGS84,true);
			return mathTransform;
		} catch (Exception t) {
			throw t;
		}
	}

}