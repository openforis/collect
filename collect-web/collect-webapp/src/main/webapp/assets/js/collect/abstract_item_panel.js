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
	$this.editItem(selectedItem);
};

Collect.AbstractItemPanel.prototype.editItem = function(item) {
	this.openItemEditDialog(item);
};

Collect.AbstractItemPanel.prototype.duplicateItem = function(item) {
	var $this = this;
	var newItem = jQuery.extend({}, item);
	newItem.id = null;
	$this.openItemEditDialog(newItem);
};

Collect.AbstractItemPanel.prototype.initDataGrid = function() {
	var $this = this;
	var gridContainer = $this.$panel.find(".grid");
	
	var options = $.extend($this.getCommonDataGridOptions(), $this.getDataGridOptions());
	
	gridContainer.bootstrapTable(options);
	
	$(window).resize(function() {
		$this.resizeDataGrid();
	});
	
	$this.dataGrid = gridContainer.data('bootstrap.table');
};

Collect.AbstractItemPanel.prototype.getCommonDataGridOptions = function() {
	return {
		cache: false,
		height: this.calculateTableHeight(),
		onDblClickRow: $.proxy(this.editItem, this)
	};
};

Collect.AbstractItemPanel.prototype.getDataGridOptions = function() {
};

Collect.AbstractItemPanel.prototype.openItemEditDialog = function(item) {
	var dialogController = new this.dialogControllerClass();
	dialogController.open(item);
};

Collect.AbstractItemPanel.prototype.onSurveyChanged = function() {
	this.refreshDataGrid();
}

Collect.AbstractItemPanel.prototype.refreshDataGrid = function() {
	if (this.dataGrid == null) {
		this.initDataGrid();
	} else {
		this.dataGrid.refresh();
		this.resizeDataGrid();
	}
};

Collect.AbstractItemPanel.prototype.onPanelShow = function() {
	this.resizeDataGrid();
}

Collect.AbstractItemPanel.prototype.resizeDataGrid = function() {
	var $this = this;
	var gridContainer = $this.$panel.find(".grid");
	gridContainer.bootstrapTable('resetView', {
		height : $this.calculateTableHeight()
	});
};

Collect.AbstractItemPanel.prototype.calculateTableHeight = function() {
	var $this = this;
    return $(window).height() - 250;
};

Collect.AbstractItemPanel.prototype.createGridItemDeleteColumn = function() {
	return Collect.Grids.createDeleteColumn(this.deleteItem, this);
};

Collect.AbstractItemPanel.prototype.createGridItemEditColumn = function() {
	return Collect.Grids.createEditColumn(this.editItem, this);
};

Collect.AbstractItemPanel.prototype.createGridItemDuplicateColumn = function() {
	return Collect.Grids.createDuplicateColumn(this.duplicateItem, this);
};

Collect.AbstractItemPanel.prototype.deleteItem = function(item) {
	var $this = this;
	OF.Alerts.confirm("Do you want to delete this " + $this.itemName + "?", function() {
		$this.itemService.remove(item.id, function() {
			EventBus.dispatch($this.deletedEventName, $this);
		});
	});
};
