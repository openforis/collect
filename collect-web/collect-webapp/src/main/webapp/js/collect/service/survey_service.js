Collect.SurveyService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "surveys/";
};

Collect.SurveyService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.SurveyService.prototype.loadSummaries = function(onSuccess, onError) {
	this.send("summaries.json", null, "GET",  onSuccess, onError);
};