Collect.AbstractItemEditDialogController = function() {
	this.contentUrl = "to be defined by subclass";
	this.initialized = false;
	this.content = null;
	this.form = null;
	this.itemEditService = null;
	this.item = null;
};

Collect.AbstractItemEditDialogController.prototype.init = function(callback) {
	var $this = this;
	$this.loadInstanceVariables(function() {
		$this.initContent(function() {
			callback();
		});
	});
};

Collect.AbstractItemEditDialogController.prototype.open = function(item) {
	var $this = this;
	$this.item = item;
	
	function doOpen() {
		if (item) {
			OF.UI.Forms.fill($this.form, item);
		}
		$this.content.modal('show');
	};
	if ($this.initialized) {
		doOpen();
	} else {
		$this.init(function() {
			doOpen();
		})
	}
};

Collect.AbstractItemEditDialogController.prototype.close = function() {
	this.content.modal('hide');
	this.content.remove();
};

Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables = function(callback) {
	callback();
};

Collect.AbstractItemEditDialogController.prototype.initContent = function(callback) {
	var $this = this;
	$this.loadContent(function() {
		$this.initFormElements();
		$this.initEventListeners();
		callback();
	});
};

Collect.AbstractItemEditDialogController.prototype.loadContent = function(callback) {
	var $this = this;
	OF.Remote.loadHtml($this.contentUrl, function(content) {
			$this.content = content;
			callback();
		}, function() {
			collect.error.apply(this, arguments);
		}
	);
};

Collect.AbstractItemEditDialogController.prototype.initFormElements = function() {
	var $this = this;
	$this.form = $this.content.find("form");
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
		$this.itemEditService.save(item, function() {
			$this.dispatchItemSavedEvent();
			$this.close();
		});
	}
};

Collect.AbstractItemEditDialogController.prototype.cancelHandler = function() {
	this.close();
};

Collect.AbstractItemEditDialogController.prototype.validateForm = function() {
	return true;
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

