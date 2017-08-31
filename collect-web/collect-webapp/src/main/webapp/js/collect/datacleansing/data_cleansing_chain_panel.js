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
		OF.i18n.initializeAll($rowEl);
		
		var stepsTableEl = $rowEl.find("table.steps");
		stepsTableEl.bootstrapTable({
			width: 500,
			height: 200,
			columns: [
				{field: "title", title: OF.i18n.prop("collect.global.title"), width: 300},
				{field: "queryTitle", title: OF.i18n.prop("collect.data_cleansing.data_cleansing_step.query_title"), width: 200}
		    ],
		    data: chain.steps
		});
		
		$this.itemService.loadReports(chain.id, function(reports) {
			var reportsTableEl = $rowEl.find("table.reports");
			reportsTableEl.bootstrapTable({
				height: 120,
				width: 480,
				columns: [
					{field: "creationDate", title: OF.i18n.prop("collect.global.creation_date"), 
						formatter: OF.Dates.formatToPrettyDateTime, align: "right", sortable: true, width: 130},
					{field: "datasetSize", title: OF.i18n.prop("collect.data_cleansing.data_cleansing_chain.report.dataset_size"), 
							align: "right", sortable: true, width: 100, align: "right"},
					{field: "lastRecordModifiedDate", title: OF.i18n.prop("collect.data_cleansing.data_cleansing_chain.report.last_record_modified_date"),
						formatter: OF.Dates.formatToPrettyDateTime, align: "right", sortable: true, width: 80},
					{field: "cleansedRecords", title: OF.i18n.prop("collect.data_cleansing.data_cleansing_chain.report.cleansed_records"), 
							width: 70, align: "right"},
					{field: "cleansedNodes", title: OF.i18n.prop("collect.data_cleansing.data_cleansing_chain.report.cleansed_values"), 
								width: 70, align: "right"}
			    ],
			    data: reports
			});
		});
	};
	
	function detailFormatter(index, chain) {
		var html = 
			'<fieldset style="margin-left: 60px !important; width: 950px;" ' +
				'class="compact">' +
	        	'<legend data-i18n="collect.data_cleansing.data_cleansing_chain.cleansing_steps"></legend>' +
	        	'<table class="steps"></table>' +
        	'</fieldset>' +
       		'<fieldset style="margin-left: 60px !important; width: 950px" ' +
				'class="compact">' +
				'<legend data-i18n="collect.data_cleansing.data_cleansing_chain.reports"></legend>' +
				'<table class="reports"></table>' +
			'</fieldset>';
        return html;
	}
	
	return {
	    url: "api/datacleansing/datacleansingchains",
	    detailView: true,
	    detailFormatter: detailFormatter,
	    onExpandRow: onExpandRow,
	    columns: [
			{field: "title", title: OF.i18n.prop("collect.global.title"), width: 800, sortable: true},
			{field: "creationDate", title: OF.i18n.prop("collect.global.creation_date"), 
				formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			{field: "modifiedDate", title: OF.i18n.prop("collect.global.modified_date"), 
					formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			$this.createGridItemEditColumn(),
			$this.createGridItemDeleteColumn()
		]
	};
};
