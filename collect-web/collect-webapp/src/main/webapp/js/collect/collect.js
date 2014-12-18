Collect = function() {
	this.activeSurvey = null;
};

Collect.prototype.init = function() {
	this.activeSurvey = null;
	this.sessionService = new Collect.SessionService();
	this.surveyService = new Collect.SurveyService();
	this.dataErrorQueryService = new Collect.DataErrorQueryService();
	this.dataErrorTypeService = new Collect.DataErrorTypeService();
	
	//survey select dialog
	var surveySelectDialogController = new Collect.SurveySelectDialogController();
	surveySelectDialogController.open();
	
	this.initDataErrorTypePanel();
	this.initDataErrorQueryPanel();
	
	this.initGlobalEventHandlers();
};

Collect.prototype.initGlobalEventHandlers = function() {
	var $this = this;
	EventBus.addEventListener(Collect.DataErrorTypeDialogController.DATA_ERROR_TYPE_SAVED, function() {
		$this.dataErrorTypeDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataErrorTypeDialogController.DATA_ERROR_TYPE_DELETED, function() {
		$this.dataErrorTypeDataGrid.refresh();
	});
	
	EventBus.addEventListener(Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_SAVED, function() {
		$this.dataErrorQueryDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_DELETED, function() {
		$this.dataErrorQueryDataGrid.refresh();
	});
};

Collect.prototype.initDataErrorTypePanel = function() {
	$('#newDataErrorTypeBtn').click($.proxy(function() {
		var dialogController = new Collect.DataErrorTypeDialogController();
		dialogController.open();
	}, this));
	
	$('#editDataErrorTypeBtn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		var dialogController = new Collect.DataErrorTypeDialogController();
		dialogController.open(selectedItem);
	}, this));
	
	$('#deleteDataErrorTypeBtn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		OF.UI.confirm("Do you want to delete this Data Error Type?", function() {
			collect.dataErrorTypeService.remove(selectedItem.id, function() {
				EventBus.dispatch(Collect.DataErrorTypeDialogController.DATA_ERROR_TYPE_DELETED, $this);
			});
		});
	}, this));
	
	function getSelectedItem() {
		var $this = this;
		var selections = $this.dataErrorTypeDataGrid.getSelections();
		return selections.length == 0 ? null : selections[0];
	}
};

Collect.prototype.initDataErrorQueryPanel = function() {
	$('#new-data-error-query-btn').click($.proxy(function() {
		var dialogController = new Collect.DataErrorQueryDialogController();
		dialogController.open();
	}, this));
	
	$('#edit-data-error-query-btn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		var dialogController = new Collect.DataErrorQueryDialogController();
		dialogController.open(selectedItem);
	}, this));
	
	$('#delete-data-error-query-btn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		OF.UI.confirm("Do you want to delete this Data Error Type?", function() {
			collect.dataErrorQueryService.remove(selectedItem.id, function() {
				EventBus.dispatch(Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_DELETED, $this);
			});
		});
	}, this));
	
	function getSelectedItem() {
		var $this = this;
		var selections = $this.dataErrorQueryDataGrid.getSelections();
		return selections.length == 0 ? null : selections[0];
	}
};

Collect.prototype.initDataErrorTypeGrid = function() {
	var $this = this;
	$('#dataerrortypegrid').bootstrapTable({
	    url: "/collect/datacleansing/dataerrortypes/list.json",
	    cache: false,
	    clickToSelect: true,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "code", title: "Code"},
			{field: "label", title: "Label"},
			{field: "description", title: "Description"}
		]
	});
	$this.dataErrorTypeDataGrid = $('#dataerrortypegrid').data('bootstrap.table');
};

Collect.prototype.initDataErrorQueryGrid = function() {
	var $this = this;
	$('#dataerrorquerygrid').bootstrapTable({
	    url: "/collect/datacleansing/dataerrorqueries/list.json",
	    cache: false,
	    clickToSelect: true,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title"},
			{field: "description", title: "Description"}
		]
	});
	$this.dataErrorQueryDataGrid = $('#dataerrorquerygrid').data('bootstrap.table');
};

Collect.prototype.setActiveSurvey = function(surveySummary) {
	var $this = this;
	$this.surveyService.loadById(surveySummary.id, function(survey) {
		$this.activeSurvey = new Collect.Metamodel.Survey(survey);
		$this.initDataErrorTypeGrid();
		$this.initDataErrorQueryGrid();
	});
};

Collect.prototype.error = function(jqXHR, status, errorThrown) {
	alert(status);
};

$(function() {
	collect = new Collect();
	collect.init();
});