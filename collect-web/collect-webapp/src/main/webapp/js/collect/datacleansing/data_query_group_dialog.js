Collect.DataQueryGroupDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_query_group_dialog.html";
	this.itemEditService = collect.dataQueryGroupService;
	this.queryDataGrid = null;
	this.querySelectPicker = null;
};

Collect.DataQueryGroupDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataQueryGroupDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansing.DATA_QUERY_GROUP_SAVED, this);
};

Collect.DataQueryGroupDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, [function() {
		//load data queries
		collect.dataQueryService.loadAll(function(queries) {
			$this.allQueries = queries;
			callback();
		});
	}]);
};

Collect.DataQueryGroupDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		var $this = this;
		$this.allQueries.sort(function(a, b) {
			return a.prettyFormatTitle.localeCompare(b.prettyFormatTitle);
		});
		var select = $this.content.find('select[name="query"]');
		OF.UI.Forms.populateSelect(select, $this.allQueries, "id", "prettyFormatTitle");
		select.doubleList({
			title: "Data Queries",
			json: false
		});
		$this.queriesDualListBox = select.data("doubleList");
		
		callback();
	});
};

Collect.DataQueryGroupDialogController.prototype.extractFormObject = function() {
	var formItem = Collect.AbstractItemEditDialogController.prototype.extractFormObject.apply(this);
	formItem.queryIds = this.queriesDualListBox.getSelectedValues();
	return formItem;
};

Collect.DataQueryGroupDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.queriesDualListBox.selectItems($this.item.queries);
		callback();
	});
};

Collect.DataQueryGroupDialogController.prototype.validateForm = function(callback) {
	var selectedQueryIds = this.queriesDualListBox.getSelectedValues();
	if (selectedQueryIds.length == 0) {
		OF.Alerts.showWarning("Please add at least one query");
		return false;
	}
	return true;
};
