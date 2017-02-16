Collect.DataCleansing = function() {
	
};

Collect.DataCleansing.WORKFLOW_STEPS = [{name: "ENTRY", label: "Data Entry"}, 
	                                    {name: "CLEANSING", label: "Data Cleansing"},
	                                    {name: "ANALYSIS", label: "Data Analysis"}
	                                    ];

Collect.DataCleansing.DATA_QUERY_TYPE_SAVED = "dataQueryTypeSaved";
Collect.DataCleansing.DATA_QUERY_TYPE_DELETED = "dataQueryTypeSaved";
Collect.DataCleansing.DATA_QUERY_SAVED = "dataQuerySaved";
Collect.DataCleansing.DATA_QUERY_DELETED = "dataQueryDeleted";
Collect.DataCleansing.DATA_QUERY_GROUP_SAVED = "dataQueryGroupSaved";
Collect.DataCleansing.DATA_QUERY_GROUP_DELETED = "dataQueryGroupDeleted";
Collect.DataCleansing.DATA_REPORT_CREATED = "dataReportCreated";
Collect.DataCleansing.DATA_REPORT_DELETED = "dataReportDeleted";
Collect.DataCleansing.DATA_CLEANSING_STEP_SAVED = "dataCleansingStepSaved";
Collect.DataCleansing.DATA_CLEANSING_STEP_DELETED = "dataCleansingStepDeleted";
Collect.DataCleansing.DATA_CLEANSING_CHAIN_SAVED = "dataCleansingChainSaved";
Collect.DataCleansing.DATA_CLEANSING_CHAIN_DELETED = "dataCleansingChainDeleted";

Collect.DataCleansing.VIEW_STATE_NO_SURVEY_SELECTED = "noSurveySelected";
Collect.DataCleansing.VIEW_STATE_SURVEY_SELECTED = "surveySelected";
Collect.DataCleansing.VIEW_STATE_TEMPORARY_SURVEY_SELECTED = "temporarySurveySelected";

Collect.DataCleansing.prototype.init = function() {
	this.initDataQueryTypePanel();
	this.initDataQueryPanel();
	this.initDataQueryGroupPanel();
	this.initDataCleansingStepPanel();
	this.initDataCleansingChainPanel();
	this.initDataReportPanel();
	
	this.panels = [this.dataQueryTypePanel, 
	               this.dataQueryPanel, 
	               this.dataQueryGroupPanel, 
	               this.dataQueryGroupPanel, 
	               this.dataReportPanel, 
	               this.dataCleansingStepPanel,
	               this.dataCleansingChainPanel];
	
	this.initGlobalEventHandlers();
	
	this.initView();
};

Collect.DataCleansing.prototype.initView = function() {
	var $this = this;
	var mainNavBar = $("#survey-selected-container").find(".navbar-nav");
	mainNavBar.on("shown.bs.tab", function(evt) {
		var relatedPanel = $(evt.target.hash)[0];
		setTimeout(function() {
			$this.onTabShown(relatedPanel);
		}, 200);
	});
	this.checkViewState();
};

Collect.DataCleansing.prototype.onTabShown = function(targetPanel) {
	var $this = this;
	$.each($this.panels, function(idx, panel) {
		if (panel.$panel.get(0) == targetPanel) {
			panel.onPanelShow();
		}
	});
};

Collect.DataCleansing.prototype.checkViewState = function() {
	var state = null;
	if (collect.activeSurvey == null) {
		state = Collect.DataCleansing.VIEW_STATE_NO_SURVEY_SELECTED;
	} else if (collect.activeSurvey.temporary) {
		state = Collect.DataCleansing.VIEW_STATE_TEMPORARY_SURVEY_SELECTED;
	} else {
		state = Collect.DataCleansing.VIEW_STATE_SURVEY_SELECTED;
	}
	this.changeViewState(state);
};

Collect.DataCleansing.prototype.changeViewState = function(state) {
	switch (state) {
	case Collect.DataCleansing.VIEW_STATE_NO_SURVEY_SELECTED:
		$("#no-survey-selected-container").show();
		$("#survey-selected-container").hide();
		break;
	case Collect.DataCleansing.VIEW_STATE_SURVEY_SELECTED:
		$("#no-survey-selected-container").hide();
		$("#survey-selected-container").show();
		$("#data-report-tab").show();
		break;
	case Collect.DataCleansing.VIEW_STATE_TEMPORARY_SURVEY_SELECTED:
		$("#no-survey-selected-container").hide();
		$("#survey-selected-container").show();
		$("#data-report-tab").hide();
		break;
	};
};

