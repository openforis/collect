Collect.DataCleansing.DataErrorQueryPanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Data Error Query", 
			Collect.DataErrorQueryDialogController,
			collect.dataErrorQueryService, 
			Collect.DataCleansing.DATA_ERROR_QUERY_DELETED]);
};

Collect.DataCleansing.DataErrorQueryPanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataErrorQueryPanelController.prototype.getDataGridOptions = function() {
	var $this = this;
	return {
	    url: "datacleansing/dataerrorqueries/list.json",
	    height: 400,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "typeCode", title: "Error Type", sortable: true, width: 50},
			{field: "severity", title: "Severity", sortable: true, width: 50},
			{field: "queryTitle", title: "Query Title", sortable: true, width: 400},
			{field: "queryDescription", title: "Query Description", sortable: false, width: 400},
			$this.createGridItemDeleteColumn()
		]
	};
};
