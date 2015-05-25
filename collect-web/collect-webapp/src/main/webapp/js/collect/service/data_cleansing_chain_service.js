Collect.DataCleansingChainService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "datacleansing/datacleansingchains/";
};

Collect.DataCleansingChainService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataCleansingChainService.prototype.run = function(chainId, recordStep, onSuccess, onError) {
	var data = {chainId: chainId, recordStep: recordStep};
	this.send("run.json", data, "POST", onSuccess, onError);
};