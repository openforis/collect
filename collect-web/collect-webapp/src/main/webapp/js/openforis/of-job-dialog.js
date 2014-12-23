OF.UI.JobDialog = function(jobRetrievalUrl) {
	this.jobRetrievalUrl = jobRetrievalUrl;
	this.service = new Collect.AbstractService();
	this.job = null;
};

OF.UI.JobDialog.prototype.open = function() {
	var $this = this;
	$this.loadJob(function() {
		
	});
};

OF.UI.JobDialog.prototype.initContent = function() {
	var template = 
		'<div class="dialog">' +
			'<div class="content">' + 
				'<div class="progress-bar progress-bar-info" role="progressbar" aria-valuemin="0" aria-valuemax="100">' +
			    	'<span class="sr-only"></span>' +
			    '</div>' +
			'</div>' +
		'</div>';
	$(template);
};

OF.UI.JobDialog.prototype.loadJob = function(onSuccess) {
	var $this = this;
	$this.service.send($this.jobRetrievalUrl, null, "GET", function(job) {
		$this.job = job;
	});
};