var SEPARATOR_MULTIPLE_PARAMETERS = "==="; //used to separate multiple attribute VALUES
var SEPARATOR_MULTIPLE_VALUES = ";";
var DATE_FORMAT = 'MM/DD/YYYY';
var TIME_FORMAT = 'HH:ss';
var ACTIVELY_SAVED_FIELD_ID = "collect_boolean_actively_saved";
var NESTED_ATTRIBUTE_ID_PATTERN = /\w+\[\w+\]\.\w+/;

var $form = null; //to be initialized
var lastUpdateRequest = null; //last update request sent to the server
var currentStepIndex = null;

//To be used by the method that saves the data automatically when the user
//interacts with the form
//DO NOT REMOVE
var ajaxTimeout = null;
var consoleBox = null;

$(function() {
	if (DEBUG) {
		initLogConsole();
		log("initializing");
		log("using host: " + HOST);
	}

	$form = $("#formAll");
	$stepsContainer = $(".steps");

	initSteps();
	fillYears();
	initCodeButtonGroups();
	initDateTimePickers();
	initBooleanButtons();
	initializeChangeEventSaver();
	// Declares the Jquery Dialog ( The Bootstrap dialog does
	// not work in Google Earth )
	$("#dialogSuccess").dialog({
		modal : true,
		width : "400",
		autoOpen : false,
		buttons : {
			Ok : function() {
				$(this).dialog("close");
			}
		}
	});
	// SAVING DATA WHEN USER SUBMITS
	$form.submit(function(e) {
		e.preventDefault();
		
		clearTimeout(ajaxTimeout); 	// So that the form
									// is not saved twice
									// if the user
									// clicks the submit
									// button before the
									// auto-save timeout
									// has started
									// Mark this as the "real submit" (as opposed
									// when saving data just because the user closes
									// the window) so we can show the placemark as
									// interpreted
		setActivelySaved(true);

		submitForm($(this), 0);
	});

	$(".code-item").tooltip();

	checkIfPlacemarkAlreadyFilled(0);
});

var ajaxDataUpdate = function(delay, timesTried) {
	if (typeof delay == "undefined") {
		delay = 100;
	}
	if (DEBUG) {
		log("sending update request (delay=" + delay + ")");
	}
	abortLastUpdateRequest();

	setActivelySaved(false);

	// Set a timeout so that the data is only sent to the server
	// if the user stops clicking for over one second

	ajaxTimeout = setTimeout(function() {
		var data = createPlacemarkUpdateRequest();
		
		lastUpdateRequest = $.ajax({
			data : data,
			type : "POST",
			url : $form.attr("action"),
			timeout: 1000,
			dataType : 'json'
		})
		.done(function(json) {
			if (DEBUG) {
				log("data updated successfully");
			}
			interpretJsonSaveResponse(json, false);
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			if (DEBUG) {
				log("error updating data. Text status = " + textStatus + "; error thrown = " + errorThrown );
			}
			// try again
			if (typeof timesTried == "undefined") {
				timesTried = 0;
			}
			if( timesTried < 5){
				ajaxDataUpdate(delay, timesTried + 1);
			}
		})
		.always(function() {
			lastUpdateRequest = null;
		});
	}, delay);
};

var createPlacemarkUpdateRequest = function() {

	// Remove the value from the fields that are hidden!
	$(".notrelevant").find("input[type='hidden']").each( function(){
		$(this).val('');
	});
	$(".notrelevant").find(":input:not(:button)").each( function(){
		$(this).val('');
	});
	
	var data = {
		placemarkId : getPlacemarkId(),
		values : serializeFormToJSON($form),
		currentStep : currentStepIndex
	};
	return data;
}

var abortLastUpdateRequest = function() {
	clearTimeout(ajaxTimeout);

	if (lastUpdateRequest != null) {
		if (DEBUG) {
			log("abort last update request");
		}
		lastUpdateRequest.abort();
		lastUpdateRequest = null;
	}
};

