Collect = function() {
	this.activeSurvey = null;
}

Collect.prototype.init = function() {
	this.activeSurvey = null;
	this.sessionService = new Collect.SessionService();
	this.surveyService = new Collect.SurveyService();
	
	var selectSurveyDialogController = new Collect.SelectSurveyDialogController();
	selectSurveyDialogController.open();
};

Collect.prototype.setActiveSurvey = function(survey) {
	collect.activeSurvey = survey;
};

Collect.prototype.error = function(jqXHR, status, errorThrown) {
	alert(status);
};

$(function() {
	collect = new Collect();
	collect.init();
});