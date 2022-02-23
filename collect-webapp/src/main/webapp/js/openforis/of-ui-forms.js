OF.UI.Forms = function() {};

/**
 * Populate a select using a list of items
 * Option value is set according to the specified valueKey and 
 * option text content is set according to the specified labelKey
 * 
 * @param $select
 * @param items
 * @param valueKey (optional, default value will be item.toString())
 * @param labelKeyOrFunction (optional, default is valueKey, if specified)
 * @param callback
 */
OF.UI.Forms.populateSelect = function($select, items, valueKey, labelKeyOrFunction, addEmptyOption) {
	$select.empty();

	if (addEmptyOption) {
		$select.append($("<option />").val("").text(""));
	}
	
	$.each(items, function(i, item) {
		var value = item.hasOwnProperty(valueKey) ? item[valueKey]: item;
		var label = null;
		if (labelKeyOrFunction) {
			var typeOfLabelKey = typeof labelKeyOrFunction;
			switch (typeOfLabelKey) {
			case "function":
				label = labelKeyOrFunction(item);
				break;
			case "string":
				label = OF.Objects.getProperty(item, labelKeyOrFunction);
				break;
			}
		}
		if (label == null || label == "") {
			label = value;
		}
		$select.append($("<option />").val(value).text(label));
	});
	$select.val([]);
};

OF.UI.Forms.selectOptionsInSelect = function($select, items, valueKey) {
	var options = $select.find("option");
	$.each(options, function(i, option) {
		var item = OF.Arrays.findItem(items, valueKey, option.value);
		var selected = item != null;
		$(option).attr('selected', selected ? 'selected': null);
	});
};

OF.UI.Forms.populateDropdown = function($dropdownContainer, items, valueKey, labelKey) {
	var dropdownMenu = $dropdownContainer.find(".dropdown-menu");
	dropdownMenu.empty();

	$.each(items, function(i, item) {
		var value = item.hasOwnProperty(valueKey) ? item[valueKey]: item;
		var label = item.hasOwnProperty(labelKey) ? item[labelKey]: value;
		var item = $('<li role="presentation" />');
		var link = $('<a role="menuitem" tabindex="-1" href="#" />');
		link.text(label);
		item.append(link);
		dropdownMenu.append(item);
	});
};

OF.UI.Forms.toJSON = function($form) {
	var result = {};
	var array = $form.serializeArray();
    $.each(array, function() {
    	var correctedValue;
    	if (this.value == null) {
    		correctedValue = '';
    	} else if (typeof this.value === 'string') {
    		correctedValue = this.value.trim();
    	} else {
    		correctedValue = this.value;
    	}
    	result[this.name] = correctedValue;
    });
    return result;
};

OF.UI.Forms.getFieldNames = function(form) {
	var fields = [];
	var array = form.serializeArray();
    $.each(array, function() {
    	fields.push(this.name);
    });
    return fields;
};

OF.UI.Forms.getParentForm = function(inputField) {
	if (typeof inputField == "form") {
		return inputField;
	} else {
		var form = inputField.closest("form");
		return form;
	}
};

OF.UI.Forms.getInputFields = function($form, fieldName) {
	var fieldNames = [];
	var inputFields = [];
	if (fieldName) {
		fieldNames.push(fieldName);
	} else {
		fieldNames = OF.UI.Forms.getFieldNames($form);
	}
	fieldNames.each(function(i, fieldName) {
		var field = $('[name='+fieldName+']', $form);
		if ( field.length == 1 ) {
			inputFields.push(field);
		} else {
			field.each(function(i, $inputField) {
				inputFields.push($inputField);
			});
		}
	});
	return inputFields;
}

/**
 * Set the specified values into a form according to the field names
 * 
 * @param $form
 * @param $data
 */
OF.UI.Forms.fill = function($form, $data) {
	$.each($data, function(fieldName, value) {
		OF.UI.Forms.setFieldValue($form, fieldName, value);
    });
};
/**
 * Sets the metadata "visited" on the field with the specified value
 */
OF.UI.Forms.setFieldVisited = function(field, visited) {
	field.data("visited", typeof visited == 'undefined' || visited);
};

OF.UI.Forms.setAllFieldsVisited = function(form, visited) {
	var fieldNames = OF.UI.Forms.getFieldNames(form);
	$.each(fieldNames, function(i, fieldName) {
		var fields = OF.UI.Forms.getInputFields(form, fieldName);
		fields.each(function(i, field) {
			field.data("visited", typeof visited == 'undefined' || visited);
		});
	});
};

OF.UI.Forms.setFieldValue = function($form, fieldName, value) {
	var $inputFields = $('[name='+fieldName+']', $form);
	if ( $inputFields.length == 1 ) {
		var inputFieldEl = $inputFields[0];
		switch(OF.UI.Forms.getInputType(inputFieldEl)) {
			case "hidden":  
			case "text" :   
			case "textarea":  
				inputFieldEl.value = value;   
				break;
		}
	} else {
		$inputFields.each(function(i, $inputField) {
			switch(OF.UI.Forms.getInputType($inputField)) {
				case "radio" : 
				case "checkbox":
					var checked = $(this).attr('value') == value;
					$(this).attr("checked", checked); 
					break;  
			}
		});
	}
};

/**
 * Returns the input type of a field.
 * If the field is not a "input" element, then returns the node name of the element.
 *  
 * @param inputField
 * @returns
 */
OF.UI.Forms.getInputType = function(inputField) {
	if ( inputField instanceof jQuery ) {
		if ( inputField.length == 1 ) {
			var field = inputField.get(0);
			return OF.UI.Forms.getInputType(field);
		} else {
			//no single input field found
			return null;
		}
	}
	var type = inputField.type;
	if ( ! type ) {
		//e.g. textarea element
		type = inputField.nodeName.toLowerCase();
	}
	return type;
};

/**
 * Returns the label associated to the specified field 
 * 
 * @param $field
 * @returns
 */
OF.UI.Forms.getFieldLabel = function($field) {
	var $formGroup = $field.closest('.form-group');
	var $labelEl = $formGroup.find('.control-label');
	return $labelEl == null ? "": $labelEl.text();
};