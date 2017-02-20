Collect.Metamodel = function() {};

Collect.Metamodel.Survey = function(json) {
	var $this = this;
	
	$.extend($this, json);
	
	$this.nodeById = new Array();
	$this.init();
};

Collect.Metamodel.Survey.prototype.init = function() {
	var $this = this;
	
	function toNodeDefinitions(list) {
		var result = new Array();
		$.each(list, function(idx, item) {
			var node = toNodeDefinition(item);
			result.push(node);
		});
		return result;
	}
	
	function toNodeDefinition(json) {
		var node;
		if (json.type == "ENTITY") {
			node = new Collect.Metamodel.EntityDefinition(json);
			node.children = toNodeDefinitions(json.children); 
		} else {
			node = new Collect.Metamodel.AttributeDefinition(json);
		}
		return node;
	}
	
	this.rootEntities = toNodeDefinitions(this.rootEntities);
	
	//index node definitions
	this.traverse(function(nodeDef) {
		$this.nodeById[nodeDef.id] = nodeDef;
	});
};

Collect.Metamodel.Survey.prototype.getDefinition = function(id) {
	var node = this.nodeById[id];
	return node;
};

Collect.Metamodel.Survey.prototype.getMainRootEntity = function() {
	return this.rootEntities.length > 0 ? this.rootEntities[0] : null;
}

Collect.Metamodel.Survey.prototype.getRooEntityKeyDefinitions = function() {
	return this.getKeyDefinitions(this.getMainRootEntity());
};

Collect.Metamodel.Survey.prototype.getKeyDefinitions = function(rootEntity) {
	var result = new Array();
	var stack = new Array();
	stack.push(rootEntity);
	while (stack.length > 0) {
		var nodeDef = stack.pop();
		if (nodeDef instanceof Collect.Metamodel.AttributeDefinition && nodeDef.key) {
			result.push(nodeDef);
		} else if(nodeDef instanceof Collect.Metamodel.EntityDefinition && (nodeDef.root || ! nodeDef.multiple)) {
			nodeDef.children.forEach(function(childDef) {
				stack.push(childDef);
			});
		}
	}
	return result;
};

Collect.Metamodel.Survey.prototype.traverse = function(fun) {
	var stack = new Array();
	stack = stack.concat(this.rootEntities);
	while (stack.length > 0) {
		var nodeDef = stack.pop();
		fun(nodeDef);
		if (nodeDef instanceof Collect.Metamodel.EntityDefinition) {
			$.each(nodeDef.children, function(idx, childDef) {
				childDef.parent = nodeDef;
				stack.push(childDef);
			});
		}
	}
};

Collect.Metamodel.NodeDefinition = function(json) {
	$.extend(this, json);
}

Collect.Metamodel.NodeDefinition.prototype.getPath = function() {
	var result = "";
	var currentNode = this;
	while (currentNode != null) {
		result = "/" + currentNode.name + result;
		currentNode = currentNode.parent;
	}
	return result;
}

Collect.Metamodel.NodeDefinition.prototype.getLabelOrName = function() {
	return this.label == null ? this.name : this.label;
}

Collect.Metamodel.AttributeDefinition = function(json) {
	Collect.Metamodel.NodeDefinition.apply(this, arguments);
}

Collect.Metamodel.AttributeDefinition.prototype = Object.create(Collect.Metamodel.NodeDefinition.prototype);

Collect.Metamodel.EntityDefinition = function(json) {
	Collect.Metamodel.NodeDefinition.apply(this, arguments);
}

Collect.Metamodel.EntityDefinition.prototype = Object.create(Collect.Metamodel.NodeDefinition.prototype);
