Collect.DataErrorTypeDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "/collect/datacleansing/data_error_type_dialog.html";
	this.surveySummaries = null;
	this.itemEditService = collect.dataErrorTypeService;
};

Collect.DataErrorTypeDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorTypeDialogController.DATA_ERROR_TYPE_SAVED = "dataErrorTypeSaved";

Collect.DataErrorTypeDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataErrorTypeDialogController.DATA_ERROR_TYPE_SAVED, this);
};

