Collect.DataErrorQueryGroupDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_error_query_group_dialog.html";
	this.itemEditService = collect.dataErrorQueryGroupService;
	this.queryDataGrid = null;
	this.querySelectPicker = null;
};

Collect.DataErrorQueryGroupDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorQueryGroupDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansing.DATA_ERROR_QUERY_GROUP_SAVED, this);
};

Collect.DataErrorQueryGroupDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, [function() {
		//load data error queries
		collect.dataErrorQueryService.loadAll(function(queries) {
			$this.allQueries = queries;
			callback();
		});
	}]);
};

Collect.DataErrorQueryGroupDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		var $this = this;
		$this.allQueries.sort(function(a, b) {
			return a.prettyFormatTitle.localeCompare(b.prettyFormatTitle);
		});
		var select = $this.content.find('select[name="errorQuery"]');
		OF.UI.Forms.populateSelect(select, $this.allQueries, "id", "prettyFormatTitle");
		select.doubleList({
			title: "Data Error Queries",
			json: false
		});
		$this.queriesDualListBox = select.data("doubleList");
		
		callback();
	});
};

Collect.DataErrorQueryGroupDialogController.prototype.extractFormObject = function() {
	var formItem = Collect.AbstractItemEditDialogController.prototype.extractFormObject.apply(this);
	formItem.queryIds = this.queriesDualListBox.getSelectedValues();
	return formItem;
};

Collect.DataErrorQueryGroupDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.queriesDualListBox.selectItems($this.item.queries);
		callback();
	});
};

Collect.DataErrorQueryGroupDialogController.prototype.validateForm = function(callback) {
	var selectedQueryIds = this.queriesDualListBox.getSelectedValues();
	if (selectedQueryIds.length == 0) {
		OF.Alerts.showWarning("Please add at least one query");
		return false;
	}
	return true;
};
