Collect.DataCleansingStepValueDialogController = function(index, dataQuery) {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_cleansing_step_value_dialog.html";
	this.itemEditService = collect.dataCleansingStepValueService;
	this.index = index;
	this.dataQuery = dataQuery;
	
	this.updateTypes = [{name: "ATTRIBUTE", label : "Attribute"}, {name: "FIELD", label : "Field"}];
};

Collect.DataCleansingStepValueDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataCleansingStepValueDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		{//init update type change listener
			var radioButtons = $this.content.find('input[name="updateType"]');
			radioButtons.change(function() {
				$this.fieldChangeHandler();
				$this.updateViewState(function(){});
			});
			$this.initFieldFixExpressionFields();
		}
		callback();
	});
};

Collect.DataCleansingStepValueDialogController.prototype.extractFormObject = function() {
	var $this = this;
	var formObject = Collect.AbstractItemEditDialogController.prototype.extractFormObject.call($this);
	formObject.queryId = $this.dataQuery.id;
	formObject.index = $this.index;
	switch(formObject.updateType) {
	case "ATTRIBUTE":
		formObject.fixExpressions = null;
		break;
	case "FIELD":
		formObject.fixExpression = null;
		break;
	}
	return formObject;
};

Collect.DataCleansingStepValueDialogController.prototype.updateViewState = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.updateViewState.call(this, function() {
		var item = $this.extractFormObject();
		switch(item.updateType) {
		case "ATTRIBUTE":
			$this.content.find(".attributeFixExpressionContainer").show();
			$this.content.find(".fieldFixExpressionsContainer").hide();
			break;
		case "FIELD":
			$this.content.find(".attributeFixExpressionContainer").hide();
			$this.content.find(".fieldFixExpressionsContainer").show();
			break;
		}
		callback.call($this);
	});
};

Collect.DataCleansingStepValueDialogController.prototype.initFieldFixExpressionFields = function() {
	var $this = this;
	var fieldNames = $this.getAttributeFieldNames();

	var container = $this.content.find(".fieldFixExpressionsContainer");
	container.empty();
	
	for (var fieldIdx = 0; fieldIdx < fieldNames.length; fieldIdx++) {
		var fieldName = fieldNames[fieldIdx];
		var formGroup = $("<div>")
		formGroup.attr("class", "form-group")
		var label = $("<label>");
		label.text(fieldName);
		var textInput = $("<input>");
		textInput.attr("type", "text");
		textInput.attr("class", "form-control")
		textInput.attr("name", $this.getFieldFixExpressionInputFieldName(fieldIdx));
		textInput.focusout($.proxy($this.fieldFocusOutHandler, $this, textInput));
		
		formGroup.append(label);
		formGroup.append(textInput);
		
		container.append(formGroup);
	}
};

Collect.DataCleansingStepValueDialogController.prototype.getFieldFixExpressionInputFieldName = function(fieldIdx, escapeSpecialCharacters) {
	var result = "fieldFixExpressions";
	if (escapeSpecialCharacters) {
		result += "\\";
	}
	result += "[" + fieldIdx;
	if (escapeSpecialCharacters) {
		result += "\\";
	}
	result += "]";
	return result;
};

Collect.DataCleansingStepValueDialogController.prototype.getAttributeFieldNames = function() {
	var $this = this;
	var query = $this.dataQuery;
	var attrDef = collect.activeSurvey.getDefinition(query.attributeDefinitionId);
	var fieldNames = attrDef.fieldNames;
	return fieldNames;
};