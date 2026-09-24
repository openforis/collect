package org.openforis.collect.io.metadata.collectearth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.PROPERTY_KEY_BUFFER_SHAPE;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.PROPERTY_KEY_DISTANCE_BETWEEN_PLOTS;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.PROPERTY_KEY_DISTANCE_TO_BUFFERS;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.PROPERTY_KEY_DISTANCE_TO_PLOT_BOUNDARIES;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.PROPERTY_KEY_INNER_POINT_SIDE;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.PROPERTY_KEY_LARGE_CENTRAL_PLOT_SIDE;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.PROPERTY_KEY_SAMPLE_POINTS;
import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.PROPERTY_KEY_SAMPLE_SHAPE;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.CollectAnnotations.CollectEarthPlotShape;
import org.openforis.collect.metamodel.CollectAnnotations.CollectEarthReferenceAreaShape;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;

/**
 * Verifies that the plot layout written into the project definition of a Collect Earth project file
 * is the one that Collect Earth expects, and that importing such a project file restores it.
 */
public class CollectEarthProjectPropertiesTest {

	private CollectSurvey survey;
	private CollectAnnotations annotations;

	@Before
	public void init() {
		survey = (CollectSurvey) new CollectSurveyContext().createSurvey();
		annotations = survey.getAnnotations();
	}

	@Test
	public void squarePlotWritesTheDistancesOfItsGrid() {
		annotations.setCollectEarthPlotShape(CollectEarthPlotShape.SQUARE);
		annotations.setCollectEarthSamplePoints(25);
		annotations.setCollectEarthDistanceBetweenSamplePoints(20);
		annotations.setCollectEarthDistanceToPlotBoundaries(10);
		annotations.setCollectEarthInnerPointSide(2);

		Properties p = writeProjectProperties();
		assertEquals("SQUARE", p.get(PROPERTY_KEY_SAMPLE_SHAPE));
		assertEquals("25", p.get(PROPERTY_KEY_SAMPLE_POINTS));
		assertEquals("20", p.get(PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS));
		assertEquals("10", p.get(PROPERTY_KEY_DISTANCE_TO_PLOT_BOUNDARIES));
		assertEquals("2", p.get(PROPERTY_KEY_INNER_POINT_SIDE));
	}

	@Test
	public void surveyDefinedByPlotAreaKeepsTheDistancesItUsedToGenerate() {
		// the plot layout of a survey created before the distances became configurable must not change
		annotations.setCollectEarthPlotArea(0.25d);
		annotations.setCollectEarthSamplePoints(9);

		Properties p = writeProjectProperties();
		assertEquals("SQUARE", p.get(PROPERTY_KEY_SAMPLE_SHAPE));
		assertEquals("9", p.get(PROPERTY_KEY_SAMPLE_POINTS));
		assertEquals("17", p.get(PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS));
		assertEquals("8", p.get(PROPERTY_KEY_DISTANCE_TO_PLOT_BOUNDARIES));
	}

	@Test
	public void optionsUnusedByThePlotShapeAreEmptied() {
		annotations.setCollectEarthPlotShape(CollectEarthPlotShape.CIRCLE);
		annotations.setCollectEarthSamplePoints(9);
		annotations.setCollectEarthDistanceBetweenSamplePoints(30);
		annotations.setCollectEarthLargeCentralPlotSide(50);
		annotations.setCollectEarthDistanceBetweenPlots(80);

		Properties p = writeProjectProperties();
		assertEquals("CIRCLE", p.get(PROPERTY_KEY_SAMPLE_SHAPE));
		assertEquals("30", p.get(PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS));
		// a round plot has no margin, but Collect Earth requires a number
		assertEquals("0", p.get(PROPERTY_KEY_DISTANCE_TO_PLOT_BOUNDARIES));
		assertEquals("", p.get(PROPERTY_KEY_LARGE_CENTRAL_PLOT_SIDE));
		assertEquals("", p.get(PROPERTY_KEY_DISTANCE_BETWEEN_PLOTS));
	}

	@Test
	public void nfiClusterWritesTheDistanceBetweenItsPlots() {
		annotations.setCollectEarthPlotShape(CollectEarthPlotShape.NFI_THREE_CIRCLES);
		annotations.setCollectEarthDistanceBetweenSamplePoints(15);
		annotations.setCollectEarthDistanceBetweenPlots(80);
		annotations.setCollectEarthInnerPointSide(3);

		Properties p = writeProjectProperties();
		assertEquals("NFI_THREE_CIRCLES", p.get(PROPERTY_KEY_SAMPLE_SHAPE));
		assertEquals("15", p.get(PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS));
		assertEquals("80", p.get(PROPERTY_KEY_DISTANCE_BETWEEN_PLOTS));
		assertEquals("3", p.get(PROPERTY_KEY_INNER_POINT_SIDE));
		assertEquals("", p.get(PROPERTY_KEY_SAMPLE_POINTS));
	}

