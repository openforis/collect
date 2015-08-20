Collect.DataCleansing.DataCleansingChainPanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Cleansing Chain", 
			Collect.DataCleansingChainDialogController,
			collect.dataCleansingChainService, 
			Collect.DataCleansing.DATA_CLEANSING_CHAIN_DELETED]);
};

Collect.DataCleansing.DataCleansingChainPanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataCleansingChainPanelController.prototype.initDataGrid = function() {
	var $this = this;
	var gridId = 'datacleansingchaingrid';
	var gridContainer = $("#" + gridId);
	gridContainer.bootstrapTable({
	    url: "datacleansing/datacleansingchains/list.json",
	    cache: false,
	    clickToSelect: true,
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
	});
	$this.dataGrid = gridContainer.data('bootstrap.table');
	
	function onExpandRow(index, chain, $rowEl) {
		var table = $rowEl.find("table");
		table.bootstrapTable({
			width: 600,
			columns: [
				{field: "id", title: "Id", visible: false},
				{field: "title", title: "Title", width: 400},
				{field: "queryTitle", title: "Query Title", width: 200}
		    ],
		    data: chain.steps
		});
	};
	
	function detailFormatter(index, chain) {
		var html = 
        	'<fieldset style="margin-left: 60px !important;" ' +
				'class="compact">' +
	        	'<legend>Steps</legend>' +
	        	'<table></table>' +
           '</fieldset>';
        return html;
	}
};
