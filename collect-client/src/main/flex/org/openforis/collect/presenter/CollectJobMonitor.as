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
	public class CollectJobMonitor extends AbstractPresenter {
		
		private static const STATUS_UPDATE_DELAY:int = 2000;
		
		private var _progressTimer:Timer;
		private static var _job:JobProxy;

		public function CollectJobMonitor() {
			super(null);
		}
		
		public function startProgressTimer():void {
			if ( _progressTimer == null ) {
				_progressTimer = new Timer(STATUS_UPDATE_DELAY);
				_progressTimer.addEventListener(TimerEvent.TIMER, progressTimerHandler);
			}
			_progressTimer.start();
		}
		
		public static function get currentJob():JobProxy {
			return _job;
		}
		
		protected function stopProgressTimer():void {
			if ( _progressTimer != null ) {
				_progressTimer.stop();
				_progressTimer = null;
			}
		}
		
		protected function progressTimerHandler(event:TimerEvent):void {
			loadCurrentJobAndUpdateState();
		}
		
		private function loadCurrentJobAndUpdateState():void {
			var oldJob:JobProxy = _job;
			function onComplete():void {
				if (_job != null && (oldJob == null || oldJob.id != _job.id || oldJob.status != _job.status || oldJob.running)) {
					CollectJobStatusPopUp.openPopUp();
					dispatchJobUpdateEvent();
				}
			}
			if (_job == null) {
				loadApplicationJob(function():void {
					if (_job == null && Application.activeSurvey != null) {
						loadSurveyJob(function():void {
							onComplete();
						});
					} else {
						onComplete();
					}
				});
			} else if (_job is ApplicationLockingJobProxy) {
				loadApplicationJob(function():void {
					onComplete();
				});
			} else if (Application.activeSurvey != null) {
				loadSurveyJob(function():void {
					onComplete();
				});
			}
		}
		
		private function loadApplicationJob(complete:Function):void {
			ClientFactory.collectJobClient.getApplicationJob(new AsyncResponder(
				function(event:ResultEvent, token:Object = null):void {
					_job = event.result as JobProxy;
					complete();
				}, faultHandler
			));
		}
		
		private function loadSurveyJob(complete:Function):void {
			ClientFactory.collectJobClient.getSurveyJob(new AsyncResponder(
				function(event:ResultEvent, token:Object = null):void {
					_job = event.result as JobProxy;
					complete();
				}, faultHandler
			), Application.activeSurvey.id);
		}

		private function dispatchJobUpdateEvent():void {
			if (_job != null) {
				eventDispatcher.dispatchEvent(new CollectJobEvent(CollectJobEvent.COLLECT_JOB_STATUS_UPDATE, _job));
				if (_job.completed) {
					eventDispatcher.dispatchEvent(new CollectJobEvent(CollectJobEvent.COLLECT_JOB_COMPLETE, _job));
				}
			}
		}
	}
}