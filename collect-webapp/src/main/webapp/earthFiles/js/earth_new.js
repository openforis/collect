var SEPARATOR_MULTIPLE_PARAMETERS = "==="; //used to separate multiple attribute VALUES
var SEPARATOR_MULTIPLE_VALUES = ";";
var DATE_FORMAT = 'MM/DD/YYYY';
var TIME_FORMAT = 'HH:ss';

var ACTIVELY_SAVED_FIELD_ID = "collect_boolean_actively_saved";
var NESTED_ATTRIBUTE_ID_PATTERN = /\w+\[\w+\]\.\w+/;
var EXTRA_FIELD_CLASS = "extra";
var MAX_DATA_UPDATE_RETRY_COUNT = 0;
var REQUEST_TIMEOUT = 10000;

var DEFAULT_STATE = "default";
var LOADING_STATE = "loading";
var COLLECT_EARTH_NOT_RUNNING_STATE = "collectEarthNotRunning";
var ERROR_STATE = "error";

var $form = null; //to be initialized
var stateByInputFieldName = {};
var lastUpdateRequest = null; //last update request sent to the server
var lastUpdateInputFieldName = null;
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

		submitData();
	});

	$(".code-item").tooltip();

	loadPlacemarkData();
});

var submitData = function() {
	sendDataUpdateRequest(findById(ACTIVELY_SAVED_FIELD_ID), true, true);
};

var updateData = function(inputField, delay) {
	sendDataUpdateRequest(inputField, false, true, delay);
};

var sendCreateNewRecordRequest = function() {
	sendDataUpdateRequest(findById(ACTIVELY_SAVED_FIELD_ID), false, true);
};

var sendDataUpdateRequest = function(inputField, activelySaved, blockUI, delay, retryCount) {
	delay = defaultIfNull(delay, 100);
	retryCount = defaultIfNull(retryCount, 0);
	if (DEBUG) {
		log("1/4 sending update request (delay=" + delay + ")");
	}
	var inputFieldName = $(inputField).attr("id");
	if (lastUpdateInputFieldName == inputFieldName) {
		abortLastUpdateRequest(); 	// So that the form
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
	}
	setActivelySaved(activelySaved);

	// Set a timeout so that the data is only sent to the server
	// if the user stops clicking for over one second

	ajaxTimeout = setTimeout(function() {
		if (DEBUG) {
			log("2/4 create update request");
		}
		var data = createPlacemarkUpdateRequest(PREVIEW ? null : inputField);
		
		lastUpdateRequest = $.ajax({
			data : data,
			type : "POST",
			url : $form.attr("action"),
			timeout: REQUEST_TIMEOUT,
			dataType : 'json',
			beforeSend : function() {
				if (blockUI) {
					if (activelySaved) {
						$.blockUI({
							message : 'Submitting data..'
						});
					} else {
						$.blockUI({
							message : null,
							overlayCSS: { backgroundColor: 'transparent' }
						});
					}
				}
			}
		})
		.done(function(json) {
			if (DEBUG) {
				log("4/4 json response received");
			}
			if (json.success) {
				handleSuccessfullDataUpdateResponse(json, activelySaved, blockUI);
			} else {
				handleFailureDataUpdateResponse(inputField, activelySaved, blockUI, retryCount, 
					json.message);
			}
		})
		.fail(function(xhr, textStatus, errorThrown) {
			// try again
			if("abort" != errorThrown) {
				if (isSuccessfullResponse(xhr.responseText)) {
					if (DEBUG) {
						log("failed but the response is successfull: " + xhr.responseText);
					}
					handleSuccessfullDataUpdateResponse($.parseJSON(xhr.responseText), activelySaved, blockUI);
				} else {
					handleFailureDataUpdateResponse(inputField, activelySaved, blockUI, retryCount, 
						errorThrown, xhr, textStatus, errorThrown);
				}
			}
		})
		.always(function() {
			ajaxTimeout = null;
			lastUpdateRequest = null;
			lastUpdateInputFieldName = null;

		});
		if (DEBUG) {
			log("3/4 request sent, waiting for response...");
		}
	}, delay);
	
	lastUpdateInputFieldName = inputFieldName;
};

