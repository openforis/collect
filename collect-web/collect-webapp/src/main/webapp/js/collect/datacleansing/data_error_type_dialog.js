Collect.DataErrorTypeDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_error_type_dialog.html";
	this.itemEditService = collect.dataErrorTypeService;
};

Collect.DataErrorTypeDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorTypeDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansing.DATA_ERROR_TYPE_SAVED, this);
};
