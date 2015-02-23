Collect.DataUpdateService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "datacleansing/dataupdate/";
};

Collect.DataUpdateService.prototype = Object.create(Collect.AbstractService.prototype);

Collect.DataUpdateService.prototype.start = function(formItem, onSuccess, onError) {
	this.send("start.json", formItem, "POST", onSuccess, onError);
};
