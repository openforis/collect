package org.openforis.collect.io.metadata.samplingpointdata;

import static org.openforis.idm.metamodel.SpatialReferenceSystem.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView.Distribution;
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
					
			
	public List<SamplingDesignItem> generate(
			double boundaryLonMin, double boundaryLonMax,
			double boundaryLatMin, double boundaryLatMax, 
			int numPlots, Distribution plotDistribution, double plotResolution, double plotWidth,
			int samplesPerPlot, Distribution sampleDistribution, double sampleResolution, double sampleWidth) {
		
		Coordinate latLonAoiCenter = new Coordinate(boundaryLonMin + (boundaryLonMax - boundaryLonMin) / 2, 
				boundaryLatMin + (boundaryLatMax - boundaryLatMin) / 2, WGS84_SRS_ID);
		
		Coordinate reprojectedAoiCenter = reprojectFromLatLonToWebMarcator(latLonAoiCenter);
		
		List<SamplingDesignItem> items = new ArrayList<SamplingDesignItem>(numPlots + numPlots * samplesPerPlot);
		
		List<Coordinate> plotLocations = generateLocationsInCircle(reprojectedAoiCenter, plotWidth / 2, numPlots, plotResolution, plotDistribution);
		
		for (int plotIdx = 0; plotIdx < plotLocations.size(); plotIdx++) {
			Coordinate plotCenter = plotLocations.get(plotIdx);

			Coordinate latLonPlotCenter = reprojectFromLatLonToWebMarcator(plotCenter);
			
			SamplingDesignItem plotCenterItem = new SamplingDesignItem();
			plotCenterItem.setLevelCodes(Arrays.asList(String.valueOf(plotIdx + 1)));
			plotCenterItem.setX(latLonPlotCenter.getX());
			plotCenterItem.setY(latLonPlotCenter.getY());
			items.add(plotCenterItem);
			
			List<Coordinate> sampleLocations = generateLocationsInCircle(plotCenter, sampleWidth / 2, samplesPerPlot, sampleResolution, sampleDistribution);
			for (int sampleIdx = 0; sampleIdx < sampleLocations.size(); sampleIdx++) {
				Coordinate sampleLocation = sampleLocations.get(sampleIdx);
				Coordinate latLonSampleLocation = reprojectFromWebMarcatorToLatLon(sampleLocation);
				SamplingDesignItem sampleItem = new SamplingDesignItem();
				sampleItem.setLevelCodes(Arrays.asList(String.valueOf(plotIdx + 1), String.valueOf(sampleIdx + 1)));
				sampleItem.setX(latLonSampleLocation.getX());
				sampleItem.setY(latLonSampleLocation.getY());
				items.add(sampleItem);
			}
		}
		return items;
	}

	private List<Coordinate> generateLocationsInCircle(Coordinate center, double radius,
			int numberOfLocations, double resolution, Distribution distribution) {
		List<Coordinate> result = new ArrayList<Coordinate>(numberOfLocations);
		switch(distribution) {
		case RANDOM:
			for (int i = 0; i < numberOfLocations; i++) {
				double offsetAngle = Math.random() * Math.PI * 2;
				double offsetMagnitude = Math.random() * radius;
				double xOffset = offsetMagnitude * Math.cos(offsetAngle);
				double yOffset = offsetMagnitude * Math.sin(offsetAngle);
				result.add(new Coordinate(center.getX() + xOffset, center.getY() + yOffset, 
						center.getSrsId()));
			}
			break;
		case GRIDDED:
			double left = center.getX() - radius;
			double top = center.getY() - radius;
			double radiusSquared = radius * radius;
			double numberOfSteps = Math.floor(2 * radius / resolution);
			for (int xStep = 0; xStep < numberOfSteps; xStep++) {
				double x = left + xStep * resolution;
				for (int yStep = 0; yStep < numberOfSteps; yStep++) {
					double y = top + yStep * resolution;
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
