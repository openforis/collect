OF.UI.JobDialog = function(doNotAllowCancel) {
	this.doNotAllowCancel = doNotAllowCancel;
	this.job = null;
	this.content = null;
	this.progressBarEl = null;
	this.okBtn = null;
	this.cancelBtn = null;
	this.init();
};

OF.UI.JobDialog._CONTENT_TEMPLATE =
	'<div class="modal fade">' +
		'<div class="modal-dialog">' +
			'<div class="modal-content">' +
				'<div class="modal-header">' +
					'<h4 class="modal-title">Process status</h4>' +
				'</div>' +
				'<div class="modal-body">' +
					'<div class="row">' +
						'<div class="progress-bar" role="progressbar" aria-valuemin="0" aria-valuemax="100"' + 
							' style="min-width: 2em; height: 22px;">' +
						'</div>' +
					'</div>' +
					'<div class="row">' +
						'<div class="col-sm-6">' +
						    '<label data-i180n="collect.global.elapsed_time.label">Elapsed time</label>' +
					    	'<label> : </label>' +
					    	'<label class="elapsed-time-content"></label>' +
					    '</div>' +
				    	'<div style="text-align: right" id="remaining-time-container" class="col-sm-6">' +
					    	'<label data-i180n="collect.global.remaining_time.label">Remaining time</label>' +
					    	'<label> : </label>' +
					    	'<label class="remaining-time-content"></label>' +
					    '</div>' +
				    '</div>' +
			    '</div>' +
			    '<div class="modal-footer">' + 
			    	'<button type="button" class="btn btn-primary ok-btn">Ok</button>' +
			    	'<button type="button" class="btn btn-default cancel-btn">Cancel</button>' +
			    '</div>' +
		    '</div>' +
		'</div>' +
	'</div>';

OF.UI.JobDialog.prototype.init = function() {
	var $this = this;
	this.initContent();
	if (this.doNotAllowCancel) {
		this.cancelBtn.hide();
	} else {
		this.cancelBtn.click(function() {
			EventBus.dispatch(OF.JobMonitor.CANCEL_JOB);
			$this.close();
		});
	}
	this.okBtn.click(function() {
		$this.close();
	});
	this.content.modal({backdrop: "static", keyboard: false});
	
	EventBus.addEventListener(OF.JobMonitor.JOB_PROGRESS, function(event, job) {
		this.updateUI(job);
	}, this);
};

OF.UI.JobDialog.prototype.updateUI = function(job) {
	var $this = this;

	var styleName = null;
	var completionPercent = null;
	var running = false;
	
	switch(job.status) {
	case "PENDING":
		styleName = "progress-bar-info progress-bar-striped";
		completionPercent = 100;
		break;
	case "RUNNING":
		styleName = "progress-bar-info";
		completionPercent = job.progressPercent;
		running = true;
		break;
	case "COMPLETED":
		styleName = "progress-bar-success";
		completionPercent = 100;
		break;
	case "FAILED":
		styleName = "progress-bar-danger";
		completionPercent = 100;
		break;
	case "ABORTED":
		styleName = "progress-bar-warning";
		completionPercent = 100;
		break;
	}
	if (running) {
		this.okBtn.hide();
		this.cancelBtn.show();
	} else {
		this.okBtn.show();
		this.cancelBtn.hide();
	}
	$this.progressBarEl.text(completionPercent + "%");
	$this.progressBarEl.css("width", completionPercent + "%");
	$this.progressBarEl.removeClass("progress-bar-info progress-bar-striped");
	$this.progressBarEl.addClass(styleName);
	if (job.status == "RUNNING") {
		$this.content.find(".remaining-time-content").text($this._getRemainingTimeText(job.remainingMinutes));
	} else {
		$this.content.find("#remaining-time-container").hide();
	}
	$this.content.find(".elapsed-time-content").text($this._getElapsedTimeText(job.elapsedTime));
};

OF.UI.JobDialog.prototype._getElapsedTimeText = function(elapsedTime) {
	if (elapsedTime == null) {
		return OF.i18n.prop("collect.global.remaining_time.calculating");
	} else {
		var elapsedSeconds = OF.Strings.leftPad("" + (Math.floor(elapsedTime / 1000) % 60), "0", 2);
		var elapsedMinutes = Math.floor(elapsedTime / 60000);
		return elapsedMinutes + ":" + elapsedSeconds;
	}
}

OF.UI.JobDialog.prototype._getRemainingTimeText = function(remainingMinutes) {
	if (remainingMinutes == null) {
		return OF.i18n.prop("collect.global.remaining_time.calculating");
	} else if (remainingMinutes <= 1) {
		return OF.i18n.prop("collect.global.remaining_time.less_than_one_minute");
	} else {
		return OF.i18n.prop("collect.global.remaining_time.remaining_minutes", remainingMinutes);
	}
};

OF.UI.JobDialog.prototype.initContent = function() {
	this.content = $(OF.UI.JobDialog._CONTENT_TEMPLATE);
	this.progressBarEl = this.content.find(".progress-bar");
	this.cancelBtn = this.content.find(".cancel-btn");
	this.okBtn = this.content.find(".ok-btn");
	this.okBtn.hide();
	this.cancelBtn.hide();
};

OF.UI.JobDialog.prototype.close = function() {
	this.content.modal('hide');
	this.content.remove();
};
