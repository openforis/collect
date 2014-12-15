Collect.SelectSurveyDialog = function() {
	this.init();
};

Collect.SelectSurveyDialog.prototype.init = function() {
	Collect.SurveyService.loadSummaries(function() {
		alert("ok");
	});
};