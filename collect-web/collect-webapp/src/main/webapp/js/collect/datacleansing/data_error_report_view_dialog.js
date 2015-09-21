Collect.DataErrorReportViewDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_error_report_view_dialog.html";
	this.itemEditService = collect.dataErrorReportService;
	this.queryGroups = null;
	this.queryGroupSelectPicker = null;
	this.recordStepSelectPicker = null;
	this.reportItemsDataGrid = null;
};

Collect.DataErrorReportViewDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorReportViewDialogController.prototype.initEventListeners = function() {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initEventListeners.call(this);
};

Collect.DataErrorReportViewDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.call(this, function() {
		collect.dataErrorQueryGroupService.loadAll(function(queryGroups) {
			$this.queryGroups = queryGroups;
			callback();
		});
	});
};

Collect.DataErrorReportViewDialogController.prototype.initFormElements = function(callback) {
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
		{
			var columns = [
               {field: "selected", title: "", radio: true},
               {field: "id", title: "Id", visible: false}
	        ];
			columns = columns.concat(Collect.Grids.createRootEntityKeyColumns(collect.activeSurvey));
			columns = columns.concat([
              {field: "errorType.prettyLabel", title: "Error Type", width: 120},
              {field: "severity", title: "Severity", width: 100},
              {field: "queryTitle", title: "Query", width: 400},
              {field: "nodePath", title: "Path", width: 400},
              {field: "attributeValue", title: "Value", width: 400},
			]);
			var el = $this.content.find('.data-error-report-items-grid');
			el.bootstrapTable({
			    url: "datacleansing/dataerrorreports/" + $this.item.id + "/items.json",
			    cache: false,
			    clickToSelect: true,
			    pagination: true,
			    sidePagination: "server",
			    pageList: [10, 20, 50, 100],
			    width: 800,
			    height: 400,
			    columns: columns
			});
			$this.reportItemsDataGrid = el.data('bootstrap.table');
		}
		
		$this.content.find(".export-to-csv-btn").click($.proxy(function() {
			var report = $this.extractFormObject();
			collect.dataErrorReportService.exportToCSV(report.id);
		}, $this));
		
		$this.content.find(".export-to-csv-for-collect-earth-btn").click($.proxy(function() {
			var report = $this.extractFormObject();
			collect.dataErrorReportService.exportToCSVForCollectEarth(report.id);
		}, $this));
		
		callback();
	});
};

Collect.DataErrorReportViewDialogController.prototype.extractFormObject = function() {
	var item = Collect.AbstractItemEditDialogController.prototype.extractFormObject.apply(this);
	return item;
};

Collect.DataErrorReportViewDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.queryGroupSelectPicker.val($this.item.errorQueryGroup.id);
		callback();
	});
};
