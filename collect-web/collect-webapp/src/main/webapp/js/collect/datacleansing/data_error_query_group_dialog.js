Collect.DataErrorQueryGroupDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_error_query_group_dialog.html";
	this.itemEditService = collect.dataErrorQueryGroupService;
	this.queryDataGrid = null;
	this.querySelectPicker = null;
	this.queries = [];
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
			if ($this.item == null || $this.item.queries == null || $this.item.queries.length == 0) {
				$this.availableNewQueries = queries;
			} else {
				$this.availableNewQueries = new Array();
				for (var idx = 0; idx < queries.length; idx++) {
					var query = queries[idx];
					var itemQuery = OF.Arrays.findItem($this.item.queries, "id", query.id);
					if (itemQuery == null) {
						$this.availableNewQueries.push(query);
					}
				}
			}
			callback();
		});
	}]);
};

Collect.DataErrorQueryGroupDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		$this.initNewQuerySelectPicker();
		
		var getSelectedQueryToAdd = function() {
			var selectedQueryId = $this.addQuerySelectPicker.val();
			if (selectedQueryId == null || selectedQueryId == '') {
				return null;
			}
			var selectedQuery = OF.Arrays.findItem($this.availableNewQueries, "id", selectedQueryId);
			return selectedQuery;
		};
		
		$this.content.find(".add-query-btn").click($.proxy(function() {
			var selectedQuery = getSelectedQueryToAdd();
			if (selectedQuery == null) {
				return;
			}
			$this.queries.push(selectedQuery);
			
			OF.Arrays.removeItem($this.availableNewQueries, selectedQuery);
			
			$this.refreshQueryDataGrid();
			
			$this.initNewQuerySelectPicker();
		}, $this));
		
		$this.content.find(".remove-query-btn").click($.proxy(function() {
			var selectedQuery = $this.getSelectedQuery();
			if (selectedQuery == null) {
				return;
			}
			$this.deleteQuery(selectedQuery);
		}, $this));
		
		var moveSelectedQuery = function(up) {
			var $this = this;
			var selectedQuery = $this.getSelectedQuery();
			if (selectedQuery == null) {
				return;
			}
			var queryIndex = $this.queries.indexOf(selectedQuery);
			if (up && queryIndex == 0 || ! up && queryIndex == $this.queries.length) {
				return;
			}
			var toIndex = queryIndex + (up ? -1 : 1);
			OF.Arrays.shiftItem($this.queries, selectedQuery, toIndex);
			
			$this.refreshQueryDataGrid();
		};
		
		$this.content.find(".move-query-up-btn").click($.proxy(function() {
			$.proxy(moveSelectedQuery, $this)(true);
		}, $this));
		
		$this.content.find(".move-query-down-btn").click($.proxy(function() {
			$.proxy(moveSelectedQuery, $this)(false);
		}, $this));
		
		$this.initQueryDataGrid();
		
		$this.onQuerySelectionChange();
		
		callback();
	});
};

Collect.DataErrorQueryGroupDialogController.prototype.extractFormObject = function() {
	var formItem = Collect.AbstractItemEditDialogController.prototype.extractFormObject.apply(this);
	formItem.queryIds = new Array();
	var queries = this.queries;
	for (var idx = 0; idx < queries.length; idx++) {
		var query = queries[idx];
		formItem.queryIds.push(query.id);
	}
	return formItem;
};

Collect.DataErrorQueryGroupDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.queries = OF.Arrays.clone($this.item.queries);
		$this.refreshQueryDataGrid();
		callback();
	});
};

Collect.DataErrorQueryGroupDialogController.prototype.validateForm = function(callback) {
	if (this.queries.length == 0) {
		OF.Alerts.showWarning("Please add at least one query");
		return false;
	}
	return true;
};

Collect.DataErrorQueryGroupDialogController.prototype.initQueryDataGrid = function() {
	var $this = this;
	var gridContainer = $this.content.find(".query-grid");
	
	gridContainer.bootstrapTable({
	    clickToSelect: true,
	    reorderableRows: true,
	    height: 150,
	    width: 800,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title", width: 400},
			{field: "queryTitle", title: "Query Title", width: 200},
			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100},
			{field: "modifiedDate", title: "Modified Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100},
			Collect.Grids.createDeleteColumn($this.deleteQuery, $this)
		]
	});
	$this.queryDataGrid = gridContainer.data('bootstrap.table');
	gridContainer.on("check.bs.table", function() {
		$this.onQuerySelectionChange();
	});
	$this.onQuerySelectionChange();
};

Collect.DataErrorQueryGroupDialogController.prototype.initNewQuerySelectPicker = function() {
	var $this = this;
	$this.availableNewQueries.sort(function(a, b) {
		return a.prettyFormatTitle.localeCompare(b.prettyFormatTitle);
	});
	var select = $this.content.find('select[name="errorQuery"]');
	OF.UI.Forms.populateSelect(select, $this.availableNewQueries, "id", "prettyFormatTitle", true);
	select.selectpicker();
	$this.addQuerySelectPicker = select.data().selectpicker;
	$this.addQuerySelectPicker.refresh();
};

Collect.DataErrorQueryGroupDialogController.prototype.deleteQuery = function(query) {
	var $this = this;
	OF.Alerts.confirm("Remove the cleansing query from this chain?", function() {
		OF.Arrays.removeItem($this.queries, query);
		$this.refreshQueryDataGrid();
		
		$this.availableNewQueries.push(query);
		$this.initNewQuerySelectPicker();
	});
};

Collect.DataErrorQueryGroupDialogController.prototype.onQuerySelectionChange = function() {
	var $this = this;
	var selectedQuery = $this.getSelectedQuery();
	var querySelected = selectedQuery != null;
	$this.content.find(".query-selected-enabled").prop("disabled", ! querySelected);
};

Collect.DataErrorQueryGroupDialogController.prototype.getSelectedQuery = function () {
	var $this = this;
	var selections = $this.queryDataGrid.getSelections();
	return selections.length == 0 ? null : selections[0];
}

Collect.DataErrorQueryGroupDialogController.prototype.refreshQueryDataGrid = function() {
	var $this = this;
	var data = $this.queries ? $this.queries : null;
	$this.queryDataGrid.load(data);
	$this.onQuerySelectionChange();
};
