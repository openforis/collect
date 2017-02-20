OF.Batch = function() {};

OF.Batch.BatchProcessor = function(totalItems, batchSize, processFn, interval) {
	this.totalItems = totalItems;
	this.batchSize = batchSize;
	this.processFn = processFn;
	this.blocks = Math.ceil(totalItems / batchSize);
	this.nextBlockIndex = 0;
	this.progressPercent = 0;
	this.running = false;
}

OF.Batch.BatchProcessor.prototype = {
	start : function() {
		var $this = this;
		this.running = true;
		$this.processNextIfPossible();
	},
	stop : function() {
		this.running = false;
		if ($this.timeout != null) {
			clearInterval($this.timeout);
		}
	},
	processNext : function() {
		var $this = this;
		$this.progressPercent = Math.floor((100 * ($this.nextBlockIndex + 1)) / $this.blocks);
		var blockOffset = $this.nextBlockIndex * $this.batchSize;
		$this.processFn(blockOffset);
		$this.nextBlockIndex++;
	},
	processNextIfPossible : function() {
		if (this.nextBlockIndex < this.blocks) {
			this.processNext();
		} else {
			this.running = false;
		}
	}
};