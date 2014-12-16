Collect = function() {
	this.activeSurvey = null;
}

Collect.prototype.init = function() {
	this.activeSurvey = null;
	this.sessionService = new Collect.SessionService();
	this.surveyService = new Collect.SurveyService();
	this.dataErrorQueryService = new Collect.DataErrorQueryService();
	this.dataErrorTypeService = new Collect.DataErrorTypeService();
	
	var surveySelectDialogController = new Collect.SurveySelectDialogController();
	surveySelectDialogController.open();
};

Collect.prototype.initDataErrorTypeGrid = function() {
	$('#dataerrortypegrid').bootstrapTable({
	    url: "/collect/datacleansing/dataerrortypes/list.json",
	    cache: false,
	    columns: [
			{field: "id", title: "Id", visible: false},
			{field: "code", title: "Code"},
			{field: "title", title: "Title"}
		],
	});
};

Collect.prototype.setActiveSurvey = function(survey) {
	collect.activeSurvey = survey;
	
	this.initDataErrorTypeGrid();

	var dataErrorQueryDialogController = new Collect.DataErrorQueryDialogController();
	dataErrorQueryDialogController.open();
};

Collect.prototype.error = function(jqXHR, status, errorThrown) {
	alert(status);
};

$(function() {
	collect = new Collect();
	collect.init();
});