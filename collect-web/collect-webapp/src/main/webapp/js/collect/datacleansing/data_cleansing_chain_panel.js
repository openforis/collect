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
			width: 500,
			height: 200,
			columns: [
				{field: "title", title: "Title", width: 300},
				{field: "queryTitle", title: "Query Title", width: 200}
		    ],
		    data: chain.steps
		});
		
		$this.itemService.loadReports(chain.id, function(reports) {
			var reportsTableEl = $rowEl.find("table.reports");
			reportsTableEl.bootstrapTable({
				height: 120,
				width: 480,
				columns: [
					{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, align: "right", sortable: true, width: 130},
					{field: "datasetSize", title: "Dataset Size", align: "right", sortable: true, width: 100, align: "right"},
					{field: "lastRecordModifiedDate", title: "Last Record Modified", 
						formatter: OF.Dates.formatToPrettyDateTime, align: "right", sortable: true, width: 80},
					{field: "cleansedRecords", title: "Cleansed Records", width: 70, align: "right"},
					{field: "cleansedNodes", title: "Cleansed Nodes", width: 70, align: "right"}
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
	    onExpandRow: onExpandRow,
	    columns: [
			{field: "title", title: "Title", width: 800, sortable: true},
			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			{field: "modifiedDate", title: "Modified Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			$this.createGridItemEditColumn(),
			$this.createGridItemDeleteColumn()
		]
	};
};
