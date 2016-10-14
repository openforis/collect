Collect.DataCleansingStepDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_cleansing_step_dialog.html";
	this.itemEditService = collect.dataCleansingStepService;
	this.queries = null;
	this.querySelectPicker = null;
	this.cleansingTypeSelectPicker = null;
	this.stepsDataGrid = null;
	this.addStepSelectPicker = null;
	this.updateValuesFieldset = null;
		
	this.maxFieldNumber = 6;
	
	this.cleansingTypes = [
                       		{code: "a", label : "Update Attribute"}, 
                       		{code: "e", label : "Delete Entity"}, 
                       		{code: 'r', label: "Delete Record"}
			              ];
	this.updateTypes = [
                    		{name: "ATTRIBUTE", label : "Attribute"}, 
                    		{name: "FIELD", label : "Field by Field"}
	                   ];
	
	this.updateValues = [];
	this.addNewUpdateValue();
};

Collect.DataCleansingStepDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataCleansingStepDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansing.DATA_CLEANSING_STEP_SAVED, this);
};

Collect.DataCleansingStepDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, [function() {
		//load data queries
		collect.dataQueryService.loadAll(function(queries) {
			$this.queries = queries;
			callback();
		});
	}]);
};

Collect.DataCleansingStepDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		{//init query select picker
			var select = $this.content.find('select[name="queryId"]');
			OF.UI.Forms.populateSelect(select, $this.queries, "id", "title", true);
			select.selectpicker();
			$this.querySelectPicker = select.data().selectpicker;
			select.change(function() {
				$this.updateView();
			});
		}
		{//init query select picker
			var select = $this.content.find('select[name="typeCode"]');
			OF.UI.Forms.populateSelect(select, $this.cleansingTypes, "code", "label", true);
			select.selectpicker();
			$this.cleansingTypeSelectPicker = select.data().selectpicker;
			select.change(function() {
				$this.updateView();
			});
		}
		$this.content.find(".update-value-add-btn").click(function() {
			$this.addNewUpdateValue();
			$this.refreshUpdateValueGrid();
		});
		
		$this.content.find(".update-value-edit-btn").click(function() {
			var selectedItem = $this.getSelectedUpdateValue();
			if (selectedItem != null) {
				var index = $this.updateValues.indexOf(selectedItem);
				var dialog = new Collect.DataCleansingStepValueDialogController(index, $this.getSelectedQuery());
				dialog.open(selectedItem);
			}
		});
		
		$this.updateValuesFieldset = $this.content.find(".update-values");
		
		$this.updateView();
		
		callback();
	});
};

Collect.DataCleansingStepDialogController.prototype.afterOpen = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.afterOpen.call(this);
	setTimeout(function() {
		$this.refreshUpdateValueGrid();
	}, 200);
};

Collect.DataCleansingStepDialogController.prototype.updateView = function() {
	var $this = this;
	var item = $this.extractFormObject();
	var query = $this.getSelectedQuery();
	$this.updateValuesFieldset.toggle(query != null && item.typeCode == 'a');
	var attributePath = null; 
	var attributeType = null;
	if (query != null) {
		$this.initUpdateValueGrid();
		var attrDef = collect.activeSurvey.getDefinition(query.attributeDefinitionId);
		attributePath = attrDef.getPath();
		attributeType = attrDef.attributeType;
	}
	$this.form.find("input[name=query-attribute-path]").val(attributePath);
	$this.form.find("input[name=query-attribute-type]").val(attributeType);
};

Collect.DataCleansingStepDialogController.prototype.removeErrorsInForm = function() {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.removeErrorsInForm.call($this);
	OF.UI.Forms.Validation.removeErrorFromElement($this.updateValuesFieldset);
	$this.refreshUpdateValueGrid();
};

