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
	
	private static final String INVALID_SRS_MESSAGE_KEY = "samplingDesignImport.parsingError.invalidSRS";
	private static final String INVALID_LATITUDE_MESSAGE_KEY = "samplingDesignImport.parsingError.invalidLatitude";
	private static final String INVALID_LONGITUDE_MESSAGE_KEY = "samplingDesignImport.parsingError.invalidLongitude";
	
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
		validateSRSId(line);
		validateLatitude(line);
		validateLatitude(line);
		validateLongitude(line);
	}
	
	protected void validateSRSId(SamplingDesignLine line) {
		String srsId = line.getSrsId();
		SpatialReferenceSystem srs = survey.getSpatialReferenceSystem(srsId);
		if ( srs == null ) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, 
					line.getLineNumber(), 
					SamplingDesignFileColumn.SRS_ID.getName(), 
					INVALID_SRS_MESSAGE_KEY);
			errors.add(error);
		}
	}

	protected void validateLatitude(SamplingDesignLine line) {
		String latitudeStr = line.getLatitude();
		try {
			Integer.parseInt(latitudeStr);
			//if ( latitude < )
		} catch (NumberFormatException e) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, 
					line.getLineNumber(), 
					SamplingDesignFileColumn.LATITUDE.getName(), 
					INVALID_LATITUDE_MESSAGE_KEY);
			errors.add(error);
		}
	}

	protected void validateLongitude(SamplingDesignLine line) {
		String latitudeStr = line.getLongitude();
		try {
			Integer.parseInt(latitudeStr);
			//if ( latitude < )
		} catch (NumberFormatException e) {
			ParsingError error = new ParsingError(ErrorType.INVALID_VALUE, 
					line.getLineNumber(), 
					SamplingDesignFileColumn.LONGITUDE.getName(), 
					INVALID_LONGITUDE_MESSAGE_KEY);
			errors.add(error);
		}
	}
	
	public List<ParsingError> getErrors() {
		return errors;
	}

}
