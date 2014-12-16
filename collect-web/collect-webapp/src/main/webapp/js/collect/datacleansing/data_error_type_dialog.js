Collect.DataErrorTypeDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "/collect/datacleansing/data_error_type_dialog.html";
	this.surveySummaries = null;
};

Collect.DataErrorTypeDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorTypeDialogController.prototype.loadInstanceVariables = function(callback) {
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, arguments);
};

Collect.DataErrorTypeDialogController.prototype.initFormElements = function() {
	var $this = this;
};

Collect.DataErrorTypeDialogController.prototype.applyHandler = function() {
	var $this = this;
	if ($this.validateForm()) {
		
	}
};

Collect.DataErrorTypeDialogController.prototype.cancelHandler = function() {
	this.close();
};

Collect.DataErrorTypeDialogController.prototype.validateForm = function() {
	var $this = this;
};