var submitForm = function(submitCounter) {
	abortLastUpdateRequest();

	var data = createPlacemarkUpdateRequest();

	lastUpdateRequest = $.ajax({
		data : data,
		type : "POST",
		url : $form.attr("action"),
		dataType : 'json',
		timeout : 10000,
		beforeSend : function() {
			$.blockUI({
				message : 'Sumitting data..'
			});
		}
	})
	.done(function(json) {
		if (DEBUG) {
			log("data submitted successfully");
		}
		setStepsAsVisited();
		interpretJsonSaveResponse(json, true);
	})
	.fail(function(jqXHR, textStatus, errorThrown) {
			if (DEBUG) {
				log("Error submitting data " + textStatus + " - " + errorThrown);
			}
			
			// try again
			if (typeof submitCounter == "undefined") {
				submitCounter = 0;
			}
			
			if (submitCounter < 5) {
				submitForm(submitCounter + 1);
			} else {
				showErrorMessage("Cannot save the data, the Collect Earth server is not running!");
			}
			
	}).always(function() {
		lastUpdateRequest = null;
		$.unblockUI();
	});
};

var interpretJsonSaveResponse = function(json, showUpdateMessage) {
	if (showUpdateMessage) { // show feedback message
		if (json.success) {
			if (json.validData) {
				showSuccessMessage(json.message);
				forceWindowCloseAfterDialogCloses($("#dialogSuccess"));
			} else {
				var message = "";
				$.each(json.inputFieldInfoByParameterName, function(key, info) {
					if (info.inError) {
						var inputField = findById(key);
						var label;
						if (NESTED_ATTRIBUTE_ID_PATTERN.test(key)) {
							label = getEnumeratedEntityNestedAttributeErrorMessageLabel(inputField);
						} else {
							label = inputField.length > 0 ? OF.UI.Forms.getFieldLabel(inputField): "";
						}
						message += label + " : " + info.errorMessage + "<br>";
					}
				});
				showErrorMessage(message);

				// Resets the "actively saved" parameter to false so that it is
				// not sent as true when the user fixes the validation
				setActivelySaved(false);
			}
		} else {
			showErrorMessage(json.message);
		}
	}
	updateInputFieldsState(json.inputFieldInfoByParameterName);
	fillDataWithJson(json.inputFieldInfoByParameterName);
};

var getEnumeratedEntityNestedAttributeErrorMessageLabel = function(inputField) {
	var columnIndex = inputField.closest("td").index();
	var rowHeadingEl = inputField.closest("tr").find("td").eq(0);
	var rowHeading = rowHeadingEl.text();
	var attributeHeadingEl = inputField.closest("table").find("thead tr th").eq(columnIndex);
	var attributeHeading = attributeHeadingEl.text();
	var entityHeading = inputField.closest("fieldset").find("legend").text();
	var label = entityHeading + " (" + rowHeading + ") " + attributeHeading;
	return label;
};

