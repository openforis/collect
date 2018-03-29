Collect.JobService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "api/job/";
};

Collect.JobService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.JobService.prototype.getApplicationJob = function(onSuccess, onError) {
	this.send("application-job.json", null, "GET", onSuccess, onError);
};

Collect.JobService.prototype.getSurveyJob = function(surveyId, onSuccess, onError) {
	this.send("survey-job.json", {surveyId: surveyId}, "GET", onSuccess, onError);
};
