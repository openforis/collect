OF.UI.JobDialog = function() {
	this.job = null;
	this.content = null;
	this.progressBarEl = null;
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
					'<div class="progress-bar" role="progressbar" aria-valuemin="0" aria-valuemax="100" style="height: 15px">' +
				    	'<span class="sr-only"></span>' +
				    '</div>' +
			    '</div>' +
			    '<div class="modal-footer">' + 
			    '</div>' +
		    '</div>' +
		'</div>' +
	'</div>';

OF.UI.JobDialog.prototype.init = function() {
	this.initContent();
	this.content.modal("show");
	
	EventBus.addEventListener(OF.JobMonitor.JOB_PROGRESS, function(event, job) {
		this.updateUI(job);
	}, this);
};

OF.UI.JobDialog.prototype.updateUI = function(job) {
	var $this = this;

	var styleName = null;
	var percentWidth = null;
	
	switch(job.status) {
	case "PENDING":
		styleName = "progress-bar-info progress-bar-striped";
		percentWidth = 100;
		break;
	case "RUNNING":
		styleName = "progress-bar-info";
		percentWidth = job.progressPercent;
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
	$this.progressBarEl.css("width", percentWidth + "%");
	$this.progressBarEl.removeClass("progress-bar-info progress-bar-striped");
	$this.progressBarEl.addClass(styleName);
};

OF.UI.JobDialog.prototype.initContent = function() {
	var $this = this;
	$this.content = $(OF.UI.JobDialog._CONTENT_TEMPLATE);
	$this.progressBarEl = $this.content.find(".progress-bar");
};
