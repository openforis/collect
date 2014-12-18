Collect.Metamodel = function() {};

Collect.Metamodel.Survey = function(json) {
	var $this = this;
	
	$.extend($this, json);
	
	$this.nodeById = new Array();
	$this.init();
};

Collect.Metamodel.Survey.prototype.init = function() {
	var stack = new Array();
	stack = stack.concat(this.rootEntities);
	while (stack.length > 0) {
		var node = stack.pop();
		
		this.nodeById[node.id] = node;
		
		if (node.type == "ENTITY") {
			stack = stack.concat(node.children);
		}
	}
};

Collect.Metamodel.Survey.prototype.getDefinition = function(id) {
	var node = this.nodeById[id];
	return node;
};