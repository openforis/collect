package org.openforis.collect.io.metadata.collectearth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.CollectAnnotations.CollectEarthPlotShape;
import org.openforis.collect.metamodel.CollectAnnotations.CollectEarthReferenceAreaShape;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;

public class CollectEarthPlotLayoutTest {

	private static final double DELTA = 0.0001d;

	private CollectSurvey survey;
	private CollectAnnotations annotations;

	@Before
	public void init() {
		survey = (CollectSurvey) new CollectSurveyContext().createSurvey();
		annotations = survey.getAnnotations();
	}

	@Test
	public void newSurveyHasOneHectareSquarePlotWithoutReferenceArea() {
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		assertEquals(CollectEarthPlotShape.SQUARE, layout.getPlotShape());
		assertEquals(25, layout.getSamplePoints());
		assertEquals(20, layout.getDistanceBetweenSamplePoints());
		assertEquals(10, layout.getDistanceToPlotBoundaries());
		assertEquals(2, layout.getInnerPointSide());
		assertEquals(1d, layout.getPlotAreaHectares(), DELTA);
		assertFalse(layout.isReferenceAreaEnabled());
		assertNull(layout.getReferenceAreaHectares());
	}

	@Test
	public void distancesOfSurveyDefinedByPlotAreaAreDerivedFromIt() {
		annotations.setCollectEarthPlotArea(0.25d);
		annotations.setCollectEarthSamplePoints(9);
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		// 50 m side: 8 m margin and 17 m between the 3x3 points, as the project export always calculated
		assertEquals(8, layout.getDistanceToPlotBoundaries());
		assertEquals(17, layout.getDistanceBetweenSamplePoints());
	}

	@Test
	public void plotWithoutPointsDefinedByPlotAreaIsSizedByItsMargin() {
		annotations.setCollectEarthPlotArea(1d);
		annotations.setCollectEarthSamplePoints(0);
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		assertEquals(50, layout.getDistanceToPlotBoundaries());
		assertFalse(layout.isDistanceBetweenSamplePointsApplicable());
		assertFalse(layout.isInnerPointSideApplicable());
		assertEquals(1d, layout.getPlotAreaHectares(), DELTA);
	}

	@Test
	public void configuredDistancesPrevailOverPlotArea() {
		annotations.setCollectEarthPlotArea(10d);
		annotations.setCollectEarthSamplePoints(9);
		annotations.setCollectEarthDistanceBetweenSamplePoints(30);
		annotations.setCollectEarthDistanceToPlotBoundaries(5);
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		assertEquals(30, layout.getDistanceBetweenSamplePoints());
		assertEquals(5, layout.getDistanceToPlotBoundaries());
		// side = 2 * 5 + 2 * 30 = 70 m
		assertEquals(0.49d, layout.getPlotAreaHectares(), DELTA);
	}

	@Test
	public void applicableOptionsDependOnPlotShape() {
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);

		layout.setPlotShape(CollectEarthPlotShape.SQUARE_WITH_LARGE_CENTRAL_PLOT);
		assertTrue(layout.isSamplePointsApplicable());
		assertTrue(layout.isDistanceToPlotBoundariesApplicable());
		assertTrue(layout.isLargeCentralPlotSideApplicable());
		assertTrue(layout.isReferenceAreaApplicable());
		assertFalse(layout.isDistanceBetweenPlotsApplicable());

		layout.setPlotShape(CollectEarthPlotShape.CIRCLE);
		layout.setSamplePoints(0);
		assertTrue(layout.isSamplePointsApplicable());
		assertTrue("the radius of a round plot is always needed", layout.isDistanceBetweenSamplePointsApplicable());
		assertFalse(layout.isDistanceToPlotBoundariesApplicable());
		assertFalse(layout.isInnerPointSideApplicable());
		assertTrue(layout.isReferenceAreaApplicable());

		layout.setPlotShape(CollectEarthPlotShape.NFI_THREE_CIRCLES);
		assertFalse(layout.isSamplePointsApplicable());
		assertTrue(layout.isDistanceBetweenSamplePointsApplicable());
		assertTrue(layout.isInnerPointSideApplicable());
		assertTrue(layout.isDistanceBetweenPlotsApplicable());
		assertFalse(layout.isReferenceAreaApplicable());

