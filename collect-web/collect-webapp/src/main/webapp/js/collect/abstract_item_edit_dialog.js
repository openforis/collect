Collect.AbstractItemEditDialogController = function() {
	this.contentUrl = "to be defined by subclass";
	this.initialized = false;
	this.content = null;
	this.form = null;
	this.itemEditService = null;
	this.item = null;
	this.doNotAllowCancel = false;
};

Collect.AbstractItemEditDialogController.prototype.init = function(callback) {
	var $this = this;
	$this.loadInstanceVariables(function() {
		$this.initContent(function() {
			callback.call($this);
		});
	});
};

Collect.AbstractItemEditDialogController.prototype.checkViewState = function() {
};

Collect.AbstractItemEditDialogController.prototype.changeViewState = function(state) {
};

Collect.AbstractItemEditDialogController.prototype.open = function(item, doNotAllowCancel) {
	var $this = this;
	$this.item = item;
	$this.doNotAllowCancel = doNotAllowCancel;
	
	if ($this.initialized) {
		$this.doOpen();
	} else {
		$this.init(function() {
			$this.doOpen();
		});
	}
};

Collect.AbstractItemEditDialogController.prototype.beforeOpen = function(callback) {
	var $this = this;
	if ($this.item) {
		$this.fillForm(function() {
			$this.afterFillForm(function() {
				$this.updateViewState(function() {
					callback.call($this);
				});
			});
		});
	} else {
		$this.updateViewState(function() {
			callback.call($this);
		});
	}
};

/**
 * Called during the beforeOpen phase, after the filling of the form (if it occurs).
 * 
 * @param callback
 */
Collect.AbstractItemEditDialogController.prototype.afterFillForm = function(callback) {
	this.updateViewState(function() {
		callback();
	});
};

Collect.AbstractItemEditDialogController.prototype.updateViewState = function(callback) {
	var $this = this;
	callback.call($this);
};

Collect.AbstractItemEditDialogController.prototype.doOpen = function() {
	var $this = this;
	
	$this.content.on('hide.bs.modal', function (e) {
		 $this.content.remove();
	});
	
	$this.beforeOpen(function() {
		var options = {
			backdrop: "static"
		};
		if ($this.doNotAllowCancel) {
			options.keyboard = false;
		}
		$this.content.modal(options);
		
		$this.afterOpen();
	});
};

Collect.AbstractItemEditDialogController.prototype.afterOpen = function() {
};

Collect.AbstractItemEditDialogController.prototype.close = function() {
	this.content.modal('hide');
};

Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables = function(callback) {
	callback.call(this);
};

Collect.AbstractItemEditDialogController.prototype.initContent = function(callback) {
	var $this = this;
	$this.loadContent(function() {
		if ($this.doNotAllowCancel) {
			$this.content.find(".close").hide();
			$this.content.find(".cancel-btn").hide();
		}
		
		OF.i18n.initializeAll($this.content);
		
		$this.initFormElements(function() {
			$this.initEventListeners();
			$this.initFormElementsChangeListeners();
			callback.call($this);
		});
	});
};

Collect.AbstractItemEditDialogController.prototype.loadContent = function(callback) {
	var $this = this;
	OF.Remote.loadHtml($this.contentUrl + "?v=" + Collect.VERSION, 
		function(content) {
			$this.content = content;
			callback();
		}, 
		function() {
			collect.error.apply(this, arguments);
		}
	);
};

Collect.AbstractItemEditDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	$this.form = $this.content.find("form");
	if (callback) {
		callback.call($this);
	}
};

Collect.AbstractItemEditDialogController.prototype.initFormElementsChangeListeners = function(callback) {
	var $this = this;
	$this.form.find(".form-control").each(function() {
		var field = $(this);
		field.focusout($.proxy($this.fieldFocusOutHandler, $this, field));
		
		field.change(function() {
			$this.fieldChangeHandler(field);
		});
	});
};

Collect.AbstractItemEditDialogController.prototype.fieldFocusOutHandler = function(field) {
	var $this = this;
	OF.UI.Forms.setFieldVisited(field);
	$this.fieldChangeHandler(field);
};

Collect.AbstractItemEditDialogController.prototype.initEventListeners = function() {
	var $this = this;
	$this.content.find(".apply-btn").click(
		$.proxy(this.applyHandler, $this, false)
	);
	$this.content.find(".save-and-close-btn").click(
		$.proxy(this.applyHandler, $this, true)
	);
	$this.content.find(".cancel-btn").click(
		$.proxy(this.cancelHandler, $this)
	);
};

Collect.AbstractItemEditDialogController.prototype.applyHandler = function(close) {
	var $this = this;
	if ($this.validateForm()) {
		var item = $this.extractFormObject();
		$this.itemEditService.save(item, function(response) {
			$this.item = response.form;
			OF.UI.Forms.fill($this.form, $this.item);
			
			$this.dispatchItemSavedEvent();
			if (close) {
				$this.close();
			}
			$this.removeErrorsInForm();
		}, function(response) {
			if (response.errors.length > 0) {
				OF.Alerts.showError("Errors in the form: \n" + OF.UI.Forms.Validation.getFormErrorMessage($this.form, response.errors));
			}
			$this.showErrorsInForm(response.errors, false);
		});
	}
};

Collect.AbstractItemEditDialogController.prototype.fieldChangeHandler = function(field) {
	var $this = this;
	if ($this.itemEditService) {
		var item = $this.extractFormObject();
		$this.itemEditService.validate(item, function(response) {
			$this.removeErrorsInForm();
		}, function(response) {
			$this.showErrorsInForm(response.errors, true);
		});
	}
};

Collect.AbstractItemEditDialogController.prototype.removeErrorsInForm = function() {
	OF.UI.Forms.Validation.removeErrors(this.form);
};

Collect.AbstractItemEditDialogController.prototype.showErrorsInForm = function(errors, considerOnlyVisitedFields) {
	OF.UI.Forms.Validation.updateErrors(this.form, errors, considerOnlyVisitedFields);
};

Collect.AbstractItemEditDialogController.prototype.cancelHandler = function() {
	this.close();
};

Collect.AbstractItemEditDialogController.prototype.validateForm = function() {
	return true;
};

Collect.AbstractItemEditDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	OF.UI.Forms.fill($this.form, $this.item);
	
	//field.data("visited", true);
	callback.call($this);
};

Collect.AbstractItemEditDialogController.prototype.extractFormObject = function() {
	var $this = this;
	var oldItem = $this.item;
	var item = OF.UI.Forms.toJSON($this.form);
	item.id = oldItem == null ? null: oldItem.id;
	return item;
};

Collect.AbstractItemEditDialogController.prototype.dispatchItemSavedEvent = function() {
};

