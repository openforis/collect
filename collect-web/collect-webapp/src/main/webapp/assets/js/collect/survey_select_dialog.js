Collect.SurveySelectDialogController = function() {
	Collect.AbstractItemEditDialogController.apply(this, arguments);
	this.contentUrl = "datacleansing/survey_select_dialog.html";
	this.surveySummaries = null;
};

Collect.SurveySelectDialogController.getPrettyShortLabel = function(surveySummary) {
	var label = surveySummary.name;
	if (surveySummary.temporary) {
		label += " (temporary)"; 
	}
	return label;
};

Collect.SurveySelectDialogController.getPrettyLabel = function(surveySummary) {
	var label = surveySummary.name;
	if (OF.Strings.isNotBlank(surveySummary.projectName)) {
		label += " - " + surveySummary.projectName;
	}
	if (surveySummary.temporary) {
		label += " (temporary)"; 
	}
	return label;
};

Collect.SurveySelectDialogController.prototype = Object.create(Collect.AbstractItemEditDialogController.prototype);

Collect.SurveySelectDialogController.prototype.loadInstanceVariables = function(callback) {
	var $this = this;
	collect.surveyService.loadSummaries(function(summaries) {
		$this.surveySummaries = summaries;
		callback();
	}, null, collect.loggedUser.id);
};

Collect.SurveySelectDialogController.prototype.initFormElements = function(callback) {
	var $this = this;
	Collect.AbstractItemEditDialogController.prototype.initFormElements.call(this, function() {
		var select = $this.content.find('.survey-select');
		var surveyLabelFunction = function(survey) {
			return Collect.SurveySelectDialogController.getPrettyLabel(survey);
		}
		OF.UI.Forms.populateSelect(select, $this.surveySummaries, "id", surveyLabelFunction, true);
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
		OF.Alerts.showWarning('Please select a survey');
		return false;
	} else {
		return true;
	}
};
