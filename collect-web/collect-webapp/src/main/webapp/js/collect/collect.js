Collect = function() {
	this.activeSurvey = null;
};

Collect.VERSION = "${PROJECT_VERSION}";

Collect.SURVEY_CHANGED = "surveyChanged";

Collect.prototype.init = function() {
	this.activeSurvey = null;
	this.sessionService = new Collect.SessionService();
	this.surveyService = new Collect.SurveyService();
	this.dataErrorTypeService = new Collect.DataErrorTypeService();
	this.dataQueryService = new Collect.DataQueryService();
	this.dataErrorQueryService = new Collect.DataErrorQueryService();
	this.dataErrorReportService = new Collect.DataErrorReportService();
	this.geoDataService = new Collect.GeoDataService();
	this.dataCleansingStepService = new Collect.DataCleansingStepService();
	this.dataCleansingChainService = new Collect.DataCleansingChainService();
	this.jobService = new Collect.JobService();
	
	this.initDataErrorTypePanel();
	this.initDataQueryPanel();
	this.initDataErrorQueryPanel();
	this.initDataCleansingStepPanel();
	this.initDataCleansingChainPanel();
	this.initDataErrorReportsPanel();
	
	//this.mapPanelComposer = new Collect.DataCleansing.MapPanelComposer($("#map-panel"));
	
	this.initGlobalEventHandlers();
	
	this.checkActiveSurveySelected();
};

Collect.prototype.checkActiveSurveySelected = function() {
	var $this = this;
	var openSurveySelectDialog = function() {
		var surveySelectDialogController = new Collect.SurveySelectDialogController();
		surveySelectDialogController.open(null, true);
	};
	this.sessionService.getActiveSurvey(function(survey) {
		if (survey == null) {
			collect.surveyService.loadSummaries(function(summaries) {
				switch(summaries.length) {
				case 0:
					OF.Alerts.warn("Please define a survey and publish it before using the Data Cleansing Toolkit");
					break;
				case 1:
					var surveySummary = summaries[0];
					collect.sessionService.setActiveSurvey(surveySummary.id, function() {
						collect.setActiveSurvey(surveySummary);
					});
					break;
				default:
					openSurveySelectDialog();
				}
			});
		} else {
			$this.activeSurvey = new Collect.Metamodel.Survey(survey);
			EventBus.dispatch(Collect.SURVEY_CHANGED, $this);
		}
	}, function() {
		openSurveySelectDialog();
	});
};

Collect.prototype.initGlobalEventHandlers = function() {
	var $this = this;
	$("#home-survey-selector-button").click(function() {
		new Collect.SurveySelectDialogController().open();
	});
	EventBus.addEventListener(Collect.SURVEY_CHANGED, function() {
		$this.initDataErrorTypeGrid();
		$this.initDataErrorQueryGrid();
		$this.initDataErrorReportGrid();
		$this.initDataQueryGrid();
		$this.initDataCleansingStepGrid();
		$this.initDataCleansingChainGrid();
		//$this.initMapPanel();
		$("#home-survey-selector-button").text($this.activeSurvey.name);
	});
	EventBus.addEventListener(Collect.DataErrorTypeDialogController.DATA_ERROR_TYPE_SAVED, function() {
		$this.dataErrorTypeDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataErrorTypeDialogController.DATA_ERROR_TYPE_DELETED, function() {
		$this.dataErrorTypeDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_SAVED, function() {
		$this.dataErrorQueryDataGrid.refresh();
		$this.dataErrorReportDataGrid.refresh();
		$this.dataCleansingStepDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_DELETED, function() {
		$this.dataErrorQueryDataGrid.refresh();
		$this.dataErrorReportDataGrid.refresh();
		$this.dataCleansingStepDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataErrorReportDialogController.DATA_ERROR_REPORT_CREATED, function() {
		$this.dataErrorReportDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataErrorReportDialogController.DATA_ERROR_REPORT_DELETED, function() {
		$this.dataErrorReportDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataQueryDialogController.DATA_QUERY_SAVED, function() {
		$this.dataQueryDataGrid.refresh();
		$this.dataErrorQueryDataGrid.refresh();
		$this.dataErrorReportDataGrid.refresh();
		$this.dataCleansingStepDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataQueryDialogController.DATA_QUERY_DELETED, function() {
		$this.dataQueryDataGrid.refresh();
		$this.dataErrorReportDataGrid.refresh();
		$this.dataCleansingStepDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_SAVED, function() {
		$this.dataCleansingStepDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_DELETED, function() {
		$this.dataCleansingStepDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataCleansingChainDialogController.DATA_CLEANSING_CHAIN_SAVED, function() {
		$this.dataCleansingChainDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataCleansingChainDialogController.DATA_CLEANSING_CHAIN_DELETED, function() {
		$this.dataCleansingChainDataGrid.refresh();
	});
};

Collect.prototype.initDataQueryPanel = function() {
	var $this = this;
	var panel = $("#data-query-panel");
	panel.find(".new-btn").click($.proxy(function() {
		var dialogController = new Collect.DataQueryDialogController();
		dialogController.open();
	}, this));
	
	panel.find(".edit-btn").click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		var dialogController = new Collect.DataQueryDialogController();
		dialogController.open(selectedItem);
	}, this));
	
	panel.find(".delete-btn").click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		OF.Alerts.confirm("Do you want to delete this Data Query?", function() {
			collect.dataQueryService.remove(selectedItem.id, function() {
				EventBus.dispatch(Collect.DataQueryDialogController.DATA_QUERY_DELETED, $this);
			});
		});
	}, this));
	
	function getSelectedItem() {
		var $this = this;
		var selections = $this.dataQueryDataGrid.getSelections();
		return selections.length == 0 ? null : selections[0];
	}
};

Collect.prototype.initDataCleansingStepPanel = function() {
	var $this = this;
	var panel = $("#data-cleansing-step-panel");
	panel.find(".new-btn").click($.proxy(function() {
		var dialogController = new Collect.DataCleansingStepDialogController();
		dialogController.open();
	}, this));
	
	panel.find(".edit-btn").click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		var dialogController = new Collect.DataCleansingStepDialogController();
		dialogController.open(selectedItem);
	}, this));
	
	panel.find(".delete-btn").click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		OF.Alerts.confirm("Do you want to delete this Data Cleansing Step?", function() {
			collect.dataCleansingStepService.remove(selectedItem.id, function() {
				EventBus.dispatch(Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_DELETED, $this);
			});
		});
	}, this));
	
	function getSelectedItem() {
		var $this = this;
		var selections = $this.dataCleansingStepDataGrid.getSelections();
		return selections.length == 0 ? null : selections[0];
	}
};

