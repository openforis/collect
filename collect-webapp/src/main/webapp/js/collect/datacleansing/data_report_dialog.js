Collect.DataReportDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_report_dialog.html";
	this.itemEditService = collect.dataReportService;
	this.queryGroups = null;
	this.queryGroupSelectPicker = null;
	this.recordStepSelectPicker = null;
};

Collect.DataReportDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataReportDialogController.prototype.initEventListeners = function() {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initEventListeners.call(this);
	$this.content.find('.generate-btn').click(
		$.proxy($this.generateClickHandler, $this)
	);
};

Collect.DataReportDialogController.prototype.generateClickHandler = function() {
	var $this = this;
	if ($this.validateForm()) {
		var item = $this.extractFormObject();
		collect.dataReportService.generateReport(item.queryGroupId, item.recordStep, function() {
			new OF.UI.JobDialog();
			new OF.JobMonitor("api/datacleansing/datareports/generate/job.json", function() {
				EventBus.dispatch(Collect.DataCleansing.DATA_REPORT_CREATED, $this);
			});
			$this.close();
		});
	}
};

Collect.DataReportDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.call(this, function() {
		collect.dataQueryGroupService.loadAll(function(queryGroups) {
			$this.queryGroups = queryGroups;
			callback.call($this);
		});
	});
};

Collect.DataReportDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		{
			var select = $this.content.find('select[name="queryGroupId"]');
			OF.UI.Forms.populateSelect(select, $this.queryGroups, "id", "title", true);
			select.selectpicker();
			$this.queryGroupSelectPicker = select.data().selectpicker;
		}
		{
			var select = $this.content.find('select[name="recordStep"]');
			OF.UI.Forms.populateSelect(select, Collect.DataCleansing.WORKFLOW_STEPS, "name", "label");
			select.selectpicker();
			$this.recordStepSelectPicker = select.data().selectpicker;
		}
		callback.call($this);
	});
};

Collect.DataReportDialogController.prototype.extractFormObject = function() {
	var item = Collect.AbstractItemEditDialogController.prototype.extractFormObject.apply(this);
	return item;
};

Collect.DataReportDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.queryGroupSelectPicker.val($this.item.queryGroupId);
		callback.call($this);
	});
};

Collect.DataReportDialogController.prototype.validateForm = function() {
	var $this = this;
	var item = $this.extractFormObject();
	if (! item.queryGroupId) {
		OF.Alerts.showWarning('Please select a query group');
		return false;
	} else {
		return true;
	}
};
