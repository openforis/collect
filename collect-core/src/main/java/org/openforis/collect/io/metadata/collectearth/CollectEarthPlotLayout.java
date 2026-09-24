package org.openforis.collect.io.metadata.collectearth;

import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.metamodel.CollectAnnotations.CollectEarthReferenceAreaShape;
import org.openforis.collect.metamodel.CollectAnnotations.CollectEarthPlotShape;
import org.openforis.collect.model.CollectSurvey;

/**
 * Layout of a Collect Earth plot as it can be configured in the plot options of Collect Earth:
 * which options apply to every plot shape and the geometry (areas, reference area limits) derived from them.
 * It follows the rules of the PlotOptionsPanel of Collect Earth, so that a project behaves in the same way
 * when it is configured from the survey designer or from Collect Earth.
 *
 * All the distances are in meters.
 */
public class CollectEarthPlotLayout {

	/** Property keys used in the Collect Earth project file (ce.properties) */
	public static final String PROPERTY_KEY_SAMPLE_SHAPE = "sample_shape";
	public static final String PROPERTY_KEY_SAMPLE_POINTS = "number_of_sampling_points_in_plot";
	public static final String PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS = "distance_between_sample_points";
	public static final String PROPERTY_KEY_DISTANCE_TO_PLOT_BOUNDARIES = "distance_to_plot_boundaries";
	public static final String PROPERTY_KEY_INNER_POINT_SIDE = "inner_point_side";
	public static final String PROPERTY_KEY_LARGE_CENTRAL_PLOT_SIDE = "large_central_plot_side";
	public static final String PROPERTY_KEY_DISTANCE_BETWEEN_PLOTS = "distance_between_plots";
	public static final String PROPERTY_KEY_BUFFER_SHAPE = "buffer_shape";
	public static final String PROPERTY_KEY_DISTANCE_TO_BUFFERS = "distance_to_buffers";

	public static final int MIN_DISTANCE_BETWEEN_SAMPLE_POINTS = 2;
	public static final int MAX_DISTANCE_BETWEEN_SAMPLE_POINTS = 1000;
	public static final int MIN_DISTANCE_TO_PLOT_BOUNDARIES = 0;
	public static final int MAX_DISTANCE_TO_PLOT_BOUNDARIES = 500;
	public static final int MIN_INNER_POINT_SIDE = 2;
	public static final int MAX_INNER_POINT_SIDE = 100;
	public static final int MIN_LARGE_CENTRAL_PLOT_SIDE = 2;
	public static final int MAX_LARGE_CENTRAL_PLOT_SIDE = 200;
	public static final int MIN_DISTANCE_BETWEEN_PLOTS = 2;
	public static final int MAX_DISTANCE_BETWEEN_PLOTS = 1000;
	public static final int MIN_REFERENCE_AREA_DISTANCE = 1;
	public static final int MAX_REFERENCE_AREA_DISTANCE = 10000;

	/** Below this ratio between the reference area and the plot area the reference area shows too little of the surroundings. */
	public static final double MIN_REFERENCE_AREA_TO_PLOT_AREA_RATIO = 10d;

	/** Defaults of the options that a project definition can leave out, as the survey annotations define them */
	private static final int DEFAULT_SAMPLE_POINTS = Annotation.COLLECT_EARTH_SAMPLE_POINTS.getDefaultValue();
	private static final int DEFAULT_INNER_POINT_SIDE = Annotation.COLLECT_EARTH_INNER_POINT_SIDE.getDefaultValue();
	private static final int DEFAULT_LARGE_CENTRAL_PLOT_SIDE = Annotation.COLLECT_EARTH_LARGE_CENTRAL_PLOT_SIDE.getDefaultValue();
	private static final int DEFAULT_DISTANCE_BETWEEN_PLOTS = Annotation.COLLECT_EARTH_DISTANCE_BETWEEN_PLOTS.getDefaultValue();

	/** Margin that Collect Earth adds to the radius when drawing the outline of circle and hexagon plots. */
	private static final double ROUND_PLOT_OUTLINE_MARGIN = 5d;
	private static final double COS_30 = Math.cos(Math.toRadians(30));
	/** Area of a regular hexagon = factor * (distance from the center to a vertex)^2 */
	private static final double HEXAGON_AREA_FACTOR = 3 * Math.sqrt(3) / 2;
	private static final double SQUARE_METERS_PER_HECTARE = 10000d;