Collect.prototype.initDataCleansingChainPanel = function() {
	var $this = this;
	var panel = $("#data-cleansing-chain-panel");
	
	panel.find(".new-btn").click($.proxy(function() {
		var dialogController = new Collect.DataCleansingChainDialogController();
		dialogController.open();
	}, this));
	
	panel.find(".edit-btn").click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		var dialogController = new Collect.DataCleansingChainDialogController();
		dialogController.open(selectedItem);
	}, this));
	
	panel.find(".delete-btn").click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		OF.Alerts.confirm("Do you want to delete this Data Cleansing Chain?", function() {
			collect.dataCleansingChainService.remove(selectedItem.id, function() {
				EventBus.dispatch(Collect.DataCleansingChainDialogController.DATA_CLEANSING_CHAIN_DELETED, $this);
			});
		});
	}, this));
	
	function getSelectedItem() {
		var $this = this;
		var selections = $this.dataCleansingChainDataGrid.getSelections();
		return selections.length == 0 ? null : selections[0];
	}
};

Collect.prototype.initDataErrorReportsPanel = function() {
	var $this = this;

	$('#new-data-error-report-btn').click($.proxy(function() {
		var dialogController = new Collect.DataErrorReportDialogController();
		dialogController.open();
	}, this));
	
	$('#view-data-error-report-btn').click($.proxy(function() {
		var dialogController = new Collect.DataErrorReportViewDialogController();
		dialogController.open($.proxy(getSelectedItem, this)());
	}, this));
	
	$('#delete-data-error-report-btn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		OF.Alerts.confirm("Do you want to delete this Data Error Report?", function() {
			collect.dataErrorReportService.remove(selectedItem.id, function() {
				EventBus.dispatch(Collect.DataErrorReportDialogController.DATA_ERROR_REPORT_DELETED, $this);
			});
		});
	}, this));
	
	function getSelectedItem() {
		var selections = $this.dataErrorReportDataGrid.getSelections();
		return selections.length == 0 ? null : selections[0];
	}
};

Collect.prototype.initDataErrorTypePanel = function() {
	var $this = this;
	var panel = $("#data-error-type-panel");
	
	panel.find('.new-btn').click($.proxy(function() {
		var dialogController = new Collect.DataErrorTypeDialogController();
		dialogController.open();
	}, this));
	
	panel.find('.edit-btn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		var dialogController = new Collect.DataErrorTypeDialogController();
		dialogController.open(selectedItem);
	}, this));
	
	panel.find('.delete-btn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		OF.Alerts.confirm("Do you want to delete this Data Error Type?", function() {
			collect.dataErrorTypeService.remove(selectedItem.id, function() {
				EventBus.dispatch(Collect.DataErrorTypeDialogController.DATA_ERROR_TYPE_DELETED, $this);
			});
		});
	}, this));
	
	function getSelectedItem() {
		var $this = this;
		var selections = $this.dataErrorTypeDataGrid.getSelections();
		return selections.length == 0 ? null : selections[0];
	}
};