Collect.DataCleansingStepDialogController.prototype.showErrorsInForm = function(errors, considerOnlyVisitedFields) {
	var $this = this;
	
	Collect.AbstractItemEditDialogController.prototype.showErrorsInForm.call(this, errors, considerOnlyVisitedFields);
	
	var error = OF.UI.Forms.Validation.findError(errors, "updateValues");
	if (error) {
		$this.updateValuesFieldset.addClass('has-error');
		OF.UI.Forms.Validation.createErrorTooltip($this.updateValuesFieldset, error, "Update with values");
	} else {
		OF.UI.Forms.Validation.removeErrorFromElement($this.updateValuesFieldset);
	}
	$this.refreshUpdateValueGrid();
};

Collect.DataCleansingStepDialogController.prototype.initUpdateValueGrid = function() {
	var $this = this;

	if ($this.updateValueGrid) {
		$this.updateValueGrid.destroy();
	}
	var gridContainer = $this.content.find(".update-value-grid");

	var gridFields = [
	     { name: "updateType", title: "Update Type", width: "115px", type : "select", 
	    	 items : $this.updateTypes, valueField : "name", textField : "label" },
	     { name: "condition", title: "Condition", width: "100px", type : "textarea"},
	     { name: "fixExpression", title: "Attribute fix expression", width: "150px", type : "textarea"}
	];
	var fieldNames = $this.getAttributeFieldNames();
	for (var i=0; i < fieldNames.length; i++) {
		gridFields.push({
			name : "fieldFixExpressions[" + i + "]",
			title : fieldNames[i],
			type : "textarea",
			width : "120px"
		});
	}
	
	//control bar
	gridFields.push({ type: "control", editButton: true, modeSwitchButton: false });
	
	gridContainer.jsGrid({
		data : $this.updateValues,
		fields : gridFields,
		editing : true,
		deleteConfirm : "Do you really want to delete the row?",
		onItemUpdated : function() {
			$this.fieldChangeHandler();
		},
		rowClick : function() {}, //disable default behavior (do not edit row when clicking on it)
		width: "100%"
	});
	
	var $gridData = gridContainer.find(".jsgrid-grid-body tbody");
	$gridData.sortable();
	
	$this.updateValueGrid = gridContainer.data("JSGrid");
};

Collect.DataCleansingStepDialogController.prototype.refreshUpdateValueGrid = function() {
	var $this = this;
	if ($this.updateValueGrid) {
		$this.updateValueGrid.refresh();
	}
};

Collect.DataCleansingStepDialogController.prototype.getSelectedUpdateValue = function() {
	var $this = this;
	var grid = $this.updateValueGrid;
	var editingRow = grid._editingRow;
	if (editingRow == null) {
		return null;
	} else {
		var selectedItem = editingRow.data().JSGridItem;
		return selectedItem;
	}
}

Collect.DataCleansingStepDialogController.prototype.addNewUpdateValue = function() {
	var $this = this;
	var newItem = {updateType: "ATTRIBUTE", condition: "", fixExpression: ""};
	var maxFieldNumber = 6;
	for (var i=0; i < $this.maxFieldNumber; i++) {
		newItem["fieldFixExpression_" + i] = "";
	}
	$this.updateValues.push(newItem);
}

Collect.DataCleansingStepDialogController.prototype.getAttributeFieldNames = function() {
	var $this = this;
	var query = $this.getSelectedQuery();
	if (query != null) {
		var attrDef = collect.activeSurvey.getDefinition(query.attributeDefinitionId);
		var fieldNames = attrDef.fieldNames;
		return fieldNames;
	}
};

Collect.DataCleansingStepDialogController.prototype.getSelectedQuery = function() {
	var $this = this;
	var queryId = $this.querySelectPicker.val();
	if (! queryId || queryId.length == 0) {
		return null;
	} else {
		var query = OF.Arrays.findItem($this.queries, "id", queryId);
		return query;
	}
};

Collect.DataCleansingStepDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.querySelectPicker.val($this.item.queryId);
		$this.cleansingTypeSelectPicker.val($this.item.typeCode);
		$this.updateValues = $this.item.updateValues;
		
		$this.updateView();
		
		callback();
	});
};

Collect.DataCleansingStepDialogController.prototype.extractFormObject = function() {
	var $this = this;
	var formItem = Collect.AbstractItemEditDialogController.prototype.extractFormObject.call(this);
	formItem.updateValues = $this.updateValues;
	return formItem;
};