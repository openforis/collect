Collect.DataCleansingStepDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_cleansing_step_dialog.html";
	this.itemEditService = collect.dataCleansingStepService;
	this.queries = null;
	this.querySelectPicker = null;
};

Collect.DataCleansingStepDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_SAVED = "dataCleansingStepSaved";
Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_DELETED = "dataCleansingStepDeleted";

Collect.DataCleansingStepDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_SAVED, this);
};

Collect.DataCleansingStepDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, [function() {
		collect.dataQueryService.loadAll(function(queries) {
			$this.queries = queries;
			callback();
		});
	}]);
};

Collect.DataCleansingStepDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		{//init query select picker
			var select = $this.content.find('select[name="queryId"]');
			OF.UI.Forms.populateSelect(select, $this.queries, "id", "title", true);
			select.selectpicker();
			$this.querySelectPicker = select.data().selectpicker;
		}
		callback();
	});
};

Collect.DataCleansingStepDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.querySelectPicker.val($this.item.queryId);
		callback();
	});
};

