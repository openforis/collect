package org.openforis.collect.metamodel.samplingpointdata;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.openforis.idm.metamodel.SpatialReferenceSystem.LAT_LON_SRS;
import static org.openforis.idm.metamodel.SpatialReferenceSystem.LAT_LON_SRS_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Test;
import org.openforis.collect.io.metadata.samplingpointdata.SamplingPointDataGenerator;
import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView.Distribution;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.idm.geospatial.CoordinateUtils;
import org.openforis.idm.model.Coordinate;

public class SamplingPointDataGeneratorTest {

	@Test
	public void griddedPlotsGenerationTest() {
		SamplingPointDataGenerator generator = new SamplingPointDataGenerator();
		Coordinate topLeftCoordinate = new Coordinate(12.369192d, 41.987927d, LAT_LON_SRS_ID); 
		Coordinate bottomRightCoordinate = new Coordinate(12.621191d, 41.802904d, LAT_LON_SRS_ID); 
		
		int numPlots = 16;
		int samplesPerPlot = 10;
		int plotWidth = 200;
		
		List<SamplingDesignItem> items = generator.generate(topLeftCoordinate.getX(), bottomRightCoordinate.getX(), 
				topLeftCoordinate.getY(), bottomRightCoordinate.getY(), 
				numPlots, Distribution.GRIDDED, 50, plotWidth, 
				samplesPerPlot, Distribution.RANDOM, 20, 10);
		
		//printLatLonPoints(items);
		
		assertEquals(numPlots + numPlots*samplesPerPlot, items.size());
		List<SamplingDesignItem> plotItems = getSamplingItemsInLevel(items, 1);
		assertEquals(numPlots, plotItems.size());
		for (SamplingDesignItem plotItem : plotItems) {
			assertPointInSquare(plotItem, topLeftCoordinate, bottomRightCoordinate);
		}
	}
	
	@Test
	public void randomSamplingPointsGenerationTest() {
		SamplingPointDataGenerator generator = new SamplingPointDataGenerator();
		Coordinate topLeftCoordinate = new Coordinate(12.369192d, 41.987927d, LAT_LON_SRS_ID); 
		Coordinate bottomRightCoordinate = new Coordinate(12.621191d, 41.802904d, LAT_LON_SRS_ID); 
		
		int numPlots = 16;
		int samplesPerPlot = 10;
		int plotWidth = 200;
		
		List<SamplingDesignItem> items = generator.generate(topLeftCoordinate.getX(), bottomRightCoordinate.getX(), 
				topLeftCoordinate.getY(), bottomRightCoordinate.getY(), 
				numPlots, Distribution.GRIDDED, 50, plotWidth, 
				samplesPerPlot, Distribution.RANDOM, 20, 10);
		
		List<SamplingDesignItem> samplingPointItems = getSamplingItemsInLevel(items, 2);
		assertEquals(numPlots*samplesPerPlot, samplingPointItems.size());
		for (final SamplingDesignItem samplingPointItem : samplingPointItems) {
			SamplingDesignItem plotItem = (SamplingDesignItem) CollectionUtils.find(items, new Predicate() {
				public boolean evaluate(Object object) {
					return Arrays.asList(samplingPointItem.getLevelCode(1)).equals(((SamplingDesignItem) object).getLevelCodes());
				}
			});
			assertNotNull(plotItem);
			assertPointInCircle(samplingPointItem, new Coordinate(plotItem.getX(), plotItem.getY(), LAT_LON_SRS_ID), plotWidth / 2);
		}
	}

	private void assertPointInSquare(SamplingDesignItem plotItem, Coordinate upperLeftCoordinate, Coordinate bottomRightCoordinate) {
		assertTrue(betweenExclusive(plotItem.getX(), upperLeftCoordinate.getX(), bottomRightCoordinate.getX()));
		assertTrue(betweenExclusive(plotItem.getY(), bottomRightCoordinate.getY(), upperLeftCoordinate.getY()));
	}
	
	private void assertPointInCircle(SamplingDesignItem item, Coordinate center, int radius) {
		double distance = CoordinateUtils.distance(
				LAT_LON_SRS, new double[]{item.getX(), item.getY()}, 
				LAT_LON_SRS, new double[]{center.getX(), center.getY()});
		assertTrue(distance < radius);
	}
	
	private boolean betweenExclusive(double value, double min, double max) {
		return value > min && value < max;
	}

	private List<SamplingDesignItem> getSamplingItemsInLevel(List<SamplingDesignItem> items, int level) {
		List<SamplingDesignItem> result = new ArrayList<SamplingDesignItem>();
		for (SamplingDesignItem item : items) {
			if (item.getLevel() == level) {
				result.add(item);
			}
		}
		return result;
	}

	@SuppressWarnings("unused")
	private void printLatLonPoints(List<SamplingDesignItem> items) {
		StringBuilder sb = new StringBuilder();
		for (SamplingDesignItem item : items) {
//			if (item.getLevel() == 1) {
				sb.append(item.getY());
				sb.append(',');
				sb.append(item.getX());
				sb.append('\n');
//			}
		}
		System.out.println(sb.toString());
	}
	
}
