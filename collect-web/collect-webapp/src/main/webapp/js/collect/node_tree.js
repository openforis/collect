Collect.NodeTree = function(treeDiv, survey, disabledFilterFunction, startFromEntityId, hiddenFieldName, creationCompleteHandler) {
	this.treeDiv = treeDiv;
	this.survey = survey;
	this.disabledFilterFunction = disabledFilterFunction;
	this.startFromEntityId = startFromEntityId;
	this.hiddenFieldName = hiddenFieldName;
	this.creationCompleteHandler = creationCompleteHandler;
	
	this.init();
};

Collect.NodeTree.prototype.init = function() {
	var $this = this;
	$this.buildTreeNodes();
	if (this.creationCompleteHandler) {
		setTimeout(function() {
			$this.creationCompleteHandler();
		}, 100);
	}
};

Collect.NodeTree.prototype.buildTreeNodes = function() {
	var $this = this;
	var createTreeNode = function(node) {
		var text = "[" + node.name + "]";
		if (node.label != null) {
			text += " - " + node.label;
		}
		var treeNode = {
			id: 	node.id,
			text: 	text, 
			state: 	{disabled: $this.disabledFilterFunction ? $this.disabledFilterFunction(node) : false}
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
	var rootNodes;
	if (this.startFromEntityId) {
		var startFromEntity = this.survey.getDefinition(this.startFromEntityId);
		rootNodes = startFromEntity.children;
	} else {
		rootNodes = this.survey.rootEntities;
	}
	var rootTreeNodes = new Array();
	for (var idx = 0; idx < rootNodes.length; idx++) {
		var rootEntity = rootNodes[idx];
		rootTreeNodes.push(createTreeNode(rootEntity));
	}
	var jstree = this.treeDiv.jstree({
		'core' : {
			'data' : rootTreeNodes
		}
	});
	var hiddenField = $("<input type='hidden'>");
	hiddenField.prop("name", this.hiddenFieldName);
	
	this.treeDiv.append(hiddenField);

	$this.jstree = this.treeDiv.data().jstree;
};

Collect.NodeTree.prototype.addSelectNodeHandler = function(handler) {
	this.jstree.element.on('select_node.jstree', handler);
};

Collect.NodeTree.prototype.selectNode = function(nodeId) {
	this.jstree.select_node(nodeId);
};

Collect.NodeTree.prototype.getSelectedNodeId = function() {
	var selectedTreeNodeIds = this.jstree.get_selected();
	if (selectedTreeNodeIds == null || selectedTreeNodeIds.length == 0) {
		return null;
	} else {
		return selectedTreeNodeIds[0];
	}
};

Collect.NodeTree.prototype.onReady = function(callback) {
	var $this = this;
	var treeReadyHandler = function() {
		$this.jstree.element.off("ready.jstree", treeReadyHandler);
		callback();
	};
	$this.jstree.element.on("ready.jstree", treeReadyHandler);
};

Collect.NodeTree.prototype.destroy = function() {
	this.jstree.destroy();
};

Collect.NodeTree.prototype.refresh = function() {
	this.destroy();
	this.buildTreeNodes();
};

Collect.EntityTree = function(treeDiv, survey, hiddenFieldName, creationCompleteHandler) {
	var disabledFilterFunction = function(node) {
		return node.type == "ATTRIBUTE";
	};
	Collect.NodeTree.call(this, treeDiv, survey, disabledFilterFunction, null, hiddenFieldName, creationCompleteHandler);
};

Collect.EntityTree.prototype = Object.create(Collect.NodeTree.prototype);

Collect.AttributeTree = function(treeDiv, survey, parentEntityTree, hiddenFieldName, creationCompleteHandler) {
	this.parentEntityTree = parentEntityTree;
	var disabledFilterFunction = function(node) {
		return node.type == "ENTITY";
	};
	var selectedEntityDefId = parentEntityTree ? parentEntityTree.getSelectedNodeId(): null;
	
	var $this = this;
	if (parentEntityTree) {
		parentEntityTree.addSelectNodeHandler(function() {
			$this.startFromEntityId = parentEntityTree.getSelectedNodeId();
			$this.refresh();
		});
	}

	Collect.NodeTree.call(this, treeDiv, survey, disabledFilterFunction, selectedEntityDefId, hiddenFieldName, creationCompleteHandler);
};

Collect.AttributeTree.prototype = Object.create(Collect.NodeTree.prototype);