var isValidResponse = function(text) {
	try {
		var json = $.parseJSON(text);
		return json.hasOwnProperty("success");
	} catch(error) {
		return false;
	}
};

var isSuccessfullResponse = function(text) {
	try {
		var json = $.parseJSON(text);
		return json.success;
	} catch(error) {
		return false;
	}
};

var handleSuccessfullDataUpdateResponse = function(json, showFeedbackMessage, unblockWhenDone) {
	if (DEBUG) {
		log("data updated successfully, updating UI...");
	}
	if (showFeedbackMessage) {
		setStepsAsVisited();
	}
	interpretJsonSaveResponse(json, showFeedbackMessage);
	
	changeState(DEFAULT_STATE);
	
	if (unblockWhenDone) {
		$.unblockUI();
	}
	if (DEBUG) {
		log("UI update complete");
	}
};

var handleFailureDataUpdateResponse = function(inputField, activelySaved, blockUI, retryCount, errorMessage, xhr, textStatus, errorThrown) {
	if (DEBUG) {
		log("error updating data: " + errorMessage);
	}
	//reload placemark: shown form could be not consistent with stored data
	loadPlacemarkData(true);
};

var createPlacemarkUpdateRequest = function(inputField) {
	var values;
	if (inputField == null) {
		values = serializeFormToJSON($form);
	} else {
		values = {};
		values[encodeURIComponent($(inputField).attr('name'))] = $(inputField).val();
		values[encodeURIComponent(ACTIVELY_SAVED_FIELD_ID)] = findById(ACTIVELY_SAVED_FIELD_ID).val();
		$form.find("." + EXTRA_FIELD_CLASS).each(function() {
			var $this = $(this);
			values[encodeURIComponent($this.attr('name'))] = $this.val()
		});
	}
	var data = {
		placemarkId : getPlacemarkId(),
		values : values,
		currentStep : currentStepIndex,
		partialUpdate : true
	};
	return data;
}

var abortLastUpdateRequest = function() {
	if (ajaxTimeout != null) {
		clearTimeout(ajaxTimeout);
	}
	if (lastUpdateRequest != null) {
		if (DEBUG) {
			log("abort last update request");
		}
		lastUpdateRequest.abort();
		lastUpdateRequest = null;
	}
};

var undoChanges = function() {
	fillDataWithJson(stateByInputFieldName);
};

var interpretJsonSaveResponse = function(json, showFeedbackMessage) {
	if (DEBUG) {
		log("Parsing response:")
	}
	
	if (DEBUG) {
		log("1/4: Update field status cache")
	}
	updateFieldStateCache(json.inputFieldInfoByParameterName);
	
	if (DEBUG) {
		log("2/4: Update input field status")
	}
	updateInputFieldsState(json.inputFieldInfoByParameterName);

	if (DEBUG) {
		log("3/4: Fill data in input fields")
	}
	fillDataWithJson(json.inputFieldInfoByParameterName);
	
	if (DEBUG) {
		log("4/4: Update steps error feedback")
	}
	updateStepsErrorFeedback();

	if (DEBUG) {
		log("Response parsed correctly");
	}
	
	if (showFeedbackMessage) { // show feedback message
		if (json.success) {
			if (isAnyErrorInForm()) {
				var message = "<ul>";
				for(var key in stateByInputFieldName) {
					var info = stateByInputFieldName[key];
					if (info.inError) {
						var inputField = findById(key);
						var label;
						if (NESTED_ATTRIBUTE_ID_PATTERN.test(key)) {
							label = getEnumeratedEntityNestedAttributeErrorMessageLabel(inputField);
						} else {
							label = inputField.length > 0 ? OF.UI.Forms.getFieldLabel(inputField): "";
						}
						message += "<li>" + label + " : " + info.errorMessage + "</li>";
					}
				}
				message += "</ul>";
				showErrorMessage(message);

				// Resets the "actively saved" parameter to false so that it is
				// not sent as true when the user fixes the validation
				setActivelySaved(false);
			} else {
				showSuccessMessage(json.message);
				forceWindowCloseAfterDialogCloses($("#dialogSuccess"));
			}
		} else {
			showErrorMessage(json.message);
		}
	}
};

