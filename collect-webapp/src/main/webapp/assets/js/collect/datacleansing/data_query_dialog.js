Collect.DataQueryDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/data_query_dialog.html";
	this.itemEditService = collect.dataQueryService;
	
	this.queryTypes = null;
	this.queryTypeSelectPicker = null;
	this.errorSeveritySelectPicker = null;
	this.errorSeverities = [
            {code: "NO_ERROR", label: "No Error"},
        	{code: "WARNING", label: "Warning"},
        	{code: "ERROR", label: "Error"} 
    ];
};

Collect.DataQueryDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.DataQueryDialogController.prototype.dispatchItemSavedEvent = function() {
	EventBus.dispatch(Collect.DataCleansing.DATA_QUERY_SAVED, this);
};

Collect.DataQueryDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.loadInstanceVariables.apply(this, [function() {
		collect.dataQueryTypeService.loadAll(function(queryTypes) {
			$this.queryTypes = queryTypes;
			
			callback();
		});
	}]);
};

Collect.DataQueryDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		{//init error type select picker
			var select = $this.content.find('select[name="typeId"]');
			OF.UI.Forms.populateSelect(select, $this.queryTypes, "id", "prettyLabel", true);
			select.selectpicker();
			$this.queryTypeSelectPicker = select.data().selectpicker;
		}
		{//init severity select picker
			var select = $this.content.find('select[name="errorSeverity"]');
			OF.UI.Forms.populateSelect(select, $this.errorSeverities, "code", "label");
			select.selectpicker();
			$this.errorSeveritySelectPicker = select.data().selectpicker;
		}
		{//init record step select
			var select = $this.content.find('select[name="recordStep"]');
			OF.UI.Forms.populateSelect(select, Collect.DataCleansing.WORKFLOW_STEPS, "name", "label");
			select.selectpicker();
			$this.recordStepSelectPicker = select.data().selectpicker;
			$this.recordStepSelectPicker.refresh();
		}
		
		$this.initTestAndRunContainer();
		
		$this.entityTree = new Collect.EntityTree($this.content.find('.entity-tree'), collect.activeSurvey, "entityDefinitionId", function() {
			$this.attributeTree = new Collect.AttributeTree($this.content.find('.attribute-tree'), collect.activeSurvey, $this.entityTree, "attributeDefinitionId", callback);
		});
	});
};

Collect.DataQueryDialogController.prototype.initTestAndRunContainer = function() {
	var $this = this;
	
	if (collect.activeSurvey.temporary) {
		$this.content.find("#query-test-and-run-tab").hide();
	} else {
		var testResultGridContainer = $this.content.find(".test-result-grid");
		
		var columns = Collect.Grids.createRootEntityKeyColumns(collect.activeSurvey);
		columns = columns.concat([
	          {field: "nodePath", title: "Path", width: 400},
	          {field: "attributeValue", title: "Value", width: 400}
	    ]);
		testResultGridContainer.bootstrapTable({
			url: this.itemEditService.contextPath + "/test-result.json",
			cache: false,
			height: 200,
			columns: columns
		});
		$this.testResultDataGrid = testResultGridContainer.data('bootstrap.table');
		$this.testResultDataGrid.$container.hide();
		
		$this.content.find(".test-btn").click($.proxy(function() {
			var query = $this.extractFormObject();
			query.recordStep = $this.recordStepSelectPicker.val();
			collect.dataQueryService.startTest(query, function() {
				monitorJob($this.itemEditService.contextPath + "/test-job.json", function() {
					$this.testResultDataGrid.refresh();
					$this.testResultDataGrid.$container.show();
				});
			});
		}, $this));
		
		$this.content.find(".export-to-csv-btn").click($.proxy(function() {
			var query = $this.extractFormObject();
			query.recordStep = $this.recordStepSelectPicker.val();
			collect.dataQueryService.startExport(query, function() {
				monitorJob($this.itemEditService.contextPath + "/export-job.json", function() {
					collect.dataQueryService.downloadResult();
				});
			});
		}, $this));
		
		var monitorJob = function(jobMonitorUrl, complete) {
			var jobDialog = new OF.UI.JobDialog();
			new OF.JobMonitor(jobMonitorUrl, function() {
				jobDialog.close();
				complete();
			});
		};
	}
};

Collect.DataQueryDialogController.prototype.fillForm = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.fillForm.call(this, function() {
		$this.queryTypeSelectPicker.val($this.item.typeId);
		$this.errorSeveritySelectPicker.val($this.item.errorSeverity);

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

