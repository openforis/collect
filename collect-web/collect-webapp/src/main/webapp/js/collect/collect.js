Collect = function() {
	this.activeSurvey = null;
	this.sessionService = new Collect.SessionService();
	this.surveyService = new Collect.SurveyService();
	this.dataService = new Collect.DataService();
	this.dataQueryTypeService = new Collect.DataQueryTypeService();
	this.dataQueryService = new Collect.DataQueryService();
	this.dataQueryGroupService = new Collect.DataQueryGroupService();
	this.dataReportService = new Collect.DataReportService();
	this.geoDataService = new Collect.GeoDataService();
	this.dataCleansingStepService = new Collect.DataCleansingStepService();
	this.dataCleansingStepValueService = new Collect.DataCleansingStepValueService();
	this.dataCleansingChainService = new Collect.DataCleansingChainService();
	this.jobService = new Collect.JobService();
};

Collect.VERSION = "PROJECT_VERSION";

Collect.SURVEY_CHANGED = "surveyChanged";

Collect.prototype.init = function() {
	this.initI18n();
	
	this.initGlobalEventHandlers();
	
	this.initView();
};

Collect.prototype.initI18n = function() {
	jQuery.i18n.properties({
	    name:'messages', 
	    path:'assets/bundle/', 
	    mode:'both', // We specified mode: 'both' so translated values will be available as JS vars/functions and as a map
	});
	
	OF.i18n.initializeAll();
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
	loadImage("footerImg", "footer", "assets/images/footer.jpg");
};

Collect.prototype.checkActiveSurveySelected = function() {
	var $this = this;
	var openSurveySelectDialog = function() {
		var surveySelectDialogController = new Collect.SurveySelectDialogController();
		surveySelectDialogController.open();
	};
	this.sessionService.getActiveSurvey(function(survey) {
		if (survey == null) {
			collect.surveyService.loadSummaries(function(summaries) {
				switch(summaries.length) {
				case 0:
					OF.Alerts.warn("Please define a survey before using the Data Cleansing Toolkit");
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