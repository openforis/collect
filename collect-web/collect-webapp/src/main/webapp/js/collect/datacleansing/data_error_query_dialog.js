Collect.DataErrorQueryDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_error_query_dialog.html";
	this.itemEditService = collect.dataErrorQueryService;
	this.errorTypes = null;
	this.errorTypeSelectPicker = null;
	this.queries = null;
	this.querySelectPicker = null;
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
			
			collect.dataQueryService.loadAll(function(queries) {
				$this.queries = queries;
				callback();
			});
		});
	}]);
};

Collect.DataErrorQueryDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		{//init error type select picker
			var select = $this.content.find('select[name="typeId"]');
			OF.UI.Forms.populateSelect(select, $this.errorTypes, "id", "code", true);
			select.selectpicker();
			$this.errorTypeSelectPicker = select.data().selectpicker;
		}
		{//init query select picker
			var select = $this.content.find('select[name="queryId"]');
			OF.UI.Forms.populateSelect(select, $this.queries, "id", "title", true);
			select.selectpicker();
			$this.querySelectPicker = select.data().selectpicker;
		}
		callback();
	});
};

Collect.DataErrorQueryDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.errorTypeSelectPicker.val($this.item.typeId);
		$this.querySelectPicker.val($this.item.queryId);
		callback();
	});
};

