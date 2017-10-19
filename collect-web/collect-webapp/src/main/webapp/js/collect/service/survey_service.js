Collect.SurveyService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "api/survey";
};

Collect.SurveyService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.SurveyService.prototype.loadSummaries = function(onSuccess, onError, userId, excludeTemporary) {
	this.send("", {userId: userId, includeTemporary: ! excludeTemporary}, "GET",  onSuccess, onError);
};

Collect.SurveyService.prototype.loadFullPublishedSurveys = function(onSuccess, onError, userId) {
	this.send("", {userId: userId, includeTemporary: false, full: true}, "GET",  onSuccess, onError);
};