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
					    '<div style="text-align: right">' +
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
	var percentWidth = null;
	var running = false;
	
	switch(job.status) {
	case "PENDING":
		styleName = "progress-bar-info progress-bar-striped";
		percentWidth = 100;
		break;
	case "RUNNING":
		styleName = "progress-bar-info";
		percentWidth = job.progressPercent;
		running = true;
		break;
	case "COMPLETED":
		styleName = "progress-bar-success";
		percentWidth = 100;
		break;
	case "FAILED":
		styleName = "progress-bar-danger";
		percentWidth = 100;
		break;
	case "ABORTED":
		styleName = "progress-bar-warning";
		percentWidth = 100;
		break;
	}
	if (running) {
		this.okBtn.hide();
		this.cancelBtn.show();
	} else {
		this.okBtn.show();
		this.cancelBtn.hide();
	}
	var remainingTimeText;
	if (job.remainingMinutes == null) {
		remainingTimeText = OF.i18n.prop("collect.global.remaining_time.calculating");
	} else if (job.remainingMinutes == 1) {
		remainingTimeText = OF.i18n.prop("collect.global.remaining_time.less_than_one_minute");
	} else {
		remainingTimeText = remainingTimeText = OF.i18n.prop("collect.global.remaining_time.remaining_minutes", job.remainingMinutes);
	}
	$this.progressBarEl.text(percentWidth + "%");
	$this.progressBarEl.css("width", percentWidth + "%");
	$this.progressBarEl.removeClass("progress-bar-info progress-bar-striped");
	$this.progressBarEl.addClass(styleName);
	$this.content.find(".remaining-time-content").text(remainingTimeText);
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
