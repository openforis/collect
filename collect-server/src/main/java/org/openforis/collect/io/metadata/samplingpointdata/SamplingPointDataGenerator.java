package org.openforis.collect.io.metadata.samplingpointdata;

import static org.openforis.idm.metamodel.SpatialReferenceSystem.LAT_LON_SRS;
import static org.openforis.idm.metamodel.SpatialReferenceSystem.LAT_LON_SRS_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.openforis.collect.metamodel.samplingdesign.SamplingPointGenerationSettings;
import org.openforis.collect.metamodel.samplingdesign.SamplingPointLevelGenerationSettings;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.idm.geospatial.CoordinateUtils;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;

/**
 * 
 * @author S. Ricci
 * @author G. Johnson
 *
 */
public class SamplingPointDataGenerator {

	public static final SpatialReferenceSystem WEB_MARCATOR_SRS = new SpatialReferenceSystem(
			"EPSG:3857",
			"PROJCS[\"WGS 84 / Pseudo-Mercator\"," +
			"       GEOGCS[\"WGS 84\"," +
			"           DATUM[\"WGS_1984\"," +
			"               SPHEROID[\"WGS 84\",6378137,298.257223563," +
			"                   AUTHORITY[\"EPSG\",\"7030\"]]," +
			"               AUTHORITY[\"EPSG\",\"6326\"]]," +
			"           PRIMEM[\"Greenwich\",0," +
			"               AUTHORITY[\"EPSG\",\"8901\"]]," +
			"           UNIT[\"degree\",0.0174532925199433," +
			"               AUTHORITY[\"EPSG\",\"9122\"]]," +
			"           AUTHORITY[\"EPSG\",\"4326\"]]," +
			"       PROJECTION[\"Mercator_1SP\"]," +
			"       PARAMETER[\"central_meridian\",0]," +
			"       PARAMETER[\"scale_factor\",1]," +
			"       PARAMETER[\"false_easting\",0]," +
			"       PARAMETER[\"false_northing\",0]," +
			"       UNIT[\"metre\",1," +
			"           AUTHORITY[\"EPSG\",\"9001\"]]," +
			"       AXIS[\"X\",EAST]," +
			"       AXIS[\"Y\",NORTH]," +
			"       EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs\"]," +
			"       AUTHORITY[\"EPSG\",\"3857\"]]",
			"WGS84 web mercator"
			);
	
	private CollectSurvey survey;
	private SamplingPointGenerationSettings configuration;
	
	public SamplingPointDataGenerator(CollectSurvey survey, SamplingPointGenerationSettings configuration) {
		super();
		this.survey = survey;
		this.configuration = configuration;
	}

	public List<SamplingDesignItem> generate() {
		Coordinate latLonAoiCenter = calculateAoiCenter();
		
		return generateItems(0, Collections.<String>emptyList(), latLonAoiCenter);
	}

	private List<SamplingDesignItem> generateItems(int levelIdx, List<String> previousLevelKeys, Coordinate latLonAoiCenter) {
		List<SamplingDesignItem> items = new ArrayList<SamplingDesignItem>();
		Coordinate reprojectedAoiCenter = reprojectFromLatLonToWebMarcator(latLonAoiCenter);
		double areaWidth = calculateAoiWidth(levelIdx);
		SamplingPointLevelGenerationSettings pointsConfiguration = configuration.getLevelsSettings().get(levelIdx);
		
		List<Coordinate> locations = generateLocations(reprojectedAoiCenter, areaWidth, pointsConfiguration);
		
		for (int locationIdx = 0; locationIdx < locations.size(); locationIdx++) {
			Coordinate webMarcatorCenter = locations.get(locationIdx);

			Coordinate latLonCenter = reprojectFromWebMarcatorToLatLon(webMarcatorCenter);
			
			SamplingDesignItem item = new SamplingDesignItem();
			item.setSrsId(LAT_LON_SRS_ID);
			item.setSurveyId(survey.getId());
			List<String> itemKeys = new ArrayList<String>(previousLevelKeys);
			itemKeys.add(String.valueOf(locationIdx + 1));
			item.setLevelCodes(itemKeys);
			item.setX(latLonCenter.getX());
			item.setY(latLonCenter.getY());
			items.add(item);
			
			if (levelIdx < configuration.getLevelsSettings().size() - 1) {
				items.addAll(generateItems(levelIdx + 1, itemKeys, latLonCenter));
			}
		}
		return items;
	}