	private CollectEarthPlotShape plotShape = CollectEarthPlotShape.SQUARE;
	private int samplePoints;
	private int distanceBetweenSamplePoints;
	private int distanceToPlotBoundaries;
	private int innerPointSide;
	private int largeCentralPlotSide;
	private int distanceBetweenPlots;
	private CollectEarthReferenceAreaShape referenceAreaShape = CollectEarthReferenceAreaShape.NONE;
	private Integer referenceAreaDistance;

	public static CollectEarthPlotLayout fromSurvey(CollectSurvey survey) {
		CollectAnnotations annotations = survey.getAnnotations();
		CollectEarthPlotLayout layout = new CollectEarthPlotLayout();
		layout.plotShape = annotations.getCollectEarthPlotShape();
		layout.samplePoints = annotations.getCollectEarthSamplePoints();
		Integer distanceBetweenSamplePoints = annotations.getCollectEarthDistanceBetweenSamplePoints();
		Integer distanceToPlotBoundaries = annotations.getCollectEarthDistanceToPlotBoundaries();
		// surveys edited before the distances became configurable define the plot by its area
		double legacyPlotSide = Math.sqrt(annotations.getCollectEarthPlotArea() * SQUARE_METERS_PER_HECTARE);
		layout.distanceToPlotBoundaries = distanceToPlotBoundaries == null
				? calculateDistanceToPlotBoundaries(legacyPlotSide, layout.samplePoints)
				: distanceToPlotBoundaries;
		layout.distanceBetweenSamplePoints = distanceBetweenSamplePoints == null
				? calculateDistanceBetweenSamplePoints(legacyPlotSide, layout.samplePoints, layout.distanceToPlotBoundaries)
				: distanceBetweenSamplePoints;
		layout.innerPointSide = annotations.getCollectEarthInnerPointSide();
		layout.largeCentralPlotSide = annotations.getCollectEarthLargeCentralPlotSide();
		layout.distanceBetweenPlots = annotations.getCollectEarthDistanceBetweenPlots();
		layout.referenceAreaShape = annotations.getCollectEarthReferenceAreaShape();
		layout.referenceAreaDistance = annotations.getCollectEarthReferenceAreaDistance();
		return layout;
	}

	public void saveTo(CollectSurvey survey) {
		CollectAnnotations annotations = survey.getAnnotations();
		annotations.setCollectEarthPlotShape(plotShape);
		annotations.setCollectEarthSamplePoints(samplePoints);
		annotations.setCollectEarthDistanceBetweenSamplePoints(distanceBetweenSamplePoints);
		annotations.setCollectEarthDistanceToPlotBoundaries(distanceToPlotBoundaries);
		annotations.setCollectEarthInnerPointSide(innerPointSide);
		annotations.setCollectEarthLargeCentralPlotSide(largeCentralPlotSide);
		annotations.setCollectEarthDistanceBetweenPlots(distanceBetweenPlots);
		annotations.setCollectEarthReferenceAreaShape(referenceAreaShape);
		annotations.setCollectEarthReferenceAreaDistance(referenceAreaDistance);
		// the plot is now defined by the distances above
		annotations.setCollectEarthPlotArea(null);
	}

	/**
	 * Reads the plot layout of the project definition of a Collect Earth project file.
	 * Collect Earth leaves empty the options that the plot shape does not use and tolerates
	 * decimal distances and shape names written in any case, as they can be edited by hand.
	 */
	public static CollectEarthPlotLayout fromProjectProperties(Properties p) {
		CollectEarthPlotLayout layout = new CollectEarthPlotLayout();
		layout.setPlotShape(getEnumProperty(p, PROPERTY_KEY_SAMPLE_SHAPE, CollectEarthPlotShape.class, CollectEarthPlotShape.SQUARE));
		layout.samplePoints = getIntegerProperty(p, PROPERTY_KEY_SAMPLE_POINTS, DEFAULT_SAMPLE_POINTS);
		layout.distanceBetweenSamplePoints = getIntegerProperty(p, PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS, 0);
		layout.distanceToPlotBoundaries = getIntegerProperty(p, PROPERTY_KEY_DISTANCE_TO_PLOT_BOUNDARIES, 0);
		layout.innerPointSide = getIntegerProperty(p, PROPERTY_KEY_INNER_POINT_SIDE, DEFAULT_INNER_POINT_SIDE);
		layout.largeCentralPlotSide = getIntegerProperty(p, PROPERTY_KEY_LARGE_CENTRAL_PLOT_SIDE, DEFAULT_LARGE_CENTRAL_PLOT_SIDE);
		layout.distanceBetweenPlots = getIntegerProperty(p, PROPERTY_KEY_DISTANCE_BETWEEN_PLOTS, DEFAULT_DISTANCE_BETWEEN_PLOTS);

		// distance_to_buffers can hold a comma separated list of distances (e.g. "70,112,194"); only the first one
		// is kept, as a single reference area is what the survey designer configures
		Integer referenceAreaDistance = parseInteger(StringUtils.substringBefore(p.getProperty(PROPERTY_KEY_DISTANCE_TO_BUFFERS), ","));
		if (referenceAreaDistance != null) {
			// projects that set distance_to_buffers before the shape became configurable expect square reference areas
			layout.referenceAreaShape = getEnumProperty(p, PROPERTY_KEY_BUFFER_SHAPE, CollectEarthReferenceAreaShape.class, CollectEarthReferenceAreaShape.SQUARE);
			layout.referenceAreaDistance = referenceAreaDistance;
		}
		return layout;
	}

