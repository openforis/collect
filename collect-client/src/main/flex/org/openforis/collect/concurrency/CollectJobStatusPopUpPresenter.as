package org.openforis.collect.concurrency {
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
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
		
		override protected function removeBroadcastEventListeners():void {
			super.removeBroadcastEventListeners();
			EventDispatcherFactory.getEventDispatcher().removeEventListener(CollectJobEvent.COLLECT_JOB_STATUS_UPDATE, jobStatusUpdateHandler);
		};
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.closeButton.addEventListener(MouseEvent.CLICK, closeHandler);
			view.okButton.addEventListener(MouseEvent.CLICK, closeHandler);
			view.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
		}
		
		override protected function closeHandler(event:Event = null):void {
			var _job:JobProxy = view.job;
			if ( _job != null && ((_job is JobProxy && JobProxy(_job).running))) {
				AlertUtil.showMessage("job.cannot_close_popup_job_still_running");
			} else {
				CollectJobStatusPopUp.closePopUp();
			}
		}

		protected function cancelButtonClickHandler(event:MouseEvent):void {
			var _job:JobProxy = view.job;
			if (_job != null) {
				ClientFactory.collectJobClient.abortJob(_cancelJobResponder, _job.id);
			}
		}
		
		private function cancelJobResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}

		private function jobStatusUpdateHandler(event:CollectJobEvent):void {
			view.job = event.job;
			updateView();
		}
		
		public function updateView():void {
			var job:JobProxy = view.job;
			if (job == null) {
				resetView();
			} else {
				view.title = Message.get(job.name);
				var progress:int = job.progressPercent;
				if ( job.running && progress <= 100 ) {
					view.currentState = CollectJobStatusPopUp.STATE_RUNNING;
					view.progressBar.setProgress(progress, 100);
					view.progressLabel.text = Message.get("job.running");
					view.cancelButton.visible = view.cancelButton.includeInLayout = Application.user.canCancelApplicationLockingJob;
				} else {
					switch ( job.status.name ) {
						case JobProxy$Status.COMPLETED.name:
							view.currentState = CollectJobStatusPopUp.STATE_COMPLETE;
							if (view.autoclose) {
								closeHandler();
							}
							break;
						case JobProxy$Status.FAILED.name:
							AlertUtil.showError("job.error");
							resetView();
							closeHandler();
							break;
						case JobProxy$Status.ABORTED.name:
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