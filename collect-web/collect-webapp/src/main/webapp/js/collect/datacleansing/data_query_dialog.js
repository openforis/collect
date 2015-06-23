Collect.DataQueryDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_query_dialog.html";
	this.itemEditService = collect.dataQueryService;
};

Collect.DataQueryDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataQueryDialogController.DATA_QUERY_SAVED = "dataQuerySaved";
Collect.DataQueryDialogController.DATA_QUERY_DELETED = "dataQueryDeleted";

Collect.DataQueryDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataQueryDialogController.DATA_QUERY_SAVED, this);
};

Collect.DataQueryDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		var monitorJob = function(jobMonitorUrl, complete) {
			var jobDialog = new OF.UI.JobDialog();
			new OF.JobMonitor(jobMonitorUrl, function() {
				jobDialog.close();
				complete();
			});
		};
		
		{//init record step select
			var select = $this.content.find('select[name="recordStep"]');
			OF.UI.Forms.populateSelect(select, Collect.DataCleansing.WORKFLOW_STEPS, "name", "label");
			select.selectpicker();
			$this.recordStepSelectPicker = select.data().selectpicker;
			$this.recordStepSelectPicker.refresh();
		}
		
		var testResultGridContainer = $this.content.find(".test-result-grid");
		testResultGridContainer.bootstrapTable({
			url: this.itemEditService.contextPath + "test-result.json",
			cache: false,
			clickToSelect: true,
			height: 200,
			columns: [
		          {field: "key1", title: "Key1"},
		          {field: "key2", title: "Key2"},
		          {field: "key3", title: "Key3"},
		          {field: "nodePath", title: "Path"},
		          {field: "attributeValue", title: "Value"}
		          ]
		});
		$this.testResultDataGrid = testResultGridContainer.data('bootstrap.table');
		$this.testResultDataGrid.$container.hide();
		
		$this.content.find(".test-btn").click($.proxy(function() {
			var query = $this.extractFormObject();
			query.recordStep = $this.recordStepSelectPicker.val();
			collect.dataQueryService.startTest(query, function() {
				monitorJob($this.itemEditService.contextPath + "test-job.json", function() {
					$this.testResultDataGrid.refresh();
					$this.testResultDataGrid.$container.show();
				});
			});
		}, $this));
		
		$this.content.find(".export-to-csv-btn").click($.proxy(function() {
			var query = $this.extractFormObject();
			query.recordStep = $this.recordStepSelectPicker.val();
			collect.dataQueryService.startExport(query, function() {
				monitorJob($this.itemEditService.contextPath + "export-job.json", function() {
					collect.dataQueryService.downloadResult();
				});
			});
		}, $this));
		
		$this.entityTree = new Collect.EntityTree($this.content.find('.entity-tree'), collect.activeSurvey, "entityDefinitionId", function() {
			$this.attributeTree = new Collect.AttributeTree($this.content.find('.attribute-tree'), collect.activeSurvey, $this.entityTree, "attributeDefinitionId", callback);
		});
	});
};

Collect.DataQueryDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
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

