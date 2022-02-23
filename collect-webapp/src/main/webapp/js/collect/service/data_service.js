Collect.DataService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "api/";
};

Collect.DataService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataService.prototype.countRecords = function(surveyId, rootEntityDefinitionId, onSuccess, onError) {
	var data = {
		rootEntityDefinitionId: rootEntityDefinitionId,
	}
	this.send("survey/" + surveyId + "/data/records/count.json", data, "GET", onSuccess, onError);
};
