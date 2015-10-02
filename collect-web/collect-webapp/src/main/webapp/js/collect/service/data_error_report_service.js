Collect.DataErrorReportService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "datacleansing/dataerrorreports/";
};

Collect.DataErrorReportService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataErrorReportService.prototype.generateReport = function(queryGroupId, recordStep, onSuccess, onError) {
	this.send("generate.json", {queryGroupId: queryGroupId, recordStep: recordStep}, "POST", onSuccess, onError);
};

Collect.DataErrorReportService.prototype.startExportToCSV = function(reportId, onSuccess, onError) {
	this.send(reportId + "/start-export.json", null, "POST", onSuccess, onError);
};

Collect.DataErrorReportService.prototype.startExportToCSVForCollectEarth = function(reportId, onSuccess, onError) {
	this.send(reportId + "/start-export-for-collect-earth.json", null, "POST", onSuccess, onError);
};

Collect.DataErrorReportService.prototype.downloadGeneratedExport = function(reportId) {
	window.open(this.contextPath + reportId + "/report.csv", "_blank");
};