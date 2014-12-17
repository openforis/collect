Collect.AbstractItemEditDialogController = function() {
	this.contentUrl = "to be defined by subclass";
};

Collect.AbstractItemEditDialogController.prototype.initialize = function(callback) {
	var $this = this;
	$this.loadInstanceVariables(function() {
		$this.initializeContent(function() {
			callback();
		});
	});
};

Collect.AbstractItemEditDialogController.prototype.open = function() {
	var $this = this;
	$this.content.modal('show');
};

Collect.AbstractItemEditDialogController.prototype.close = function() {
	this.content.modal('hide');
	this.content.remove();
};

Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables = function(callback) {
	callback();
};

Collect.AbstractItemEditDialogController.prototype.initializeContent = function(callback) {
	var $this = this;
	$this.loadContent(function() {
		$this.initializeFormElements();
		$this.initializeEventListeners();
		callback();
	});
};

Collect.AbstractItemEditDialogController.prototype.loadContent = function(callback) {
	var $this = this;
	OpenForis.Async.loadHtml($this.contentUrl, function(content) {
			$this.content = content;
			callback();
		}, function() {
			collect.error.apply(this, arguments);
		}
	);
};

Collect.AbstractItemEditDialogController.prototype.initializeFormElements = function() {
};

Collect.AbstractItemEditDialogController.prototype.initializeEventListeners = function() {
	var $this = this;
	var applyBtn = $this.content.find(".apply-btn");
	applyBtn.click($.proxy(this.applyHandler, $this));
	var cancelBtn = $this.content.find(".cancel-btn");
	cancelBtn.click($.proxy(this.cancelHandler, $this));
};

Collect.AbstractItemEditDialogController.prototype.applyHandler = function() {
};

Collect.AbstractItemEditDialogController.prototype.cancelHandler = function() {
	this.close();
};

Collect.AbstractItemEditDialogController.prototype.validateForm = function() {
	return true;
};
