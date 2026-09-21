package org.openforis.collect.io.metadata.collectearth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
		assertEquals("SQUARE", p.get("sample_shape"));
		assertEquals("25", p.get("number_of_sampling_points_in_plot"));
		assertEquals("20", p.get("distance_between_sample_points"));
		assertEquals("10", p.get("distance_to_plot_boundaries"));
		assertEquals("2", p.get("inner_point_side"));
	}

	@Test
	public void surveyDefinedByPlotAreaKeepsTheDistancesItUsedToGenerate() {
		// the plot layout of a survey created before the distances became configurable must not change
		annotations.setCollectEarthPlotArea(0.25d);
		annotations.setCollectEarthSamplePoints(9);

		Properties p = writeProjectProperties();
		assertEquals("SQUARE", p.get("sample_shape"));
		assertEquals("9", p.get("number_of_sampling_points_in_plot"));
		assertEquals("17", p.get("distance_between_sample_points"));
		assertEquals("8", p.get("distance_to_plot_boundaries"));
	}

	@Test
	public void optionsUnusedByThePlotShapeAreEmptied() {
		annotations.setCollectEarthPlotShape(CollectEarthPlotShape.CIRCLE);
		annotations.setCollectEarthSamplePoints(9);
		annotations.setCollectEarthDistanceBetweenSamplePoints(30);
		annotations.setCollectEarthLargeCentralPlotSide(50);
		annotations.setCollectEarthDistanceBetweenPlots(80);

		Properties p = writeProjectProperties();
		assertEquals("CIRCLE", p.get("sample_shape"));
		assertEquals("30", p.get("distance_between_sample_points"));
		// a round plot has no margin, but Collect Earth requires a number
		assertEquals("0", p.get("distance_to_plot_boundaries"));
		assertEquals("", p.get("large_central_plot_side"));
		assertEquals("", p.get("distance_between_plots"));
	}

	@Test
	public void nfiClusterWritesTheDistanceBetweenItsPlots() {
		annotations.setCollectEarthPlotShape(CollectEarthPlotShape.NFI_THREE_CIRCLES);
		annotations.setCollectEarthDistanceBetweenSamplePoints(15);
		annotations.setCollectEarthDistanceBetweenPlots(80);
		annotations.setCollectEarthInnerPointSide(3);

		Properties p = writeProjectProperties();
		assertEquals("NFI_THREE_CIRCLES", p.get("sample_shape"));
		assertEquals("15", p.get("distance_between_sample_points"));
		assertEquals("80", p.get("distance_between_plots"));
		assertEquals("3", p.get("inner_point_side"));
		assertEquals("", p.get("number_of_sampling_points_in_plot"));
	}

	@Test
	public void referenceAreaIsWrittenAsBufferShapeAndDistance() {
		annotations.setCollectEarthReferenceAreaShape(CollectEarthReferenceAreaShape.HEXAGON);
		annotations.setCollectEarthReferenceAreaDistance(300);

		Properties p = writeProjectProperties();
		assertEquals("HEXAGON", p.get("buffer_shape"));
		assertEquals("300", p.get("distance_to_buffers"));
	}

	@Test
	public void withoutReferenceAreaTheBufferDistanceIsEmptied() {
		// Collect Earth only overwrites the properties present in the project file: an empty distance clears
		// the reference area of a project loaded before this one
		annotations.setCollectEarthReferenceAreaDistance(300);

		Properties p = writeProjectProperties();
		assertEquals("NONE", p.get("buffer_shape"));
		assertEquals("", p.get("distance_to_buffers"));
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
		p.put("sample_shape", "HEXAGON");
		p.put("number_of_sampling_points_in_plot", "9");
		p.put("distance_between_sample_points", "40");

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
		p.put("distance_to_buffers", " 70 , 112 , 194 ");
		p.put("buffer_shape", "hexagon");
		p.put("distance_between_sample_points", "22.0");

		CollectEarthPlotLayout restored = CollectEarthPlotLayout.fromProjectProperties(p);
		assertEquals(CollectEarthReferenceAreaShape.HEXAGON, restored.getReferenceAreaShape());
		assertEquals(Integer.valueOf(70), restored.getReferenceAreaDistance());
		assertEquals(22, restored.getDistanceBetweenSamplePoints());
	}

	@Test
	public void projectFileWithoutDistanceHasNoReferenceArea() {
		Properties p = new Properties();
		p.put("buffer_shape", "SQUARE");
		p.put("distance_to_buffers", "");

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
