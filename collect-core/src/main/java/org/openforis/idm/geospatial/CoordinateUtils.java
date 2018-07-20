package org.openforis.idm.geospatial;

import static org.openforis.idm.metamodel.SpatialReferenceSystem.LAT_LON_SRS;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.openforis.idm.metamodel.SpatialReferenceSystem;

/**
 * @author Daniel Wiell
 */
public class CoordinateUtils {
	
	/**
     * The Authalic mean radius (A<subscript>r</subscript>) of the earth
     * [6371.0072 km] (see <a
     * href="http://en.wikipedia.org/wiki/Earth_radius#Authalic_radius"
     * target="_blank">Wikipedia</a>).
     */
    private static final double EARTH_RADIUS_MEAN = 6371.0072;

    /**
     * The equatorial radius of the earth [6378.1370 km] (see <a
     * href="http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius"
     * target="_blank">Wikipedia</a>) as derived from the WGS-84 ellipsoid.
     */
    private static final double EARTH_RADIUS_EQUATORIAL = 6378.1370;

    /**
     * The polar radius of the earth [6356.7523 km] (see <a
     * href="http://en.wikipedia.org/wiki/Earth_radius#Polar_radius"
     * target="_blank">Wikipedia</a>) as derived from the WGS-84 ellipsoid.
     */
    private static final double EARTH_RADIUS_POLAR = 6356.7523;
    
    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static Map<String, GeodeticCRS> crsCache = new ConcurrentHashMap<String, GeodeticCRS>();
    private static Map<String, List<CoordinateOperation>> operationCache = new ConcurrentHashMap<String, List<CoordinateOperation>>();

    public static double[] transform(SpatialReferenceSystem from, double[] coord, SpatialReferenceSystem to) {
        if (from.equals(LAT_LON_SRS)) {
            return doTransform(from, coord, to);
        } else {
	        double[] latLonCoord = doTransform(from, coord, LAT_LON_SRS);
	        return doTransform(LAT_LON_SRS, latLonCoord, to);
        }
    }

    private static double[] doTransform(SpatialReferenceSystem from, double[] coord, SpatialReferenceSystem to) {
        List<CoordinateOperation> coordOps = coordinateOperations(from, to);
        double[] result = Arrays.copyOf(coord, coord.length); // Need to copy since operations might change array in place
        if (!coordOps.isEmpty()) {
            for (CoordinateOperation op : coordOps) {
                try {
                    result = op.transform(result); // This might change the array in-place
                } catch (IllegalCoordinateException e) {
                    throw new IllegalStateException("Failed to transform " + Arrays.asList(coord) + " from" + from + " to " + to, e);
                }
    		}
    	}
        return result;
    }

    private static List<CoordinateOperation> coordinateOperations(SpatialReferenceSystem from, SpatialReferenceSystem to) {
        String key = operationCacheKey(from, to);
        List<CoordinateOperation> coordOps = operationCache.get(key);
        if (coordOps == null) {
	        coordOps = CoordinateOperationFactory.createCoordinateOperations(crs(from), crs(to));
	        operationCache.put(key, coordOps);
        }
        return coordOps;
    }

    private static String operationCacheKey(SpatialReferenceSystem from, SpatialReferenceSystem to) {
        return from.getId() + "|" + to.getId();
    }

    private static GeodeticCRS crs(SpatialReferenceSystem srs) {
        GeodeticCRS crs = crsCache.get(srs.getId());
        if (crs != null)
            return crsCache.get(srs.getId());
        try {
            crs = (GeodeticCRS) CRS_FACTORY.createFromPrj(srs.getWellKnownText().trim());
            crsCache.put(srs.getId(), crs);
            return crs;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create projection for Spacial Reference System " + srs, e);
        }
    }

    public static double distance(SpatialReferenceSystem fromSrs, double[] from, SpatialReferenceSystem toSrs, double[] to) {
        double[] fromLatLng = transform(fromSrs, from, LAT_LON_SRS);
        double[] toLatLng = transform(toSrs, to, LAT_LON_SRS);

        double lat1 = fromLatLng[1];
        double lon1 = fromLatLng[0];
        double lat2 = toLatLng[1];
        double lon2 = toLatLng[0];

    	double avgLat = (lat1 + lat2) / 2;
        final double R = radiusAtLocation(deg2rad(avgLat)); // Radius of the earth

        Double latDistance = deg2rad(lat2 - lat1);
        Double lonDistance = deg2rad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters
    }

    public static double bearing(SpatialReferenceSystem fromSrs, double[] from, SpatialReferenceSystem toSrs, double[] to) {
        double[] fromLatLng = transform(fromSrs, from, LAT_LON_SRS);
        double[] toLatLng = transform(toSrs, to, LAT_LON_SRS);

        double longitude1 = fromLatLng[0];
        double longitude2 = toLatLng[0];
        double latitude1 = Math.toRadians(fromLatLng[1]);
        double latitude2 = Math.toRadians(toLatLng[1]);
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    
    private static double radiusAtLocation(double latRad) {
        double cosL = Math.cos(latRad);
        double sinL = Math.sin(latRad);
        double C1 = cosL * EARTH_RADIUS_EQUATORIAL;
        double C2 = C1 * EARTH_RADIUS_EQUATORIAL;
        double C3 = sinL * EARTH_RADIUS_POLAR;
        double C4 = C3 * EARTH_RADIUS_POLAR;
        return Math.sqrt((C2 * C2 + C4 * C4) / (C1 * C1 + C3 * C3));
    }
}