Collect.AbstractService = function() {
	this.contextPath = "./";
};

Collect.AbstractService.prototype.loadAll = function(onSuccess, onError) {
	this.send("list.json", null, "GET", onSuccess, onError);
};

Collect.AbstractService.prototype.loadById = function(id, onSuccess, onError) {
	this.send(id + ".json", null, "GET", onSuccess, onError);
};

Collect.AbstractService.prototype.save = function(data, onSuccess, onError) {
	this.send("save.json", data, "POST", onSuccess, onError);
};

Collect.AbstractService.prototype.remove = function(id, onSuccess, onError) {
	this.send(id + ".json", null, "DELETE", onSuccess, onError);
};

Collect.AbstractService.prototype.send = function(url, data, method, onSuccess, onError) {
	var $this = this;
	$.ajax({
		url: $this.contextPath + url,
		cache: false,
		dataType:"json",
		method: method ? method: "GET",
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
