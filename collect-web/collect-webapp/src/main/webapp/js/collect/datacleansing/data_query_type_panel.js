Collect.DataCleansing.DataQueryTypePanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Data Query Type", 
			Collect.DataQueryTypeDialogController,
			collect.dataQueryTypeService, 
			Collect.DataCleansing.DATA_QUERY_TYPE_DELETED]);
};

Collect.DataCleansing.DataQueryTypePanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataQueryTypePanelController.prototype.getDataGridOptions = function() {
	var $this = this;
	return {
	    url: "datacleansing/dataquerytypes/list.json",
	    columns: [
			{field: "code", title: "Code", width: "200px", sortable: true},
			{field: "label", title: "Label", width: "400px", sortable: true},
			{field: "description", title: "Description", width: "400px", sortable: true},
			$this.createGridItemEditColumn(),
			$this.createGridItemDeleteColumn()
		]
	};
};
