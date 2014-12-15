OpenForis.UI.Form = function() {};

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
OpenForis.UI.Form.populateSelect = function($select, items, valueKey, labelKey, addEmptyOption) {
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


OpenForis.UI.Form.populateDropdown = function($dropdownContainer, items, valueKey, labelKey) {
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