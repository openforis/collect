Collect.DataErrorQueryDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "/collect/datacleansing/data_error_query_dialog.html";
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
		
		$this.initEntityTree(function() {
			$this.initAttributeTree(function() {
				callback();
			});
		});
	});
};

Collect.DataErrorQueryDialogController.prototype.extractJSONItem = function() {
	var item = Collect.AbstractItemEditDialogController.prototype.extractJSONItem.apply(this);
	item.entityDefinitionId = this.getSelectedNodeId(this.entityTree);
	item.attributeDefinitionId = this.getSelectedNodeId(this.attributeTree);
	return item;
};

Collect.DataErrorQueryDialogController.prototype.initEntityTree = function(callback) {
	var $this = this;
	var disabledFilterFunction = function(node) {
		return node.type == "ATTRIBUTE";
	};
	$this.entityTree = $this.initNodeTree('.entity-tree', collect.activeSurvey, disabledFilterFunction);
	$this.runWhenTreeIsReady($this.entityTree, callback);
	
	$this.entityTree.element.on('select_node.jstree', function() {
		$this.initAttributeTree();
	});
};

Collect.DataErrorQueryDialogController.prototype.initAttributeTree = function(callback) {
	var disabledFilterFunction = function(node) {
		return node.type == "ENTITY";
	};
	if (this.attributeTree) {
		this.attributeTree.destroy();
	}
	var selectedEntityDefId = this.entityTree ? this.getSelectedNodeId(this.entityTree): null;
	if (selectedEntityDefId != null) {
		this.attributeTree = this.initNodeTree('.attribute-tree', collect.activeSurvey, disabledFilterFunction, selectedEntityDefId);
		if (callback) $this.runWhenTreeIsReady(this.attributeTree, callback);
	} else {
		if (callback) callback();
	}
};

Collect.DataErrorQueryDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.errorTypeSelectPicker.val($this.item.typeId);

		//1. select entity definition in tree
		$this.selectTreeNode($this.entityTree, $this.item.entityDefinitionId);
		
		//2. wait for attribute tree loading (ready event will be triggered)
		$this.runWhenTreeIsReady($this.attributeTree, function() {
			
			//3. select attribute definition in tree
			$this.selectTreeNode($this.attributeTree, $this.item.attributeDefinitionId);
			
			//4. call callback function
			callback();
		});
	});
};

Collect.DataErrorQueryDialogController.prototype.initNodeTree = function(treeDivId, survey, disabledFilterFunction, startFromEntityId) {
	var createTreeNode = function(node) {
		var text = "[" + node.name + "]";
		if (node.label != null) {
			text += " - " + node.label;
		}
		var treeNode = {
			id: 	node.id,
			text: 	text, 
			state: 	{disabled: disabledFilterFunction ? disabledFilterFunction(node) : false}
		};
		if (node.type == "ENTITY") {
			var children = new Array();
			for (var idx = 0; idx < node.children.length; idx++) {
				var child = node.children[idx];
				var childTreeNode = createTreeNode(child);
				children.push(childTreeNode);
			}
			treeNode.children = children;
		}
		return treeNode;
	};
	var $this = this;
	var rootNodes;
	if (startFromEntityId) {
		var startFromEntity = survey.getDefinition(startFromEntityId);
		rootNodes = startFromEntity.children;
	} else {
		rootNodes = survey.rootEntities;
	}
	var rootTreeNodes = new Array();
	for (var idx = 0; idx < rootNodes.length; idx++) {
		var rootEntity = rootNodes[idx];
		rootTreeNodes.push(createTreeNode(rootEntity));
	}
	var treeDiv = $this.content.find(treeDivId);
	var jstree = treeDiv.jstree({
		'core' : {
			'data' : rootTreeNodes
		}
	});
	var tree = treeDiv.data().jstree;
	return tree;
};

Collect.DataErrorQueryDialogController.prototype.selectTreeNode = function(tree, nodeId) {
	tree.select_node(nodeId);
};

Collect.DataErrorQueryDialogController.prototype.getSelectedNodeId = function(tree) {
	var selectedTreeNodeIds = tree.get_selected();
	if (selectedTreeNodeIds == null || selectedTreeNodeIds.length == 0) {
		return null;
	} else {
		return selectedTreeNodeIds[0];
	}
};

Collect.DataErrorQueryDialogController.prototype.runWhenTreeIsReady = function(tree, callback) {
	var treeReadyHandler = function() {
		tree.element.off("ready.jstree", treeReadyHandler);
		callback();
	};
	tree.element.on("ready.jstree", treeReadyHandler);
};