var updateInputFieldsState = function(inputFieldInfoByParameterName) {
	if (DEBUG) {
		log("updating input fields state...");
		log("updating possible values in parent-child coded variables");
	}
	$.each(inputFieldInfoByParameterName, function(fieldName, info) {
		var el = findById(fieldName);
		if (el.length == 1) {
			var parentCodeFieldId = el.data("parentIdFieldId");
			var hasParentCode = parentCodeFieldId && parentCodeFieldId != "";
			if (hasParentCode) {
				switch (el.data("fieldType")) {
				case "CODE_SELECT":
					var oldValue = el.val();
					var possibleItems = info.possibleCodedItems ? info.possibleCodedItems: [];
					OF.UI.Forms.populateSelect(el, possibleItems, "code", "label");
					el.val(oldValue);
					if (el.val() == null) {
						//TODO set first option
						el.val("-1"); //set N/A option by default
					}
					break;
				case "CODE_BUTTON_GROUP":
					var parentCodeInfo = inputFieldInfoByParameterName[parentCodeFieldId];
					var parentCodeId = parentCodeInfo.codeItemId;
					var groupContainer = el.closest(".code-items-group");
					
					var validItemsContainer = groupContainer.find(".code-items[data-parent-id='" + parentCodeId + "']");
					if (validItemsContainer.is(':hidden')) {
						var itemsContainers = groupContainer.find(".code-items");
						itemsContainers.hide();
						
						validItemsContainer.show();
					}
					break;
				}
			}
		}
	});
	if (DEBUG) {
		log("updating errors feedback");
	}
	var errors = [];
	$.each(inputFieldInfoByParameterName, function(fieldName, info) {
		if (info.inError) {
			errors.push({
				field : fieldName,
				defaultMessage : info.errorMessage
			});
		}
	});
	
	OF.UI.Forms.Validation.updateErrors($form, errors, false, {doNotIncludeFieldLabel: true});

	updateStepsErrorFeedback();

	if (DEBUG) {
		log("updating fields relevance");
	}

	// manage fields visibility
	$.each(inputFieldInfoByParameterName, function(fieldName, info) {
		var field = findById(fieldName);
		var formGroup = field.closest('.form-group');
		formGroup.toggleClass("notrelevant", !(info.visible));
	});

	// manage tabs/steps visibility
	$form.find(".step").each(function(index, value) {
		var stepBody = $(this);
		var hasNestedVisibleFormFields = stepBody
				.find(".form-group:not(.notrelevant)").length > 0;
		toggleStepVisibility(index, hasNestedVisibleFormFields);
	});
	
	// manage entity visibility
	$form.find(".entity-group").each(function(index, value) {
		var entityBody = $(this);
		var hasNestedVisibleFormFields = entityBody
				.find(".form-group:not(.notrelevant)").length > 0;
		entityBody.toggleClass("notrelevant", !hasNestedVisibleFormFields);
	});
	
	if (DEBUG) {
		log("input fields state updated successfully");
	}
};

var getStepHeading = function(index) {
	var stepHeading = $form.find(".steps .steps ul li").eq(index);
	return stepHeading;
};

var toggleStepVisibility = function(index, visible) {
	var stepBody = $form.find(".step").eq(index);
	var stepHeading = getStepHeading(index);
	if (visible) {
		if (stepHeading.hasClass("notrelevant")) {
			stepHeading.removeClass("notrelevant");
			if (stepHeading.hasClass("done")) {
				stepHeading.removeClass("disabled");
			}
		}
	} else {
		stepHeading.addClass("disabled notrelevant");
	}
	stepHeading.toggle(visible);
	
	if (! stepHeading.hasClass("current")) {
		stepBody.hide();
	}
};

var showCurrentStep = function() {
	if (currentStepIndex != null && currentStepIndex > 0) {
		setStepsAsVisited(currentStepIndex);
		var currentStepHeading = getStepHeading(currentStepIndex); 
		//set current step active only if relevant
		var relevant = ! currentStepHeading.hasClass("notrelevant");
		if (relevant) {
			$stepsContainer.steps("setCurrentIndex", currentStepIndex);
		}
	}
};

var setStepsAsVisited = function(upToStepIndex) {
	if (! upToStepIndex) {
		upToStepIndex = $stepsContainer.find(".steps ul li").length - 1;
	}
	for (var stepIndex = 0; stepIndex <= upToStepIndex; stepIndex ++) {
		var stepHeading = getStepHeading(stepIndex);
		stepHeading.addClass("visited");
		stepHeading.removeClass("disabled");
	}
}

var updateStepsErrorFeedback = function() {
	$form.find(".step").each(function(index, value) {
		var stepHeading = getStepHeading(index);
		if (stepHeading.hasClass("visited")) {
			var hasErrors = $(this).find(".form-group.has-error").length > 0;
			stepHeading.toggleClass("error", hasErrors);
		}
	});
};

