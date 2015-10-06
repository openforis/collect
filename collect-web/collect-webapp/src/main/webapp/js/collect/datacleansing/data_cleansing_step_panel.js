Collect.DataCleansing.DataCleansingStepPanelController = function($panel) {
	Collect.AbstractItemPanel.apply(this, [$panel, "Cleansing Step", 
			Collect.DataCleansingStepDialogController,
			collect.dataCleansingStepService, 
			Collect.DataCleansing.DATA_CLEANSING_STEP_DELETED]);
};

Collect.DataCleansing.DataCleansingStepPanelController.prototype = Object.create(Collect.AbstractItemPanel.prototype);

Collect.DataCleansing.DataCleansingStepPanelController.prototype.getDataGridOptions = function() {
	var $this = this;
	return {
	    url: "datacleansing/datacleansingsteps/list.json",
	    height: 400,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title", width: 400, sortable: true},
			{field: "queryTitle", title: "Query Title", width: 400, sortable: true},
			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			{field: "modifiedDate", title: "Modified Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			$this.createGridItemDeleteColumn()
		]
	};
};
