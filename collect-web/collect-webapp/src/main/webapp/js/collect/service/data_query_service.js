Collect.DataQueryService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "datacleansing/dataquery/";
};

Collect.DataQueryService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataQueryService.prototype.start = function(query, onSuccess, onError) {
	this.send("start.json", query, "POST", onSuccess, onError);
};