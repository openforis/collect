package org.openforis.collect.designer.form.validator;

import static org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout.*;

import org.openforis.collect.designer.form.SurveyMainInfoFormObject;
import org.openforis.collect.designer.viewmodel.SurveyBaseVM;
import org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 *
 * @author S. Ricci
 *
 */
public class SurveyMainInfoFormValidator extends FormValidator {

	protected static final String NAME_FIELD = "name";
	protected static final String COLLECT_EARTH_PLOT_SHAPE_FIELD = "collectEarthPlotShape";
	protected static final String COLLECT_EARTH_SAMPLE_POINTS_FIELD = "collectEarthSamplePoints";
	protected static final String COLLECT_EARTH_DISTANCE_BETWEEN_SAMPLE_POINTS_FIELD = "collectEarthDistanceBetweenSamplePoints";
	protected static final String COLLECT_EARTH_DISTANCE_TO_PLOT_BOUNDARIES_FIELD = "collectEarthDistanceToPlotBoundaries";
	protected static final String COLLECT_EARTH_INNER_POINT_SIDE_FIELD = "collectEarthInnerPointSide";
	protected static final String COLLECT_EARTH_LARGE_CENTRAL_PLOT_SIDE_FIELD = "collectEarthLargeCentralPlotSide";
	protected static final String COLLECT_EARTH_DISTANCE_BETWEEN_PLOTS_FIELD = "collectEarthDistanceBetweenPlots";
	protected static final String COLLECT_EARTH_REFERENCE_AREA_SHAPE_FIELD = "collectEarthReferenceAreaShape";
	protected static final String COLLECT_EARTH_REFERENCE_AREA_DISTANCE_FIELD = "collectEarthReferenceAreaDistance";

	private static final String REFERENCE_AREA_NOT_ENCLOSING_PLOT_MESSAGE_KEY = "survey.collect_earth.plot_layout.reference_area_distance.error.not_enclosing_plot";

	public SurveyMainInfoFormValidator() {
		blocking = true;
	}

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		if (isCollectEarthSurvey(ctx)) {
			validateCollectEarthPlotLayout(ctx);
		}
	}

	protected boolean validateName(ValidationContext ctx) {
		String field = NAME_FIELD;
		if ( validateRequired(ctx, field) && validateInternalName(ctx, field) ) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isCollectEarthSurvey(ValidationContext ctx) {
		Object vm = getVM(ctx);
		return vm instanceof SurveyBaseVM && ((SurveyBaseVM) vm).isCollectEarthSurvey();
	}

	/**
	 * Only the options used by the selected plot shape are validated, with the same limits of the plot options of Collect Earth
	 */
	protected void validateCollectEarthPlotLayout(ValidationContext ctx) {
		CollectEarthPlotLayout layout = SurveyMainInfoFormObject.createCollectEarthPlotLayout(
				this.<String>getValue(ctx, COLLECT_EARTH_PLOT_SHAPE_FIELD, false),
				this.<String>getValue(ctx, COLLECT_EARTH_SAMPLE_POINTS_FIELD, false),
				this.<Integer>getValue(ctx, COLLECT_EARTH_DISTANCE_BETWEEN_SAMPLE_POINTS_FIELD, false),
				this.<Integer>getValue(ctx, COLLECT_EARTH_DISTANCE_TO_PLOT_BOUNDARIES_FIELD, false),
				this.<Integer>getValue(ctx, COLLECT_EARTH_INNER_POINT_SIDE_FIELD, false),
				this.<Integer>getValue(ctx, COLLECT_EARTH_LARGE_CENTRAL_PLOT_SIDE_FIELD, false),
				this.<Integer>getValue(ctx, COLLECT_EARTH_DISTANCE_BETWEEN_PLOTS_FIELD, false),
				this.<String>getValue(ctx, COLLECT_EARTH_REFERENCE_AREA_SHAPE_FIELD, false),
				this.<Integer>getValue(ctx, COLLECT_EARTH_REFERENCE_AREA_DISTANCE_FIELD, false));

		boolean plotValid = true;
		if (layout.isDistanceBetweenSamplePointsApplicable()) {
			plotValid &= validateRange(ctx, COLLECT_EARTH_DISTANCE_BETWEEN_SAMPLE_POINTS_FIELD,
					MIN_DISTANCE_BETWEEN_SAMPLE_POINTS, MAX_DISTANCE_BETWEEN_SAMPLE_POINTS);
		}
		if (layout.isDistanceToPlotBoundariesApplicable()) {
			plotValid &= validateRange(ctx, COLLECT_EARTH_DISTANCE_TO_PLOT_BOUNDARIES_FIELD,
					MIN_DISTANCE_TO_PLOT_BOUNDARIES, MAX_DISTANCE_TO_PLOT_BOUNDARIES);
		}
		if (layout.isInnerPointSideApplicable()) {
			validateRange(ctx, COLLECT_EARTH_INNER_POINT_SIDE_FIELD, MIN_INNER_POINT_SIDE, MAX_INNER_POINT_SIDE);
		}
		if (layout.isLargeCentralPlotSideApplicable()) {
			validateRange(ctx, COLLECT_EARTH_LARGE_CENTRAL_PLOT_SIDE_FIELD,
					MIN_LARGE_CENTRAL_PLOT_SIDE, MAX_LARGE_CENTRAL_PLOT_SIDE);
		}
		if (layout.isDistanceBetweenPlotsApplicable()) {
			validateRange(ctx, COLLECT_EARTH_DISTANCE_BETWEEN_PLOTS_FIELD,
					MIN_DISTANCE_BETWEEN_PLOTS, MAX_DISTANCE_BETWEEN_PLOTS);
		}
		if (layout.isReferenceAreaEnabled()
				&& validateRange(ctx, COLLECT_EARTH_REFERENCE_AREA_DISTANCE_FIELD, MIN_REFERENCE_AREA_DISTANCE, MAX_REFERENCE_AREA_DISTANCE)
				&& plotValid) {
			validateReferenceAreaEnclosesPlot(ctx, layout);
		}
	}

	private boolean validateRange(ValidationContext ctx, String field, int min, int max) {
		return validateRequired(ctx, field)
				&& validateGreaterThan(ctx, field, min, false)
				&& validateLessThan(ctx, field, max, false);
	}

	/**
	 * The reference area gives an idea of the surroundings of the plot, so it must always be larger than the plot
	 */
	private boolean validateReferenceAreaEnclosesPlot(ValidationContext ctx, CollectEarthPlotLayout layout) {
		int minimum = layout.getMinimumReferenceAreaDistance();
		if (layout.getReferenceAreaDistance() < minimum) {
			addInvalidMessage(ctx, COLLECT_EARTH_REFERENCE_AREA_DISTANCE_FIELD,
					Labels.getLabel(REFERENCE_AREA_NOT_ENCLOSING_PLOT_MESSAGE_KEY, new Object[] {minimum}));
			return false;
		}
		return true;
	}

}
