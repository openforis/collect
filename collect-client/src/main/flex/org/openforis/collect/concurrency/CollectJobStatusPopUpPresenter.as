package org.openforis.collect.concurrency {
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.CollectJobEvent;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.presenter.CollectJobMonitor;
	import org.openforis.collect.presenter.PopUpPresenter;
	import org.openforis.collect.remoting.service.concurrency.proxy.SurveyLockingJobProxy;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.concurrency.proxy.JobProxy;
	import org.openforis.concurrency.proxy.JobProxy$Status;
	
	public class CollectJobStatusPopUpPresenter extends PopUpPresenter {
		
		private var _cancelJobResponder:AsyncResponder;
		
		public function CollectJobStatusPopUpPresenter(view:CollectJobStatusPopUp) {
			super(view);
			_cancelJobResponder = new AsyncResponder(cancelJobResultHandler, faultHandler);
		}
		
		private function get view():CollectJobStatusPopUp {
			return CollectJobStatusPopUp(_view);
		}
		
		override public function init():void {
			super.init();
			updateView();
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
			EventDispatcherFactory.getEventDispatcher().addEventListener(CollectJobEvent.COLLECT_JOB_STATUS_UPDATE, jobStatusUpdateHandler);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.closeButton.addEventListener(MouseEvent.CLICK, closeHandler);
			view.okButton.addEventListener(MouseEvent.CLICK, closeHandler);
			view.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
		}
		
		override protected function closeHandler(event:Event = null):void {
			var _job:JobProxy = CollectJobMonitor.currentJob;
			if ( _job != null && ((_job is JobProxy && JobProxy(_job).running))) {
				AlertUtil.showMessage("job.cannot_close_popup_job_still_running");
			} else {
				CollectJobStatusPopUp.closePopUp();
			}
		}

		protected function cancelButtonClickHandler(event:MouseEvent):void {
			var _job:JobProxy = CollectJobMonitor.currentJob;
			if (_job != null) {
				if (_job is SurveyLockingJobProxy) {
					ClientFactory.collectJobClient.abortSurveyJob(_cancelJobResponder, SurveyLockingJobProxy(_job).surveyId);
				} else {
					ClientFactory.collectJobClient.abortApplicationJob(_cancelJobResponder);
				}
			}
		}
		
		private function cancelJobResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}

		private function jobStatusUpdateHandler(event:CollectJobEvent):void {
			updateView();
		}
		
		private function updateView():void {
			var _job:JobProxy = CollectJobMonitor.currentJob;
			if (_job == null) {
				resetView();
			} else {
				var job:JobProxy = _job;
				view.title = Message.get(job.name);
				var progress:int = job.progressPercent;
				if ( job.running && progress <= 100 ) {
					view.currentState = CollectJobStatusPopUp.STATE_RUNNING;
					view.progressBar.setProgress(progress, 100);
					var progressText:String = Message.get("job.running");
					view.progressLabel.text = progressText;
				} else {
					switch ( job.status ) {
						case JobProxy$Status.COMPLETED:
							view.currentState = CollectJobStatusPopUp.STATE_COMPLETE;
							if (view.autoclose) {
								closeHandler();
							}
							break;
						case JobProxy$Status.FAILED:
							AlertUtil.showError("job.error");
							resetView();
							closeHandler();
							break;
						case JobProxy$Status.ABORTED:
							resetView();
							closeHandler();
							break;
						default:
							//process starting in a while...
					}
				}
			}
		}
		
		protected function resetView():void {
			view.currentState = CollectJobStatusPopUp.STATE_LOADING;
		}
		
	}
}