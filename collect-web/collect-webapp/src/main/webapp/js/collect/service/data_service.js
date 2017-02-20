Collect.DataService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "";
};

Collect.DataService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataService.prototype.countRecords = function(surveyId, rootEntityDefinitionId, onSuccess, onError) {
	var data = {
		rootEntityDefinitionId: rootEntityDefinitionId,
	}
	this.send("surveys/" + surveyId + "/records/count.json", data, "GET", onSuccess, onError);
};