		layout.setPlotShape(CollectEarthPlotShape.KML_POLYGON);
		assertFalse(layout.isSamplePointsApplicable());
		assertFalse(layout.isDistanceBetweenSamplePointsApplicable());
		assertFalse(layout.isInnerPointSideApplicable());
		assertFalse(layout.isReferenceAreaApplicable());
	}

	@Test
	public void referenceAreaIsIgnoredByPlotShapesThatCannotHaveOne() {
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		layout.setReferenceAreaShape(CollectEarthReferenceAreaShape.SQUARE);
		layout.setReferenceAreaDistance(200);
		assertTrue(layout.isReferenceAreaEnabled());
		layout.setPlotShape(CollectEarthPlotShape.NFMA);
		assertFalse(layout.isReferenceAreaEnabled());
	}

	@Test
	public void referenceAreaMustEncloseSquarePlot() {
		// 1 ha square plot: half side of 50 m
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		layout.setReferenceAreaShape(CollectEarthReferenceAreaShape.SQUARE);
		assertEquals(51, layout.getMinimumReferenceAreaDistance());
		layout.setReferenceAreaShape(CollectEarthReferenceAreaShape.CIRCLE);
		// the circle has to reach the corners of the plot: 50 * sqrt(2) = 70.7
		assertEquals(72, layout.getMinimumReferenceAreaDistance());
		layout.setReferenceAreaShape(CollectEarthReferenceAreaShape.HEXAGON);
		// 50 * (cos(30) + sin(30)) / cos(30) = 78.9
		assertEquals(80, layout.getMinimumReferenceAreaDistance());
	}

	@Test
	public void referenceAreaMustEncloseOutlineOfRoundPlot() {
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		layout.setPlotShape(CollectEarthPlotShape.CIRCLE);
		layout.setDistanceBetweenSamplePoints(30);
		// Collect Earth draws the outline of round plots 5 m beyond their radius
		layout.setReferenceAreaShape(CollectEarthReferenceAreaShape.CIRCLE);
		assertEquals(36, layout.getMinimumReferenceAreaDistance());
		layout.setReferenceAreaShape(CollectEarthReferenceAreaShape.SQUARE);
		assertEquals(36, layout.getMinimumReferenceAreaDistance());
		layout.setReferenceAreaShape(CollectEarthReferenceAreaShape.HEXAGON);
		// 35 / cos(30) = 40.4
		assertEquals(42, layout.getMinimumReferenceAreaDistance());
	}

	@Test
	public void recommendedReferenceAreaCoversTenTimesThePlotArea() {
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		layout.setReferenceAreaShape(CollectEarthReferenceAreaShape.SQUARE);
		// sqrt(10 ha) / 2 = 158.1
		assertEquals(159, layout.getRecommendedReferenceAreaDistance());

		layout.setReferenceAreaDistance(158);
		assertTrue(layout.isReferenceAreaTooSmall());
		layout.setReferenceAreaDistance(159);
		assertFalse(layout.isReferenceAreaTooSmall());
		assertEquals(10.1124d, layout.getReferenceAreaHectares(), DELTA);

		layout.setReferenceAreaShape(CollectEarthReferenceAreaShape.CIRCLE);
		// sqrt(10 ha / PI) = 178.4
		assertEquals(179, layout.getRecommendedReferenceAreaDistance());
	}

	@Test
	public void plotAreaOfRoundPlotsDependsOnTheRadius() {
		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		layout.setDistanceBetweenSamplePoints(100);
		layout.setPlotShape(CollectEarthPlotShape.CIRCLE);
		assertEquals(Math.PI, layout.getPlotAreaHectares(), DELTA);
		layout.setPlotShape(CollectEarthPlotShape.HEXAGON);
		assertEquals(3 * Math.sqrt(3) / 2, layout.getPlotAreaHectares(), DELTA);
	}

	@Test
	public void plotLayoutIsStoredInSurveyAnnotations() {
		annotations.setCollectEarthPlotShape(CollectEarthPlotShape.HEXAGON);
		annotations.setCollectEarthSamplePoints(16);
		annotations.setCollectEarthDistanceBetweenSamplePoints(40);
		annotations.setCollectEarthInnerPointSide(4);
		annotations.setCollectEarthReferenceAreaShape(CollectEarthReferenceAreaShape.CIRCLE);
		annotations.setCollectEarthReferenceAreaDistance(150);

		CollectEarthPlotLayout layout = CollectEarthPlotLayout.fromSurvey(survey);
		assertEquals(CollectEarthPlotShape.HEXAGON, layout.getPlotShape());
		assertEquals(16, layout.getSamplePoints());
		assertEquals(40, layout.getDistanceBetweenSamplePoints());
		assertEquals(4, layout.getInnerPointSide());
		assertEquals(CollectEarthReferenceAreaShape.CIRCLE, layout.getReferenceAreaShape());
		assertEquals(Integer.valueOf(150), layout.getReferenceAreaDistance());
		assertTrue(layout.isReferenceAreaEnabled());
	}
}
