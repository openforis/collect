OF.JobMonitor = function(jobRetrievalUrl) {
	this.jobRetrievalUrl = jobRetrievalUrl;
	this.service = new Collect.AbstractService();
	this.job = null;
	this.timer = null;
	this.init();
};

//Events
OF.JobMonitor.JOB_PROGRESS = "jobProgress";
//internal
OF.JobMonitor._JOB_UPDATE_INTERVAL = 2000;

OF.JobMonitor.prototype.init = function() {
	var $this = this;
	var startTimer = function() {
		$this.timer = setTimeout(function() {
			$this.loadJob(function() {
				var job = $this.job;
				EventBus.dispatch(OF.JobMonitor.JOB_PROGRESS, $this, job);
				if (job.status == "RUNNING") {
					startTimer();
				}
			});
		}, OF.JobMonitor._JOB_UPDATE_INTERVAL);
	};
	startTimer();
};

OF.JobMonitor.prototype.loadJob = function(onSuccess) {
	var $this = this;
	$this.service.send($this.jobRetrievalUrl, null, "GET", function(job) {
		$this.job = job;
		onSuccess();
	});
};