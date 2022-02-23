Collect.DataCleansingChainDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_cleansing_chain_dialog.html";
	this.itemEditService = collect.dataCleansingChainService;
	this.queries = null;
	this.stepsDataGrid = null;
	this.querySelectPicker = null;
	this.steps = [];
};

Collect.DataCleansingChainDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataCleansingChainDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansing.DATA_CLEANSING_CHAIN_SAVED, this);
};

Collect.DataCleansingChainDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, [function() {
		//load data cleansing steps
		collect.dataCleansingStepService.loadAll(function(steps) {
			if ($this.item == null || $this.item.steps == null || $this.item.steps.length == 0) {
				$this.availableNewSteps = steps;
			} else {
				$this.availableNewSteps = new Array();
				for (var idx = 0; idx < steps.length; idx++) {
					var step = steps[idx];
					var itemStep = OF.Arrays.findItem($this.item.steps, "id", step.id);
					if (itemStep == null) {
						$this.availableNewSteps.push(step);
					}
				}
			}
			callback();
		});
	}]);
};

Collect.DataCleansingChainDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		{//init record step select
			var select = $this.content.find('select[name="recordStep"]');
			OF.UI.Forms.populateSelect(select, Collect.DataCleansing.WORKFLOW_STEPS, "name", "label");
			select.selectpicker();
			$this.recordStepSelectPicker = select.data().selectpicker;
			$this.recordStepSelectPicker.refresh();
		}
		
		$this.initNewStepSelectPicker();
		
		var monitorJob = function(jobMonitorUrl, complete) {
			var jobDialog = new OF.UI.JobDialog();
			new OF.JobMonitor(jobMonitorUrl, function() {
				jobDialog.close();
				complete();
			});
		};
		
		$this.content.find(".run-btn").click($.proxy(function() {
			var chain = $this.extractFormObject();
			var recordStep = $this.recordStepSelectPicker.val();
			collect.dataCleansingChainService.run(chain.id, recordStep, function() {
				monitorJob(collect.jobService.contextPath + "survey-job.json?surveyId=" + collect.activeSurvey.id, function() {
					EventBus.dispatch(Collect.DataCleansing.DATA_CLEANSING_CHAIN_SAVED, $this);
					OF.Alerts.success("collect.data_cleansing.cleansing_chain.run_successfully");
				});
			});
		}, $this));
		
		var getSelectedStepToAdd = function() {
			var selectedStepId = $this.addStepSelectPicker.val();
			if (selectedStepId == null || selectedStepId == '') {
				return null;
			}
			var selectedStep = OF.Arrays.findItem($this.availableNewSteps, "id", selectedStepId);
			return selectedStep;
		};
		
		$this.content.find(".add-step-btn").click($.proxy(function() {
			var selectedStep = getSelectedStepToAdd();
			if (selectedStep == null) {
				return;
			}
			$this.steps.push(selectedStep);
			
			OF.Arrays.removeItem($this.availableNewSteps, selectedStep);
			
			$this.refreshStepsDataGrid();
			
			$this.initNewStepSelectPicker();
		}, $this));
		
		$this.content.find(".remove-step-btn").click($.proxy(function() {
			var selectedStep = $this.getSelectedStep();
			if (selectedStep == null) {
				return;
			}
			$this.deleteStep(selectedStep);
		}, $this));
		
		var moveSelectedStep = function(up) {
			var $this = this;
			var selectedStep = $this.getSelectedStep();
			if (selectedStep == null) {
				return;
			}
			var stepIndex = $this.steps.indexOf(selectedStep);
			if (up && stepIndex == 0 || ! up && stepIndex == $this.steps.length) {
				return;
			}
			var toIndex = stepIndex + (up ? -1 : 1);
			OF.Arrays.shiftItem($this.steps, selectedStep, toIndex);
			
			$this.refreshStepsDataGrid();
		};
		
		$this.content.find(".move-step-up-btn").click($.proxy(function() {
			$.proxy(moveSelectedStep, $this)(true);
		}, $this));
		
		$this.content.find(".move-step-down-btn").click($.proxy(function() {
			$.proxy(moveSelectedStep, $this)(false);
		}, $this));
		
		$this.initStepsDataGrid();
		
		$this.onStepSelectionChange();
		
		callback();
	});
};

Collect.DataCleansingChainDialogController.prototype.extractFormObject = function() {
	var formItem = Collect.AbstractItemEditDialogController.prototype.extractFormObject.apply(this);
	formItem.stepIds = new Array();
	var steps = this.steps;
	for (var idx = 0; idx < steps.length; idx++) {
		var step = steps[idx];
		formItem.stepIds.push(step.id);
	}
	return formItem;
};

Collect.DataCleansingChainDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.steps = OF.Arrays.clone($this.item.steps);
		$this.refreshStepsDataGrid();
		callback();
	});
};

Collect.DataCleansingChainDialogController.prototype.validateForm = function(callback) {
	if (this.steps.length == 0) {
		OF.Alerts.showWarning("Please add at least one step");
		return false;
	}
	return true;
};

Collect.DataCleansingChainDialogController.prototype.initStepsDataGrid = function() {
	var $this = this;
	var gridContainer = $this.content.find(".step-grid");
	
	gridContainer.bootstrapTable({
	    clickToSelect: true,
	    height: 200,
	    width: 800,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "title", title: "Title", width: 400},
			{field: "queryTitle", title: "Query Title", width: 200},
			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100},
			{field: "modifiedDate", title: "Modified Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100},
			Collect.Grids.createDeleteColumn($this.deleteStep, $this)
		]
	});
	$this.stepsDataGrid = gridContainer.data('bootstrap.table');
	gridContainer.on("check.bs.table", function() {
		$this.onStepSelectionChange();
	});
	$this.onStepSelectionChange();
};

Collect.DataCleansingChainDialogController.prototype.initNewStepSelectPicker = function() {
	var $this = this;
	$this.availableNewSteps.sort(function(a, b) {
		return a.title.localeCompare(b.title);
	});
	var select = $this.content.find('select[name="cleansingStep"]');
	OF.UI.Forms.populateSelect(select, $this.availableNewSteps, "id", "title", true);
	select.selectpicker();
	$this.addStepSelectPicker = select.data().selectpicker;
	$this.addStepSelectPicker.refresh();
};

Collect.DataCleansingChainDialogController.prototype.deleteStep = function(step) {
	var $this = this;
	OF.Alerts.confirm("Remove the cleansing step from this chain?", function() {
		OF.Arrays.removeItem($this.steps, step);
		$this.refreshStepsDataGrid();
		
		$this.availableNewSteps.push(step);
		$this.initNewStepSelectPicker();
	});
};

Collect.DataCleansingChainDialogController.prototype.onStepSelectionChange = function() {
	var $this = this;
	var selectedStep = $this.getSelectedStep();
	var stepSelected = selectedStep != null;
	$this.content.find(".step-selected-enabled").prop("disabled", ! stepSelected);
};

Collect.DataCleansingChainDialogController.prototype.getSelectedStep = function () {
	var $this = this;
	var selections = $this.stepsDataGrid.getSelections();
	return selections.length == 0 ? null : selections[0];
}

Collect.DataCleansingChainDialogController.prototype.refreshStepsDataGrid = function() {
	var $this = this;
	var data = $this.steps ? $this.steps : null;
	$this.stepsDataGrid.load(data);
	$this.onStepSelectionChange();
};
