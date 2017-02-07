OF.JobMonitor = function(jobRetrievalUrl, onComplete) {
	this.jobRetrievalUrl = jobRetrievalUrl;
	this.onComplete = onComplete;
	
	this.service = new Collect.AbstractService();
	this.job = null;
	this.timer = null;
	this.init();
};

//Events
OF.JobMonitor.JOB_PROGRESS = "jobProgress";
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
				switch(job.status) {
				case "RUNNING":
					startTimer();
					break;
				case "COMPLETED":
					if ($this.onComplete) {
						$this.onComplete();
					}
					break;
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
		if (job != null) {
			$this.job = job;
			onSuccess();
		} else if ($this.job == null) {
			//job completed immediately
			if ($this.onComplete) {
				$this.onComplete();
			}
		} else {
			//monitoring a survey or application locking job that is completed, retrieve it using job ID
			$this.service.send("job.json", {jobId: $this.job.id}, "GET", function(job) {
				$this.job = job;
				onSuccess();
			});
		}
	});
};

OF.JobMonitor.prototype.cancelJobHandler = function() {
	var $this = this;
	$this.service.send($this.jobRetrievalUrl, null, "DELETE", function() {
		EventBus.removeEventListener(OF.JobMonitor.CANCEL_JOB, $this.cancelJobHandler);
	});
};
