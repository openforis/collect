Collect.DataQueryFormController = function(form) {
	this.form = form;
	this.entityTree = null;
	this.attributeTree = null;
};

Collect.DataQueryFormController.prototype.init = function(callback) {
	var $this = this;
	{
		//record step select
		var select = $this.form.find('select[name="recordStep"]');
		OF.UI.Forms.populateSelect(select, [{name: "ENTRY", label: "Data Entry"}, 
		                                    {name: "CLEANSING", label: "Data Cleansing"},
		                                    {name: "ANALYSIS", label: "Data Analysis"}
		                                    ], "name", "label");
		select.selectpicker();
		$this.recordStepSelectPicker = select.data().selectpicker;
		$this.recordStepSelectPicker.refresh();
	}
	$this.initEntityTree(function() {
		$this.initAttributeTree(function() {
			callback();
		});
	});
};

Collect.DataQueryFormController.prototype.extractJSONItem = function() {
	var $this = this;
	var item = OF.UI.Forms.toJSON($this.form);
	item.entityDefinitionId = this.getSelectedNodeId(this.entityTree);
	item.attributeDefinitionId = this.getSelectedNodeId(this.attributeTree);
	return item;
};

Collect.DataQueryFormController.prototype.initEntityTree = function(callback) {
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

Collect.DataQueryFormController.prototype.initAttributeTree = function(callback) {
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

Collect.DataQueryFormController.prototype.fillForm = function(callback) {
	var $this = this;
	OF.UI.Forms.fill($this.form, $this.item);
	//1. select entity definition in tree
	$this.selectTreeNode($this.entityTree, $this.item.entityDefinitionId);
	
	//2. wait for attribute tree loading (ready event will be triggered)
	$this.runWhenTreeIsReady($this.attributeTree, function() {
		
		//3. select attribute definition in tree
		$this.selectTreeNode($this.attributeTree, $this.item.attributeDefinitionId);
		
		//4. call callback function
		callback();
	});
};

Collect.DataQueryFormController.prototype.initNodeTree = function(treeDivClass, survey, disabledFilterFunction, startFromEntityId) {
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
	var treeDiv = $this.form.find(treeDivClass);
	var jstree = treeDiv.jstree({
		'core' : {
			'data' : rootTreeNodes
		}
	});
	var tree = treeDiv.data().jstree;
	return tree;
};

Collect.DataQueryFormController.prototype.selectTreeNode = function(tree, nodeId) {
	tree.select_node(nodeId);
};

Collect.DataQueryFormController.prototype.getSelectedNodeId = function(tree) {
	var selectedTreeNodeIds = tree.get_selected();
	if (selectedTreeNodeIds == null || selectedTreeNodeIds.length == 0) {
		return null;
	} else {
		return selectedTreeNodeIds[0];
	}
};

Collect.DataQueryFormController.prototype.runWhenTreeIsReady = function(tree, callback) {
	var treeReadyHandler = function() {
		tree.element.off("ready.jstree", treeReadyHandler);
		callback();
	};
	tree.element.on("ready.jstree", treeReadyHandler);
};
