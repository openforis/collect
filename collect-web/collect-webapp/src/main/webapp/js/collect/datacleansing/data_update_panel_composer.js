Collect.DataCleansing.DataUpdatePanelComposer = function(container) {
	this.container = container;
	this.init();
};

Collect.DataCleansing.DataUpdatePanelComposer.prototype.init = function(callback) {
	var $this = this;
	$this.form = $this.container.find("form");
	
	$this.container.find(".run-btn").click($.proxy($this.runClickHandler, $this));
	
	//init record step select picker
	{
		var select = $this.form.find('select[name="recordStep"]');
		OF.UI.Forms.populateSelect(select, Collect.DataCleansing.WORKFLOW_STEPS, "name", "label");
		select.selectpicker();
		$this.recordStepSelectPicker = select.data().selectpicker;
	}

	$this.entityTree = new Collect.EntityTree($this.form.find('.entity-tree'), collect.activeSurvey, function() {
		$this.attributeTree = new Collect.AttributeTree($this.form.find('.attribute-tree'), collect.activeSurvey, $this.entityTree, callback);
	});
};

Collect.DataCleansing.DataUpdatePanelComposer.prototype.runClickHandler = function() {
	var $this = this;
	var formItem = OF.UI.Forms.toJSON($this.form);
	formItem.entityDefinitionId = this.entityTree.getSelectedNodeId();
	formItem.attributeDefinitionId = this.attributeTree.getSelectedNodeId();
	
	collect.dataUpdateService.start(formItem, function() {
		new OF.JobMonitor("datacleansing/dataupdate/job.json");
		new OF.UI.JobDialog();
	});
};
