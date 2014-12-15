Collect.SurveyService = function() {
	this.contextPath = "/surveys/";
};

Collect.SurveyService.prototype.loadSummaries = function(callback) {
	$.ajax({
		url: contextPath + "summaries.json",
		dataType:"json"
	}).done(function(response){
		callback(response);
	}).error( function() {
		Collect.error.apply( this , arguments );
	});
};