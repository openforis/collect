OF.Batch = function() {};

OF.Batch.BatchProcessor = function(totalItems, batchSize, processFn, interval) {
	this.totalItems = totalItems;
	this.batchSize = batchSize;
	this.processFn = processFn;
	this.blocks = Math.ceil(totalItems / batchSize);
	this.nextBlockIndex = 0;
	this.progressPercent = 0;
	this.running = false;
	this.interval = OF.Objects.defaultIfNull(interval, 2000); //interval in ms
	this.timeout = null;
}

OF.Batch.BatchProcessor.prototype = {
	start : function() {
		this.running = true;
		this.processNextIfPossible();
	},
	stop : function() {
		this.running = false;
		if ($this.timeout != null) {
			clearTimeout($this.timeout);
		}
	},
	processNext : function() {
		var $this = this;
		$this.progressPercent = Math.floor((100 * ($this.nextBlockIndex + 1)) / $this.blocks);
		var blockOffset = $this.nextBlockIndex * $this.batchSize;
		$this.processFn(blockOffset);
		
		if ($this.running) {
			$this.timeout = setTimeout(function() {
				$this.nextBlockIndex++;
				$this.processNextIfPossible();
			}, $this.interval);
		}
	},
	processNextIfPossible : function() {
		if (this.nextBlockIndex < this.blocks) {
			this.processNext();
		} else {
			this.running = false;
		}
	}
};