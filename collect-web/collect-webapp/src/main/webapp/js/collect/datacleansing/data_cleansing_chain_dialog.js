Collect.DataCleansingChainDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_cleansing_chain_dialog.html";
	this.itemEditService = collect.dataCleansingChainService;
	this.queries = null;
	this.stepsDataGrid = null;
	this.querySelectPicker = null;
};

Collect.DataCleansingChainDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataCleansingChainDialogController.DATA_CLEANSING_CHAIN_SAVED = "dataCleansingChainSaved";
Collect.DataCleansingChainDialogController.DATA_CLEANSING_CHAIN_DELETED = "dataCleansingChainDeleted";

Collect.DataCleansingChainDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansingChainDialogController.DATA_CLEANSING_CHAIN_SAVED, this);
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
		var initNewStepSelectPicker = function() {
			var select = $this.content.find('select[name="cleansingStep"]');
			OF.UI.Forms.populateSelect(select, $this.availableNewSteps, "id", "title", true);
			select.selectpicker();
			$this.addStepSelectPicker = select.data().selectpicker;
			$this.addStepSelectPicker.refresh();
		}
		initNewStepSelectPicker();
		
		var monitorJob = function(jobMonitorUrl, complete) {
			var jobDialog = new OF.UI.JobDialog();
			new OF.JobMonitor(jobMonitorUrl, function() {
				jobDialog.close();
				complete();
			});
		};
		
		$this.content.find(".run-btn").click($.proxy(function() {
			var chain = $this.extractJSONItem();
			var recordStep = $this.recordStepSelectPicker.val();
			collect.dataCleansingChainService.run(chain.id, recordStep, function() {
				monitorJob(collect.jobService.contextPath + "survey-job.json?surveyId=" + collect.activeSurvey.id, function() {
					//TODO
				});
			});
		}, $this));
		
		$this.content.find(".add-step-btn").click($.proxy(function() {
			var selectedStepId = $this.addStepSelectPicker.val();
			if (selectedStepId == null || selectedStepId == '') {
				return;
			}
			var selectedStep = OF.Arrays.findItem($this.availableNewSteps, "id", selectedStepId);
			$this.item.steps.push(selectedStep);
			
			OF.Arrays.removeItem($this.availableNewSteps, selectedStep);
			
			$this.initStepsDataGrid();
			initNewStepSelectPicker();
		}, $this));
		
		$this.initStepsDataGrid();
		
		callback();
	});
};

Collect.DataCleansingChainDialogController.prototype.extractJSONItem = function() {
	var item = Collect.AbstractItemEditDialogController.prototype.extractJSONItem.apply(this);
//	item.stepIds = new Array();
	item.steps = new Array();
	var steps = this.item.steps;
	for (var idx = 0; idx < steps.length; idx++) {
		var step = steps[idx];
//		item.stepIds.push(step.id);
		item.steps.push(step);
	}
	return item;
};

Collect.DataCleansingChainDialogController.prototype.initStepsDataGrid = function() {
	var $this = this;
	var gridContainer = $(".step-grid");
	gridContainer.bootstrapTable({
	    data: $this.item == null ? null : $this.item.steps,
	    clickToSelect: true,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title"},
			{field: "queryTitle", title: "Query Title"},
			{field: "creationDate", title: "Creation Date"},
			{field: "modifiedDate", title: "Modified Date"}
		]
	});
	$this.stepsDataGrid = gridContainer.data('bootstrap.table');
};

