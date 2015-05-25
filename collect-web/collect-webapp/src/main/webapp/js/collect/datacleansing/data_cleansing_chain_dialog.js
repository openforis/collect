Collect.DataCleansingChainDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_cleansing_chain_dialog.html";
	this.itemEditService = collect.dataCleansingChainService;
	this.queries = null;
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
		collect.dataQueryService.loadAll(function(queries) {
			$this.queries = queries;
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
		
		callback();
	});
};

//Collect.DataCleansingChainDialogController.prototype.fillForm = function(callback) {
//	var $this = this;
//	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
//		callback();
//	});
//};

