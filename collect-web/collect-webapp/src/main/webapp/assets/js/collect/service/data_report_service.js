Collect.DataReportService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "api/datacleansing/datareports";
};

Collect.DataReportService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataReportService.prototype.generateReport = function(queryGroupId, recordStep, onSuccess, onError) {
	this.send("generate.json", {queryGroupId: queryGroupId, recordStep: recordStep}, "POST", onSuccess, onError);
};

Collect.DataReportService.prototype.startExportToCSV = function(reportId, onSuccess, onError) {
	this.send(reportId + "/start-export.json", null, "POST", onSuccess, onError);
};

Collect.DataReportService.prototype.startExportToCSVForCollectEarth = function(reportId, onSuccess, onError) {
	this.send(reportId + "/start-export-for-collect-earth.json", null, "POST", onSuccess, onError);
};

Collect.DataReportService.prototype.downloadGeneratedExport = function(reportId) {
	window.open(this.contextPath + reportId + "/report.csv", "_blank");
};