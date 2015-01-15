OF.JobMonitor = function(jobRetrievalUrl) {
	this.jobRetrievalUrl = jobRetrievalUrl;
	this.service = new Collect.AbstractService();
	this.job = null;
	this.timer = null;
	this.init();
};

//Events
OF.JobMonitor.JOB_PROGRESS = "jobProgress";
//Events
OF.JobMonitor.CANCEL_JOB = "cancelJob";
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
	
	EventBus.addEventListener(OF.JobMonitor.CANCEL_JOB, $.proxy($this.cancelJobHandler, $this));
};

OF.JobMonitor.prototype.loadJob = function(onSuccess) {
	var $this = this;
	$this.service.send($this.jobRetrievalUrl, null, "GET", function(job) {
		$this.job = job;
		onSuccess();
	});
};

OF.JobMonitor.prototype.cancelJobHandler = function() {
	var $this = this;
	$this.service.send($this.jobRetrievalUrl, null, "DELETE", function() {
		EventBus.removeEventListener(OF.JobMonitor.CANCEL_JOB, $this.cancelJobHandler);
	});
};
