/**
 * 
 */
package org.openforis.collect.geospatial;


import static org.geotools.referencing.CRS.parseWKT;
import static org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
import static org.openforis.idm.metamodel.SpatialReferenceSystem.WGS84_SRS_ID;

import java.util.ArrayList;
import java.util.Collections;
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
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.coordinate.Position;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;

/**
 * @author M. Togna
 */
public class GeoToolsCoordinateOperations {

	private static final Log LOG = LogFactory.getLog(GeoToolsCoordinateOperations.class);

	// private static CoordinateOperationFactory CO_FACTORY;
	// private static Map<String, CoordinateReferenceSystem> SYSTEMS;
	private static TransformCache transformCache;
	private static Map<String, CoordinateReferenceSystem> CRS_BY_SRS_ID;

	static {
		init();
	}

	private static void init() {
		try {
			// SYSTEMS = new HashMap<String, CoordinateReferenceSystem>();
			transformCache = new TransformCache();
			CRS_BY_SRS_ID = new HashMap<String, CoordinateReferenceSystem>();
			
			CRS_BY_SRS_ID.put(WGS84_SRS_ID, WGS84);
			
			CoordinateReferenceSystem webMercatorCrs = CRS.decode(SpatialReferenceSystem.WEB_MARCATOR_SRS_ID);
			MathTransform webMarcatorToLatLonTransform = CRS.findMathTransform(webMercatorCrs, WGS84, true);
			transformCache.put(SpatialReferenceSystem.WEB_MARCATOR_SRS_ID, WGS84_SRS_ID, webMarcatorToLatLonTransform);
			
			MathTransform latLonToWebMarcatorTransform = CRS.findMathTransform(WGS84, webMercatorCrs, true);
			transformCache.put(WGS84_SRS_ID, SpatialReferenceSystem.WEB_MARCATOR_SRS_ID, latLonToWebMarcatorTransform);
			CRS_BY_SRS_ID.put(SpatialReferenceSystem.WEB_MARCATOR_SRS_ID, webMercatorCrs);
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

	public void validateWKT(String wkt) throws Exception {
		parseWKT(wkt);
	}
	
	public double orthodromicDistance(double startX, double startY, String startSRSId, double destX, double destY, String destSRSId) throws CoordinateOperationException {
		Position startingPosition = toWgs84(startX, startY, startSRSId);
		Position destinationPosition = toWgs84(destX, destY, destSRSId);
		return orthodromicDistance(startingPosition, destinationPosition);
	}

	public double orthodromicDistance(Coordinate startingCoordinate, Coordinate destinationCoordinate) throws CoordinateOperationException {
		double startX = startingCoordinate.getX();
		double startY = startingCoordinate.getY();
		String startSRSId = startingCoordinate.getSrsId();

		double destX = destinationCoordinate.getX();
		double destY = destinationCoordinate.getY();
		String destSRSId = destinationCoordinate.getSrsId();

		return orthodromicDistance(startX, startY, startSRSId, destX, destY, destSRSId);
	}

	public SpatialReferenceSystem fetchSRS(String code) {
		return fetchSRS(code, Collections.singleton("en"));
	}

	public SpatialReferenceSystem fetchSRS(String code, Set<String> labelLanguages) {
		try {
			CRSAuthorityFactory factory = CRS.getAuthorityFactory(true);
			CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem(code);
			SpatialReferenceSystem result = new SpatialReferenceSystem(code, crs.toWKT(), null);
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
		InternationalString scope = crs.getScope();
		if ( scope != null && StringUtils.isNotBlank(scope) ) {
			parts.add(scope.toString());
		}
		String result = StringUtils.join(parts, "\n");
		return result;
	}
	
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
	
	/**
	 * @deprecated Use {@linkplain registerSRS} instead
	 * @param srss
	 */
	public void parseSRS(List<SpatialReferenceSystem> srss) {
		registerSRS(srss);
	}
	
	public void registerSRS(List<SpatialReferenceSystem> srss) {
		for (SpatialReferenceSystem srs : srss) {
			registerSRS(srs);
		}
	}
	
	/**
	 * @deprecated Use {@linkplain registerSRS} instead
	 * @param srs
	 */
	public void parseSRS(SpatialReferenceSystem srs) {
		registerSRS(srs);
	}
	
	public static void registerSRS(SpatialReferenceSystem srs) {
		String srsId = srs.getId();
		MathTransform latLonTransform = transformCache.get(SpatialReferenceSystem.WGS84_SRS_ID, srsId);
		if (latLonTransform == null) {
			String wkt = srs.getWellKnownText();
			try {
				CoordinateReferenceSystem crs = parseWKT(wkt);
				latLonTransform = findToWGS84MathTransform(crs);
				transformCache.put(srsId, SpatialReferenceSystem.WGS84_SRS_ID, latLonTransform);
				CRS_BY_SRS_ID.put(srsId, crs);
			} catch (Exception e) {
				//TODO throw exception
				//throw new CoordinateOperationException(String.format("Error parsing SpatialRefernceSystem with id %s and Well Known Text %s", srsId, wkt), e);
				if (LOG.isErrorEnabled()) {
					LOG.error(String.format("Error parsing SpatialRefernceSystem with id %s and Well Known Text %s", srsId, wkt), e);
				}
			}
		}
	}
	
	public Coordinate convertToWgs84(Coordinate coordinate) {
		String toSrsId = WGS84_SRS_ID;
		return convertTo(coordinate, toSrsId);
	}

	public Coordinate convertTo(Coordinate coordinate, String toSrsId) {
		if (toSrsId.equals(coordinate.getSrsId())) {
			return coordinate;
		} else {
			Position position = toWgs84(coordinate.getX(), coordinate.getY(), coordinate.getSrsId());
			DirectPosition directPosition = position.getDirectPosition();
			return new Coordinate(directPosition.getOrdinate(0), directPosition.getOrdinate(1), toSrsId);
		}
	}
	
	public Coordinate convertToWebMarcator(Coordinate coordinate) {
		return convert(coordinate.getX(), coordinate.getY(), coordinate.getSrsId(), SpatialReferenceSystem.WEB_MARCATOR_SRS_ID);
	}
	
	public Coordinate fromLatLonToWebMarcator(double lat, double lon) {
		return convert(lon, lat, SpatialReferenceSystem.WGS84_SRS_ID, SpatialReferenceSystem.WEB_MARCATOR_SRS.getId());
	}

	private Coordinate convert(double x, double y, String fromSrsId, String toSrsId) {
		CoordinateReferenceSystem fromCRS = CRS_BY_SRS_ID.get(fromSrsId);
		try {
			DirectPosition src = new DirectPosition2D(x, y);
			CoordinateReferenceSystem toCrs = CRS_BY_SRS_ID.get(toSrsId);
			MathTransform transform = CRS.findMathTransform(fromCRS, toCrs);
			if (transform == null) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Unknown CRS: " + toSrsId);
				}
				return new Coordinate(0d, 0d, toSrsId);
			}
			DirectPosition directPosition = transform.transform(src, null);
			double[] coord = directPosition.getCoordinate();
			return new Coordinate(coord[0], coord[1], toSrsId);
		} catch (Throwable t) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error converting lat lon to web marcator: lat=" + y + " lon=" + x, t);
			}
			return new Coordinate(0d, 0d, toSrsId);
		}
	}

	private static Position toWgs84(double x, double y, String srsId) {
		try {
			DirectPosition src = new DirectPosition2D(x, y);
			MathTransform transform = transformCache.get(srsId, SpatialReferenceSystem.WGS84_SRS_ID);
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
			}
			return new DirectPosition2D(0, 0);
		}
	}

	private static MathTransform findToWGS84MathTransform(CoordinateReferenceSystem crs) throws FactoryException {
		MathTransform mathTransform = CRS.findMathTransform(crs, WGS84, true);
		return mathTransform;
	}

	private static class TransformCache {
		
		private Map<TransformKey, MathTransform> srsIdToTransforms = new HashMap<TransformKey, MathTransform>();
		
		public MathTransform get(String fromSrsId, String toSrsId) {
			return srsIdToTransforms.get(new TransformKey(fromSrsId, toSrsId));
		}
		
		public void put(String fromSrsId, String toSrsId, MathTransform transform) {
			srsIdToTransforms.put(new TransformKey(fromSrsId, toSrsId), transform);
		}
		
		private static class TransformKey {
			
			private String fromSrsId;
			private String toSrsId;
			
			public TransformKey(String fromSrsId, String toSrsId) {
				super();
				this.fromSrsId = fromSrsId;
				this.toSrsId = toSrsId;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((fromSrsId == null) ? 0 : fromSrsId.hashCode());
				result = prime * result + ((toSrsId == null) ? 0 : toSrsId.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				TransformKey other = (TransformKey) obj;
				if (fromSrsId == null) {
					if (other.fromSrsId != null)
						return false;
				} else if (!fromSrsId.equals(other.fromSrsId))
					return false;
				if (toSrsId == null) {
					if (other.toSrsId != null)
						return false;
				} else if (!toSrsId.equals(other.toSrsId))
					return false;
				return true;
			}
			
		}
		
	}
	
}