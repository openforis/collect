Collect.GeoDataService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "";
};

Collect.GeoDataService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.GeoDataService.prototype.loadCoordinateValues = function(surveyId, coordinateAttributeId, srsId, 
		recordOffset, maxNumberOfRecords, onSuccess, onError) {
	var data = {
		coordinateAttributeId: coordinateAttributeId,
		srsId: srsId,
		recordOffset: recordOffset, 
		maxNumberOfRecords: maxNumberOfRecords
	};
	this.send("survey/" + surveyId + "/data/coordinatevalues.json", data, "GET", onSuccess, onError);
};

Collect.GeoDataService.prototype.loadGeometryValues = function(surveyId, attributeId, srsId,
		recordOffset, maxNumberOfRecords, onSuccess, onError) {
	var data = {
		attributeId: attributeId,
		srsId: srsId,
		recordOffset: recordOffset, 
		maxNumberOfRecords: maxNumberOfRecords
	};
	this.send("survey/" + surveyId + "/data/geometries.json", data, "GET", onSuccess, onError);
};

Collect.GeoDataService.prototype.loadSamplingPointCoordinates = function(surveyId, recordOffset, maxNumberOfRecords, onSuccess, onError) {
	var data = {
		recordOffset: recordOffset, 
		maxNumberOfRecords: maxNumberOfRecords
	};
	this.send("survey/" + surveyId + "/sampling-point-data.json", data, "GET", onSuccess, onError);
};