Collect.DataCleansing.prototype.initGlobalEventHandlers = function() {
	var $this = this;
	$("#home-survey-selector-button").click(function() {
		new Collect.SurveySelectDialogController().open();
	});
	EventBus.addEventListener(Collect.SURVEY_CHANGED, function() {
		$.each($this.panels, function(idx, panel) {
			panel.onSurveyChanged();
		});
		$("#selected-survey-label").text(Collect.SurveySelectDialogController.getPrettyShortLabel(collect.activeSurvey));
		
		$this.checkViewState();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_QUERY_TYPE_SAVED, function() {
		$this.dataQueryTypePanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_QUERY_TYPE_DELETED, function() {
		$this.dataQueryTypePanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_QUERY_GROUP_SAVED, function() {
		$this.dataQueryGroupPanel.refreshDataGrid();
		$this.dataReportPanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_QUERY_GROUP_DELETED, function() {
		$this.dataQueryGroupPanel.refreshDataGrid();
		$this.dataReportPanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_REPORT_CREATED, function() {
		$this.dataReportPanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_REPORT_DELETED, function() {
		$this.dataReportPanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_QUERY_SAVED, function() {
		$this.dataQueryPanel.refreshDataGrid();
		$this.dataQueryGroupPanel.refreshDataGrid();
		$this.dataReportPanel.refreshDataGrid();
		$this.dataCleansingStepPanel.refreshDataGrid();
		$this.dataCleansingChainPanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_QUERY_DELETED, function() {
		$this.dataQueryPanel.refreshDataGrid();
		$this.dataQueryGroupPanel.refreshDataGrid();
		$this.dataReportPanel.refreshDataGrid();
		$this.dataCleansingStepPanel.refreshDataGrid();
		$this.dataCleansingChainPanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_CLEANSING_STEP_SAVED, function() {
		$this.dataCleansingStepPanel.refreshDataGrid();
		$this.dataCleansingChainPanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_CLEANSING_STEP_DELETED, function() {
		$this.dataCleansingStepPanel.refreshDataGrid();
		$this.dataCleansingChainPanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_CLEANSING_CHAIN_SAVED, function() {
		$this.dataCleansingChainPanel.refreshDataGrid();
	});
	EventBus.addEventListener(Collect.DataCleansing.DATA_CLEANSING_CHAIN_DELETED, function() {
		$this.dataCleansingChainPanel.refreshDataGrid();
	});
};

Collect.DataCleansing.prototype.initDataQueryTypePanel = function() {
	this.dataQueryTypePanel = new Collect.DataCleansing.DataQueryTypePanelController($("#data-query-type-panel"));
	this.dataQueryTypePanel.init();
};

Collect.DataCleansing.prototype.initDataQueryPanel = function() {
	this.dataQueryPanel = new Collect.DataCleansing.DataQueryPanelController($("#data-query-panel"));
	this.dataQueryPanel.init();
};

Collect.DataCleansing.prototype.initDataQueryGroupPanel = function() {
	this.dataQueryGroupPanel = new Collect.DataCleansing.DataQueryGroupPanelController($("#data-query-group-panel"));
	this.dataQueryGroupPanel.init();
};

Collect.DataCleansing.prototype.initDataReportPanel = function() {
	this.dataReportPanel = new Collect.DataCleansing.DataReportPanelController($("#data-report-panel"));
	this.dataReportPanel.init();
};

Collect.DataCleansing.prototype.initDataCleansingStepPanel = function() {
	this.dataCleansingStepPanel = new Collect.DataCleansing.DataCleansingStepPanelController($("#data-cleansing-step-panel"));
	this.dataCleansingStepPanel.init();
};

Collect.DataCleansing.prototype.initDataCleansingChainPanel = function() {
	this.dataCleansingChainPanel = new Collect.DataCleansing.DataCleansingChainPanelController($("#data-cleansing-chain-panel"));
	this.dataCleansingChainPanel.init();
};

$(function() {
	collect.datacleansing = new Collect.DataCleansing();
	collect.datacleansing.init();
	
	collect.checkActiveSurveySelected();
});