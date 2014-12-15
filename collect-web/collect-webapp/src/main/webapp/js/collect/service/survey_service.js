Collect.SurveyService = function() {
	this.contextPath = "/collect/surveys/";
};

Collect.SurveyService.prototype.loadSummaries = function(callback) {
	var $this = this;
	$.ajax({
		url: $this.contextPath + "summaries.json",
		dataType:"json"
	}).done(function(summaries){
		callback(summaries);
	}).error( function() {
		collect.error.apply(this, arguments);
	});
};