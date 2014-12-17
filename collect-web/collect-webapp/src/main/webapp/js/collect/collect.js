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
	
	//data error type panel
	this.initDataErrorTypePanel();
	
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

Collect.prototype.setActiveSurvey = function(survey) {
	collect.activeSurvey = survey;
	
	this.initDataErrorTypeGrid();
};

Collect.prototype.error = function(jqXHR, status, errorThrown) {
	alert(status);
};

$(function() {
	collect = new Collect();
	collect.init();
});