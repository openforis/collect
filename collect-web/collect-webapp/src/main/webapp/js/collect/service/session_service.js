Collect.SessionService = function() {
	this.contextPath = "/collect/";
};

Collect.SessionService.prototype.setActiveSurvey = function(surveyId, onSuccess, onError) {
	var $this = this;
	$.ajax({
		url: $this.contextPath + "setActiveSurvey.json",
		cache: false,
		dataType:"json",
		data: {surveyId: surveyId}
	}).done(function(response) {
		onSuccess();
	}).error(function() {
		collect.error.apply(this, arguments);
		if (onError) {
			onError();
		}
	});
}
