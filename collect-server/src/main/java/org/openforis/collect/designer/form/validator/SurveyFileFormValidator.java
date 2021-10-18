package org.openforis.collect.designer.form.validator;

import static org.openforis.collect.designer.form.SurveyFileFormObject.FILENAMES_FIELD_NAME;
import static org.openforis.collect.designer.form.SurveyFileFormObject.TYPE_FIELD_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.designer.viewmodel.SurveyFileVM;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyFileFormValidator extends FormValidator {
	
	protected static final String SURVEY_MANAGER_ARG = "surveyManager";
	
	private static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[\\w-\\.]+\\.[\\w-]+$");
	
	private static final String SAIKU_QUERY_FILE_EXTENSION = "saiku";
	
	private static final Set<String> RESERVED_FILENAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			SurveyFileType.COLLECT_EARTH_AREA_PER_ATTRIBUTE.getFixedFilename(), "balloon.html", "collectEarthCubes.xml.fmt", "kml_template.fmt", "placemark.idm.xml",
			"project_definition.properties", "README.txt", "test_plots.ced", "earthFiles",
			"data", "files", "sampling_design", "species", "idml.xml", "info.properties")));

	@Override
	protected void internalValidate(ValidationContext ctx) {
		if (validateTypeUniqueness(ctx)) {
			if (validateFilenames(ctx)) {
				validateFilenamesUniqueness(ctx);
			}
		}
	}

	private boolean validateTypeUniqueness(ValidationContext ctx) {
		List<SurveyFile> otherSurveyFiles = loadSurveyFilesDifferentFromThis(ctx);
		String typeName = getValue(ctx, TYPE_FIELD_NAME);
		SurveyFileType type = SurveyFileType.valueOf(typeName);
		switch (type) {
		case SURVEY_GUIDE:
		case COLLECT_EARTH_AREA_PER_ATTRIBUTE:
		case COLLECT_EARTH_EE_SCRIPT:
			if (containsFileWithType(otherSurveyFiles, type)) {
				addInvalidMessage(ctx, TYPE_FIELD_NAME, Labels.getLabel("survey.file.error.type_already_defined"));
				return false;
			} else {
				return true;
			}
		default:
			return true;
		}
	}

	private boolean validateFilenames(ValidationContext ctx) {
		if (validateRequired(ctx, FILENAMES_FIELD_NAME)) { 
			String filenames = getValue(ctx, FILENAMES_FIELD_NAME);
			boolean valid = true;
			for (String filename : filenames.split("\n")) {
				if (!validateFilenamePattern(ctx, filename)) {
					return false;
				}
			}
			if (valid) {
				return validateFilenamesUniqueness(ctx);
			}
		}
		return false;
	}
	
	private boolean validateFilenamePattern(ValidationContext ctx, String filename) {
		if (validateRegEx(ctx, VALID_FILENAME_PATTERN, FILENAMES_FIELD_NAME, "survey.file.error.invalid_filename")) {
			String typeName = getValue(ctx, TYPE_FIELD_NAME);
			SurveyFileType type = SurveyFileType.valueOf(typeName);
			switch (type) {
			case COLLECT_EARTH_AREA_PER_ATTRIBUTE:
				String expectedFileName = SurveyFileType.COLLECT_EARTH_AREA_PER_ATTRIBUTE.getFixedFilename();
				if (!expectedFileName.equals(filename)) {
					String message = Labels.getLabel("survey.file.error.unexpected_filename",
							new String[] { expectedFileName });
					addInvalidMessage(ctx, message);
					return false;
				}
			case COLLECT_EARTH_SAIKU_QUERY:
				String extension = FilenameUtils.getExtension(filename);
				if (! SAIKU_QUERY_FILE_EXTENSION.equalsIgnoreCase(extension)) {
					String message = Labels.getLabel("survey.file.error.invalid_extension", 
							new String[] { SAIKU_QUERY_FILE_EXTENSION, extension });
					addInvalidMessage(ctx, FILENAMES_FIELD_NAME, message);
					return false;
				}
			default:
				if (RESERVED_FILENAMES.contains(filename)) {
					addInvalidMessage(ctx, FILENAMES_FIELD_NAME, Labels.getLabel("survey.file.error.reserved_filename"));
					return false;
				} else {
					return true;
				}
			}
		} else {
			return false;
		}
	}

	private boolean validateFilenamesUniqueness(ValidationContext ctx) {
		List<SurveyFile> otherSurveyFiles = loadSurveyFilesDifferentFromThis(ctx);
		String filenames = getValue(ctx, FILENAMES_FIELD_NAME);
		List<String> filenamesList = Arrays.asList(filenames.split("\n"));
		for (SurveyFile surveyFile : otherSurveyFiles) {
			if (filenamesList.contains(surveyFile.getFilename())) {
				addInvalidMessage(ctx, FILENAMES_FIELD_NAME, Labels.getLabel("survey.file.error.duplicate_filename"));
				return false;
			}
		}
		return true;
	}
	
	private List<SurveyFile> loadSurveyFilesDifferentFromThis(ValidationContext ctx) {
		List<SurveyFile> result = new ArrayList<SurveyFile>();
		SurveyFileVM vm = getVM(ctx);
		CollectSurvey survey = vm.getSurvey();
		SurveyManager surveyManager = getSurveyManager(ctx);
		List<SurveyFile> surveyFiles = surveyManager.loadSurveyFileSummaries(survey);
		SurveyFile editedSurveyFile = vm.getEditedItem();
		for (SurveyFile surveyFile : surveyFiles) {
			if (! surveyFile.getId().equals(editedSurveyFile.getId())) {
				result.add(surveyFile);
			}
		}
		return result;
	}
	
	private boolean containsFileWithType(List<SurveyFile> files, SurveyFileType type) {
		for (SurveyFile surveyFile : files) {
			if (type == surveyFile.getType()) {
				return true;
			}
		}
		return false;
	}
	
	protected SurveyManager getSurveyManager(ValidationContext ctx) {
		return (SurveyManager) ctx.getValidatorArg(SURVEY_MANAGER_ARG);
	}


}
