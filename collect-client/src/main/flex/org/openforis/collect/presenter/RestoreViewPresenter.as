package org.openforis.collect.presenter {
	
	import flash.events.DataEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.MouseEvent;
	import flash.events.ProgressEvent;
	import flash.events.TimerEvent;
	import flash.net.FileFilter;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;
	import flash.utils.Timer;
	
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Proxy;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.ui.component.RestoreView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.concurrency.proxy.JobProxy;
	import org.openforis.concurrency.proxy.JobProxy$Status;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class RestoreViewPresenter extends AbstractPresenter {
		
		private static const PROGRESS_DELAY:int = 2000;
		private static const ALLOWED_IMPORT_FILE_EXTENSIONS:Array = new Array("*.collect-data", "*.collect-backup");
		
		protected var _fileReference:FileReference;
		protected var _fileFilter:FileFilter;
		private var _cancelResponder:IResponder;
		private var _restoreResponder:IResponder;
		private var _getStateResponder:IResponder;
		private var _progressTimer:Timer;
		private var _type:String;
		private var _job:Proxy;
		private var _firstOpen:Boolean = true;
		private var _jobLockId:String;
		
		public function RestoreViewPresenter(view:RestoreView) {
			super(view);
			this._restoreResponder = new AsyncResponder(restoreResultHandler, faultHandler);
			this._cancelResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			this._getStateResponder = new AsyncResponder(getStateResultHandler, faultHandler);
			
			_fileFilter = new FileFilter("Collect Backup files", ALLOWED_IMPORT_FILE_EXTENSIONS.join("; "));
			_fileReference = new FileReference();
		}
		
		private function get view():RestoreView {
			return RestoreView(_view);
		}
		
		override public function init():void {
			super.init();
			initView();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			view.selectFileButton.addEventListener(MouseEvent.CLICK, selectFileButtonClickHandler);
			view.restoreButton.addEventListener(MouseEvent.CLICK, restoreButtonClickHandler);
			view.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
			view.restoreAnotherFileButton.addEventListener(MouseEvent.CLICK, restoreAnotherFileHandler);
			
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
		}
		
		/*override protected function closeHandler(event:Event = null):void {
			if ( _job != null && _job is JobProxy && JobProxy(_job).running ) {
				AlertUtil.showMessage("restore.cannotClosePopUp");
			} else {
				PopUpManager.removePopUp(view);
			}
		}*/
		
		private function selectFileButtonClickHandler(event:MouseEvent):void {
			_fileReference.browse([_fileFilter]);
		}
		
		protected function restoreButtonClickHandler(event:MouseEvent):void {
			if ( ! validateForm() ) {
				return;
			}
			AlertUtil.showConfirm("Data will be restored, continue?", null, 
				"Confirm data restore",
				startUpload);
		}
		
		private function validateForm():Boolean {
			if (StringUtil.isBlank(view.selectedFileName.text)) {
				AlertUtil.showError("restore.error.select_file");
				return false;
			}
			return true;
		}
		
		protected function cancelButtonClickHandler(event:MouseEvent):void {
			ClientFactory.dataImportClient.cancel(_cancelResponder);
		}
		
		private function restoreAnotherFileHandler(event:MouseEvent):void {
			resetView();
		}
		
		protected function restoreResultHandler(event:ResultEvent, token:Object = null):void {
			_job = event.result as Proxy;
			updateView();
		}
		
		protected function startProgressTimer():void {
			if ( _progressTimer == null ) {
				_progressTimer = new Timer(PROGRESS_DELAY);
				_progressTimer.addEventListener(TimerEvent.TIMER, progressTimerHandler);
			}
			_progressTimer.start();
		}
		
		protected function stopProgressTimer():void {
			if ( _progressTimer != null ) {
				_progressTimer.stop();
				_progressTimer = null;
			}
		}
		
		protected function progressTimerHandler(event:TimerEvent):void {
			updateRestoreState();
		}
		
		protected function updateRestoreState():void {
			if (_jobLockId != null) {
				ClientFactory.collectJobClient.getJob(_getStateResponder, _jobLockId);
			} else {
				resetView();
			}
		}
		
		protected function cancelResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}
		
		protected function getStateResultHandler(event:ResultEvent, token:Object = null):void {
			_job = event.result as Proxy;
			updateView();
		}
		
		protected function updateView():void {
			if ( _job != null ) {
				var job:JobProxy = _job as JobProxy;
				var progress:int = job.progressPercent;
				if ( job.running && progress <= 100 ) {
					view.currentState = RestoreView.STATE_PROCESSING;
					view.progressBar.setProgress(progress, 100);
					var progressText:String = Message.get("global.processing");
					view.progressLabel.text = progressText;
					if ( _progressTimer == null ) {
						startProgressTimer();
					}
				} else if ( _firstOpen ) {
					resetView();
				} else {
					switch ( job.status ) {
					case JobProxy$Status.COMPLETED:
						view.currentState = RestoreView.STATE_COMPLETE;
						stopProgressTimer();
						break;
					case JobProxy$Status.FAILED:
						AlertUtil.showError("restore.error");
						resetView();
						break;
					case JobProxy$Status.ABORTED:
						AlertUtil.showError("restore.cancelled");
						resetView();
						break;
					default:
						//process starting in a while...
						startProgressTimer();
					}
				}
			} else {
				resetView();
			}
			_firstOpen = false;
		}
		
		protected function resetView():void {
			stopProgressTimer();
			_job = null;
			view.currentState = RestoreView.STATE_PARAMETER_SELECTION;
			checkEnabledFields();
		}
		
		protected function initView():void {
			populateForm();
			
			view.currentState = RestoreView.STATE_PARAMETER_SELECTION;

			//try to see if there is an export still running
			updateRestoreState();
		}
		
		protected function checkEnabledFields():void {
		}
		
		protected function populateForm():void {
		}
		
		protected function fileReferenceSelectHandler(event:Event):void {
			view.selectedFileName.text = FileReference(event.target).name;
		}
		
		private function fileReferenceProgressHandler(event:ProgressEvent):void {
			view.progressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		private function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			view.currentState = RestoreView.STATE_LOADING;
			
			_jobLockId = event.data;
			
			updateRestoreState();
			startProgressTimer();
		}
		
		private function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			AlertUtil.showError("Error uploading file", [event.text]);
			view.currentState = RestoreView.STATE_PARAMETER_SELECTION;
		}
		
		private function startUpload():void {
			updateViewForUploading();
			
			//workaround for firefox/chrome flahplayer bug
			//url +=";jsessionid=" + Application.sessionId;
			
			var restoreUrl:String = "/collect/survey-data/restore.json";
			var request:URLRequest = new URLRequest(restoreUrl);
			//request paramters
			request.method = URLRequestMethod.POST;
			
			request.data = new URLVariables();
			request.data.name = _fileReference.name;
			
			_fileReference.upload(request, "fileData");
		}
		
		protected function updateViewForUploading():void {
			view.currentState = RestoreView.STATE_UPLOADING;
		}
		

	}
}