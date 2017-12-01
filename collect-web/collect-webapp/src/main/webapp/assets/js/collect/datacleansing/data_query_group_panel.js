Collect.DataCleansing.DataQueryGroupPanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Data Query Group", 
			Collect.DataQueryGroupDialogController,
			collect.dataQueryGroupService, 
			Collect.DataCleansing.DATA_QUERY_GROUP_DELETED]);
};

Collect.DataCleansing.DataQueryGroupPanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataQueryGroupPanelController.prototype.getDataGridOptions = function() {
	var $this = this;
	
	function onExpandRow(index, group, $rowEl) {
		var table = $rowEl.find("table");
		table.bootstrapTable({
			width: 600,
			columns: [
				{field: "typeCode", title: "Type", width: 100},
				{field: "errorSeverity", title: "Error Severity", width: 100},
				{field: "title", title: "Title", width: 400}
		    ],
		    data: group.queries
		});
	};
	
	function detailFormatter(index, chain) {
		var html = 
        	'<fieldset style="margin-left: 60px !important;" ' +
				'class="compact">' +
	        	'<legend>Queries</legend>' +
	        	'<table></table>' +
           '</fieldset>';
        return html;
	}
	
	return {
	    url: "api/datacleansing/dataquerygroups",
	    detailView: true,
	    detailFormatter: detailFormatter,
	    onExpandRow: onExpandRow,
	    columns: [
			{field: "title", title: "Title", width: 800, sortable: true},
//			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
//			{field: "modifiedDate", title: "Modified Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			$this.createGridItemEditColumn(),
			$this.createGridItemDeleteColumn()
		]
	};
};
