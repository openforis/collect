Collect.DataCleansing.DataCleansingChainPanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Cleansing Chain", 
			Collect.DataCleansingChainDialogController,
			collect.dataCleansingChainService, 
			Collect.DataCleansing.DATA_CLEANSING_CHAIN_DELETED]);
};

Collect.DataCleansing.DataCleansingChainPanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataCleansingChainPanelController.prototype.getDataGridOptions = function() {
	var $this = this;
	
	function onExpandRow(index, chain, $rowEl) {
		var stepsTableEl = $rowEl.find("table.steps");
		stepsTableEl.bootstrapTable({
			width: 600,
			columns: [
				{field: "id", title: "Id", visible: false},
				{field: "title", title: "Title", width: 400},
				{field: "queryTitle", title: "Query Title", width: 200}
		    ],
		    data: chain.steps
		});
		
		$this.itemService.loadReports(chain.id, function(reports) {
			var reportsTableEl = $rowEl.find("table.reports");
			reportsTableEl.bootstrapTable({
				width: 600,
				columns: [
					{field: "id", title: "Id", visible: false},
					{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, align: "right", sortable: true, width: 130},
					{field: "datasetSize", title: "Dataset Size", align: "right", sortable: true, width: 100, align: "right"},
					{field: "lastRecordModifiedDate", title: "Last Record Modified", 
						formatter: OF.Dates.formatToPrettyDateTime, align: "right", sortable: true, width: 130},
					{field: "cleansedRecords", title: "Cleansed Records", width: 100, align: "right"},
					{field: "cleansedNodes", title: "Cleansed Nodes", width: 100, align: "right"}
			    ],
			    data: reports
			});
		});
	};
	
	function detailFormatter(index, chain) {
		var html = 
			'<fieldset style="margin-left: 60px !important;" ' +
				'class="compact">' +
	        	'<legend>Cleansing Steps</legend>' +
	        	'<table class="steps"></table>' +
        	'</fieldset>' +
       		'<fieldset style="margin-left: 60px !important;" ' +
				'class="compact">' +
				'<legend>Reports</legend>' +
				'<table class="reports"></table>' +
			'</fieldset>';
        return html;
	}
	
	return {
	    url: "datacleansing/datacleansingchains/list.json",
	    detailView: true,
	    detailFormatter: detailFormatter,
	    height: 400,
	    onExpandRow: onExpandRow,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title", width: 800, sortable: true},
			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			{field: "modifiedDate", title: "Modified Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			$this.createGridItemDeleteColumn()
		]
	};
};
