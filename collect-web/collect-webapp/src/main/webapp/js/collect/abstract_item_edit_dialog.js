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
	beforeOpen(function() {
		$this.content.modal($this.doNotAllowCancel ? {backdrop: "static", keyboard: false} : 'show');
	});
};

Collect.AbstractItemEditDialogController.prototype.close = function() {
	this.content.modal('hide');
	this.content.remove();
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
			callback.call($this);
		});
	});
};

Collect.AbstractItemEditDialogController.prototype.loadContent = function(callback) {
	var $this = this;
	OF.Remote.loadHtml($this.contentUrl, 
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

Collect.AbstractItemEditDialogController.prototype.initEventListeners = function() {
	var $this = this;
	var applyBtn = $this.content.find(".apply-btn");
	applyBtn.click($.proxy(this.applyHandler, $this));
	var cancelBtn = $this.content.find(".cancel-btn");
	cancelBtn.click($.proxy(this.cancelHandler, $this));
};

Collect.AbstractItemEditDialogController.prototype.applyHandler = function() {
	var $this = this;
	if ($this.validateForm()) {
		var item = $this.extractJSONItem();
		$this.itemEditService.save(item, function(response) {
			if (response.statusOk) {
				$this.dispatchItemSavedEvent();
				$this.close();
			} else {
				OF.UI.showError("Errors in the form: " + OF.UI.Forms.Validation.getFormErrorMessage($this.form, response.errors));
				OF.UI.Forms.Validation.updateErrors($this.form, response.errors);
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

