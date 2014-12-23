Collect.DataErrorReportViewDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "/collect/datacleansing/data_error_report_view_dialog.html";
	this.itemEditService = collect.dataErrorReportService;
	this.queries = null;
	this.querySelectPicker = null;
	this.recordStepSelectPicker = null;
	this.reportItemsDataGrid = null;
};

Collect.DataErrorReportViewDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorReportViewDialogController.prototype.initEventListeners = function() {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initEventListeners.call(this);
	$this.content.find('.generate-btn').click(function() {
		if ($this.validateForm()) {
			var item = $this.extractJSONItem();
			collect.dataErrorReportService.generateReport(item.queryId, item.recordStep, function() {
				alert("Report Generation Started");
				$this.close();
			});
		}
	});
};

Collect.DataErrorReportViewDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.call(this, function() {
		collect.dataErrorQueryService.loadAll(function(queries) {
			$this.queries = queries;
			callback();
		});
	});
};

Collect.DataErrorReportViewDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
//		{
//			var select = $this.content.find('select[name="queryId"]');
//			OF.UI.Forms.populateSelect(select, $this.queries, "id", "title", true);
//			select.selectpicker();
//			$this.querySelectPicker = select.data().selectpicker;
//		}
//		{
//			var select = $this.content.find('select[name="recordStep"]');
//			OF.UI.Forms.populateSelect(select, [{name: "ENTRY", label: "Data Entry"}, 
//			                                    {name: "CLEANSING", label: "Data Cleansing"},
//			                                    {name: "ANALYSIS", label: "Data Analysis"}
//			                                    ], "name", "label");
//			select.selectpicker();
//			$this.recordStepSelectPicker = select.data().selectpicker;
//		}
		{
			var el = $this.content.find('.data-error-report-items-grid');
			el.bootstrapTable({
			    url: "/collect/datacleansing/dataerrorreports/" + $this.item.id + "/items.json",
			    cache: false,
			    clickToSelect: true,
			    columns: [
		          	{field: "selected", title: "", radio: true},
					{field: "id", title: "Id", visible: false},
					{field: "key1", title: "Key1"},
					{field: "key2", title: "Key2"},
					{field: "key3", title: "Key3"},
					{field: "nodePath", title: "Path"},
					{field: "attributeValue", title: "Value"}
				]
			});
			$this.reportItemsDataGrid = el.data('bootstrap.table');
		}
		callback();
	});
};

Collect.DataErrorReportViewDialogController.prototype.extractJSONItem = function() {
	var item = Collect.AbstractItemEditDialogController.prototype.extractJSONItem.apply(this);
	return item;
};

Collect.DataErrorReportViewDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
//		$this.querySelectPicker.val($this.item.queryId);
		callback();
	});
};
