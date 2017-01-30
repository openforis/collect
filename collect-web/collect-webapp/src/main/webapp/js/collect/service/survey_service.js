Collect.SurveyService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "survey/";
};

Collect.SurveyService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.SurveyService.prototype.loadSummaries = function(onSuccess, onError, excludeTemporary) {
	this.send("summaries.json", {includeTemporary: ! excludeTemporary}, "GET",  onSuccess, onError);
};