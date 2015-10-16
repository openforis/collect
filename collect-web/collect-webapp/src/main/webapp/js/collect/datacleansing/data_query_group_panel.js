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
				{field: "id", title: "Id", visible: false},
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
	    url: "datacleansing/dataquerygroups/list.json",
	    detailView: true,
	    detailFormatter: detailFormatter,
	    height: 400,
	    onExpandRow: onExpandRow,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title", width: 800, sortable: true},
//			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
//			{field: "modifiedDate", title: "Modified Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			$this.createGridItemDeleteColumn()
		]
	};
};
