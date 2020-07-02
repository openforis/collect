package org.openforis.collect.io.metadata.samplingdesign;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SpatialReferenceSystem;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignLineValidator {
	
	private static final String INVALID_X_MESSAGE_KEY = "survey.sampling_point_data.import_data.error.invalid_x";
	private static final String INVALID_Y_MESSAGE_KEY = "survey.sampling_point_data.import_data.error.invalid_y";
	private static final String INVALID_SRS_ID_MESSAGE_KEY = "survey.sampling_point_data.import_data.error.invalid_srs_id";
	
	private CollectSurvey survey;
	private List<ParsingError> errors;
	
	SamplingDesignLineValidator(CollectSurvey survey) {
		super();
		this.survey = survey;
		this.errors = new ArrayList<ParsingError>();
	}

	public static SamplingDesignLineValidator createInstance(CollectSurvey survey) {
		return new SamplingDesignLineValidator(survey);
	}

	public void validate(SamplingDesignLine line) {
		validateX(line);
		validateY(line);
		validateSrsId(line);
	}
	
	protected void validateX(SamplingDesignLine line) {
		String xStr = line.getX();
		try {
			Double.parseDouble(xStr);
		} catch (NumberFormatException e) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, 
					line.getLineNumber(), 
					SamplingDesignFileColumn.X.getColumnName(), 
					INVALID_X_MESSAGE_KEY);
			errors.add(error);
		}
	}

	protected void validateY(SamplingDesignLine line) {
		String yStr = line.getY();
		try {
			Double.parseDouble(yStr);
		} catch (NumberFormatException e) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, 
					line.getLineNumber(), 
					SamplingDesignFileColumn.Y.getColumnName(), 
					INVALID_Y_MESSAGE_KEY);
			errors.add(error);
		}
	}
	
	protected void validateSrsId(SamplingDesignLine line) {
		String srsId = line.getSrsId();
		SpatialReferenceSystem srs = survey.getSpatialReferenceSystem(srsId);
		if ( srs == null ) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, 
					line.getLineNumber(), 
					SamplingDesignFileColumn.SRS_ID.getColumnName(), 
					INVALID_SRS_ID_MESSAGE_KEY);
			error.setMessageArgs(new String[] {srsId});
			errors.add(error);
		}
	}

	public List<ParsingError> getErrors() {
		return errors;
	}

}
