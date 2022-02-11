Collect.DataQueryTypeService = function() {
	Collect.AbstractService.apply(this, arguments);
	this.contextPath = "api/datacleansing/dataquerytypes";
};

Collect.DataQueryTypeService.prototype = Object.create(Collect.AbstractService.prototype);
