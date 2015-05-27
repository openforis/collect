Collect.DataCleansingStepDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_cleansing_step_dialog.html";
	this.itemEditService = collect.dataCleansingStepService;
	this.queries = null;
	this.querySelectPicker = null;
	this.stepsDataGrid = null;
	this.addStepSelectPicker = null;
};

Collect.DataCleansingStepDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_SAVED = "dataCleansingStepSaved";
Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_DELETED = "dataCleansingStepDeleted";

Collect.DataCleansingStepDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_SAVED, this);
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
				$this.initFieldFixExpressionFields();
			});
		}
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
			var cleansingStep = $this.extractJSONItem();
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
	var item = $this.extractJSONItem();
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

Collect.DataCleansingStepDialogController.prototype.initFieldFixExpressionFields = function() {
	var $this = this;
	var queryId = $this.querySelectPicker.val()
	var query = OF.Arrays.findItem($this.queries, "id", queryId);
	var attrDef = collect.activeSurvey.getDefinition(query.attributeDefinitionId);

	var container = $this.content.find(".fieldFixExpressionsContainer");
	container.empty();
	
	var fieldNames = attrDef.fieldNames;
	for (var fieldIdx = 0; fieldIdx < fieldNames.length; fieldIdx++) {
		var fieldName = fieldNames[fieldIdx];
		var formGroup = $("<div>")
		formGroup.attr("class", "form-group")
		var label = $("<label>");
		label.text(fieldName);
		var textInput = $("<input>");
		textInput.attr("type", "text");
		textInput.attr("class", "form-control")
		textInput.attr("name", "fieldFixExpressions[" + fieldIdx + "]");
		formGroup.append(label);
		formGroup.append(textInput);
		
		container.append(formGroup);
	}
};

Collect.DataCleansingStepDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.querySelectPicker.val($this.item.queryId);
		
		$this.initFieldFixExpressionFields();
		
		callback();
	});
};
