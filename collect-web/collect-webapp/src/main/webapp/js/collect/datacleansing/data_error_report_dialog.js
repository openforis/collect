Collect.DataErrorReportDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_error_report_dialog.html";
	this.itemEditService = collect.dataErrorReportService;
	this.queries = null;
	this.querySelectPicker = null;
	this.recordStepSelectPicker = null;
};

Collect.DataErrorReportDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorReportDialogController.DATA_ERROR_REPORT_DELETED = "dataErrorReportDeleted";
Collect.DataErrorReportDialogController.DATA_ERROR_REPORT_CREATED = "dataErrorReportCreated";

Collect.DataErrorReportDialogController.prototype.initEventListeners = function() {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initEventListeners.call(this);
	$this.content.find('.generate-btn').click(
		$.proxy(Collect.AbstractItemEditDialogController.prototype.generateClickHandler, $this)
	);
};

Collect.AbstractItemEditDialogController.prototype.generateClickHandler = function() {
	var $this = this;
	if ($this.validateForm()) {
		var item = $this.extractJSONItem();
		collect.dataErrorReportService.generateReport(item.queryId, item.recordStep, function() {
			new OF.JobMonitor("datacleansing/dataerrorreports/generate/job.json");
			new OF.UI.JobDialog();
			$this.close();
		});
	}
};

Collect.DataErrorReportDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.call(this, function() {
		collect.dataErrorQueryService.loadAll(function(queries) {
			$this.queries = queries;
			callback();
		});
	});
};

Collect.DataErrorReportDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		{
			var select = $this.content.find('select[name="queryId"]');
			OF.UI.Forms.populateSelect(select, $this.queries, "id", "title", true);
			select.selectpicker();
			$this.querySelectPicker = select.data().selectpicker;
		}
		{
			var select = $this.content.find('select[name="recordStep"]');
			OF.UI.Forms.populateSelect(select, Collect.DataCleansing.WORKFLOW_STEPS, "name", "label");
			select.selectpicker();
			$this.recordStepSelectPicker = select.data().selectpicker;
		}
		callback();
	});
};

Collect.DataErrorReportDialogController.prototype.extractJSONItem = function() {
	var item = Collect.AbstractItemEditDialogController.prototype.extractJSONItem.apply(this);
	return item;
};

Collect.DataErrorReportDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.querySelectPicker.val($this.item.queryId);
	});
};
