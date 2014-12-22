Collect.DataErrorReportService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "/collect/datacleansing/dataerrorreports/";
};

Collect.DataErrorReportService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataErrorReportService.prototype.generateReport = function(queryId, recordStep, onSuccess, onError) {
	this.send("generate.json", {queryId: queryId, recordStep: recordStep}, "POST", onSuccess, onError);
};
