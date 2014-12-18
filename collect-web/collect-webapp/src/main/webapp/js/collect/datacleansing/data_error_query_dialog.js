Collect.DataErrorQueryDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "/collect/datacleansing/data_error_query_dialog.html";
	this.surveySummaries = null;
	this.itemEditService = collect.dataErrorQueryService;
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

Collect.DataErrorQueryDialogController.prototype.initFormElements = function() {
	Collect.AbstractItemEditDialogController.prototype.initFormElements.apply(this, arguments);
	var $this = this;
	var select = $this.content.find('select[name="errorTypeId"]');
	OF.UI.Forms.populateSelect(select, $this.errorTypes, "id", "code", true);
	$this.errorTypeSelectPicker = select.selectpicker();
	
	$this.initEntityTree();
	$this.initAttributeTree();
};

Collect.DataErrorQueryDialogController.prototype.extractJSONItem = function() {
	var getSelectedNodeId = function(tree) {
		var selectedTreeNodeIds = tree.get_selected()
		if (selectedTreeNodeIds == null || selectedTreeNodeIds.length == 0) {
			return null;
		} else {
			var treeNode = tree.get_node(selectedTreeNodeIds[0]);
			return treeNode.data.nodeId;
		}
	}
	var item = Collect.AbstractItemEditDialogController.prototype.extractJSONItem.apply(this);
	item.typeId = this.errorTypeSelectPicker.val();
	item.entityDefinitionId = getSelectedNodeId(this.entityTree);
	item.attributeDefinitionId = getSelectedNodeId(this.attributeTree);
	return item;
};

Collect.DataErrorQueryDialogController.prototype.initEntityTree = function() {
	var disabledFilterFunction = function(node) {
		return node.type == "ATTRIBUTE";
	};
	this.entityTree = this.initNodeTree('.entity-tree', collect.activeSurvey, disabledFilterFunction);
};

Collect.DataErrorQueryDialogController.prototype.initAttributeTree = function() {
	var disabledFilterFunction = function(node) {
		return node.type == "ENTITY";
	};
	var selectedEntityDefId = getSelectedNodeId(this.entityTree);
	this.attributeTree = this.initNodeTree('.attribute-tree', collect.activeSurvey, disabledFilterFunction, selectedEntityDefId);
};

Collect.DataErrorQueryDialogController.prototype.initNodeTree = function(treeDivId, survey, disabledFilterFunction, startFromEntityId) {
	var createTreeNode = function(node) {
		var text = "[" + node.name + "]";
		if (node.label != null) {
			text += " - " + node.label;
		}
		var treeNode = {
			data: {nodeId: node.id},
			text: text, 
			state: {disabled: disabledFilterFunction ? disabledFilterFunction : false}
		};
		if (node.type == "ENTITY") {
			var children = new Array();
			for (var idx = 0; idx < node.children.length; idx++) {
				var child = node.children[idx];
				var childTreeNode = createTreeNode(child)
				children.push(childTreeNode);
			}
			treeNode.children = children;
		}
		return treeNode;
	};
	var $this = this;
	var rootTreeNodes = new Array();
	if (startFromEntityId) {
		var startFromEntity = survey.getDefinition(startFromEntityId);
		for (var idx = 0; idx < startFromEntity.children.length; idx++) {
			var child = startFromEntity.children[idx];
			rootTreeNodes.push(createTreeNode(child));
		}
	} else {
		for (var idx = 0; idx < survey.rootEntities.length; idx++) {
			var rootEntity = survey.rootEntities[idx];
			rootTreeNodes.push(createTreeNode(rootEntity));
		}
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

