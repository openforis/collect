Collect.DataQueryService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "api/datacleansing/dataqueries";
};

Collect.DataQueryService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataQueryService.prototype.startExport = function(query, onSuccess, onError) {
	this.send("/start-export.json", query, "POST", onSuccess, onError);
};

Collect.DataQueryService.prototype.startTest = function(query, onSuccess, onError) {
	this.send("/start-test.json", query, "POST", onSuccess, onError);
};

Collect.DataQueryService.prototype.loadTestResult = function(query, onSuccess, onError) {
	this.send("/test-result.json", query, "GET", onSuccess, onError);
};

Collect.DataQueryService.prototype.downloadResult = function() {
	window.open(this.contextPath + "result.csv", "_blank");
};