var initCodeButtonGroups = function() {
	$form.find("button.code-item").click(function(event) {
		event.preventDefault();
		// update hidden input field
		var btn = $(this);
		var wasActive = btn.hasClass("active");
		//btn.toggleClass("active", !wasActive);
		var itemsContainer = btn.closest(".code-items");
		var groupContainer = itemsContainer.closest(".code-items-group");
		var inputField = groupContainer.find("input[type='hidden']");
				
		if (itemsContainer.data("toggle") == "buttons") {
		
			if( btn.val() == "none" && !wasActive){
				//remove the other active buttons
				// deselect all code item buttons
				groupContainer.find(".code-item").removeClass('active');
			}else if( btn.val() != "none"  && !wasActive){
				// If none was selected and a value different than none was selected
				var activeNoneButton = itemsContainer.find("button[value='none'].active");
				activeNoneButton.removeClass('active');
			}else if( btn.val() != "none"  && wasActive){
				// Check that if there are no values selected then none is selected!
				var buttons = itemsContainer.find("button.active");
				if(buttons.length == 1 ){ // Only the current button, which will be deselected, is selected now
					var noneButton = itemsContainer.find("button[value='none']");
					if( noneButton && !noneButton.hasClass('active') ){
						noneButton.addClass('active');
					}
				}
			}
		
			// multiple selection
			var buttons = itemsContainer.find("button.active");
			var valueParts = [];
			buttons.each(function() {
				var activeButton = $(this);
				var activeButtonValue = activeButton.val();
				var clickedButtonValue = btn.val();
				if(clickedButtonValue !== activeButtonValue){ // Do not add the button clicked here, do it on the next block so we check that it was activated
					valueParts.push(activeButtonValue);
				}
			});
			if( !wasActive) { // means that the button was not active and will be activated
				valueParts.push(btn.val());
			}
			
			value = valueParts.join(SEPARATOR_MULTIPLE_PARAMETERS);
		} else {
			// single selection
			value = btn.val();
		}
		inputField.val(value);

		ajaxDataUpdate();
	});
};

var initBooleanButtons = function() {
	$('.boolean-group').each(function() {
		var group = $(this);
		var hiddenField = group.find("input[type='hidden']");
		group.find("button").click(function() {
			var btn = $(this);
			hiddenField.val(btn.val());
			ajaxDataUpdate();
		});
	});
};

var initDateTimePickers = function() {
	// http://eonasdan.github.io/bootstrap-datetimepicker/
	$('.datepicker').datetimepicker({
		format : DATE_FORMAT
	}).on('dp.change', function(e) {
		// var inputField = $(this).find(".form-control");
		// inputField.change();
		ajaxDataUpdate();
	});

	$('.timepicker').datetimepicker({
		format : TIME_FORMAT
	}).on('dp.change', function(e) {
		ajaxDataUpdate();
	});
};

var initSteps = function() {
	$steps = $stepsContainer.steps({
		headerTag : "h3",
		bodyTag : "section",
		transitionEffect : "none",
		autoFocus : true,
		titleTemplate : "#title#",
		labels : {
			finish : SUBMIT_LABEL,
		    next: NEXT_LABEL,
		    previous: PREVIOUS_LABEL
		},
		onStepChanged : function(event, currentIndex, priorIndex) {
			var stepHeading = getStepHeading(currentIndex);
			setStepsAsVisited(currentIndex);
			if (stepHeading.hasClass("notrelevant")) {
				if (currentIndex > priorIndex) {
					$stepsContainer.steps("next");
				} else {
					$stepsContainer.steps("previous");
				}
			} else {
				currentStepIndex = currentIndex;
				ajaxDataUpdate();
			}
		},
		onFinished : function(event, currentIndex) {
			$form.submit();
		}
	});
	$stepsContainer.find("a[href='#finish']").addClass("btn-finish");
	//$stepsContainer.find(".steps ul li").removeClass("disabled"); //enable all steps
};

