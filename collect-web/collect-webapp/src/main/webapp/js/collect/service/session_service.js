Collect.SessionService = function() {
	Collect.AbstractService.apply(this, arguments);
};

Collect.SessionService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.SessionService.prototype.setActiveSurvey = function(surveyId, onSuccess, onError) {
	this.send("setActiveSurvey.json", {surveyId: surveyId}, "GET", onSuccess, onError);
}
