Collect.DataCleansingStepService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "api/datacleansing/datacleansingsteps";
};

Collect.DataCleansingStepService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataCleansingStepService.prototype.run = function(cleansingStepId, recordStep, onSuccess, onError) {
	var data = {cleansingStepId: cleansingStepId, recordStep: recordStep};
	this.send("run.json", data, "POST", onSuccess, onError);
};