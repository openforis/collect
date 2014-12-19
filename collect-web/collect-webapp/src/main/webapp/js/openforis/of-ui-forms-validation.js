OF.UI.Forms.Validation = function() {};

/**
 * Remove all validation errors shown in the form
 * 
 * @param $form
 */
OF.UI.Forms.Validation.removeErrors = function ($form) {
	$form.find('.form-group').removeClass('has-error');
	$form.find('.form-control').tooltip('destroy');
};

/**
 * Update the validation errors in the form
 * 
 * @param $form
 * @param errors
 */
OF.UI.Forms.Validation.updateErrors = function (form, errors) {
	OF.UI.Forms.Validation.removeErrors( form );
	
	$.each( errors, function( i, error ){
		var fieldName = error.field;
		var field = form.find( '[name="'+ fieldName + '"]' );
		if ( field != null ) {
			var formGroup = field.closest( '.form-group' );
			if ( !formGroup.hasClass('has-error') ){
				formGroup.addClass('has-error');
	
				OF.UI.Forms.Validation.createErrorTooltip(field, error);
			}
		}
	});
};

/**
 * Returns the message associated to the error with the specified field name.
 * If field name is not specified, than the generic form error message is returned.
 */
OF.UI.Forms.Validation.getFieldErrorMessage = function (errors, fieldName) {
	for (var i=0; i < errors.length; i++) {
		var error = errors[i];
		if ( ! fieldName && ! error.field || fieldName == error.field) {
			return error.defaultMessage;
		}
	}
	return null;
};

/**
 * Returns the error message associated to the error with no field name specified
 * or the one associated to the first error.
 */
OF.UI.Forms.Validation.getFormErrorMessage = function ($form, errors) {
	var genericErrorMessage = OF.UI.Forms.Validation.getFieldErrorMessage(errors);
	var errorMessage;
	if ( genericErrorMessage == null ) {
		var firstError = errors[0];
		var fieldName = firstError.field;
		var fieldErrorMessage = firstError.defaultMessage;
		var field = $form.find( '[name="' + fieldName + '"]' );
		var fieldLabel = OF.UI.Forms.getFieldLabel(field);
		errorMessage =  fieldLabel + " " + fieldErrorMessage;
	} else {
		errorMessage = genericErrorMessage;
	}
	return errorMessage;
};

/**
 * Create an error tooltip associated to a validation error
 * 
 * @param $field
 * @param error
 * @param fieldLabel (optional) if not specified, field label will be assigned using getFieldLabel function
 */
OF.UI.Forms.Validation.createErrorTooltip = function ($field, error, fieldLabel) {
	if ( ! fieldLabel ) {
		fieldLabel = OF.UI.Forms.getFieldLabel($field);
	}
	var errorMessage;
	if (typeof error == "string") {
		errorMessage = error;
	} else if (error.hasOwnProperty("defaultMessage")) {
		errorMessage = error.defaultMessage;
	} else {
		errorMessage = "error";
	}
	var message = fieldLabel + " " + errorMessage;
	
	var $parentModal = $field.closest('.modal');
	var container = $parentModal.length == 0 ? 'body': $parentModal; 
	
	var inputType = OF.UI.Forms.getInputType($field);
	var $targetField = inputType == 'hidden' ? $targetField = $field.siblings('.form-control'): $field;
	
	$targetField.tooltip({
		title: message,
		container: container,
		template: '<div class="tooltip error"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>'
	});
};