Collect.prototype.initDataErrorQueryPanel = function() {
	$('#new-data-error-query-btn').click($.proxy(function() {
		var dialogController = new Collect.DataErrorQueryDialogController();
		dialogController.open();
	}, this));
	
	$('#edit-data-error-query-btn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		var dialogController = new Collect.DataErrorQueryDialogController();
		dialogController.open(selectedItem);
	}, this));
	
	$('#delete-data-error-query-btn').click($.proxy(function() {
		var $this = this;
		var selectedItem = $.proxy(getSelectedItem, $this)();
		if (selectedItem == null) {
			return;
		}
		OF.Alerts.confirm("Do you want to delete this Data Error Query?", function() {
			collect.dataErrorQueryService.remove(selectedItem.id, function() {
				EventBus.dispatch(Collect.DataErrorQueryDialogController.DATA_ERROR_QUERY_DELETED, $this);
			});
		});
	}, this));
	
	function getSelectedItem() {
		var $this = this;
		var selections = $this.dataErrorQueryDataGrid.getSelections();
		return selections.length == 0 ? null : selections[0];
	}
};

Collect.prototype.initMapPanel = function() {
	this.mapPanelComposer.init();
};

Collect.prototype.initDataQueryGrid = function() {
	var $this = this;
	var gridId = 'dataquerygrid';
	var gridContainer = $("#" + gridId);
	gridContainer.bootstrapTable({
	    url: "datacleansing/dataqueries/list.json",
	    cache: false,
	    clickToSelect: true,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title"},
			{field: "creationDate", title: "Creation Date"},
			{field: "modifiedDate", title: "Modified Date"}
		]
	});
	$this.dataQueryDataGrid = gridContainer.data('bootstrap.table');
};

Collect.prototype.initDataCleansingStepGrid = function() {
	var $this = this;
	var gridId = 'datacleansingstepgrid';
	var gridContainer = $("#" + gridId);
	gridContainer.bootstrapTable({
	    url: "datacleansing/datacleansingsteps/list.json",
	    cache: false,
	    clickToSelect: true,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title"},
			{field: "queryTitle", title: "Query Title"},
			{field: "creationDate", title: "Creation Date"},
			{field: "modifiedDate", title: "Modified Date"}
		]
	});
	$this.dataCleansingStepDataGrid = gridContainer.data('bootstrap.table');
};

Collect.prototype.initDataCleansingChainGrid = function() {
	var $this = this;
	var gridId = 'datacleansingchaingrid';
	var gridContainer = $("#" + gridId);
	gridContainer.bootstrapTable({
	    url: "datacleansing/datacleansingchains/list.json",
	    cache: false,
	    clickToSelect: true,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title"},
			{field: "creationDate", title: "Creation Date"},
			{field: "modifiedDate", title: "Modified Date"}
		]
	});
	$this.dataCleansingChainDataGrid = gridContainer.data('bootstrap.table');
};

Collect.prototype.initDataErrorTypeGrid = function() {
	var $this = this;
	var gridId = 'dataerrortypegrid';
	var gridContainer = $("#" + gridId);
	gridContainer.bootstrapTable({
	    url: "datacleansing/dataerrortypes/list.json",
	    cache: false,
	    clickToSelect: true,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "code", title: "Code"},
			{field: "label", title: "Label"},
			{field: "description", title: "Description"}
		]
	});
	$this.dataErrorTypeDataGrid = gridContainer.data('bootstrap.table');
};

Collect.prototype.initDataErrorQueryGrid = function() {
	var $this = this;
	var gridId = 'dataerrorquerygrid';
	var gridContainer = $("#" + gridId);
	gridContainer.bootstrapTable({
	    url: "datacleansing/dataerrorqueries/list.json",
	    cache: false,
	    clickToSelect: true,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "typeCode", title: "Error Type"},
			{field: "queryTitle", title: "Query Title"},
			{field: "queryDescription", title: "Query Description"},
			{field: "creationDate", title: "Creation Date"},
			{field: "modifiedDate", title: "Modified Date"}
		]
	});
	$this.dataErrorQueryDataGrid = gridContainer.data('bootstrap.table');
};

Collect.prototype.initDataErrorReportGrid = function() {
	var $this = this;
	var gridId = 'data-error-report-grid';
	var gridContainer = $("#" + gridId);
	gridContainer.bootstrapTable({
	    url: "datacleansing/dataerrorreports/list.json",
	    cache: false,
	    clickToSelect: true,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "queryTitle", title: "Query"},
			{field: "typeCode", title: "Error Type"},
			{field: "creationDate", title: "Date"}
		]
	});
	$this.dataErrorReportDataGrid = gridContainer.data('bootstrap.table');
};

Collect.prototype.setActiveSurvey = function(surveySummary) {
	var $this = this;
	$this.surveyService.loadById(surveySummary.id, function(survey) {
		$this.activeSurvey = new Collect.Metamodel.Survey(survey);
		EventBus.dispatch(Collect.SURVEY_CHANGED, $this);
	});
};

Collect.prototype.error = function(jqXHR, status, errorThrown) {
	OF.Alerts.showError(status);
};

$(function() {
	collect = new Collect();
	collect.init();
});