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
		if (typeof value == 'number') {
			return value.toFixed(1) + " %";
		} else {
			return value;
		}
    }
	
	function onExpandRow(index, report, $rowEl) {
		OF.i18n.initializeAll($rowEl);
		var table = $rowEl.find("table");
		table.bootstrapTable({
			width: 600,
			columns: [
				{field: "typeCode", title: OF.i18n.prop("collect.data_cleansing.data_query.type"), width: 100},
				{field: "errorSeverity", title: OF.i18n.prop("collect.data_cleansing.data_query.error_severity"), width: 100},
				{field: "title", title: OF.i18n.prop("collect.global.title"), width: 400}
		    ],
		    data: report.queryGroup.queries
		});
	};
	
	function detailFormatter(index, chain) {
		var html = 
        	'<fieldset style="margin-left: 60px !important;" ' +
				'class="compact">' +
	        	'<legend data-i18n="collect.data_cleansing.query_group"></legend>' +
	        	'<table></table>' +
           '</fieldset>';
        return html;
	}
	
	return {
	    url: "api/datacleansing/datareports",
	    detailView: true,
	    detailFormatter: detailFormatter,
	    onExpandRow: onExpandRow,
	    columns: [
			{field: "queryGroupTitle", title: "Query Group", sortable: true, width: 300},
			{field: "datasetSize", title: "Dataset Size", align: "right", sortable: true, width: 100},
			{field: "lastRecordModifiedDate", title: "Last Record Modified", formatter: OF.Dates.formatToPrettyDateTime, 
				align: "right", sortable: true, width: 130},
			{field: "itemCount", title: "Affected Values", align: "right", sortable: true, width: 100},
			{field: "affectedRecordsCount", title: "Affected Records", align: "right", sortable: true, width: 120},
			{field: "affectedRecordsPercent", title: "Affected Records %", formatter: percentFormatter, 
				align: "right", sortable: true, width: 100},
			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, 
				align: "right", sortable: true, width: 130},
			$this.createGridItemEditColumn(),
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