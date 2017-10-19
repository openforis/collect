Collect.DataCleansingChainService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "api/datacleansing/datacleansingchains";
};

Collect.DataCleansingChainService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataCleansingChainService.prototype.run = function(chainId, recordStep, onSuccess, onError) {
	var data = {recordStep: recordStep};
	this.send("/" + chainId + "/run.json", data, "POST", onSuccess, onError);
};

Collect.DataCleansingChainService.prototype.loadReports = function(chainId, onSuccess, onError) {
	this.send("/" + chainId + "/reports.json", null, "GET", onSuccess, onError);
};
