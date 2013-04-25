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
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.PaginationBarEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.manager.process.ProcessStatus$Step;
	import org.openforis.collect.manager.referencedataimport.proxy.ReferenceDataImportStatusProxy;
	import org.openforis.collect.ui.component.datagrid.PaginationBar;
	import org.openforis.collect.ui.view.AbstractReferenceDataImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.CollectionUtil;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class AbstractReferenceDataImportPresenter extends AbstractPresenter {
		
		private static const PROGRESS_UPDATE_DELAY:int = 2000;
		private static const VALID_NAME_REGEX:RegExp = /^[a-z][a-z0-9_]*$/;
		private static const ALLOWED_IMPORT_FILE_EXTENSIONS:Array = ["*.csv"];
		private static const MAX_SUMMARIES_PER_PAGE:int = 20;
		private static const FIXED_SUMMARY_COLUMNS_LENGTH:int = 3;
		private static const VERANCULAR_NAMES_SEPARATOR:String = ", ";
		
		protected var _view:AbstractReferenceDataImportView;
		protected var _fileReference:FileReference;
		protected var _progressTimer:Timer;
		protected var _state:ReferenceDataImportStatusProxy;
		protected var _messageKeys:ReferenceDataImportMessageKeys;
		protected var _uploadUrl:String;
		protected var _uploadFileNamePrefix:String;
		
		protected var _getStatusResponder:IResponder;
		protected var _firstOpen:Boolean;
		protected var _fileFilter:FileFilter;
		
		public function AbstractReferenceDataImportPresenter(view:AbstractReferenceDataImportView, messageKeys:ReferenceDataImportMessageKeys, uploadFileNamePrefix:String) {
			this._view = view;
			this._messageKeys = messageKeys;
			
			_uploadUrl = ApplicationConstants.FILE_UPLOAD_URL;
			_uploadFileNamePrefix = uploadFileNamePrefix;
			_firstOpen = true;
			_fileReference = new FileReference();
			initFileFilter();
			
			_getStatusResponder = new AsyncResponder(getStatusResultHandler, faultHandler);
			
			super();
			
			//try to see if there is a still running process
			_view.currentState = AbstractReferenceDataImportView.STATE_LOADING;
			if ( _view.paginationBar != null ) {
				_view.paginationBar.maxRecordsPerPage = MAX_SUMMARIES_PER_PAGE;
			}
			loadInitialData();
		}
		
		protected function loadInitialData():void {
			updateStatus();
			loadSummaries();
		}
		
		private function initFileFilter():void {
			var description:String = ALLOWED_IMPORT_FILE_EXTENSIONS.join(", ");
			_fileFilter = new FileFilter(description, ALLOWED_IMPORT_FILE_EXTENSIONS.join("; "));
		}
		
		override internal function initEventListeners():void {
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			_view.importButton.addEventListener(MouseEvent.CLICK, importButtonClickHandler);
			if (_view.exportButton != null ) {
				_view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			}
			_view.cancelImportButton.addEventListener(MouseEvent.CLICK, cancelClickHandler);
			if ( _view.paginationBar != null ) {
				_view.paginationBar.addEventListener(PaginationBarEvent.PAGE_CHANGE, summaryPageChangeHandler);
			}
			_view.errorsOkButton.addEventListener(MouseEvent.CLICK, errorsOkButtonClickHandler);
			_view.closeButton.addEventListener(MouseEvent.CLICK, closeButtonClickHandler);
		}
		
		protected function summaryPageChangeHandler(event:PaginationBarEvent):void {
			loadSummaries(event.offset);
		}
		
		protected function loadSummaries(offset:int = 0):void {
			if ( offset == 0 ) {
				_view.paginationBar.showPage(1);
			}
			_view.summaryDataGrid.dataProvider = null;
			
			_view.paginationBar.currentState = PaginationBar.LOADING_STATE;
			performSummariesLoad(offset);
		}
		
		protected function performSummariesLoad(offset:int = 0):void {
			//to be implemented in sub-classes
		}
		
		protected function loadSummariesResultHandler(event:ResultEvent, token:Object = null):void {
			//to be implemented in sub-classes
		}

		protected function importButtonClickHandler(event:MouseEvent):void {
			if ( checkCanImport() ) {
				browseFileToImport();
			}
		}
		
		protected function exportButtonClickHandler(event:MouseEvent):void {
			//to be implemented in sub-classes
		}
		
		protected function browseFileToImport():void {
			_fileReference.browse([_fileFilter]);
		}
		
		protected function checkCanImport():Boolean {
			return true;
		}
		
		protected function cancelClickHandler(event:MouseEvent):void {
			switch ( _view.currentState ) {
				case AbstractReferenceDataImportView.STATE_UPLOADING:
					_fileReference.cancel();
					break;
				case AbstractReferenceDataImportView.STATE_IMPORTING:
					performImportCancel();
					break;
			}
			_view.currentState = AbstractReferenceDataImportView.STATE_DEFAULT;
		}
		
		protected function performImportCancel():void {
			//to be implemented in sub-classes
		}
		
		protected function closeButtonClickHandler(event:MouseEvent):void {
			if ( _state == null || _state.step == ProcessStatus$Step.RUN ) {
				closePopUp();
			} else {
				AlertUtil.showConfirm(_messageKeys.CONFIRM_CLOSE, null, _messageKeys.CONFIRM_CLOSE_TITLE, performCancelThenClose);
			}
		}
		
		protected function performCancelThenClose():void {
			//to be implemented in sub-classes
		}
		
		protected function closePopUp():void {
			//to be implemented in sub-classes
		}
		
		protected function fileReferenceSelectHandler(event:Event):void {
			AlertUtil.showConfirm(_messageKeys.CONFIRM_IMPORT, null, 
				_messageKeys.CONFIRM_IMPORT_TITLE,
				startUpload);
		}
		
		protected function startUpload():void {
			updateViewForUploading();
			
			//workaround for firefox/chrome flahplayer bug
			//url +=";jsessionid=" + Application.sessionId;
			
			var request:URLRequest = new URLRequest(_uploadUrl);
			//request paramters
			request.method = URLRequestMethod.POST;
			var parameters:URLVariables = new URLVariables();
			//parameters.name = _fileReference.name;
			var ext:String = getExtension(_fileReference.name);
			parameters.name = _uploadFileNamePrefix + ext;
			parameters.sessionId = Application.sessionId;
			request.data = parameters;
			_fileReference.upload(request, "fileData");
		}
		
		protected function getExtension(fileName:String):String {
			var name:String = fileName;
			var indexOfDot:int = name.lastIndexOf(".");
			var ext:String = name.substr(indexOfDot);
			return ext;
		}
		
		protected function fileReferenceProgressHandler(event:ProgressEvent):void {
			_view.progressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			_view.currentState = AbstractReferenceDataImportView.STATE_LOADING;
			performProcessStart();
			startProgressTimer();
		}
		
		protected function performProcessStart():void {
			//to be implemented in sub-classes
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			_view.currentState = AbstractReferenceDataImportView.STATE_DEFAULT;
			AlertUtil.showError(_messageKeys.UPLOADING_FILE_ERROR, [event.text]);
		}
		
		protected function startResultHandler(event:ResultEvent, token:Object = null):void {
			getStatusResultHandler(event, token);
		}
		
		protected function cancelResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}
		
		protected function errorsOkButtonClickHandler(event:MouseEvent):void {
			resetView();
		}
		
		protected function updateStatus():void {
			//implement in sub-class
		}
		
		protected function getStatusResultHandler(event:ResultEvent, token:Object = null):void {
			_state = event.result as ReferenceDataImportStatusProxy;
			updateView();
		}
		
		protected function startProgressTimer():void {
			if ( _progressTimer == null ) {
				_progressTimer = new Timer(PROGRESS_UPDATE_DELAY);
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
			updateStatus();
		}
		
		protected function updateView():void {
			if(_state == null || (_firstOpen && _state.step != ProcessStatus$Step.RUN) ) {
				resetView();
			} else {
				var step:ProcessStatus$Step = _state.step;
				switch ( step ) {
					case ProcessStatus$Step.INIT:
						_view.currentState = AbstractReferenceDataImportView.STATE_LOADING;
						startProgressTimer();
						break;
					case ProcessStatus$Step.RUN:
						updateViewForRunning();
						break;
					case ProcessStatus$Step.COMPLETE:
						stopProgressTimer();
						updateViewProcessComplete();
						break;
					case ProcessStatus$Step.ERROR:
						stopProgressTimer();
						updateViewForError();
						break;
					case ProcessStatus$Step.CANCEL:
						stopProgressTimer();
						AlertUtil.showError(_messageKeys.CANCELLED);
						resetView();
						break;
					default:
						resetView();
				}
			}
			_firstOpen = false;
		}
		
		protected function updateViewForUploading():void {
			_view.currentState = AbstractReferenceDataImportView.STATE_UPLOADING;
			_view.progressTitleText = Message.get(_messageKeys.UPLOADING_FILE);
			_view.progressLabelText = "";
		}
		
		protected function updateViewForRunning():void {
			_view.currentState = AbstractReferenceDataImportView.STATE_IMPORTING;
			_view.progressTitleText = Message.get(_messageKeys.IMPORTING);
			updateProgressBar(_messageKeys.IMPORTING_PROGRESS_LABEL);
		}
		
		protected function updateViewForError():void {
			if ( CollectionUtil.isEmpty(_state.errors) ) {
				_view.currentState = AbstractReferenceDataImportView.STATE_DEFAULT;
				AlertUtil.showError(_messageKeys.ERROR, [_state.errorMessage]);
			} else {
				_view.currentState = AbstractReferenceDataImportView.STATE_ERROR;
				_view.errorsDataGrid.dataProvider = _state.errors;
			}
		}
		
		protected function updateViewProcessComplete():void {
			_view.currentState = AbstractReferenceDataImportView.STATE_DEFAULT;
			AlertUtil.showMessage(_messageKeys.COMPLETED, [_state.processed, _state.total]);
			loadSummaries();
		}
		
		protected function updateProgressBar(progressLabelResource:String):void {
			var progressText:String;
			if ( _state.total <= 0 ) {
				if ( _state.processed > 0 ) {
					progressText = Message.get(progressLabelResource, [_state.processed, '-']);
				} else {
					progressText = Message.get(_messageKeys.PROCESSING);
				}
				_view.progressBar.setProgress(0, 0);
			} else {
				progressText = Message.get(progressLabelResource, [_state.processed, _state.total]);
				_view.progressBar.setProgress(_state.processed, _state.total);
			}
			_view.progressLabelText = progressText;
		}
		
		protected function resetView():void {
			_state = null;
			_view.currentState = AbstractReferenceDataImportView.STATE_DEFAULT;
			stopProgressTimer();
		}
		
	}
}
