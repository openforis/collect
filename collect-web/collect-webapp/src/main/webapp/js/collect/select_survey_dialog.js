Collect.SelectSurveyDialogController = function() {
	this.surveySummaries = null;
};

Collect.SelectSurveyDialogController.prototype.initEventListeners = function() {
	var $this = this;
	$this.content.find(".apply-btn").click($.proxy(this.applyHandler, $this));
};

Collect.SelectSurveyDialogController.prototype.applyHandler = function() {
	var $this = this;
	var surveyId = $this.surveySelectPicker.val();
	collect.sessionService.setActiveSurvey(surveyId, function() {
		var survey = OpenForis.Arrays.findItem($this.surveySummaries, "id", surveyId);
		collect.activeSurvey = survey;
		$this.close();
	});
};

Collect.SelectSurveyDialogController.prototype.open = function() {
	var $this = this;
	collect.surveyService.loadSummaries(function(summaries) {
		$this.surveySummaries = summaries;

		OpenForis.Async.loadHtml("/collect/datacleansing/survey_select_dialog.html", function(content) {
			$this.content = content;

			var select = $(content).find('.survey-select');
			OpenForis.UI.Form.populateSelect(select, $this.surveySummaries, "id", "projectName", true);
			$this.surveySelectPicker = select.selectpicker();
			
			$this.initEventListeners();

			content.modal('show');
		}, function() {
			collect.error.apply(this, arguments)
		});
	});
};

Collect.SelectSurveyDialogController.prototype.close = function() {
	this.content.modal('hide');
};