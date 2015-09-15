Collect.DataCleansingStepDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_cleansing_step_dialog.html";
	this.itemEditService = collect.dataCleansingStepService;
	this.queries = null;
	this.updateValues = [];
	this.querySelectPicker = null;
	this.stepsDataGrid = null;
	this.addStepSelectPicker = null;
	
	this.maxFieldNumber = 6;
	
	this.updateTypes = [{name: "ATTRIBUTE", label : "Attribute"}, {name: "FIELD", label : "Field"}];
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
				$this.initUpdateValueGrid();
			});
		}
		$this.content.find(".update-value-add-btn").click(function() {
			var newItem = {updateType: "ATTRIBUTE", condition: "", fixExpression: ""};
			var maxFieldNumber = 6;
			for (var i=0; i < $this.maxFieldNumber; i++) {
				newItem["fieldFixExpression_" + i] = "";
			}
			$this.updateValues.push(newItem);
			$this.initUpdateValueGrid();
		});
		
		{//init record step select
			var select = $this.content.find('select[name="recordStep"]');
			OF.UI.Forms.populateSelect(select, Collect.DataCleansing.WORKFLOW_STEPS, "name", "label");
			select.selectpicker();
			$this.recordStepSelectPicker = select.data().selectpicker;
			$this.recordStepSelectPicker.refresh();
		}
		
		{//
			var radioButtons = $this.content.find('input[name="updateType"]');
			radioButtons.change($.proxy($this.updateView, $this));
		}
		
		var monitorJob = function(jobMonitorUrl, complete) {
			var jobDialog = new OF.UI.JobDialog();
			new OF.JobMonitor(jobMonitorUrl, function() {
				jobDialog.close();
				complete();
			});
		};
		
		$this.content.find(".run-btn").click($.proxy(function() {
			var cleansingStep = $this.extractFormObject();
			var recordStep = $this.recordStepSelectPicker.val();
			collect.dataCleansingStepService.run(cleansingStep.id, recordStep, function() {
				monitorJob(collect.jobService.contextPath + "survey-job.json?surveyId=" + collect.activeSurvey.id, function() {
				});
			});
		}, $this));
		
		$this.updateView();
		
		callback();
	});
};

Collect.DataCleansingStepDialogController.prototype.afterOpen = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.afterOpen.call(this);
	setTimeout(function() {
		$this.updateValueGrid.refresh();
	}, 200);
};

Collect.DataCleansingStepDialogController.prototype.updateView = function() {
	var $this = this;
	var item = $this.extractFormObject();
	$("#update-values-fieldset").toggle(item.queryId != null);
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
	     { name: "condition", title: "Condition", width: "100px", type : "text"},
	     { name: "fixExpression", title: "Fix Expression", width: "150px", type : "text"}
	];
	var fieldNames = $this.getAttributeFieldNames();
	for (var i=0; i < fieldNames.length; i++) {
		gridFields.push({
			name : "fieldFixExpressions[" + i + "]",
			title : fieldNames[i],
			type : "text",
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
		width: "100%"
	});
	
	var $gridData = gridContainer.find(".jsgrid-grid-body tbody");
	$gridData.sortable();
	
	$this.updateValueGrid = gridContainer.data("JSGrid");
};

Collect.DataCleansingStepDialogController.prototype.getAttributeFieldNames = function() {
	var $this = this;
	var queryId = $this.querySelectPicker.val();
	if (! queryId || queryId.length == 0) {
		return null;
	} else {
		var query = OF.Arrays.findItem($this.queries, "id", queryId);
		var attrDef = collect.activeSurvey.getDefinition(query.attributeDefinitionId);
		var fieldNames = attrDef.fieldNames;
		return fieldNames;
	}
};

Collect.DataCleansingStepDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.querySelectPicker.val($this.item.queryId);
		$this.updateValues = $this.item.updateValues;
		
		$this.initUpdateValueGrid();
		$this.updateView();
		
		callback();
	});
};

Collect.DataCleansingStepDialogController.prototype.getFieldFixExpressionInputFieldName = function(fieldIdx, escapeSpecialCharacters) {
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

Collect.DataCleansingStepDialogController.prototype.extractFormObject = function() {
	var $this = this;
	var formItem = Collect.AbstractItemEditDialogController.prototype.extractFormObject.apply(this);
	var fieldNames = $this.getAttributeFieldNames();
	switch(formItem.updateType) {
	case "ATTRIBUTE":
		if (fieldNames != null) {
			for (var fieldIdx = 0; fieldIdx < fieldNames.length; fieldIdx++) {
				var inputFieldName = $this.getFieldFixExpressionInputFieldName(fieldIdx);
				delete formItem[inputFieldName];
			}
		}
		break;
	case "FIELD":
		formItem.fixExpression = null;
		break;
	}
	formItem.updateValues = $this.updateValues;
	
	return formItem;
};