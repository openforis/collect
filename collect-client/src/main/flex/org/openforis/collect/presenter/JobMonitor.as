package org.openforis.collect.presenter {
	import flash.events.EventDispatcher;
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.concurrency.CollectJobStatusPopUp;
	import org.openforis.collect.event.CollectJobEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.remoting.service.concurrency.proxy.ApplicationLockingJobProxy;
	import org.openforis.concurrency.proxy.JobProxy;
	import org.openforis.concurrency.proxy.JobProxy$Status;

	/**
	 * @author S. Ricci
	 */
	public class JobMonitor extends AbstractPresenter {
		
		private static const STATUS_UPDATE_DELAY:int = 2000;
		
		private var _progressTimer:Timer;
		private var jobId:String;
		private var _job:JobProxy;

		public function JobMonitor(jobId:String) {
			super(null);
			this.jobId = jobId;
		}
		
		public function start():void {
			if ( _progressTimer == null ) {
				_progressTimer = new Timer(STATUS_UPDATE_DELAY);
				_progressTimer.addEventListener(TimerEvent.TIMER, progressTimerHandler);
			}
			_progressTimer.start();
		}
		
		public function get currentJob():JobProxy {
			return _job;
		}
		
		protected function stop():void {
			if ( _progressTimer != null ) {
				_progressTimer.stop();
				_progressTimer = null;
			}
		}
		
		protected function progressTimerHandler(event:TimerEvent):void {
			loadCurrentJobAndUpdateState();
		}
		
		private function loadCurrentJobAndUpdateState():void {
			function onComplete():void {
				if (_job == null || ! _job.running) {
					stop();
				}
				if (CollectJobStatusPopUp.popUpOpen) {
					CollectJobStatusPopUp.setActiveJob(_job);
				} else {
					CollectJobStatusPopUp.openPopUp(_job);
				}
				dispatchJobUpdateEvent();
			}
			loadJob(function():void {
				onComplete();
			});
		}
		
		private function loadJob(complete:Function):void {
			ClientFactory.collectJobClient.getJob(new AsyncResponder(
				function(event:ResultEvent, token:Object = null):void {
					_job = event.result as JobProxy;
					complete();
				}, faultHandler), jobId);
		}
		
		private function dispatchJobUpdateEvent():void {
			if (_job != null) {
				eventDispatcher.dispatchEvent(new CollectJobEvent(CollectJobEvent.COLLECT_JOB_STATUS_UPDATE, _job));
				if (_job.completed || _job.aborted || _job.failed) {
					eventDispatcher.dispatchEvent(new CollectJobEvent(CollectJobEvent.COLLECT_JOB_END, _job));
				}
			}
		}
	}
}