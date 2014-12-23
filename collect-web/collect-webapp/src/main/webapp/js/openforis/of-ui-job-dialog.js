OF.UI.JobDialog = function(jobRetrievalUrl) {
	this.jobRetrievalUrl = jobRetrievalUrl;
	this.service = new Collect.AbstractService();
	this.job = null;
	this.content = null;
	this.progressBarEl = null;
	this.timer = null;
};

OF.UI.JobDialog.prototype.open = function() {
	var $this = this;
	if ($this.content == null) {
		$this.initContent();
	}
	$this.content.modal('show');
	var startTimer = function() {
		$this.timer = setTimeout(function() {
			$this.loadJob(function() {
				$this.updateUI();
				startTimer();
			});
		}, 2000);
	};
	
	startTimer();
};

OF.UI.JobDialog.prototype.updateUI = function() {
	var $this = this;
	var percentWidth;
	switch($this.job.status) {
	case "PENDING":
		styleName = "progress-bar-info progress-bar-striped";
		break;
	case "RUNNING":
		percentWidth = $this.job.progressPercent + '%';
		break;
	case "COMPLETED":
//			"RUNNING"
//			"COMPLETED"
//			"FAILED"
//			"ABORTED"
	default:
		percentWidth = "100%";
		styleName = "progress-bar-info";
	}
	$this.progressBarEl.css("width", percentWidth);
	$this.progressBarEl.removeClass("progress-bar-info progress-bar-striped");
	$this.progressBarEl.addClass(styleName);
};

OF.UI.JobDialog.prototype.initContent = function() {
	var $this = this;
	var template = 
		'<div class="dialog">' +
			'<div class="content">' + 
				'<div class="progress-bar progress-bar-info" role="progressbar" aria-valuemin="0" aria-valuemax="100">' +
			    	'<span class="sr-only"></span>' +
			    '</div>' +
			'</div>' +
		'</div>';
	$this.content = $(template);
	$this.progressBarEl = $this.content.find(".progress-bar");
};

OF.UI.JobDialog.prototype.loadJob = function(onSuccess) {
	var $this = this;
	$this.service.send($this.jobRetrievalUrl, null, "GET", function(job) {
		$this.job = job;
		onSuccess();
	});
};