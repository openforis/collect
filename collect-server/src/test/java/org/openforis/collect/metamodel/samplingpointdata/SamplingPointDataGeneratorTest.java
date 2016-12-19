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
import org.openforis.collect.io.metadata.samplingpointdata.SamplingPointDataGenerator.PointsConfiguration;
import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView.Distribution;
import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView.Shape;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.web.controller.SingleAttributeSurveyCreationParameters.SamplingPointDataConfiguration;
import org.openforis.idm.geospatial.CoordinateUtils;
import org.openforis.idm.model.Coordinate;

public class SamplingPointDataGeneratorTest {

	@Test
	public void griddedPlotsGenerationTest() {
		Coordinate topLeftCoordinate = new Coordinate(12.369192d, 41.987927d, LAT_LON_SRS_ID); 
		Coordinate bottomRightCoordinate = new Coordinate(12.621191d, 41.802904d, LAT_LON_SRS_ID); 
		
		int numPlots = 25;
		int samplesPerPlot = 10;
		int plotWidth = 1000;
		
		PointsConfiguration plotPointsConfig = new PointsConfiguration(numPlots, Shape.CIRCLE, Distribution.GRIDDED, 5000, plotWidth);
		PointsConfiguration samplePointsConfig = new PointsConfiguration(samplesPerPlot, Shape.CIRCLE, Distribution.RANDOM, 20, 10);
		
		CollectSurvey survey = createTestSurvey();
		
		SamplingPointDataConfiguration conf = new SamplingPointDataConfiguration();
		conf.setBoundaryLonMin(topLeftCoordinate.getX());
		conf.setBoundaryLonMax(bottomRightCoordinate.getX());
		conf.setBoundaryLatMin(topLeftCoordinate.getY());
		conf.setBoundaryLatMax(bottomRightCoordinate.getY());
		conf.setLevelsConfiguration(Arrays.asList(plotPointsConfig, samplePointsConfig));

		SamplingPointDataGenerator generator = new SamplingPointDataGenerator(survey, conf);
		
		List<SamplingDesignItem> items = generator.generate();
		
//		printLatLonPoints(items);
		
		assertTrue(items.size() <= numPlots + numPlots*samplesPerPlot);
		List<SamplingDesignItem> plotItems = getSamplingItemsInLevel(items, 1);
		assertTrue(plotItems.size() <= numPlots);
		for (SamplingDesignItem plotItem : plotItems) {
			assertPointInSquare(plotItem, topLeftCoordinate, bottomRightCoordinate);
		}
	}

	private CollectSurvey createTestSurvey() {
		CollectSurvey survey = (CollectSurvey) new CollectSurveyContext().createSurvey();
		survey.setId(1);
		return survey;
	}
	
	@Test
	public void randomSamplingPointsGenerationTest() {
		Coordinate topLeftCoordinate = new Coordinate(12.369192d, 41.987927d, LAT_LON_SRS_ID); 
		Coordinate bottomRightCoordinate = new Coordinate(12.621191d, 41.802904d, LAT_LON_SRS_ID); 
		
		int numPlots = 25;
		int samplesPerPlot = 10;
		int plotWidth = 1000;
		
		PointsConfiguration plotPointsConfig = new PointsConfiguration(numPlots, Shape.CIRCLE, Distribution.GRIDDED, 5000, plotWidth);
		PointsConfiguration samplePointsConfig = new PointsConfiguration(samplesPerPlot, Shape.CIRCLE, Distribution.RANDOM, 20, 10);
		
		CollectSurvey survey = createTestSurvey();
		
		SamplingPointDataConfiguration conf = new SamplingPointDataConfiguration();
		conf.setBoundaryLonMin(topLeftCoordinate.getX());
		conf.setBoundaryLonMax(bottomRightCoordinate.getX());
		conf.setBoundaryLatMin(topLeftCoordinate.getY());
		conf.setBoundaryLatMax(bottomRightCoordinate.getY());
		conf.setLevelsConfiguration(Arrays.asList(plotPointsConfig, samplePointsConfig));
		
		SamplingPointDataGenerator generator = new SamplingPointDataGenerator(survey, conf);
		
		List<SamplingDesignItem> items = generator.generate();
		
		List<SamplingDesignItem> plotItems = getSamplingItemsInLevel(items, 1);
		List<SamplingDesignItem> samplingPointItems = getSamplingItemsInLevel(items, 2);
		assertEquals(plotItems.size() * samplesPerPlot, samplingPointItems.size());
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

	private void assertPointInSquare(SamplingDesignItem plotItem, Coordinate topLeftCoordinate, Coordinate bottomRightCoordinate) {
		assertTrue(betweenExclusive(plotItem.getX(), topLeftCoordinate.getX(), bottomRightCoordinate.getX()));
		assertTrue(betweenExclusive(plotItem.getY(), bottomRightCoordinate.getY(), topLeftCoordinate.getY()));
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
