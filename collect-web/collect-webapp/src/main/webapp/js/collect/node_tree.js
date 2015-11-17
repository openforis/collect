Collect.NodeTree = function(treeDiv, survey, disabledFilterFunction, visibleFilterFunction, startFromEntityId, hiddenFieldName, creationCompleteHandler) {
	this.treeDiv = treeDiv;
	this.survey = survey;
	this.disabledFilterFunction = disabledFilterFunction;
	this.visibleFilterFunction = visibleFilterFunction;
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
			$this.jstree.open_all();
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
		var imagesFolder = "assets/images/node_types/";
		var treeNode = {
			id: 	node.id,
			text: 	text, 
			state: 	{disabled: $this.disabledFilterFunction ? $this.disabledFilterFunction(node) : false},
			icon: imagesFolder + (node.type == "ENTITY" ? "form" : node.attributeType.toLocaleLowerCase()) + "-small.png"
		};
		if (node.type == "ENTITY") {
			var children = new Array();
			for (var idx = 0; idx < node.children.length; idx++) {
				var child = node.children[idx];
				if ($this.visibleFilterFunction == null || $this.visibleFilterFunction(child)) {
					var childTreeNode = createTreeNode(child);
					children.push(childTreeNode);
				}
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
		if ($this.visibleFilterFunction == null || $this.visibleFilterFunction(rootEntity)) {
			rootTreeNodes.push(createTreeNode(rootEntity));
		}
	}
	var jstree = this.treeDiv.jstree({
		'core' : {
			'data' : rootTreeNodes
		}
	});
	$this.jstree = this.treeDiv.data().jstree;
	
	$this.treeDiv.on("changed.jstree", function() {
		var hiddenField = $this.getHiddenField();
		hiddenField.val($this.getSelectedNodeId());
		hiddenField.data("visited", true);
	});

	this.appendHiddenField();
};

Collect.NodeTree.prototype.appendHiddenField = function() {
	var hiddenField = $("<input type='hidden' class='form-control'>");
	hiddenField.prop("name", this.hiddenFieldName);
	
	this.treeDiv.after(hiddenField);
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
	try {
		this.jstree.destroy();
	} catch(e) {}
	var hiddenField = this.getHiddenField();
	hiddenField.remove();
};

Collect.NodeTree.prototype.getHiddenField = function() {
	return this.treeDiv.parent().find("input[name='" + this.hiddenFieldName +"']");
};

Collect.NodeTree.prototype.refresh = function() {
	this.destroy();
	this.buildTreeNodes();
};

Collect.EntityTree = function(treeDiv, survey, hiddenFieldName, creationCompleteHandler) {
	var visibleFilterFunction = function(node) {
		return node.type == "ENTITY";
	};
	Collect.NodeTree.call(this, treeDiv, survey, null, visibleFilterFunction, null, hiddenFieldName, creationCompleteHandler);
};

Collect.EntityTree.prototype = Object.create(Collect.NodeTree.prototype);

Collect.AttributeTree = function(treeDiv, survey, parentEntityTree, hiddenFieldName, creationCompleteHandler) {
	this.parentEntityTree = parentEntityTree;
	
	var selectedEntityDefId = parentEntityTree ? parentEntityTree.getSelectedNodeId(): null;
	
	var disabledFilterFunction = function(node) {
		return node.type == "ENTITY";
	};
	var visibleFilterFunction = null;
	
	var $this = this;
	if (parentEntityTree) {
		parentEntityTree.addSelectNodeHandler(function() {
			$this.startFromEntityId = parentEntityTree.getSelectedNodeId();
			$this.refresh();
		});
		visibleFilterFunction = function(node) {
			return parentEntityTree.getSelectedNodeId() != null;
		};
	}

	Collect.NodeTree.call(this, treeDiv, survey, disabledFilterFunction, visibleFilterFunction, selectedEntityDefId, hiddenFieldName, creationCompleteHandler);
};

Collect.AttributeTree.prototype = Object.create(Collect.NodeTree.prototype);

