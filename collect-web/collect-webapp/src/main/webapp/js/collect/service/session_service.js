Collect.SessionService = function() {
	Collect.AbstractService.apply(this, arguments);
};

Collect.SessionService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.SessionService.prototype.setActiveSurvey = function(surveyId, onSuccess, onError) {
	this.send("/session/survey.json", {surveyId: surveyId}, "POST", onSuccess, onError);
}

Collect.SessionService.prototype.getActiveSurvey = function(onSuccess, onError) {
	this.send("/session/survey.json", null, "GET", onSuccess, onError);
}
