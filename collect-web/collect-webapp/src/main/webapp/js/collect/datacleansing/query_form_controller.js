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
	$this.entityTree = new Collect.EntityTree($this.form.find('.entity-tree'), collect.activeSurvey, function() {
		$this.attributeTree = new Collect.AttributeTree($this.form.find('.attribute-tree'), collect.activeSurvey, $this.entityTree, callback);
	});
};

Collect.DataQueryFormController.prototype.extractJSONItem = function() {
	var $this = this;
	var item = OF.UI.Forms.toJSON($this.form);
	item.entityDefinitionId = this.entityTree.getSelectedNodeId();
	item.attributeDefinitionId = this.attributeTree.getSelectedNodeId();
	return item;
};

Collect.DataQueryFormController.prototype.fillForm = function(callback) {
	var $this = this;
	OF.UI.Forms.fill($this.form, $this.item);
	//1. select entity definition in tree
	$this.entityTree.selectNode($this.item.entityDefinitionId);
	
	//2. wait for attribute tree loading (ready event will be triggered)
	$this.attributeTree.onReady(function() {
		
		//3. select attribute definition in tree
		$this.attributeTree.selectNode($this.item.attributeDefinitionId);
		
		//4. call callback function
		callback();
	});
};
