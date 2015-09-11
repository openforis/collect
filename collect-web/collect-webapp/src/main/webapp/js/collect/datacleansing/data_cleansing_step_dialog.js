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

Collect.DataCleansingStepDialogController.prototype.updateView = function() {
	var $this = this;
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
};

Collect.DataCleansingStepDialogController.prototype.initUpdateValueGrid = function() {
	var $this = this;
	var fieldNames = $this.getAttributeFieldNames();
	
	var gridContainer = $this.content.find(".update-value-grid");
	var gridWidget = gridContainer.data("shieldWidgetGrid");
	if (gridWidget) {
		gridWidget.destroy();
	}
	var schemaFields = {
    	updateType : { path: "updateType", type: String },
        condition : { path: "condition", type: String },
        fixExpression : { path: "fixExpression", type: String }
    };
	for (var i=0; i < $this.maxFieldNumber; i++) {
		schemaFields["fieldFixExpression_" + i] = {path: "fieldFixExpressions_" + i, type: String};
	}
	
	var updateTypeEditor = function(cell, item) {
		var fieldName = "updateType";
		var data = [ "By attribute", "Field By Field" ];
		$('<div class="dropdown"/>')
			.appendTo(cell)
			.shieldDropDown({
				dataSource : {data : data},
				value : ! item[fieldName] ? null : item[fieldName].toString()
		}).swidget().focus();
	};
	
	var columns = [
	     { field: "updateType", title: "Update Type", width: "150px", editor: updateTypeEditor },
	     { field: "condition", title: "Condition", width: "150px" },
	     { field: "fixExpression", title: "Fix Expression", width: "150px" }
	];
	for (var i=0; i < fieldNames.length; i++) {
		columns.push({
			field: "fieldFixExpression_" + i,
			title: fieldNames[i],
			type: String
		});
	}
	
	//add delete column
	columns.push({
		width : "40px",
		title : " ",
		buttons : [ {
			cls : "deleteButton",
			commandName : "delete",
			caption : Collect.Grids.getDeleteColumnIconTemplate()
		} ]
	});
	
	gridContainer.shieldGrid({
		dataSource: {
            data: $this.updateValues,
            schema: {
                fields: schemaFields
            }
		},
        columns: columns,
          editing: {
              enabled: true,
              event: "click",
              type: "cell",
              confirmation: {
                  "delete": {
                      enabled: true,
                      template: function (item) {
                          return "Delete row";
                      }
                  }
              }
          }
	});
	
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
		
		$this.initUpdateValueGrid();
		$this.updateView();
		
		if ($this.item) {
			var fixExpressions = $this.item.fieldFixExpressions;
			for (var fieldIdx = 0; fieldIdx < fixExpressions.length; fieldIdx++) {
				var fixExpression = fixExpressions[fieldIdx];
				var inputFieldName = $this.getFieldFixExpressionInputFieldName(fieldIdx, true);
				OF.UI.Forms.setFieldValue($this.content, inputFieldName, fixExpression);
			}
		}
		
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
	return formItem;
};