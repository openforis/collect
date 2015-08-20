Collect.DataCleansing.DataErrorReportPanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Data Error Report", 
			Collect.DataErrorReportDialogController,
			collect.dataErrorReportService, 
			Collect.DataCleansing.DATA_ERROR_REPORT_DELETED]);
};

Collect.DataCleansing.DataErrorReportPanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataErrorReportPanelController.prototype.initDataGrid = function() {
	var $this = this;
	var gridId = 'data-error-report-grid';
	var gridContainer = $("#" + gridId);
	gridContainer.bootstrapTable({
	    url: "datacleansing/dataerrorreports/list.json",
	    cache: false,
	    clickToSelect: true,
	    height: 400,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "queryTitle", title: "Query", sortable: true, width: 400},
			{field: "typeCode", title: "Error Type", sortable: true, width: 100},
			{field: "itemCount", title: "Errors found", sortable: true, width: 100},
			{field: "creationDate", title: "Date", formatter: OF.Dates.formatToPrettyDateTime, sortable: true, width: 100},
			$this.createGridItemDeleteColumn()
		]
	});
	
	$this.dataGrid = gridContainer.data('bootstrap.table');
};


Collect.DataCleansing.DataErrorReportPanelController.prototype.openItemEditDialog = function(item) {
	if (item) {
		var dialogController = new Collect.DataErrorReportViewDialogController();
		dialogController.open(item);
	} else {
		var dialogController = new Collect.DataErrorReportDialogController();
		dialogController.open();
	}
};