Collect.AbstractItemPanel = function($panel, itemName, dialogControllerClass, itemService, deletedEventName, savedEventName) {
	this.$panel = $panel;
	this.itemName = itemName;
	this.dialogControllerClass = dialogControllerClass;
	this.itemService = itemService;
	this.deletedEventName = deletedEventName;
	this.savedEventName = savedEventName;
};

Collect.AbstractItemPanel.prototype.init = function() {
	var $this = this;
	var panel = this.$panel;
	
	panel.find('.new-btn').click($.proxy(function() {
		$this.openItemEditDialog();
	}, this));
	
	panel.find('.edit-btn').click($.proxy($this.editSelectedItem, $this));
	
	panel.find('.duplicate-btn').click($.proxy($this.duplicateSelectedItem, $this));
	
	panel.find('.delete-btn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $this.getSelectedItem();
		if (selectedItem == null) {
			return;
		}
		$this.deleteItem(selectedItem);
	}, this));
};

Collect.AbstractItemPanel.prototype.getSelectedItem = function() {
	var $this = this;
	var selections = $this.dataGrid.getSelections();
	return selections.length == 0 ? null : selections[0];
};

Collect.AbstractItemPanel.prototype.editSelectedItem = function() {
	var $this = this;
	var selectedItem = $this.getSelectedItem();
	if (selectedItem == null) {
		return;
	}
	$this.openItemEditDialog(selectedItem);
};

Collect.AbstractItemPanel.prototype.duplicateSelectedItem = function() {
	var $this = this;
	var selectedItem = $this.getSelectedItem();
	if (selectedItem == null) {
		return;
	}
	var newItem = jQuery.extend({}, selectedItem);
	newItem.id = null;
	$this.openItemEditDialog(newItem);
};

Collect.AbstractItemPanel.prototype.initDataGrid = function() {
	var $this = this;
	var gridContainer = $this.$panel.find(".grid");
	
	var options = $.extend($this.getCommonDataGridOptions(), $this.getDataGridOptions());
	
	gridContainer.bootstrapTable(options);
	
	$this.dataGrid = gridContainer.data('bootstrap.table');
};

Collect.AbstractItemPanel.prototype.getCommonDataGridOptions = function() {
	return {
		cache: false,
		clickToSelect: true,
	    singleSelect: true,
		onDblClickRow: $.proxy(this.editSelectedItem, this)
	};
};

Collect.AbstractItemPanel.prototype.getDataGridOptions = function() {
};

Collect.AbstractItemPanel.prototype.openItemEditDialog = function(item) {
	var dialogController = new this.dialogControllerClass();
	dialogController.open(item);
};

Collect.AbstractItemPanel.prototype.refreshDataGrid = function() {
	if (this.dataGrid == null) {
		this.initDataGrid();
	} else {
		this.dataGrid.refresh();
	}
};

Collect.AbstractItemPanel.prototype.createGridItemDeleteColumn = function() {
	return Collect.Grids.createDeleteColumn(this.deleteItem, this);
};

Collect.AbstractItemPanel.prototype.deleteItem = function(item) {
	var $this = this;
	OF.Alerts.confirm("Do you want to delete this " + $this.itemName + "?", function() {
		$this.itemService.remove(item.id, function() {
			EventBus.dispatch($this.deletedEventName, $this);
		});
	});
};
