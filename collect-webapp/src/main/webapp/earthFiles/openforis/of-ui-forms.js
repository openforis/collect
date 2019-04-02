OF.UI.Forms = function() {};

/**
 * Populate a select using a list of items
 * Option value is set according to the specified valueKey and 
 * option text content is set according to the specified labelKey
 * 
 * @param $select
 * @param items
 * @param valueKey (optional, default value will be item.toString())
 * @param labelKey (optional, default is valueKey, if specified)
 * @param callback
 */
OF.UI.Forms.populateSelect = function($select, items, valueKey, labelKey, addEmptyOption) {
	$select.empty();

	if (addEmptyOption) {
		$select.append($("<option />").val("").text(""));
	}
	
	$.each(items, function(i, item) {
		var value = item.hasOwnProperty(valueKey) ? item[valueKey]: item;
		var label = item.hasOwnProperty(labelKey) ? item[labelKey]: value;
		if (label == null || label == "") {
			label = "(" + value + ")";
		}
		$select.append($("<option />").val(value).text(label));
	});
	$select.val([]);
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
    	result[this.name] = this.value || '';
    });
    return result;
};

/**
 * Set the specified values into a form according to the field names
 * 
 * @param $form
 * @param $data
 */
OF.UI.Forms.fill = function($form, $data) {
	$.each($data, function(fieldName, value) {
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
    });
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
	$formGroup = $field.closest('.form-group');
	$labelEl = $formGroup.find('.control-label');
	var label = ( $labelEl == null ? "": $labelEl.text());
	return label == "" ? $field.attr('name') : label;
};