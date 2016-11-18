package org.openforis.collect.io.metadata.samplingpointdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.idm.model.Coordinate;

public class SamplingPointDataGenerator {

	public List<SamplingDesignItem> generate(
			double boundaryLonMin, double boundaryLonMax,
			double boundaryLatMin, double boundaryLatMax, int numPlots,
			String plotDistribution, double plotResolution, double plotWidth,
			int samplesPerPlot, double sampleResolution,
			String sampleDistribution, double sampleWidth) {
		Coordinate latLonAoiCenter = new Coordinate(boundaryLonMin + (boundaryLonMax - boundaryLonMin) / 2, 
				boundaryLatMin + (boundaryLatMax - boundaryLatMin) / 2, "EPSG:4326");
		
		Coordinate reprojectedAoiCenter = null; //reproject to EPSG:3857
		
		List<Coordinate> plotLocations = generateLocationsInCircle(reprojectedAoiCenter, plotWidth / 2, numPlots, plotResolution, plotDistribution);
		List<SamplingDesignItem> items = new ArrayList<SamplingDesignItem>(numPlots + numPlots * samplesPerPlot);
		
		for (int plotIdx = 0; plotIdx < plotLocations.size(); plotIdx++) {
			Coordinate plotCenter = plotLocations.get(plotIdx);

			Coordinate latLonPlotCenter = null; //TODO
			
			SamplingDesignItem plotCenterItem = new SamplingDesignItem();
			plotCenterItem.setLevelCodes(Arrays.asList(String.valueOf(plotIdx + 1)));
			plotCenterItem.setX(latLonPlotCenter.getX());
			plotCenterItem.setY(latLonPlotCenter.getY());
			items.add(plotCenterItem);
			
			List<Coordinate> sampleLocations = generateLocationsInCircle(plotCenter, sampleWidth / 2, samplesPerPlot, sampleResolution, sampleDistribution);
			for (int sampleIdx = 0; sampleIdx < sampleLocations.size(); sampleIdx++) {
				Coordinate sampleLocation = sampleLocations.get(sampleIdx);
				Coordinate latLonSampleLocation = null; //TODO
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
			int numberOfLocations, double resolution, String distribution) {
		List<Coordinate> result = new ArrayList<Coordinate>();
		if ("random".equals(distribution)) {
			for (int i = 0; i < numberOfLocations; i++) {
				double offsetAngle = Math.random() * Math.PI * 2;
				double offsetMagnitude = Math.random() * radius;
				double xOffset = offsetMagnitude * Math.cos(offsetAngle);
				double yOffset = offsetMagnitude * Math.sin(offsetAngle);
				result.add(new Coordinate(center.getX() + xOffset, center.getY() + yOffset, 
						center.getSrsId()));
			}
		} else {
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
}
