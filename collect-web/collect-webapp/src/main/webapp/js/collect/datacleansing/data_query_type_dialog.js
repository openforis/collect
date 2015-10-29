Collect.DataQueryTypeDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_query_type_dialog.html";
	this.itemEditService = collect.dataQueryTypeService;
};

Collect.DataQueryTypeDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataQueryTypeDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansing.DATA_QUERY_TYPE_SAVED, this);
};
