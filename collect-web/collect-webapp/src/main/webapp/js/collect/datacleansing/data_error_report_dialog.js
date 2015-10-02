Collect.DataErrorReportDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_error_report_dialog.html";
	this.itemEditService = collect.dataErrorReportService;
	this.queryGroups = null;
	this.queryGroupSelectPicker = null;
	this.recordStepSelectPicker = null;
};

Collect.DataErrorReportDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorReportDialogController.prototype.initEventListeners = function() {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initEventListeners.call(this);
	$this.content.find('.generate-btn').click(
		$.proxy($this.generateClickHandler, $this)
	);
};

Collect.DataErrorReportDialogController.prototype.generateClickHandler = function() {
	var $this = this;
	if ($this.validateForm()) {
		var item = $this.extractFormObject();
		collect.dataErrorReportService.generateReport(item.queryGroupId, item.recordStep, function() {
			new OF.UI.JobDialog();
			new OF.JobMonitor("datacleansing/dataerrorreports/generate/job.json", function() {
				EventBus.dispatch(Collect.DataCleansing.DATA_ERROR_REPORT_CREATED, $this);
			});
			$this.close();
		});
	}
};

Collect.DataErrorReportDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.call(this, function() {
		collect.dataErrorQueryGroupService.loadAll(function(queryGroups) {
			$this.queryGroups = queryGroups;
			callback.call($this);
		});
	});
};

Collect.DataErrorReportDialogController.prototype.initFormElements = function(callback) {
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

Collect.DataErrorReportDialogController.prototype.extractFormObject = function() {
	var item = Collect.AbstractItemEditDialogController.prototype.extractFormObject.apply(this);
	return item;
};

Collect.DataErrorReportDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.queryGroupSelectPicker.val($this.item.queryGroupId);
		callback.call($this);
	});
};

Collect.DataErrorReportDialogController.prototype.validateForm = function() {
	var $this = this;
	var item = $this.extractFormObject();
	if (! item.queryGroupId) {
		OF.Alerts.showWarning('Please select an error query group');
		return false;
	} else {
		return true;
	}
};