var isAnyErrorInForm = function() {
	for(var key in stateByInputFieldName) {
		var info = stateByInputFieldName[key];
		if (info.visible && info.inError) {
			return true;
		}
	};
	return false;
};

var getEnumeratedEntityNestedAttributeErrorMessageLabel = function(inputField) {
	var columnIndex = inputField.closest("td").index();
	var rowHeadingEl = inputField.closest("tr").find("td").eq(0);
	var rowHeading = rowHeadingEl.text();
	var attributeHeadingEl = inputField.closest("table").find("thead tr th").eq(columnIndex);
	var attributeHeading = attributeHeadingEl.text();
	var enumeratorHeadingEl = inputField.closest("table").find("thead tr th").eq(0);
	var enumeratorHeading = enumeratorHeadingEl.text();
	var entityHeading = inputField.closest("fieldset").find("legend").text();
	var label = entityHeading + " [" + enumeratorHeading + " " + rowHeading + "] / " + attributeHeading;
	return label;
};

var updateInputFieldsState = function(inputFieldInfoByParameterName) {
	if (DEBUG) {
		log("updating the state of " + Object.keys(inputFieldInfoByParameterName).length + " input fields...");
	}
	if (DEBUG) {
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
						//select the first option
						el.find("option:first").prop("selected", true);
					}
					break;
				case "CODE_BUTTON_GROUP":
					var parentCodeInfo = inputFieldInfoByParameterName[parentCodeFieldId];
					var parentCodeItemId = parentCodeInfo.codeItemId;
					var groupContainer = el.closest(".code-items-group");
					
					var itemsContainers = groupContainer.find(".code-items");
					//itemsContainers.hide();
					itemsContainers.css( "display", "none")

					var validItemsContainer = groupContainer.find(".code-items[data-parent-id='" + parentCodeItemId + "']");
					if (validItemsContainer.length > 0 && validItemsContainer.is(':hidden')) {
						//validItemsContainer.show();
						validItemsContainer.css( "display", "block")
					}
					break;
				}
			}
		}
	});
	if (DEBUG) {
		log("updating errors feedback");
	}
	var changedFieldNames = [];
	var errors = [];
	$.each(inputFieldInfoByParameterName, function(fieldName, info) {
		changedFieldNames.push(fieldName);
		errors.push({
			field : fieldName,
			defaultMessage : info.errorMessage
		});
	});
	OF.UI.Forms.Validation.updateErrorMessageInFields($form, changedFieldNames, errors, {doNotIncludeFieldLabel: true});

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

var updateFieldStateCache = function(inputFieldInfoByParameterName) {
	$.each(inputFieldInfoByParameterName, function(fieldName, info) {
		stateByInputFieldName[fieldName] = info;
	});
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
	setStepsAsVisited(currentStepIndex);
	var stepHeading = getStepHeading(currentStepIndex);
	var relevant = ! stepHeading.hasClass("notrelevant");
	if (relevant) {
		$stepsContainer.steps("setCurrentIndex", currentStepIndex);
	}
	stepHeading.removeClass("done");
};

var setStepsAsVisited = function(upToStepIndex) {
	if (! upToStepIndex) {
		upToStepIndex = $stepsContainer.find(".steps ul li").length - 1;
	}
	for (var stepIndex = 0; stepIndex <= upToStepIndex; stepIndex ++) {
		var stepHeading = getStepHeading(stepIndex);
		stepHeading.addClass("visited");
		stepHeading.removeClass("disabled");
		var hasErrors = $(this).find(".form-group.has-error").length > 0;
		if (! hasErrors && !(upToStepIndex == 0 && stepIndex == 0)) {
			stepHeading.addClass("done");
		}
	}
};

