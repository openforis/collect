Collect.GeoDataService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "geo/data/";
};

Collect.GeoDataService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.GeoDataService.prototype.loadCoordinateValues = function(surveyName, stepNum, coordinateAttributeId, recordOffset, maxNumberOfRecords, onSuccess, onError) {
	var data = {
		recordOffset: recordOffset, 
		maxNumberOfRecords: maxNumberOfRecords
	};
	this.send(surveyName + "/" + stepNum + "/" + coordinateAttributeId + "/coordinatevalues.json", data, "GET", onSuccess, onError);
};

Collect.GeoDataService.prototype.loadSamplingPointCoordinates = function(surveyName, recordOffset, maxNumberOfRecords, onSuccess, onError) {
	var data = {
		recordOffset: recordOffset, 
		maxNumberOfRecords: maxNumberOfRecords
	};
	this.send(surveyName + "/samplingpointcoordinates.json", data, "GET", onSuccess, onError);
};
