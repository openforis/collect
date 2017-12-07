package org.openforis.collect.io.metadata.samplingpointdata;

import static org.openforis.idm.metamodel.SpatialReferenceSystem.LAT_LON_SRS_ID;
import static org.openforis.idm.metamodel.SpatialReferenceSystem.WEB_MERCATOR_SRS_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openforis.collect.metamodel.samplingdesign.SamplingPointGenerationSettings;
import org.openforis.collect.metamodel.samplingdesign.SamplingPointLevelGenerationSettings;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.model.Coordinate;

/**
 * 
 * @author S. Ricci
 * @author G. Johnson
 *
 */
public class SamplingPointDataGenerator {

	private CollectSurvey survey;
	private List<List<SamplingDesignItem>> samplingPointsByLevel;
	private SamplingPointGenerationSettings configuration;
	private CoordinateOperations coordinateOperations;
	
	public SamplingPointDataGenerator(CoordinateOperations coordinateOperations, CollectSurvey survey, List<List<SamplingDesignItem>> samplingPointsByLevel,
			SamplingPointGenerationSettings configuration) {
		super();
		this.coordinateOperations = coordinateOperations;
		this.survey = survey;
		this.samplingPointsByLevel = samplingPointsByLevel;
		this.configuration = configuration;
	}

	public List<SamplingDesignItem> generate() {
		Coordinate latLonAoiCenter = calculateAoiCenter();
		
		return generateItems(0, Collections.<String>emptyList(), latLonAoiCenter);
	}

	private List<SamplingDesignItem> generateItems(int levelIdx, List<String> previousLevelKeys, Coordinate latLonAoiCenter) {
		if (samplingPointsByLevel != null && samplingPointsByLevel.size() > levelIdx &&
				CollectionUtils.isNotEmpty(samplingPointsByLevel.get(levelIdx))) {
			List<SamplingDesignItem> items = new ArrayList<SamplingDesignItem>();
			List<SamplingDesignItem> itemsInLevel = samplingPointsByLevel.get(levelIdx);
			items.addAll(itemsInLevel);
			for (SamplingDesignItem item : itemsInLevel) {
				item.setSurveyId(survey.getId());
				if (levelIdx < configuration.getLevelsSettings().size() - 1) {
					List<String> itemKeys = item.getLevelCodes();
					items.addAll(generateItems(levelIdx + 1, itemKeys, item.getCoordinate()));
				}
			}
			return items;
		} else {
			List<SamplingDesignItem> items = new ArrayList<SamplingDesignItem>();
			List<Coordinate> locations = generateLocations(latLonAoiCenter, levelIdx);
			
			for (int locationIdx = 0; locationIdx < locations.size(); locationIdx++) {
				Coordinate webMercatorCenter = locations.get(locationIdx);
	
				Coordinate latLonCenter = reprojectFromWebMercatorToLatLon(webMercatorCenter);
				
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
			List<Coordinate> reprojectedAoiBoundary = reprojectToWebMercator(aoiBoundary);
			double[] longitudes = new double[reprojectedAoiBoundary.size()];
			for (int i = 0; i < reprojectedAoiBoundary.size(); i++) {
				Coordinate c = reprojectedAoiBoundary.get(i);
				longitudes[i] = c.getX();
			}
			double minBoundaryLongitude = NumberUtils.min(longitudes);
			double maxBoundaryLongitude = NumberUtils.max(longitudes);
			
			return maxBoundaryLongitude - minBoundaryLongitude;
		default:
			SamplingPointLevelGenerationSettings previousLevelConfiguration = configuration.getLevelsSettings().get(levelIdx - 1);
			return previousLevelConfiguration.getPointWidth();
		}
	}

	private List<Coordinate> generateLocations(Coordinate latLonCenter, int levelIdx) {
		SamplingPointLevelGenerationSettings pointsConfiguration = configuration.getLevelsSettings().get(levelIdx);
		Coordinate webMercatorAoiCenter = reprojectFromLatLonToWebMercator(latLonCenter);
		switch(pointsConfiguration.getShape()) {
		case CIRCLE:
			double areaWidth = calculateAoiWidth(levelIdx);
			return generateLocationsInCircle(webMercatorAoiCenter, areaWidth / 2, pointsConfiguration);
		case SQUARE:
			SamplingPointLevelGenerationSettings nextLevelPointsConfiguration = levelIdx == configuration.getLevelsSettings().size() - 1 ? null :
				configuration.getLevelsSettings().get(levelIdx + 1);
			double pointWidth = nextLevelPointsConfiguration.getPointWidth();
			return generateLocationsInSquare(webMercatorAoiCenter, configuration.getAoiBoundary(), 
					pointsConfiguration.getResolution(), pointWidth);
		default:
			throw new IllegalArgumentException("Shape type not supported: " + pointsConfiguration.getShape());
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
			break;
		case CSV:
			//do nothing
			break;
		}
		return result;
	}
	
	private List<Coordinate> generateLocationsInSquare(Coordinate webMercatorAoiCenter, List<Coordinate> aoiBoundary,
			double resolution, double pointSize) {
		List<Coordinate> result = new ArrayList<Coordinate>();
		
		List<Coordinate> reprojectedAoiBoundary = reprojectToWebMercator(aoiBoundary);
		double[] latitudes = new double[reprojectedAoiBoundary.size()];
		double[] longitudes = new double[reprojectedAoiBoundary.size()];
		
		for (int i = 0; i < reprojectedAoiBoundary.size(); i++) {
			Coordinate coord = reprojectedAoiBoundary.get(i);
			latitudes[i] = coord.getY();
			longitudes[i] = coord.getX();
		}
		double pointRadius = pointSize / 2;
		double minBoundaryLatitude = NumberUtils.min(latitudes) + pointRadius;
		double maxBoundaryLatitude = NumberUtils.max(latitudes) - pointRadius;
		double minBoundaryLongitude = NumberUtils.min(longitudes) + pointRadius;
		double maxBoundaryLongitude = NumberUtils.max(longitudes) - pointRadius;
		
		for (double lat = maxBoundaryLatitude; lat >= minBoundaryLatitude; lat -= resolution) {
			for (double lon = minBoundaryLongitude; lon <= maxBoundaryLongitude; lon += resolution) {
				result.add(new Coordinate(lon, lat, webMercatorAoiCenter.getSrsId()));
			}
		}
		return result;
	}

	private double squareDistance(double x1, double y1, double x2, double y2) {
		return Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
	}
	
	private List<Coordinate> reprojectToWebMercator(List<Coordinate> aoiBoundary) {
		List<Coordinate> reprojectedAoiBoundary = new ArrayList<Coordinate>(aoiBoundary.size());
		for (Coordinate coord : aoiBoundary) {
			Coordinate reprojectedCoord = reprojectFromLatLonToWebMercator(coord);
			reprojectedAoiBoundary.add(reprojectedCoord);
		}
		return reprojectedAoiBoundary;
	}

	private Coordinate reprojectFromLatLonToWebMercator(Coordinate coordinate) {
		return coordinateOperations.convertTo(coordinate, WEB_MERCATOR_SRS_ID);
	}
	
	private Coordinate reprojectFromWebMercatorToLatLon(Coordinate coordinate) {
		return coordinateOperations.convertToWgs84(coordinate);
	}
	
}