var checkIfPlacemarkAlreadyFilled = function(checkCount) {

	var placemarkId = getPlacemarkId();

	$.ajax({data : {id : placemarkId},
		type : "GET",
		url : HOST + "placemark-info-expanded",
		dataType : 'json',
		timeout : 10000
	})
	.fail(function(jqXHR, textStatus, errorThrown) {
		// try again
		if (typeof checkCount == "undefined") {
			checkCount = 0;
		}
	
		if (checkCount < 5) {
			checkCount = checkCount + 1;
			checkIfPlacemarkAlreadyFilled(checkCount);
		} else {
			showErrorMessage("The Collect Earth server is not running!");
		}
	})
	.done(function(json) {
		if (json.success) {
			// placemark exists in database
			if (json.activelySaved
					&& json.inputFieldInfoByParameterName.collect_text_id.value != 'testPlacemark') { // 
	
				showErrorMessage("The data for this placemark has already been filled");
	
				if (json.skipFilled) {
					forceWindowCloseAfterDialogCloses($("#dialogSuccess"));
				}
			}
			setStepsAsVisited();
			// Pre-fills the form and after that initilizes the
			// change event listeners for the inputs
			updateInputFieldsState(json.inputFieldInfoByParameterName);
			fillDataWithJson(json.inputFieldInfoByParameterName);
				
			currentStepIndex = json.currentStep == null ? null
					: parseInt(json.currentStep);
			showCurrentStep();
		} else {
			// if no placemark in database, force the creation
			// of a new record
			ajaxDataUpdate();
		}
	});
};

var getPlacemarkId = function() {
	var arrayLength = EXTRA_ID_ATTRIBUTES.length;
	var id = "";
	for (var i = 0; i < arrayLength; i++) {
		id += $form.find("input[name='" + EXTRA_ID_ATTRIBUTES[i] + "']").val();
		if( i < arrayLength-1){
			id += ",";
		}
	}
	return id;
};

var isActivelySaved = function() {
	var activelySaved = findById(ACTIVELY_SAVED_FIELD_ID).val() == 'true';
	return activelySaved;
}

var setActivelySaved = function(value) {
	findById(ACTIVELY_SAVED_FIELD_ID).val(value == true || value == 'true');
}

var showSuccessMessage = function(message) {
	showMessage(message, "success");
};

var showWarningMessage = function(message) {
	showMessage(message, "warning");
};

var showErrorMessage = function(message) {
	showMessage(message, "error");
};

var showMessage = function(message, type) {
	var color;
	switch (type) {
	case "error":
		color = "red";
		break;
	case "warning":
		color = "yellow";
		break;
	case "success":
	default:
		color = "green";
	}
	$('#succ_mess').css("color", color).html(message ? message : "");
	$("#dialogSuccess").dialog("open");
};

var fillDataWithJson = function(inputFieldInfoByParameterName) {
	if (DEBUG) {
		log("setting values in input fields...");
	}

	$.each(inputFieldInfoByParameterName, function(key, info) {
		var value = info.value;
		// Do this for every key there might be different
		// type of elements with the same key than a hidden
		// input

		// var values = value == null ? []:
		// value.split(SEPARATOR_MULTIPLE_VALUES);// In
		// case
		// of
		// value
		// being
		// 0;collect_code_deforestation_reason=burnt
		var inputField = findById(key);
		if (inputField.length == 1) {
			setValueInInputField(inputField, value);
		}
	});
	if (DEBUG) {
		log("values set in input fields");
	}
}

