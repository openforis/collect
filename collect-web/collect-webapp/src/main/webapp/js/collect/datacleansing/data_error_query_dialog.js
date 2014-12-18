Collect.DataErrorQueryDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "/collect/datacleansing/data_error_query_dialog.html";
	this.surveySummaries = null;
	this.itemEditService = collect.dataErrorQueryService;
};

Collect.DataErrorQueryDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_SAVED = "dataErrorQuerySaved";
Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_DELETED = "dataErrorQueryDeleted";

Collect.DataErrorQueryDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_SAVED, this);
};

Collect.DataErrorQueryDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, [function() {
		collect.dataErrorTypeService.loadAll(function(errorTypes) {
			$this.errorTypes = errorTypes;
			callback();
		});
	}]);
};

Collect.DataErrorQueryDialogController.prototype.initFormElements = function() {
	Collect.AbstractItemEditDialogController.prototype.initFormElements.apply(this, arguments);
	var $this = this;
	var select = $this.content.find('select[name="errorTypeId"]');
	OF.UI.Forms.populateSelect(select, $this.errorTypes, "id", "code", true);
	$this.errorTypeSelectPicker = select.selectpicker();
};

Collect.DataErrorQueryDialogController.prototype.extractJSONItem = function() {
	var item = Collect.AbstractItemEditDialogController.prototype.extractJSONItem.apply(this);
	item.typeId = this.errorTypeSelectPicker.val();
	return item;
};
