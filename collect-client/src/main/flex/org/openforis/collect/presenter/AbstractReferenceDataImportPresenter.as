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
	
	import mx.collections.ArrayList;
	import mx.collections.IList;
	
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.granite.util.Enum;
	
	import org.openforis.collect.event.PaginationBarEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.io.parsing.CSVFileOptions;
	import org.openforis.collect.io.parsing.CSVFileSeparator;
	import org.openforis.collect.io.parsing.CSVFileTextDelimiter;
	import org.openforis.collect.io.parsing.FileCharset;
	import org.openforis.collect.manager.process.ProcessStatus$Step;
	import org.openforis.collect.manager.process.proxy.ProcessStatusProxy;
	import org.openforis.collect.ui.component.datagrid.PaginationBar;
	import org.openforis.collect.ui.view.AbstractReferenceDataImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.DataGrids;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class AbstractReferenceDataImportPresenter extends AbstractPresenter {
		
		private static const PROGRESS_UPDATE_DELAY:int = 2000;
		protected static const ALLOWED_IMPORT_FILE_EXTENSIONS:Array = new Array("*.csv", "*.xls", "*.xlsx");
		private static const MAX_SUMMARIES_PER_PAGE:int = 20;
		private static const FIXED_SUMMARY_COLUMNS_LENGTH:int = 3;
		private static const VERANCULAR_NAMES_SEPARATOR:String = ", ";
		
		protected var _fileReference:FileReference;
		protected var _progressTimer:Timer;
		protected var _state:ProcessStatusProxy;
		protected var _messageKeys:ReferenceDataImportMessageKeys;
		protected var _uploadUrl:String;
		protected var _uploadedTempFileName:String;
		
		protected var _getStatusResponder:IResponder;
		protected var _firstOpen:Boolean;
		
		protected var _recordsOffset:int;
		protected var _recordsPerPage:int;
		
		public function AbstractReferenceDataImportPresenter(view:AbstractReferenceDataImportView, messageKeys:ReferenceDataImportMessageKeys) {
			super(view);
			this._messageKeys = messageKeys;
			
			_uploadUrl = ApplicationConstants.FILE_UPLOAD_URL;
			_firstOpen = true;
			_fileReference = new FileReference();
			_getStatusResponder = new AsyncResponder(getStatusResultHandler, faultHandler);
			
			_recordsOffset = 0;
			_recordsPerPage = MAX_SUMMARIES_PER_PAGE
		}

		protected function createFileFilter():FileFilter {
			//var description:String = ALLOWED_IMPORT_FILE_EXTENSIONS.join(", ");
			return new FileFilter("Excel documents", ALLOWED_IMPORT_FILE_EXTENSIONS.join("; "));
		}
		
		override public function init():void {
			super.init();
			//try to see if there is a still running process
			view.currentState = AbstractReferenceDataImportView.STATE_LOADING;
			if ( view.paginationBar != null ) {
				view.paginationBar.maxRecordsPerPage = MAX_SUMMARIES_PER_PAGE;
			}
			loadInitialData();
			
			view.charsets = enumToList(FileCharset, "referenceDataImport.charset.");
			view.separators = enumToList(CSVFileSeparator, "referenceDataImport.separator.");
			view.textDelimiters = enumToList(CSVFileTextDelimiter, "referenceDataImport.text_delimiter.");
		}
		
		private function enumToList(enum:Class, labelKeyPrefix:String):IList {
			var items:IList = new ArrayList();
			var values:Array = enum["constants"];
			for each (var value:Enum in values) {
				var item:Object = {name: value.name, label: Message.get(labelKeyPrefix + value.name.toLowerCase())};
				items.addItem(item);
			}
			return items;
		}
		
		private function get view():AbstractReferenceDataImportView {
			return AbstractReferenceDataImportView(_view);
		}
		
		protected function loadInitialData():void {
			updateStatus();
			loadSummaries();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			view.importButton.addEventListener(MouseEvent.CLICK, importButtonClickHandler);
			if (view.exportButton != null ) {
				view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			}
			view.cancelImportButton.addEventListener(MouseEvent.CLICK, cancelClickHandler);
			if ( view.paginationBar != null ) {
				view.paginationBar.addEventListener(PaginationBarEvent.PAGE_CHANGE, summaryPageChangeHandler);
			}
			view.errorsOkButton.addEventListener(MouseEvent.CLICK, errorsOkButtonClickHandler);
			if ( view.exportErrorsButton != null ) {
				view.exportErrorsButton.addEventListener(MouseEvent.CLICK, exportErrorsClickHandler);
			}
			view.closeButton.addEventListener(MouseEvent.CLICK, closeButtonClickHandler);
		}
		
		protected function summaryPageChangeHandler(event:PaginationBarEvent):void {
			this._recordsOffset = event.offset;
			this._recordsPerPage = event.recordsPerPage;
			loadSummaries();
		}
		
		protected function loadSummaries():void {
			if ( _recordsOffset == 0 ) {
				view.paginationBar.showPage(1);
			}
			view.summaryDataGrid.dataProvider = null;
			
			view.paginationBar.currentState = PaginationBar.LOADING_STATE;
			performSummariesLoad(_recordsOffset, _recordsPerPage);
		}
		
		protected function performSummariesLoad(offset:int = 0, recordsPerPage:int = MAX_SUMMARIES_PER_PAGE):void {
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
			_fileReference.browse([createFileFilter()]);
		}
		
		protected function checkCanImport():Boolean {
			return true;
		}
		
		protected function cancelClickHandler(event:MouseEvent):void {
			switch ( view.currentState ) {
				case AbstractReferenceDataImportView.STATE_UPLOADING:
					_fileReference.cancel();
					break;
				case AbstractReferenceDataImportView.STATE_IMPORTING:
					performImportCancel();
					break;
			}
			backToDefaultView();
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
			confirmUploadStart(_messageKeys.CONFIRM_IMPORT);
		}
		
		protected function confirmUploadStart(message:String):void {
			AlertUtil.showConfirm(message, null, 
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
			
			request.data = new URLVariables();
			request.data.name = _fileReference.name;
			
			_fileReference.upload(request, "fileData");
		}
		
		protected function getExtension(fileName:String):String {
			var name:String = fileName;
			var indexOfDot:int = name.lastIndexOf(".");
			var ext:String = name.substr(indexOfDot);
			return ext;
		}
		
		protected function fileReferenceProgressHandler(event:ProgressEvent):void {
			view.progressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			view.currentState = AbstractReferenceDataImportView.STATE_LOADING;
			_uploadedTempFileName = event.data;
			performProcessStart();
			startProgressTimer();
		}
		
		protected function performProcessStart():void {
			//to be implemented in sub-classes
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			AlertUtil.showError(_messageKeys.UPLOADING_FILE_ERROR, [event.text]);
			backToDefaultView();
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
			_state = event.result as ProcessStatusProxy;
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
						view.currentState = AbstractReferenceDataImportView.STATE_LOADING;
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
		
		protected function backToDefaultView():void {
			view.currentState = AbstractReferenceDataImportView.STATE_DEFAULT;
		}
		
		protected function updateViewForUploading():void {
			view.currentState = AbstractReferenceDataImportView.STATE_UPLOADING;
			view.progressTitleText = Message.get(_messageKeys.UPLOADING_FILE);
			view.progressLabelText = "";
		}
		
		protected function updateViewForRunning():void {
			view.currentState = AbstractReferenceDataImportView.STATE_IMPORTING;
			view.progressTitleText = Message.get(_messageKeys.IMPORTING);
			updateProgressBar(_messageKeys.IMPORTING_PROGRESS_LABEL);
		}
		
		protected function updateViewForError():void {
			if (_state.hasOwnProperty("errors")) {
				if ( CollectionUtil.isEmpty(_state["errors"]) ) {
					AlertUtil.showError(_messageKeys.ERROR, [_state.errorMessage]);
					backToDefaultView();
				} else {
					view.currentState = AbstractReferenceDataImportView.STATE_ERROR;
					view.errorsDataGrid.dataProvider = _state["errors"];
				}
			}
		}
		
		protected function updateViewProcessComplete():void {
			var messageKey:String = _state.total == 100 ? _messageKeys.COMPLETED_IN_PERCENTAGE : _messageKeys.COMPLETED; 
			AlertUtil.showMessage(messageKey, [_state.processed, _state.total]);
			backToDefaultView();
			_recordsOffset = 0;
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
				view.progressBar.setProgress(0, 0);
			} else {
				if (_state.total == 100) {
					progressText = null; //completion percentage in progress bar 
				} else {
					progressText = Message.get(progressLabelResource, [_state.processed, _state.total]);
				}
				view.progressBar.setProgress(_state.processed, _state.total);
			}
			view.progressLabelText = progressText;
		}
		
		protected function resetView():void {
			_state = null;
			_recordsOffset = 0;
			backToDefaultView();
			stopProgressTimer();
		}
		
		protected function exportErrorsClickHandler(event:MouseEvent):void {
			DataGrids.writeToCSV(view.errorsDataGrid, "errors.csv");
		}
		
	}
}
