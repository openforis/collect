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
var $stepsContainer = null; //to be initialized
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
	initFormInputFields();
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

var defaultBeforeSendRequest = function() {
	$.blockUI({
		message : null,
		overlayCSS: { backgroundColor: 'transparent' }
	});
}

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
						defaultBeforeSendRequest();
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
				handleFailureDataUpdateResponse(json.message);
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
					handleFailureDataUpdateResponse(errorThrown);
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


var sendEntityCreateOrDeleteRequest = function (options) {
	var method = options.method || "POST";
	var url = options.url;
	var label = options.label;
	var params = options.params;
	var resolve = options.resolve;
	var reject = options.reject;
	
	var data = createPlacemarkUpdateRequest();
	for (var key in params) {
	  	if (Object.hasOwnProperty.call(params, key)) {
  	  		data[key] = params[key];
	  	}
	}
	if (DEBUG) {
		log("1/2 sending " + label + " request");
	}
	$.ajax({
		data: data,
		type: method,
		url: url,
		timeout: REQUEST_TIMEOUT,
		dataType : 'json',
		beforeSend : defaultBeforeSendRequest
	})
	.done(function(json) {
		if (DEBUG) {
			log("2/2 json response received");
		}
		if (json.success) {
			handleSuccessfullDataUpdateResponse(json);
		} else {
			handleFailureDataUpdateResponse(json.message);
		}
		resolve()
	})
	.fail(function(xhr, textStatus, errorThrown) {
		if (DEBUG) {
			log("2/2a error sending request");
		}
		reject()
	})
	.always(function() {
		$.unblockUI();
	});
}

var sendEntityCreateRequest = function (entityName, resolve, reject) {
	sendEntityCreateOrDeleteRequest({ url: HOST + 'create-entity', params: {entityName: entityName}, 
		label: "entity create", resolve: resolve, reject: reject })
}

var sendEntityDeleteRequest = function (entityName, resolve, reject) {
	sendEntityCreateOrDeleteRequest({ url: HOST + 'delete-entity', params: {entityName: entityName}, 
		label: "entity delete", resolve: resolve, reject: reject })
}

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

