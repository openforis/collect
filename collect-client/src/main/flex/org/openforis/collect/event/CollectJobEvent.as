package org.openforis.collect.event
{
	import flash.events.Event;
	
	import org.openforis.concurrency.proxy.JobProxy;
	
	/**
	 * @author S. Ricci
	 */
	public class CollectJobEvent extends Event {
		
		public static const COLLECT_JOB_STATUS_UPDATE:String = "collect_job_status_update";
		public static const COLLECT_JOB_END:String = "collect_job_end";
		private var _job:JobProxy;
		
		public function CollectJobEvent(type:String, job:JobProxy, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
			this._job = job;
		}

		public function get job():JobProxy {
			return _job;
		}

		public function set job(value:JobProxy):void {
			_job = value;
		}

	}
}