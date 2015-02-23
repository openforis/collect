Collect.DataErrorQueryDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_error_query_dialog.html";
	this.itemEditService = collect.dataErrorQueryService;
	this.errorTypes = null;
	this.entityTree = null;
	this.attributeTree = null;
	this.errorTypeSelectPicker = null;
};

Collect.DataErrorQueryDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_SAVED = "dataErrorQuerySaved";
Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_DELETED = "dataErrorQueryDeleted";

Collect.DataErrorQueryDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_SAVED, this);
};

Collect.DataErrorQueryDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, [function() {
		collect.dataErrorTypeService.loadAll(function(errorTypes) {
			$this.errorTypes = errorTypes;
			callback();
		});
	}]);
};

Collect.DataErrorQueryDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		var select = $this.content.find('select[name="typeId"]');
		OF.UI.Forms.populateSelect(select, $this.errorTypes, "id", "code", true);
		select.selectpicker();
		$this.errorTypeSelectPicker = select.data().selectpicker;
		$this.entityTree = new Collect.EntityTree($this.content.find('.entity-tree'), collect.activeSurvey, function() {
			$this.attributeTree = new Collect.AttributeTree($this.content.find('.attribute-tree'), collect.activeSurvey, $this.entityTree, callback);
		});
	});
};

Collect.DataErrorQueryDialogController.prototype.extractJSONItem = function() {
	var item = Collect.AbstractItemEditDialogController.prototype.extractJSONItem.apply(this);
	item.entityDefinitionId = this.entityTree.getSelectedNodeId();
	item.attributeDefinitionId = this.attributeTree.getSelectedNodeId();
	return item;
};

Collect.DataErrorQueryDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.errorTypeSelectPicker.val($this.item.typeId);

		//1. select entity definition in tree
		$this.entityTree.selectNode($this.item.entityDefinitionId);
		
		//2. wait for attribute tree loading (ready event will be triggered)
		$this.attributeTree.onReady(function() {
			
			//3. select attribute definition in tree
			$this.attributeTree.selectNode($this.item.attributeDefinitionId);
			
			//4. call callback function
			callback();
		});
	});
};

