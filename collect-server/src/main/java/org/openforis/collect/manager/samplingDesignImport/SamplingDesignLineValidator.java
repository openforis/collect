package org.openforis.collect.manager.samplingDesignImport;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.referenceDataImport.ParsingError;
import org.openforis.collect.manager.referenceDataImport.ParsingError.ErrorType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SpatialReferenceSystem;

/**
 * 
 * @author S. Ricci
 *
 */
public class SamplingDesignLineValidator {
	
	private static final String INVALID_X_MESSAGE_KEY = "samplingDesignImport.parsingError.invalidX";
	private static final String INVALID_Y_MESSAGE_KEY = "samplingDesignImport.parsingError.invalidY";
	private static final String INVALID_SRS_ID_MESSAGE_KEY = "samplingDesignImport.parsingError.invalidSrsId";
	
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
					SamplingDesignFileColumn.X.getName(), 
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
					SamplingDesignFileColumn.Y.getName(), 
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
					SamplingDesignFileColumn.SRS_ID.getName(), 
					INVALID_SRS_ID_MESSAGE_KEY);
			errors.add(error);
		}
	}

	public List<ParsingError> getErrors() {
		return errors;
	}

}
