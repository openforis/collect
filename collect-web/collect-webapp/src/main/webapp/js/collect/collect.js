Collect = function() {
	this.activeSurvey = null;
};

Collect.prototype.init = function() {
	this.activeSurvey = null;
	this.sessionService = new Collect.SessionService();
	this.surveyService = new Collect.SurveyService();
	this.dataErrorQueryService = new Collect.DataErrorQueryService();
	this.dataErrorTypeService = new Collect.DataErrorTypeService();
	
	var surveySelectDialogController = new Collect.SurveySelectDialogController();
	surveySelectDialogController.initialize(function() {
		surveySelectDialogController.open();
	});
	
	this.initDataErrorTypePanel();
};

Collect.prototype.initDataErrorTypePanel = function() {
	$('#newDataErrorTypeBtn').click($.proxy(function() {
		var dialogController = new Collect.DataErrorTypeDialogController();
		dialogController.initialize(function() {
			dialogController.open();
		});
	}, this));
	$('#editDataErrorTypeBtn').click($.proxy(function() {
		var $this = this;
		var selections = $this.dataErrorTypeDataGrid.getSelections();
		var dialogController = new Collect.DataErrorTypeDialogController();
		dialogController.initialize(function() {
			dialogController.open();
		});
	}, this));
};

Collect.prototype.initDataErrorTypeGrid = function() {
	var $this = this;
	$('#dataerrortypegrid').bootstrapTable({
	    url: "/collect/datacleansing/dataerrortypes/list.json",
	    cache: false,
	    columns: [
			{field: "id", title: "Id", visible: false},
			{field: "code", title: "Code"},
			{field: "title", title: "Title"}
		],
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