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
	
	this.initView();
};

Collect.prototype.initView = function() {
	var $this = this;
	var versionLabel = $.find(".applicationVersion");
	$(versionLabel).text(Collect.VERSION);
	
	$("#headerLink").click(function(){
		window.open("./", "_self")
	});
	$this.loadViewImages();
};

Collect.prototype.loadViewImages = function() {
	var DOWNLOAD_LOGO_URL = "downloadLogo.htm";

	var loadImage = function ( elId, position, defaultImageUrl ) {
		var imgEl = document.getElementById(elId);
		var tmpImg = new Image();
		tmpImg.onerror = function() {
			imgEl.src = defaultImageUrl;
		};
		tmpImg.onload = function() {
			imgEl.src = this.height > 0 ? this.src : defaultImageUrl;
		};
		tmpImg.src = DOWNLOAD_LOGO_URL + "?position=" + position + "&time=" + new Date().getTime();
	};

	loadImage("headerImg", "header", "assets/images/header.jpg");
	loadImage("logoImg", "top_right", "assets/images/default-logo.png");
	//loadImage("footerImg", "footer", "assets/images/footer.jpg");	
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
		$this.dataCleansingChainDataGrid.refresh();
	});
	EventBus.addEventListener(Collect.DataCleansingStepDialogController.DATA_CLEANSING_STEP_DELETED, function() {
		$this.dataCleansingStepDataGrid.refresh();
		$this.dataCleansingChainDataGrid.refresh();
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
		var selectedItem = $.proxy(getSelectedItem, this)();
		if (selectedItem == null) {
			return;
		}
		var dialogController = new Collect.DataErrorReportViewDialogController();
		dialogController.open(selectedItem);
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
	
	var getPrettyNodeName = function(nodeId) {
		if (! nodeId) {
			return null;
		}
		var survey = collect.activeSurvey;
		var def = survey.getDefinition(nodeId);
		return "[" + def.name + "] " + def.label;
	};
	
	var nodePrettyNameSorter = function (a, b) {
		var aName = getPrettyNodeName(a);
		var bName = getPrettyNodeName(b);
		if (aName == null && bName == null) {
			return 0;
		} else if (aName == null) {
			return -1;
		} else if (bName == null) {
			return 1;
		} else if (aName > bName) {
			return 1;
		} else if (aName < bName) {
			return -1;
		} else {
			return 0;
		}
	};
	
	gridContainer.bootstrapTable({
	    url: "datacleansing/dataqueries/list.json",
	    cache: false,
	    clickToSelect: true,
	    singleSelect: true,
	    detailView: true,
	    detailFormatter: detailFormatter,
	    height: 400,
	    width: "950px",
	    columns: [
	        {field: "id", title: "Id", visible: false},
          	{field: "selected", title: "", radio: true},
			{field: "title", title: "Title", sortable: true, width: "40%"},
			{field: "entityDefinitionId", title: "Entity", sortable: true, width: "20%",  
				sorter: nodePrettyNameSorter, formatter: getPrettyNodeName},
			{field: "attributeDefinitionId", title: "Attribute", sortable: true, width: "20%", 
				sorter: nodePrettyNameSorter, formatter: getPrettyNodeName},
			{field: "creationDate", title: "Creation Date", sortable: true, width: "10%", 
				formatter: OF.Dates.formatToPrettyDateTime},
			{field: "modifiedDate", title: "Modified Date", sortable: true, width: "10%", 
				formatter: OF.Dates.formatToPrettyDateTime}
		]
	});
	
	function detailFormatter(index, query) {
		var html = [];
		html.push('<p><b>Conditions:</b> ' + query.conditions + '</p>');
		html.push('<p><b>Description:</b> ' + query.description + '</p>');
		return html.join('');
	}
	
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
	    height: 400,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title", width: 400, sortable: true},
			{field: "queryTitle", title: "Query Title", width: 400, sortable: true},
			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			{field: "modifiedDate", title: "Modified Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true}
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
	    detailView: true,
	    detailFormatter: detailFormatter,
	    height: 400,
	    onExpandRow: onExpandRow,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "title", title: "Title", width: 800, sortable: true},
			{field: "creationDate", title: "Creation Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true},
			{field: "modifiedDate", title: "Modified Date", formatter: OF.Dates.formatToPrettyDateTime, width: 100, sortable: true}
		]
	});
	$this.dataCleansingChainDataGrid = gridContainer.data('bootstrap.table');
	
	function onExpandRow(index, chain, $rowEl) {
		var table = $rowEl.find("table");
		table.bootstrapTable({
			width: 600,
			columns: [
				{field: "id", title: "Id", visible: false},
				{field: "title", title: "Title", width: 400},
				{field: "queryTitle", title: "Query Title", width: 200}
		    ],
		    data: chain.steps
		});
	};
	
	function detailFormatter(index, chain) {
		var html = 
        	'<fieldset style="margin-left: 60px !important;" ' +
				'class="compact">' +
	        	'<legend>Steps</legend>' +
	        	'<table></table>' +
           '</fieldset>';
        return html;
	}
};

Collect.prototype.initDataErrorTypeGrid = function() {
	var $this = this;
	var gridId = 'dataerrortypegrid';
	var gridContainer = $("#" + gridId);
	gridContainer.bootstrapTable({
	    url: "datacleansing/dataerrortypes/list.json",
	    cache: false,
	    clickToSelect: true,
	    height: 400,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "code", title: "Code", width: "200px", sortable: true},
			{field: "label", title: "Label", width: "400px", sortable: true},
			{field: "description", title: "Description", width: "400px", sortable: true}
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
	    height: 400,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "typeCode", title: "Error Type", sortable: true, width: 50},
			{field: "queryTitle", title: "Query Title", sortable: true, width: 400},
			{field: "queryDescription", title: "Query Description", sortable: false, width: 400}
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
	    height: 400,
	    columns: [
          	{field: "selected", title: "", radio: true},
			{field: "id", title: "Id", visible: false},
			{field: "queryTitle", title: "Query", sortable: true, width: 400},
			{field: "typeCode", title: "Error Type", sortable: true, width: 100},
			{field: "itemCount", title: "Errors found", sortable: true, width: 100},
			{field: "creationDate", title: "Date", formatter: OF.Dates.formatToPrettyDateTime, sortable: true, width: 100}
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
	var message = OF.Strings.firstNotBlank(errorThrown, status, "Internal server error");
	OF.Alerts.showError(message);
};

$(function() {
	collect = new Collect();
	collect.init();
});