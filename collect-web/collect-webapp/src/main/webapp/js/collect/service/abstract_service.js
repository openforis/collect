Collect.AbstractService = function() {
	this.contextPath = "/collect/";
};

Collect.AbstractService.prototype.loadAll = function(onSuccess, onError) {
	this.send("list.json", null, onSuccess, onError);
};

Collect.AbstractService.prototype.loadById = function(id, onSuccess, onError) {
	this.send(id + ".json", null, onSuccess, onError);
};

Collect.AbstractService.prototype.save = function(data, onSuccess, onError) {
	this.send("save.json", data, onSuccess, onError);
};

Collect.AbstractService.prototype.send = function(url, data, onSuccess, onError) {
	var $this = this;
	$.ajax({
		url: $this.contextPath + url,
		cache: false,
		dataType:"json",
		data: data
	}).done(function(response) {
		onSuccess(response);
	}).error(function() {
		collect.error.apply(this, arguments);
		if (onError) {
			onError();
		}
	});
}