var handleFailureDataUpdateResponse = function(errorMessage) {
	// {inputField, activelySaved, blockUI, retryCount, errorMessage, xhr, textStatus, errorThrown}
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
		var $inputField = $(inputField)
		values = {};
		values[encodeURIComponent($inputField.attr('name'))] = $inputField.val();
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
	if (json.deletedEntityDefName) {
		if (DEBUG) {
			log("Removing deleted multiple form " + json.deletedEntityDefName);
		}
		deleteStepByNodeDefName(json.deletedEntityDefName)
	}
	
	if (DEBUG) {
		log("1/4: Update field status cache")
	}
	updateFieldStateCache(json.inputFieldInfoByParameterName);
	
	if (DEBUG) {
		log("1a/4: Create missing multiple form steps")
	}
	createMissingMultipleFormSteps(json.inputFieldInfoByParameterName)
	
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
	
	if (showFeedbackMessage) {
		if (DEBUG) {
			log("Showing feedback message");
		}
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

var createMissingMultipleFormSteps = function(inputFieldInfoByParameterName) {
	$.each(inputFieldInfoByParameterName, function(fieldName, info) {
		var el = findById(fieldName);
		var indexOfParenthesis = fieldName.indexOf("[");
		if (el.length == 0 && indexOfParenthesis >= 0) {
			var fieldNameFirstPart = fieldName.substring(0, indexOfParenthesis + 1);
			var fieldNameSecondPart = fieldName.substring(fieldName.indexOf("]"));
			var templateFieldName = fieldNameFirstPart + '$index' + fieldNameSecondPart;
			var templateFieldEl = findById(templateFieldName);
			if (templateFieldEl.length == 1) {
				var templateSection = templateFieldEl.closest('section');
				var templateSectionId = templateSection.attr('id');
				var sourceHeadingId = getSourceHeadingIdBySectionId(templateSectionId);
				var sourceHeading = findById(sourceHeadingId);
				var templateTabAnchorId = getSourceTabAnchorIdBySectionHeadingId(sourceHeadingId);
				var templateTabAnchor = findById(templateTabAnchorId);
				var templateSectionAbsoluteIndex = getStepHeadingsAnchors().index(templateTabAnchor);
				cloneStepTemplate({sourceHeading: sourceHeading, indexNext: templateSectionAbsoluteIndex});
			}
		}
	});
}

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

var getStepHeadings = function() {
	return $form.find(".steps .steps ul li");
}

var getStepHeadingsAnchors = function() { 
	return getStepHeadings().find('a');
}

var getStepHeading = function(index) {
	var stepHeading = getStepHeadings().eq(index);
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

var addStepHeadingDeleteButton = function(options) {
	var index = options.index
	var sourceHeading = options.sourceHeading
	
	var entityName = sourceHeading.data("nodeDefName");
	var stepHeading = getStepHeading(index);
	var entityLabel = sourceHeading.text();
	var stepsWithSameHeadingPrefix = getStepsWithSameHeadingPrefix(entityLabel);
	stepsWithSameHeadingPrefix.find('button.form-delete-btn').remove();
	
	var deleteButton = $('<button class="form-delete-btn" data-node-def-name="' + entityName + '" title="Delete"><span>X</span></button>');
	deleteButton.on("click", function () {
		sendEntityDeleteRequest(entityName, function () {}, function () {});
	});
	var stepHeadingAnchor = stepHeading.children().first();
	stepHeadingAnchor.append(deleteButton);		
}

var addStepHeadingAddButtons = function() {
	var templateSectionHeadings = $form.find(".steps .content h3.form-template");
	templateSectionHeadings.each(function (_index, templateSectionHeading) {
		var $templateSectionHeading = $(templateSectionHeading);
		var templateSectionHeadingId = $templateSectionHeading.attr('id')
		var entityName = $templateSectionHeading.data("nodeDefName");
		var entityLabel = $templateSectionHeading.text();
		var button = $('<button class="form-add-btn" data-node-def-name="' + entityName + '" title="Add"><span>+</span></button>');
		button.on("click", function () {
			addStepByNodeDefName(entityName);
		});
		var templateSectionHeadingId = $templateSectionHeading.attr('id');
		var tabAnchorId = getSourceTabAnchorIdBySectionHeadingId(templateSectionHeadingId);
		var tabAnchor = findById(tabAnchorId);
		tabAnchor.append(button);
		tabAnchor.closest('li').addClass("done");
	});	
}

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
		upToStepIndex = getStepHeadings().length - 1;
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

var initFormInputFields = function(parentContainer) {
	initCodeButtonGroups(parentContainer);
	initDateTimePickers(parentContainer);
	initBooleanButtons(parentContainer);
	initializeChangeEventSaver(parentContainer);
}

var initCodeButtonGroups = function(parentContainer) {
	(parentContainer || $form).find("button.code-item").click(function(event) {
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

var initBooleanButtons = function(parentContainer) {
	(parentContainer || $form).find('.boolean-group').each(function() {
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

var initDateTimePickers = function(parentContainer) {
	// http://eonasdan.github.io/bootstrap-datetimepicker/
	(parentContainer || $form).find('.datepicker').datetimepicker({
		format : DATE_FORMAT
	}).on('dp.change', function(e) {
		var inputField = $(this).find(".form-control");
		// inputField.change();
		updateData(inputField);
	});

	(parentContainer || $form).find('.timepicker').datetimepicker({
		format : TIME_FORMAT
	}).on('dp.change', function(e) {
		var inputField = $(this).find(".form-control");
		updateData(inputField);
	});
};

var addStepByNodeDefName = function (nodeDefName) {
	var templateSectionHeading = findTemplateSectionHeaderByNodeDefName(nodeDefName);
	var templateSectionHeadingId = templateSectionHeading.attr('id');
	var templateTabAnchorId = getSourceTabAnchorIdBySectionHeadingId(templateSectionHeadingId);
	var templateTabAnchor = findById(templateTabAnchorId);
	var index = getStepHeadingsAnchors().index(templateTabAnchor);
	addEntityAndCloneStepTemplate({sourceHeading: templateSectionHeading, indexNext: index})
}

var findTemplateSectionHeaderByNodeDefName = function (nodeDefName) {
	return $form.find(".steps .content h3.form-template[data-node-def-name='" + nodeDefName+ "']");
}

var deleteStepByNodeDefName = function(nodeDefName) {
	var templateSectionHeader = findTemplateSectionHeaderByNodeDefName(nodeDefName);
	var templateSectionHeaderId = templateSectionHeader.attr('id');
	var templateTabId = getSourceTabAnchorIdBySectionHeadingId(templateSectionHeaderId);
	var templateTab = $("#" + templateTabId);
	var templateTabText = removeSuffix(templateTab.text(), '+');
	var stepsWithSameHeading = getStepsWithSameHeadingPrefix(templateTabText)
	var stepIndexToDelete = stepsWithSameHeading.length - 1
	if (stepIndexToDelete >= 0) {
		var stepToDelete = $(stepsWithSameHeading.get(stepIndexToDelete)).children("a").first()[0];
		if (stepToDelete) {
			var stepToDeleteAbsoluteIndex = Number(stepToDelete.id.substring(stepToDelete.id.lastIndexOf('-') + 1));
			var nextCurrentSelectedIndex = stepToDeleteAbsoluteIndex - 1;
			// change current selected step before deletion, otherwise it won't be deleted
			$stepsContainer.steps("setCurrentIndex", nextCurrentSelectedIndex); 
			// remove step from steps
			$stepsContainer.steps('remove', stepToDeleteAbsoluteIndex);
			addStepHeadingAddButtons();
			if (stepIndexToDelete > 0) {
				// add step delete button to the last step with the same nodeDefName
				addStepHeadingDeleteButton({index: nextCurrentSelectedIndex, sourceHeading: templateSectionHeader});
			}
		}
	}
}

var getStepsWithSameHeadingPrefix = function(headingPrefix) {
	var stepHeadings = getStepHeadings();
	return stepHeadings.filter(function (_i, headingEl) {
		var t = getTabText(headingEl);
		return t.substring(0, t.lastIndexOf(' ')) == headingPrefix
	})
}

var cloneStepTemplate = function (options) {
	var sourceHeading = options.sourceHeading;
	var indexNext = options.indexNext;
	
	var sourceHeadingId = sourceHeading.attr('id')
	var sourceSectionId = getSourceSectionIdBySourceHeadingId(sourceHeadingId);
	var sourceSectionChildren = $("#" + sourceSectionId).children();
	var content = $(sourceSectionChildren).clone();
	var headingPrefix = sourceHeading.text();
	var newEntityIndex = getStepsWithSameHeadingPrefix(headingPrefix).length + 1 // index is 1 based
	var title = headingPrefix + " (" + (newEntityIndex) + ")";
	content.find("input, select, label, div.code-items-group, div.code-items").each(function(_i, elem) {
		var el = $(elem);
		["id", "name", "for", "data-parent-id-field-id"].forEach(function(attr) {
			replaceTextInAttribute(el, attr, "$index", newEntityIndex);
		});
	});
	initFormInputFields(content);
	$stepsContainer.steps('insert', indexNext, { title: title, content: content });
	// add "step" class to all sections in steps content (not added automatically when inserting a new step)
	$stepsContainer.find('section').addClass('step')	
	// add action buttons
	addStepHeadingAddButtons();
	addStepHeadingDeleteButton({index: indexNext, sourceHeading: sourceHeading})
}

var addEntityAndCloneStepTemplate = function (options) {
	var sourceHeading = options.sourceHeading;
	var indexNext = options.indexNext;
	
	var entityName = sourceHeading.data("nodeDefName");
	sendEntityCreateRequest(entityName, function() {
		// cloneStepTemplate({sourceHeading, indexNext});
		// new step created when response data is processed
		$stepsContainer.steps("setCurrentIndex", indexNext);
	}, function() {
		console.log("ERROR")
	})
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
		onStepChanging: function (_event, currentIndex, nextIndex) {
			var stepHeadings = getStepHeadings();
			var nextStepHeading = $(stepHeadings[nextIndex]);
			var nextHeadingId = nextStepHeading.find('a').attr('id');
			var nextSourceHeadingId = getStepSourceHeadingIdByTabHeadingId(nextHeadingId);
			var nextSourceHeading = findById(nextSourceHeadingId);
			if (nextSourceHeading.hasClass("form-template")) {
				var finalIndex = nextIndex + (nextIndex > currentIndex ? 1: -1);
				if (finalIndex >= 0 && finalIndex <= stepHeadings.length - 1 && !$(stepHeadings[finalIndex]).hasClass('notrelevant')) {
					$stepsContainer.steps('setCurrentIndex', finalIndex);				
				} 
				return false;
			}
			return true;
		},
		onStepChanged : function(_event, currentIndex, priorIndex) {
			var stepHeadings = getStepHeadings();
			var stepHeading = $(stepHeadings[currentIndex]);
			var headingId = stepHeading.find('a')[0].id
			var sourceHeadingId = getStepSourceHeadingIdByTabHeadingId(headingId)
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
				// it should not happen, prevented by onStepChanging
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
	
	addStepHeadingAddButtons()
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

var initializeChangeEventSaver = function(parentContainer) {
	// SAVING DATA WHEN DATA CHANGES
	// Bind event to Before user leaves page with function parameter e
	// The window onbeforeunload or onunload events do not work in Google Earth
	// OBS! The change event is not fired for the hidden inputs when the value
	// is updated through jQuery's val()
	(parentContainer || $form).find('input[name^=collect], textarea[name^=collect], select[name^=collect], select[name^=hidden], button[name^=collect]').change(function(e) {
		updateData(e.target);
	});
	(parentContainer || $form).find('input:text[name^=collect], textarea[name^=collect]').keyup(function(e) {
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
	 if (this.name.indexOf('$index') < 0) { // skip parameters in template
	   var key = encodeURIComponent(this.name);
	   var value = this.value || '';
	   var existingValue = o[key]
	   if (existingValue) {
		   // multiple values: transform item in "key" into an array
	       if (!existingValue.push) {
	           o[key] = [existingValue];
	       }
	       o[key].push(value);
	   } else {
	       o[key] = value;
	   }
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
	var newId = id.replace(/(:|\.|\[|\]|,|\$)/g, "\\$1");
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

var replaceTextInAttribute = function (el, attrName, textToSearch, textToReplace) {
	var attrValue = el.attr(attrName);
	if (attrValue && attrValue.indexOf(textToSearch) >= 0) {
		el.attr(attrName, attrValue.replace(textToSearch, textToReplace));
	}
}

var removeSuffix = function (text, suffix) {
	var finalLength = text.length - suffix.length
	if (text.indexOf(suffix) == finalLength) {
		return text.substring(0, finalLength)
	}
	return text
}

// steps utils
var getStepSourceHeadingIdByTabHeadingId = function(headingId) {
	return headingId.replace("-t-", "-h-")
}

var getStepSourceSectionIdByTabHeadingId = function(headingId) {
	return headingId.replace("-t-", "-p-")
}

var getSourceSectionIdBySourceHeadingId = function(sourceHeadingId) {
	return sourceHeadingId.replace("-h-", "-p-")
}

var getSourceTabAnchorIdBySectionHeadingId = function(sectionHeadingId) {
	return sectionHeadingId.replace('-h-', '-t-')
}

var getSourceHeadingIdBySectionId = function(sectionId) {
	return sectionId.replace('-p-', '-h-')
}
