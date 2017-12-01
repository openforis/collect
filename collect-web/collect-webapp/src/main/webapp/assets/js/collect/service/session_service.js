Collect.SessionService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "api/session/";
};

Collect.SessionService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.SessionService.prototype.setActiveSurvey = function(surveyId, onSuccess, onError) {
	this.send("survey", {surveyId: surveyId}, "POST", onSuccess, onError);
};

Collect.SessionService.prototype.getActiveSurvey = function(onSuccess, onError) {
	this.send("survey", null, "GET", onSuccess, onError);
};

Collect.SessionService.prototype.getLoggedUser = function(onSuccess, onError) {
	this.send("user", null, "GET", onSuccess, onError);
};