var updateStepsErrorFeedback = function() {
	$form.find(".step").each(function(index, value) {
		var stepHeading = getStepHeading(index);
		if (! stepHeading.hasClass("disabled")) {
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
		var value = null;
		var wasActive = btn.hasClass("active");
		var itemsContainer = btn.closest(".code-items");
		var groupContainer = itemsContainer.closest(".code-items-group");
		var inputField = groupContainer.find("input[type='hidden']");
				
		if (itemsContainer.data("toggle") == "buttons") {
			if (! wasActive) {
				if (btn.val() == "none") {
					//remove the other active buttons
					// deselect all code item buttons
					groupContainer.find(".code-item").removeClass('active');
				} else {
					// If none was selected and a value different than none was selected
					var activeNoneButton = itemsContainer.find("button[value='none'].active");
					activeNoneButton.removeClass('active');
				}
			} else if (btn.val() != "none") {
				// Check that if there are no values selected then none is selected!
				var buttons = itemsContainer.find("button.active");
				if(buttons.length == 1 ) { // Only the current button, which will be deselected, is selected now
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
		if (! wasActive) {
			btn.toggleClass("active", true);
		}
		
		updateData(inputField);
		
		return false;
	});
};

var initBooleanButtons = function() {
	$('.boolean-group').each(function() {
		var group = $(this);
		var hiddenField = group.find("input[type='hidden']");
		group.find("button").click(function() {
			var btn = $(this);
			hiddenField.val(btn.val());
			var wasSelected = btn.hasClass('active');
			group.find('button').removeClass('active');
			if (! wasSelected) {
				btn.addClass('active');
			}
			updateData(hiddenField);
			return false;
		});
	});
};

var initDateTimePickers = function() {
	// http://eonasdan.github.io/bootstrap-datetimepicker/
	$('.datepicker').datetimepicker({
		format : DATE_FORMAT
	}).on('dp.change', function(e) {
		var inputField = $(this).find(".form-control");
		// inputField.change();
		updateData(inputField);
	});

	$('.timepicker').datetimepicker({
		format : TIME_FORMAT
	}).on('dp.change', function(e) {
		var inputField = $(this).find(".form-control");
		updateData(inputField);
	});
};

var getSourceHeadingId = function(headingId) {
	return headingId.replace("-t-", "-h-")
}

var getSourceSectionId = function(headingId) {
	return headingId.replace("-t-", "-p-")
}

var cloneStepTemplate = function ({headingId, sourceHeading, stepHeadings, currentIndex}) {
	var sourceSectionId = getSourceSectionId(headingId);
	var sourceSection = $("#" + sourceSectionId).children(".form-group")[0];
	var content = $(sourceSection).clone();
	var headingPrefix = sourceHeading.text();
	var newEntityIndex = stepHeadings.filter((_i, headingEl) => {
		var t = getTabText(headingEl);
		return t.substring(0, t.lastIndexOf(' ')) === headingPrefix
	}).length
	var title = headingPrefix + " (" + (newEntityIndex + 1) + ")";
	content.find("input, label").each(function(_i, elem) {
		var el = $(elem);
		var id = el.attr("id");
		if (id && id.includes("$index")) {
			el.attr("id", id.replace("$index", newEntityIndex));
			var name = el.attr("name");
			el.attr("name", name.replace("$index", newEntityIndex));
		}
		var forAttr = el.attr("for");
		if (forAttr && forAttr.includes("$index")) {
			el.attr("for", forAttr.replace("$index", newEntityIndex));
		}
	});
	$stepsContainer.steps('insert', currentIndex, { title, content })
	$stepsContainer.steps("setCurrentIndex", currentIndex);
}

var initSteps = function() {
	$steps = $stepsContainer.steps({
		headerTag : "h3",
		bodyTag : "section",
		transitionEffect : "none",
		autoFocus : true,
		titleTemplate : "#title#",
		labels : {
			// These values come from the balloon.html file as they need to be localized (spanish,english,portuguese and french)
			finish : SUBMIT_LABEL,
		    next: NEXT_LABEL,
		    previous: PREVIOUS_LABEL
		},
		onStepChanged : function(_event, currentIndex, priorIndex) {
			var stepHeadings = $form.find(".steps .steps ul li");
			var stepHeading = $(stepHeadings[currentIndex]);
			var headingId = stepHeading.find('a')[0].id
			var sourceHeadingId = getSourceHeadingId(headingId)
			var sourceHeading = $("#" + sourceHeadingId)
			if (stepHeading.hasClass("notrelevant")) {
				var nextStepIndex;
				var firstRelevantHeadingIdx = findFirstRelevantElementIndex(stepHeadings, currentIndex, currentIndex < priorIndex);
				if (firstRelevantHeadingIdx >= 0) {
					nextStepIndex = firstRelevantHeadingIdx;
				} else {
					//show last card
					nextStepIndex = stepHeadings.length - 1;
				}
				$stepsContainer.steps('setCurrentIndex', nextStepIndex);
				currentStepIndex = nextStepIndex;
			} else if (sourceHeading.hasClass("form-template")) {
				var headingPrefix = sourceHeading.text();
				if (confirm("Create a new " + headingPrefix + "?")) {
					cloneStepTemplate({headingId, sourceHeading, stepHeadings, currentIndex})
				} else {
					$stepsContainer.steps('setCurrentIndex', priorIndex);
				}
			} else {
				currentStepIndex = currentIndex;
			}
			updateStepsErrorFeedback();
		},
		onFinished : function(event, currentIndex) {
			submitData();
		}
	});
	$stepsContainer.find("a[href='#finish']").addClass("btn-finish");
};

var findFirstRelevantElementIndex = function(group, startFromIndex, reverseOrder) {
	var idx = reverseOrder ? startFromIndex - 1 : startFromIndex + 1;
	while (reverseOrder ? idx >= 0 : idx < group.length) {
		var el = $(group[idx]);
		if (! el.hasClass("notrelevant")) {
			return idx;
		}
		idx = reverseOrder ? idx - 1 : idx + 1;
	}
	return -1;
};

var loadPlacemarkData = function(reloadingAfterError) {
	var placemarkId = getPlacemarkId();

	$.ajax({data : {id : placemarkId},
		type : "GET",
		url : HOST + "placemark-info-expanded",
		dataType : 'json',
		timeout : REQUEST_TIMEOUT
	})
	.fail(function(xhr, textStatus, errorThrown) {
		if (isValidResponse(xhr.responseText)) {
			//valid response but for some (unknown) reason not handled properly by jquery
			handleValidResponse($.parseJSON(xhr.responseText));
		} else {
			if (reloadingAfterError) {
				changeState(ERROR_STATE);

				var logErrorMessage = "error updating data";
				if (errorThrown) {
					logErrorMessage += ": " + errorThrown;
				}
				if (xhr) {
					if (xhr.readyState == 0) {
						logErrorMessage += "; connection refused;";
					}
					logErrorMessage += " status = " + xhr.status 
							+ "; text status = " + textStatus 
							+ "; error thrown = " + errorThrown 
							+ "; response = " + xhr.responseText;
				}
				logError(logErrorMessage);
			} else {
				changeState(COLLECT_EARTH_NOT_RUNNING_STATE);
			}
		}
	})
	.done(function(json) {
		handleValidResponse(json);
	});
	
	function handleValidResponse(json) {
		if (json.success) {
			// placemark exists in database
			if (json.activelySaved
					&& json.inputFieldInfoByParameterName.collect_text_id.value != 'testPlacemark') {
	
				showErrorMessage(PLACEMARK_ALREADY_FILLED);
	
				if (json.skipFilled) {
					forceWindowCloseAfterDialogCloses($("#dialogSuccess"));
				}
			}
			setStepsAsVisited();
			// Pre-fills the form and after that initializes the
			// change event listeners for the inputs
			interpretJsonSaveResponse(json, false);
				
			currentStepIndex = parseInteger(json.currentStep, 0);
			
			showCurrentStep();
			updateStepsErrorFeedback();

			changeState(DEFAULT_STATE);
		} else {
			// if no placemark in database, force the creation
			// of a new record
			sendCreateNewRecordRequest();
		}
	}
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
};

var setActivelySaved = function(value) {
	findById(ACTIVELY_SAVED_FIELD_ID).val(value == true || value == 'true');
};

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
	var color, successIconVisible, title;
	switch (type) {
	case "error":
		title = 'Error';
		color = "red";
		successIconVisible = false;
		break;
	case "warning":
		title = 'Warning';
		color = "yellow";
		successIconVisible = false;
		break;
	case "success":
		title = 'Success!';
		successIconVisible = true;
	default:
		color = "green";
	}
	$('#succ_mess').css("color", color).html(message ? message : "");
	$("#dialogSuccess").find(".success-icon").toggle(successIconVisible);
	$("#dialogSuccess").dialog({
		title: title
	});
	$("#dialogSuccess").dialog("open");
};

var fillDataWithJson = function(inputFieldInfoByParameterName) {
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
				group.find("button[value='" + value + "']").addClass('active');
			}
			break;
		case "CODE_BUTTON_GROUP":
			var itemsGroup = inputField.closest(".code-items-group");
			// deselect all code item buttons
			itemsGroup.find(".code-item").removeClass('active');
			if (value != null && value != "") {
				// select code item button with value equals to the specified one
				var codeItemsContainers = itemsGroup.find(".code-items");
				var activeCodeItemsContainer = getVisibleComponent(codeItemsContainers);
				var splitted = value.split(SEPARATOR_MULTIPLE_PARAMETERS);
				if (activeCodeItemsContainer != null) {
					splitted.forEach(function(value, index) {
						var button = activeCodeItemsContainer.find(".code-item[value='" + escapeRegExp(value) + "']");
						button.addClass('active');
					});
				}
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
			//select the first option
			inputField.find("option:first").prop("selected", true);
		}
		break;
	}
};

function getVisibleComponent(components) {
	if (components.length == 1) {
		return $(components[0]);
	}
	for (i = 0; i < components.length; i++) { 
		var component = $(components[i]);
		if (component.css("display")=="block") {
			return component;
		}
	}
	return null;
}

function escapeRegExp(string){
	return string.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
}

var initializeChangeEventSaver = function() {
	// SAVING DATA WHEN DATA CHANGES
	// Bind event to Before user leaves page with function parameter e
	// The window onbeforeunload or onunload events do not work in Google Earth
	// OBS! The change event is not fired for the hidden inputs when the value
	// is updated through jQuery's val()
	$('input[name^=collect], textarea[name^=collect], select[name^=collect], select[name^=hidden], button[name^=collect]').change(function(e) {
		updateData(e.target);
	});
	$('input:text[name^=collect], textarea[name^=collect]').keyup(function(e) {
		updateData(e.target, 1500);
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

var changeState = function(state) {
	switch(state) {
		case LOADING_STATE:
			$("#scrollingDiv").hide();
			$("#errorPanel").hide();
			$("#collectEarthNotRunningPanel").hide();
			$("#loadingPanel").show();
			break;
		case COLLECT_EARTH_NOT_RUNNING_STATE:
			$("#loadingPanel").hide();
			$("#errorPanel").hide();
			$("#scrollingDiv").hide();
			$("#collectEarthNotRunningPanel").show();
			break;
		case ERROR_STATE:
			$("#loadingPanel").hide();
			$("#scrollingDiv").hide();
			$("#errorPanel").show();
			break;
		default:
			$("#loadingPanel").hide();
			$("#errorPanel").hide();
			$("#collectEarthNotRunningPanel").hide();
			$("#scrollingDiv").show();
	}
}

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
	consoleBox.css("overflow", "auto");
	consoleBox.css("height", "100px");
	consoleBox.css("width", "400px");
	$("body").append(consoleBox);
};

var log = function(message) {
	var oldContent = consoleBox.html();
	var date = new Date().toJSON();
	var newContent = oldContent + "<br/>" + date + " - " + message;
	newContent = limitString(newContent, 2000);
	consoleBox.html(newContent);
	consoleBox.scrollTop(consoleBox[0].scrollHeight);
}

var logError = function(message) {
	if (consoleBox == null) {
		initLogConsole();
	}
	log(message);
};

var limitString = function(str, limit) {
	var length = str.length;
	if (length > limit) {
		return str.substring(length - limit);
	} else {
		return str;
	}
};

var defaultIfNull = function(obj, defaultValue) {
	if (typeof obj == "undefined" || obj == null) {
		return defaultValue;
	} else {
		return obj;
	}
};

var parseInteger = function(str, defaultValue) {
	if (str == null || str.length == 0) {
		return defaultValue;
	} else {
		var value = parseInt(str);
		if (isNaN(value)) {
			return defaultValue;
		} else {
			return value;
		}
	}
};

var getTabText = function (tabEl) {
	return $(tabEl).children("a").first()
		.clone()
	    .children()
	    .remove()
	    .end()
	    .text();
}