	@Test
	public void referenceAreaIsWrittenAsBufferShapeAndDistance() {
		annotations.setCollectEarthReferenceAreaShape(CollectEarthReferenceAreaShape.HEXAGON);
		annotations.setCollectEarthReferenceAreaDistance(300);

		Properties p = writeProjectProperties();
		assertEquals("HEXAGON", p.get(PROPERTY_KEY_BUFFER_SHAPE));
		assertEquals("300", p.get(PROPERTY_KEY_DISTANCE_TO_BUFFERS));
	}

	@Test
	public void withoutReferenceAreaTheBufferDistanceIsEmptied() {
		// Collect Earth only overwrites the properties present in the project file: an empty distance clears
		// the reference area of a project loaded before this one
		annotations.setCollectEarthReferenceAreaDistance(300);

		Properties p = writeProjectProperties();
		assertEquals("NONE", p.get(PROPERTY_KEY_BUFFER_SHAPE));
		assertEquals("", p.get(PROPERTY_KEY_DISTANCE_TO_BUFFERS));
	}

	@Test
	public void plotLayoutSurvivesTheRoundTripThroughAProjectFile() {
		annotations.setCollectEarthPlotShape(CollectEarthPlotShape.SQUARE_WITH_LARGE_CENTRAL_PLOT);
		annotations.setCollectEarthSamplePoints(49);
		annotations.setCollectEarthDistanceBetweenSamplePoints(25);
		annotations.setCollectEarthDistanceToPlotBoundaries(12);
		annotations.setCollectEarthInnerPointSide(4);
		annotations.setCollectEarthLargeCentralPlotSide(60);
		annotations.setCollectEarthReferenceAreaShape(CollectEarthReferenceAreaShape.CIRCLE);
		annotations.setCollectEarthReferenceAreaDistance(450);

		CollectEarthPlotLayout restored = CollectEarthPlotLayout.fromProjectProperties(writeProjectProperties());

		assertEquals(CollectEarthPlotShape.SQUARE_WITH_LARGE_CENTRAL_PLOT, restored.getPlotShape());
		assertEquals(49, restored.getSamplePoints());
		assertEquals(25, restored.getDistanceBetweenSamplePoints());
		assertEquals(12, restored.getDistanceToPlotBoundaries());
		assertEquals(4, restored.getInnerPointSide());
		assertEquals(60, restored.getLargeCentralPlotSide());
		assertEquals(CollectEarthReferenceAreaShape.CIRCLE, restored.getReferenceAreaShape());
		assertEquals(Integer.valueOf(450), restored.getReferenceAreaDistance());
	}

	@Test
	public void restoredLayoutIsStoredInTheSurveyAnnotations() {
		annotations.setCollectEarthPlotArea(0.5d);
		Properties p = new Properties();
		p.put(PROPERTY_KEY_SAMPLE_SHAPE, "HEXAGON");
		p.put(PROPERTY_KEY_SAMPLE_POINTS, "9");
		p.put(PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS, "40");

		CollectEarthPlotLayout.fromProjectProperties(p).saveTo(survey);

		assertEquals(CollectEarthPlotShape.HEXAGON, annotations.getCollectEarthPlotShape());
		assertEquals(9, annotations.getCollectEarthSamplePoints());
		assertEquals(Integer.valueOf(40), annotations.getCollectEarthDistanceBetweenSamplePoints());
		// the plot is now defined by its distances, so the area it used to be defined by is dropped
		assertEquals(Double.valueOf(1d), annotations.getCollectEarthPlotArea());
	}

	@Test
	public void projectFileWrittenByHandIsRestored() {
		Properties p = new Properties();
		// distance_to_buffers accepts a list of distances and buffer_shape is read ignoring the case
		p.put(PROPERTY_KEY_DISTANCE_TO_BUFFERS, " 70 , 112 , 194 ");
		p.put(PROPERTY_KEY_BUFFER_SHAPE, "hexagon");
		p.put(PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS, "22.0");

		CollectEarthPlotLayout restored = CollectEarthPlotLayout.fromProjectProperties(p);
		assertEquals(CollectEarthReferenceAreaShape.HEXAGON, restored.getReferenceAreaShape());
		assertEquals(Integer.valueOf(70), restored.getReferenceAreaDistance());
		assertEquals(22, restored.getDistanceBetweenSamplePoints());
	}

	@Test
	public void projectFileWithoutDistanceHasNoReferenceArea() {
		Properties p = new Properties();
		p.put(PROPERTY_KEY_BUFFER_SHAPE, "SQUARE");
		p.put(PROPERTY_KEY_DISTANCE_TO_BUFFERS, "");

		CollectEarthPlotLayout restored = CollectEarthPlotLayout.fromProjectProperties(p);
		assertEquals(CollectEarthReferenceAreaShape.NONE, restored.getReferenceAreaShape());
		assertFalse(restored.isReferenceAreaEnabled());
	}

	private Properties writeProjectProperties() {
		Properties p = new Properties();
		CollectEarthPlotLayout.fromSurvey(survey).writeProjectProperties(p);
		return p;
	}
}