	/**
	 * Writes the plot layout as the plot options of Collect Earth would do: every option is written, and the ones
	 * that the selected plot shape does not use are left empty, so that they do not keep the value of a project
	 * loaded before this one (Collect Earth only overwrites the properties present in the project file).
	 */
	public void writeProjectProperties(Properties p) {
		p.put(PROPERTY_KEY_SAMPLE_SHAPE, plotShape.name());
		p.put(PROPERTY_KEY_SAMPLE_POINTS, isSamplePointsApplicable() ? String.valueOf(samplePoints) : "");
		// these two distances are written as numbers even when they are not used, because the Collect Earth versions
		// released before the reference area options fail to open a project that leaves them empty
		p.put(PROPERTY_KEY_DISTANCE_BETWEEN_SAMPLE_POINTS, String.valueOf(
				isDistanceBetweenSamplePointsApplicable() ? distanceBetweenSamplePoints : 0));
		p.put(PROPERTY_KEY_DISTANCE_TO_PLOT_BOUNDARIES, String.valueOf(
				isDistanceToPlotBoundariesApplicable() ? distanceToPlotBoundaries : 0));
		p.put(PROPERTY_KEY_INNER_POINT_SIDE, isInnerPointSideApplicable() ? String.valueOf(innerPointSide) : "");
		p.put(PROPERTY_KEY_LARGE_CENTRAL_PLOT_SIDE, isLargeCentralPlotSideApplicable() ? String.valueOf(largeCentralPlotSide) : "");
		p.put(PROPERTY_KEY_DISTANCE_BETWEEN_PLOTS, isDistanceBetweenPlotsApplicable() ? String.valueOf(distanceBetweenPlots) : "");
		if (isReferenceAreaEnabled()) {
			p.put(PROPERTY_KEY_BUFFER_SHAPE, referenceAreaShape.name());
			p.put(PROPERTY_KEY_DISTANCE_TO_BUFFERS, String.valueOf(
					referenceAreaDistance == null ? getRecommendedReferenceAreaDistance() : referenceAreaDistance));
		} else {
			// for Collect Earth an empty distance means that there is no reference area
			p.put(PROPERTY_KEY_BUFFER_SHAPE, CollectEarthReferenceAreaShape.NONE.name());
			p.put(PROPERTY_KEY_DISTANCE_TO_BUFFERS, "");
		}
	}

