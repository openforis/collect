package org.openforis.collect.designer.form.validator;

import java.util.regex.Pattern;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SurveyFileFormValidator extends FormValidator {
	
	protected static final String FILENAME_FIELD = "filename";
	private static final Pattern VALID_FILENAME_PATTERN = Pattern.compile("^[\\w-_]+\\.[\\w-_]+$");

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateFilename(ctx);
	}

	private boolean validateFilename(ValidationContext ctx) {
		return validateRequired(ctx, FILENAME_FIELD) 
				&& validateRegEx(ctx, VALID_FILENAME_PATTERN, FILENAME_FIELD, "survey.file.invalid_filename");
	}
	
}
