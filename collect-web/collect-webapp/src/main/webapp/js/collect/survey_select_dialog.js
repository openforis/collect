Collect.SurveySelectDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "/collect/datacleansing/survey_select_dialog.html";
	this.surveySummaries = null;
};

Collect.SurveySelectDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.SurveySelectDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	collect.surveyService.loadSummaries(function(summaries) {
		$this.surveySummaries = summaries;
		callback();
	});
};

Collect.SurveySelectDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		var select = $this.content.find('.survey-select');
		OF.UI.Forms.populateSelect(select, $this.surveySummaries, "id", "projectName", true);
		$this.surveySelectPicker = select.selectpicker();
		
		callback();
	});
};

Collect.SurveySelectDialogController.prototype.applyHandler = function() {
	var $this = this;
	if ($this.validateForm()) {
		var surveyId = $this.surveySelectPicker.val();
		collect.sessionService.setActiveSurvey(surveyId, function() {
			var survey = OF.Arrays.findItem($this.surveySummaries, "id", surveyId);
			$this.close();
			collect.setActiveSurvey(survey);
		});
	}
};

Collect.SurveySelectDialogController.prototype.cancelHandler = function() {
	this.close();
};

Collect.SurveySelectDialogController.prototype.validateForm = function() {
	var $this = this;
	var surveyId = $this.surveySelectPicker.val();
	if (surveyId == null || surveyId == '') {
		alert('Please select a survey');
		return false;
	} else {
		return true;
	}
};