var setValueInInputField = function(inputField, value) {
	var tagName = inputField.prop("tagName");
	switch (tagName) {
	case "INPUT":
		if (inputField.val() != value) {
			inputField.val(value);
		}
		switch (inputField.data("fieldType")) {
		case "BOOLEAN":
			// if (inputField.prop("type") == "checkbox") {
			// inputField.prop("checked", value == "true");
			// }
			var group = inputField.closest(".boolean-group");
			group.find("button").removeClass('active');
			if (value != null && value != "") {
				group.find("button[value='" + value + "']").button('toggle');
			}
			break;
		case "CODE_BUTTON_GROUP":
			var itemsGroup = inputField.closest(".code-items-group");
			// deselect all code item buttons
			itemsGroup.find(".code-item").removeClass('active');
			if (value != null && value != "") {
				// select code item button with value equals to the specified one
				var activeCodeItemsContainer = itemsGroup
						.find(".code-items:visible");
				var splitted = value.split(SEPARATOR_MULTIPLE_PARAMETERS);
				splitted
						.forEach(function(value, index) {
							var button = activeCodeItemsContainer.find(".code-item[value='" + escapeRegExp(value) + "']");
							button.addClass('active');
						});
			}
			break;
		}
		break;
	case "TEXTAREA":
		inputField.val(value);
		break;
	case "SELECT":
		inputField.val(value);
		if (inputField.val() == null) {
			inputField.val("-1");
		}
		break;
	}
};

var initializeChangeEventSaver = function() {
	// SAVING DATA WHEN DATA CHANGES
	// Bind event to Before user leaves page with function parameter e
	// The window onbeforeunload or onunload events do not work in Google Earth
	// OBS! The change event is not fired for the hidden inputs when the value
	// is updated through jQuery's val()
	$('input[name^=collect], select[name^=collect],select[name^=hidden], button[name^=collect]').change(function(e) {
		ajaxDataUpdate();
	});

	$('input:text[name^=collect], textarea[name^=collect]').keyup(function(e) {
		ajaxDataUpdate(1500);
	});

};

var fillYears = function() {
	for (var year = new Date().getFullYear(); year > 1980; year--) {
		$('.fillYears').each(function() {
			$(this).append(
				$("<option></option>")
					.attr("value", year)
					.text(year));
		});
	}
};

var forceWindowCloseAfterDialogCloses = function($dialog) {
	$dialog.on("dialogclose", function(event, ui) {
		window.open("#" + NEXT_ID + ";flyto"); 	// balloonFlyto - annoying to have
												// the balloon open, doesn't let you
												// see the plot
	});
};

/**
 * Utility functions
 */
var serializeFormToJSON = function(form) {
   var o = {};
   var a = form.serializeArray();
   $.each(a, function() {
	   var key = encodeURIComponent(this.name);
	   var value = this.value || '';
       if (o[key]) {
           if (!o[key].push) {
               o[key] = [o[key]];
           }
           o[key].push(value);
       } else {
           o[key] = value;
       }
   });
   //include unchecked checkboxes
   form.find('input[type=checkbox]:not(:checked)').each(function() {
	   o[this.name] = this.checked;  
   });
   return o;
};

/*
var serializeForm = function(formId) {
	var form = findById(formId);
	var result = form.serialize();
	form.find('input[type=checkbox]:not(:checked)').each(function() {
		result += "&" + this.name + "=" + this.checked;
	});
	return result;
};
*/

function escapeRegExp(string){
	return string.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
};

var enableSelect = function(selectName, enable) { // #elementsCover
	$(selectName).prop('disabled', !enable);
	// $(selectName).selectpicker('refresh');
};

var findById = function(id) {
	var newId = id.replace(/(:|\.|\[|\]|,)/g, "\\$1");
	return $("#" + newId);
};

var initLogConsole = function() {
	consoleBox = $("<div>");
	$("body").append(consoleBox);
	consoleBox.css("overflow", "auto");
	consoleBox.css("height", "100px");
	consoleBox.css("width", "400px");
};

var log = function(message) {
	var oldContent = consoleBox.html();
	var date = new Date().toJSON();
	var newContent = oldContent + "<br/>" + date + " - " + message;
	newContent = limitString(newContent, 2000);
	consoleBox.html(newContent);
	consoleBox.scrollTop(consoleBox[0].scrollHeight);
};

var limitString = function(str, limit) {
	var length = str.length;
	if (length > limit) {
		return str.substring(length - limit);
	} else {
		return str;
	}
};
