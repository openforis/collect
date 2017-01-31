Collect.GeoDataService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "";
};

Collect.GeoDataService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.GeoDataService.prototype.loadCoordinateValues = function(surveyId, stepNum, coordinateAttributeId, recordOffset, maxNumberOfRecords, onSuccess, onError) {
	var data = {
		recordOffset: recordOffset, 
		maxNumberOfRecords: maxNumberOfRecords
	};
	this.send("survey/" + surveyId + "/schema/" + coordinateAttributeId + "/data/" + stepNum + "/geo/coordinatevalues.json", data, "GET", onSuccess, onError);
};

Collect.GeoDataService.prototype.loadSamplingPointCoordinates = function(surveyId, recordOffset, maxNumberOfRecords, onSuccess, onError) {
	var data = {
		recordOffset: recordOffset, 
		maxNumberOfRecords: maxNumberOfRecords
	};
	this.send("survey/" + surveyId + "/sampling-point-data", data, "GET", onSuccess, onError);
};