	private static <E extends Enum<E>> E getEnumProperty(Properties p, String key, Class<E> enumType, E defaultValue) {
		String value = p.getProperty(key);
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		try {
			return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ENGLISH));
		} catch (IllegalArgumentException e) {
			return defaultValue;
		}
	}

	private static int getIntegerProperty(Properties p, String key, int defaultValue) {
		Integer value = parseInteger(p.getProperty(key));
		return value == null ? defaultValue : value;
	}

	private static Integer parseInteger(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		try {
			// Collect Earth reads the distances as decimal numbers
			return (int) Math.round(Double.parseDouble(value.trim()));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static int calculateDistanceToPlotBoundaries(double plotSide, int samplePoints) {
		if (samplePoints == 0) {
			return (int) Math.floor(plotSide / 2);
		}
		double pointsPerSide = Math.sqrt(samplePoints);
		return (int) Math.floor((plotSide / pointsPerSide) / 2);
	}

	private static int calculateDistanceBetweenSamplePoints(double plotSide, int samplePoints, int distanceToPlotBoundaries) {
		if (samplePoints <= 1) {
			// never used by Collect Earth, but a valid value if the number of points changes
			return Math.max(MIN_DISTANCE_BETWEEN_SAMPLE_POINTS, distanceToPlotBoundaries * 2);
		}
		double pointsPerSide = Math.sqrt(samplePoints);
		return (int) Math.floor((plotSide - (distanceToPlotBoundaries * 2)) / (pointsPerSide - 1));
	}

	// ========== Options that apply to the selected plot shape ==========

	public boolean isSquarePlot() {
		return plotShape == CollectEarthPlotShape.SQUARE || plotShape == CollectEarthPlotShape.SQUARE_WITH_LARGE_CENTRAL_PLOT;
	}

	public boolean isRoundPlot() {
		return plotShape == CollectEarthPlotShape.CIRCLE || plotShape == CollectEarthPlotShape.HEXAGON;
	}

	public boolean isNfiClusterPlot() {
		return plotShape == CollectEarthPlotShape.NFI_THREE_CIRCLES || plotShape == CollectEarthPlotShape.NFI_FOUR_CIRCLES;
	}

	public boolean isSamplePointsApplicable() {
		return isSquarePlot() || isRoundPlot();
	}

	/**
	 * The same value is the distance between the points of a square plot and the radius of round and NFI cluster plots.
	 * A square plot with one point or none is sized by its margin alone.
	 */
	public boolean isDistanceBetweenSamplePointsApplicable() {
		return isRoundPlot() || isNfiClusterPlot() || (isSquarePlot() && samplePoints > 1);
	}

	public boolean isDistanceToPlotBoundariesApplicable() {
		return isSquarePlot();
	}

	/**
	 * Without sample points there are no dots to size
	 */
	public boolean isInnerPointSideApplicable() {
		return isNfiClusterPlot() || (isSamplePointsApplicable() && samplePoints >= 1);
	}

	public boolean isLargeCentralPlotSideApplicable() {
		return plotShape == CollectEarthPlotShape.SQUARE_WITH_LARGE_CENTRAL_PLOT;
	}

	public boolean isDistanceBetweenPlotsApplicable() {
		return isNfiClusterPlot();
	}

	public boolean isReferenceAreaApplicable() {
		return isSquarePlot() || isRoundPlot();
	}

	public boolean isReferenceAreaEnabled() {
		return isReferenceAreaApplicable() && referenceAreaShape != CollectEarthReferenceAreaShape.NONE;
	}

	// ========== Plot and reference area geometry ==========

	/**
	 * Half of the side of a square plot (the side is the sample point grid plus the margin on both ends)
	 * or the radius of a round plot.
	 */
	private double getPlotHalfExtent() {
		if (isRoundPlot()) {
			return distanceBetweenSamplePoints;
		}
		double side = samplePoints <= 1
				? 2d * distanceToPlotBoundaries
				: 2d * distanceToPlotBoundaries + (Math.sqrt(samplePoints) - 1) * distanceBetweenSamplePoints;
		return side / 2d;
	}

	/**
	 * Half extent of the outline actually drawn in Google Earth
	 */
	private double getPlotOutlineHalfExtent() {
		return isRoundPlot() ? getPlotHalfExtent() + ROUND_PLOT_OUTLINE_MARGIN : getPlotHalfExtent();
	}

	/**
	 * Plot area in square meters (only square and round plots have one)
	 */
	public double getPlotArea() {
		double halfExtent = getPlotHalfExtent();
		if (plotShape == CollectEarthPlotShape.CIRCLE) {
			return Math.PI * halfExtent * halfExtent;
		} else if (plotShape == CollectEarthPlotShape.HEXAGON) {
			return HEXAGON_AREA_FACTOR * halfExtent * halfExtent;
		}
		return 4 * halfExtent * halfExtent;
	}

	public double getPlotAreaHectares() {
		return getPlotArea() / SQUARE_METERS_PER_HECTARE;
	}

	/**
	 * Area in square meters enclosed by a reference area at the given distance from the plot center. The distance means what
	 * Collect Earth draws: half side of a square reference area, radius of a circular one and distance from the
	 * center to a vertex of a hexagonal one.
	 */
	public double getReferenceArea(double distance) {
		switch (referenceAreaShape) {
		case CIRCLE:
			return Math.PI * distance * distance;
		case HEXAGON:
			return HEXAGON_AREA_FACTOR * distance * distance;
		default:
			return 4 * distance * distance;
		}
	}

	/**
	 * Null when there is no reference area
	 */
	public Double getReferenceAreaHectares() {
		if (!isReferenceAreaEnabled() || referenceAreaDistance == null) {
			return null;
		}
		return getReferenceArea(referenceAreaDistance) / SQUARE_METERS_PER_HECTARE;
	}

	/**
	 * Smallest reference area distance at which the reference area still encloses the whole plot outline, so that the reference area is always
	 * larger than the plot.
	 */
	public int getMinimumReferenceAreaDistance() {
		double halfExtent = getPlotOutlineHalfExtent();
		boolean roundPlot = isRoundPlot();
		double minimum;
		switch (referenceAreaShape) {
		case CIRCLE:
			// the circle has to reach the corners of a square plot
			minimum = roundPlot ? halfExtent : halfExtent * Math.sqrt(2);
			break;
		case HEXAGON:
			// the flat sides of the hexagon lie at distance * cos(30) from the center. The corner (h, h) of a square
			// plot leans against a slanted side, at h * (cos(30) + sin(30)) from the center
			minimum = roundPlot ? halfExtent / COS_30 : halfExtent * (COS_30 + 0.5) / COS_30;
			break;
		default:
			minimum = halfExtent;
		}
		return Math.max(MIN_REFERENCE_AREA_DISTANCE, (int) Math.ceil(minimum) + 1);
	}

	/**
	 * Reference area distance at which the reference area is {@link #MIN_REFERENCE_AREA_TO_PLOT_AREA_RATIO} times the plot area
	 */
	public int getRecommendedReferenceAreaDistance() {
		double targetArea = MIN_REFERENCE_AREA_TO_PLOT_AREA_RATIO * getPlotArea();
		double distance;
		switch (referenceAreaShape) {
		case CIRCLE:
			distance = Math.sqrt(targetArea / Math.PI);
			break;
		case HEXAGON:
			distance = Math.sqrt(targetArea / HEXAGON_AREA_FACTOR);
			break;
		default:
			distance = Math.sqrt(targetArea) / 2d;
		}
		return Math.min(MAX_REFERENCE_AREA_DISTANCE, Math.max((int) Math.ceil(distance), getMinimumReferenceAreaDistance()));
	}

	/**
	 * True when the reference area shows too little of the surroundings of the plot
	 */
	public boolean isReferenceAreaTooSmall() {
		return isReferenceAreaEnabled() && referenceAreaDistance != null
				&& getReferenceArea(referenceAreaDistance) < MIN_REFERENCE_AREA_TO_PLOT_AREA_RATIO * getPlotArea();
	}

	// ========== Getters and setters ==========

	public CollectEarthPlotShape getPlotShape() {
		return plotShape;
	}

	public void setPlotShape(CollectEarthPlotShape plotShape) {
		this.plotShape = plotShape == null ? CollectEarthPlotShape.SQUARE : plotShape;
	}

	public int getSamplePoints() {
		return samplePoints;
	}

	public void setSamplePoints(int samplePoints) {
		this.samplePoints = samplePoints;
	}

	public int getDistanceBetweenSamplePoints() {
		return distanceBetweenSamplePoints;
	}

	public void setDistanceBetweenSamplePoints(int distanceBetweenSamplePoints) {
		this.distanceBetweenSamplePoints = distanceBetweenSamplePoints;
	}

	public int getDistanceToPlotBoundaries() {
		return distanceToPlotBoundaries;
	}

	public void setDistanceToPlotBoundaries(int distanceToPlotBoundaries) {
		this.distanceToPlotBoundaries = distanceToPlotBoundaries;
	}

	public int getInnerPointSide() {
		return innerPointSide;
	}

	public void setInnerPointSide(int innerPointSide) {
		this.innerPointSide = innerPointSide;
	}

	public int getLargeCentralPlotSide() {
		return largeCentralPlotSide;
	}

	public void setLargeCentralPlotSide(int largeCentralPlotSide) {
		this.largeCentralPlotSide = largeCentralPlotSide;
	}

	public int getDistanceBetweenPlots() {
		return distanceBetweenPlots;
	}

	public void setDistanceBetweenPlots(int distanceBetweenPlots) {
		this.distanceBetweenPlots = distanceBetweenPlots;
	}

	public CollectEarthReferenceAreaShape getReferenceAreaShape() {
		return referenceAreaShape;
	}

	public void setReferenceAreaShape(CollectEarthReferenceAreaShape referenceAreaShape) {
		this.referenceAreaShape = referenceAreaShape == null ? CollectEarthReferenceAreaShape.NONE : referenceAreaShape;
	}

	public Integer getReferenceAreaDistance() {
		return referenceAreaDistance;
	}

	public void setReferenceAreaDistance(Integer referenceAreaDistance) {
		this.referenceAreaDistance = referenceAreaDistance;
	}
}
