Collect.DataErrorReportService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "datacleansing/dataerrorreports/";
};

Collect.DataErrorReportService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataErrorReportService.prototype.generateReport = function(queryGroupId, recordStep, onSuccess, onError) {
	this.send("generate.json", {queryGroupId: queryGroupId, recordStep: recordStep}, "POST", onSuccess, onError);
};

Collect.DataErrorReportService.prototype.exportToCSV = function(reportId) {
	window.open(this.contextPath + reportId + "/export.csv", "_blank");
};