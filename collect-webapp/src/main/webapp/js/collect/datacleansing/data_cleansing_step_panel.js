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
	    url: "api/datacleansing/datacleansingsteps",
	    columns: [
			{field: "title", title: OF.i18n.prop("collect.global.title"), width: 400, sortable: true},
			{field: "queryTitle", title: OF.i18n.prop("collect.data_cleansing.data_cleansing_step.query_title"), 
				width: 400, sortable: true},
			{field: "creationDate", title: OF.i18n.prop("collect.global.creation_date"), 
				formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			{field: "modifiedDate", title: OF.i18n.prop("collect.global.modified_date"), 
					formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			$this.createGridItemEditColumn(),
			$this.createGridItemDeleteColumn()
		]
	};
};
