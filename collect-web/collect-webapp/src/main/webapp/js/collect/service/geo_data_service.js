Collect.GeoDataService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "geo/data/";
};

Collect.GeoDataService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.GeoDataService.prototype.loadCoordinateValues = function(surveyName, coordinateAttributeId, recordOffset, maxNumberOfRecords, onSuccess, onError) {
	var data = {
		recordOffset: recordOffset, 
		maxNumberOfRecords: maxNumberOfRecords
	};
	this.send(surveyName + "/" + coordinateAttributeId + "/coordinatevalues.json", data, "GET", onSuccess, onError);
};
