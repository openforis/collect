Collect.DataCleansing.DataReportPanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Data Report", 
			Collect.DataReportDialogController,
			collect.dataReportService, 
			Collect.DataCleansing.DATA_REPORT_DELETED]);
};

Collect.DataCleansing.DataReportPanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataReportPanelController.prototype.getDataGridOptions = function() {
	var $this = this;
	
	function percentFormatter(value, row) {
		return value.toFixed(1) + " %";
    }
	
	return {
	    url: "datacleansing/datareports/list.json",
	    height: 400,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "queryGroupTitle", title: "Query Group", sortable: true, width: 300},
			{field: "datasetSize", title: "Dataset Size", align: "right", sortable: true, width: 100},
			{field: "lastRecordModifiedDate", title: "Last Record Modified", formatter: OF.Dates.formatToPrettyDateTime, align: "right", sortable: true, width: 130},
			{field: "itemCount", title: "Values found", align: "right", sortable: true, width: 100},
			{field: "affectedRecordsCount", title: "Affected Records", align: "right", sortable: true, width: 120},
			{field: "affectedRecordsPercent", title: "Affected Records %", formatter: percentFormatter, align: "right", sortable: true, width: 100},
			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, align: "right", sortable: true, width: 130},
			$this.createGridItemDeleteColumn()
		]
	};
};

Collect.DataCleansing.DataReportPanelController.prototype.openItemEditDialog = function(item) {
	if (item) {
		var dialogController = new Collect.DataReportViewDialogController();
		dialogController.open(item);
	} else {
		var dialogController = new Collect.DataReportDialogController();
		dialogController.open();
	}
};