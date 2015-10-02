Collect.DataCleansing.DataErrorTypePanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Data Error Type", 
			Collect.DataErrorTypeDialogController,
			collect.dataErrorTypeService, 
			Collect.DataCleansing.DATA_ERROR_TYPE_DELETED]);
};

Collect.DataCleansing.DataErrorTypePanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataErrorTypePanelController.prototype.getDataGridOptions = function() {
	var $this = this;
	return {
	    url: "datacleansing/dataerrortypes/list.json",
	    height: 400,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "code", title: "Code", width: "200px", sortable: true},
			{field: "label", title: "Label", width: "400px", sortable: true},
			{field: "description", title: "Description", width: "400px", sortable: true},
			$this.createGridItemDeleteColumn()
		]
	};
};
