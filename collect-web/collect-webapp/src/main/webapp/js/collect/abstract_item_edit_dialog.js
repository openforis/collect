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

Collect.AbstractItemEditDialogController.prototype.doOpen = function() {
	var $this = this;
	
	function beforeOpen(callback) {
		if ($this.item) {
			$this.fillForm(function() {
				callback.call($this);
			});
		} else {
			callback.call($this);
		}
	};
	$this.content.on('hide.bs.modal', function (e) {
		 $this.content.remove();
	 });
	beforeOpen(function() {
		var options = {backdrop: "static"};
		if ($this.doNotAllowCancel) {
			options.keyboard = false;
		}
		$this.content.modal(options);
	});
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
	field.data("visited", true);
	$this.fieldChangeHandler();
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
		var item = $this.extractJSONItem();
		$this.itemEditService.save(item, function(response) {
			if (response.statusOk) {
				$this.item = response.form;
				$this.dispatchItemSavedEvent();
				if (close) {
					$this.close();
				}
			} else {
				OF.Alerts.showError("Errors in the form: " + OF.UI.Forms.Validation.getFormErrorMessage($this.form, response.errors));
			}
			OF.UI.Forms.Validation.updateErrors($this.form, response.errors);
		});
	}
};

Collect.AbstractItemEditDialogController.prototype.fieldChangeHandler = function() {
	var $this = this;
	if ($this.itemEditService) {
		var item = $this.extractJSONItem();
		$this.itemEditService.validate(item, function(response) {
			if (response.errors.length == 0) {
				OF.UI.Forms.Validation.removeErrors($this.form);
			} else {
				OF.UI.Forms.Validation.updateErrors($this.form, response.errors, true);
			}
		});
	}
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
	callback.call($this);
};

Collect.AbstractItemEditDialogController.prototype.extractJSONItem = function() {
	var $this = this;
	var oldItem = $this.item;
	var item = OF.UI.Forms.toJSON($this.form);
	item.id = oldItem == null ? null: oldItem.id;
	return item;
};

Collect.AbstractItemEditDialogController.prototype.dispatchItemSavedEvent = function() {
};

