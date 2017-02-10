Collect.DataService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "";
};

Collect.DataService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataService.prototype.countRecords = function(surveyId, rootEntityDefinitionId, stepNum, onSuccess, onError) {
	var data = {
		rootEntityDefinitionId: rootEntityDefinitionId,
		step: stepNum
	}
	this.send("survey/" + surveyId + "/data/records/count.json", data, "GET", onSuccess, onError);
};
