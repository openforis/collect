Collect.DataErrorQueryDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "/collect/datacleansing/data_error_query_dialog.html";
	this.surveySummaries = null;
};

Collect.DataErrorQueryDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorQueryDialogController.prototype.loadInstanceVariables = function(callback) {
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, arguments);
};

Collect.DataErrorQueryDialogController.prototype.initFormElements = function() {
	var $this = this;
};

Collect.DataErrorQueryDialogController.prototype.applyHandler = function() {
	var $this = this;
	if ($this.validateForm()) {
		
	}
};

Collect.DataErrorQueryDialogController.prototype.cancelHandler = function() {
	this.close();
};

Collect.DataErrorQueryDialogController.prototype.validateForm = function() {
	var $this = this;
};