	public Coordinate calculateAoiCenter() {
		List<Coordinate> aoiBoundary = configuration.getAoiBoundary();
		
		double[] latitudes = new double[aoiBoundary.size()];
		double[] longitudes = new double[aoiBoundary.size()];
		
		for (int i = 0; i < aoiBoundary.size(); i++) {
			Coordinate coord = aoiBoundary.get(i);
			latitudes[i] = coord.getY();
			longitudes[i] = coord.getX();
		}
		double minBoundaryLatitude = NumberUtils.min(latitudes);
		double maxBoundaryLatitude = NumberUtils.max(latitudes);
		double minBoundaryLongitude = NumberUtils.min(longitudes);
		double maxBoundaryLongitude = NumberUtils.max(longitudes);
		
		return new Coordinate(minBoundaryLongitude + (maxBoundaryLongitude - minBoundaryLongitude) / 2, 
				minBoundaryLatitude + (maxBoundaryLatitude - minBoundaryLatitude) / 2, LAT_LON_SRS_ID);
	}

	private double calculateAoiWidth(int levelIdx) {
		switch(levelIdx) {
		case 0:
			List<Coordinate> aoiBoundary = configuration.getAoiBoundary();
			List<Coordinate> reprojectedAoiBoundary = new ArrayList<Coordinate>(aoiBoundary.size());
			
			for (Coordinate coordinate : aoiBoundary) {
				reprojectedAoiBoundary.add(reprojectFromLatLonToWebMarcator(coordinate));
			}
			
			double[] longitudes = new double[reprojectedAoiBoundary.size()];
			
			for (int i = 0; i < reprojectedAoiBoundary.size(); i++) {
				Coordinate coord = reprojectedAoiBoundary.get(i);
				longitudes[i] = coord.getX();
			}
			double minBoundaryLongitude = NumberUtils.min(longitudes);
			double maxBoundaryLongitude = NumberUtils.max(longitudes);
			
			return maxBoundaryLongitude - minBoundaryLongitude;
		default:
			SamplingPointLevelGenerationSettings previousLevelConfiguration = configuration.getLevelsSettings().get(levelIdx - 1);
			return previousLevelConfiguration.getPointWidth();
		}
	}

	private List<Coordinate> generateLocations(Coordinate center, double areaWidth, SamplingPointLevelGenerationSettings c) {
		switch(c.getShape()) {
		case CIRCLE:
			return generateLocationsInCircle(center, areaWidth / 2, c);
		default:
			throw new IllegalArgumentException("Shape type not supported: " + c.getShape());
		}
	}
	
	private List<Coordinate> generateLocationsInCircle(Coordinate center, double circleRadius, SamplingPointLevelGenerationSettings c) {
		List<Coordinate> result = new ArrayList<Coordinate>(c.getNumPoints());
		double radiusSquared = circleRadius * circleRadius;
		double locationRadius = c.getPointWidth() / 2;
		switch(c.getDistribution()) {
		case RANDOM:
			for (int i = 0; i < c.getNumPoints(); i++) {
				double offsetAngle = Math.random() * Math.PI * 2;
				double offsetMagnitude = Math.random() * circleRadius;
				double xOffset = offsetMagnitude * Math.cos(offsetAngle);
				double yOffset = offsetMagnitude * Math.sin(offsetAngle);
				double x = center.getX() + xOffset;
				double y = center.getY() + yOffset;
				result.add(new Coordinate(x, y,	center.getSrsId()));
			}
			break;
		case GRIDDED:
			double left = center.getX() - circleRadius;
			double top = center.getY() - circleRadius;
			double numberOfSteps = Math.floor(2 * circleRadius / c.getResolution());
			for (int xStep = 0; xStep < numberOfSteps; xStep++) {
				double x = left + xStep * c.getResolution() + locationRadius;
				for (int yStep = 0; yStep < numberOfSteps; yStep++) {
					double y = top + yStep * c.getResolution() + locationRadius;
					if (squareDistance(x, y, center.getX(), center.getY()) < radiusSquared) {
						result.add(new Coordinate(x, y,	center.getSrsId()));
					}
				}
			}
		}
		return result;
	}
	
	private double squareDistance(double x1, double y1, double x2, double y2) {
		return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
	}
	
	private Coordinate reprojectFromLatLonToWebMarcator(Coordinate coordinate) {
		double[] reprojectedPoint = CoordinateUtils.transform(SpatialReferenceSystem.LAT_LON_SRS, 
				new double[]{coordinate.getX(), coordinate.getY()}, WEB_MARCATOR_SRS);
		return new Coordinate(reprojectedPoint[0], reprojectedPoint[1], WEB_MARCATOR_SRS.getId());
	}
	
	private Coordinate reprojectFromWebMarcatorToLatLon(Coordinate coordinate) {
		double[] reprojectedPoint = CoordinateUtils.transform(WEB_MARCATOR_SRS, 
				new double[]{coordinate.getX(), coordinate.getY()}, LAT_LON_SRS);
		return new Coordinate(reprojectedPoint[0], reprojectedPoint[1], LAT_LON_SRS.getId());
	}
	
}
