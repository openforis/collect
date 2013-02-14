package org.openforis.collect.presenter {
	
	import flash.display.DisplayObject;
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
	
	import mx.collections.IList;
	import mx.core.UIComponent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.SpeciesClient;
	import org.openforis.collect.client.SpeciesImportClient;
	import org.openforis.collect.event.PaginationBarEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Languages;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.manager.process.ProcessStatus$Step;
	import org.openforis.collect.manager.referenceDataImport.proxy.ReferenceDataImportStatusProxy;
	import org.openforis.collect.metamodel.proxy.TaxonSummariesProxy;
	import org.openforis.collect.metamodel.proxy.TaxonSummaryProxy;
	import org.openforis.collect.model.proxy.TaxonomyProxy;
	import org.openforis.collect.remoting.service.speciesImport.proxy.SpeciesImportStatusProxy;
	import org.openforis.collect.ui.component.datagrid.PaginationBar;
	import org.openforis.collect.ui.component.speciesImport.TaxonomyEditPopUp;
	import org.openforis.collect.ui.view.ReferenceDataImportView;
	import org.openforis.collect.ui.view.SpeciesImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.components.DataGrid;
	import spark.components.gridClasses.GridColumn;
	import spark.events.IndexChangeEvent;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class AbstractReferenceDataImportPresenter extends AbstractPresenter {
		
		private static const PROGRESS_UPDATE_DELAY:int = 2000;
		private static const VALID_NAME_REGEX:RegExp = /^[a-z][a-z0-9_]*$/;
		private static const IMPORT_FILE_NAME_PREFIX:Object = "species";
		private static const ALLOWED_IMPORT_FILE_EXTENSIONS:Array = ["*.csv"];
		private static const MAX_SUMMARIES_PER_PAGE:int = 20;
		private static const FIXED_SUMMARY_COLUMNS_LENGTH:int = 3;
		private static const VERANCULAR_NAMES_SEPARATOR:String = ", ";
		
		protected var _view:ReferenceDataImportView;
		private var _fileReference:FileReference;
		private var _progressTimer:Timer;
		private var _state:ReferenceDataImportStatusProxy;
		private var _messageKeys:MessageKeys;
		
		private var _getStatusResponder:IResponder;
		private var _firstOpen:Boolean;
		private var _selectedTaxonomy:TaxonomyProxy;
		private var taxonomyEditPopUp:TaxonomyEditPopUp; 
		private var fileFilter:FileFilter;
		
		public function AbstractReferenceDataImportPresenter(view:ReferenceDataImportView, messageKeys:MessageKeys) {
			this._view = view;
			this._messageKeys = messageKeys;
			
			_firstOpen = true;
			_fileReference = new FileReference();
			initFileFilter();
			
			_getStatusResponder = new AsyncResponder(getStatusResultHandler, faultHandler);
			
			super();
			
			//try to see if there is a process still running
			_view.currentState = SpeciesImportView.STATE_LOADING;
			_view.paginationBar.maxRecordsPerPage = MAX_SUMMARIES_PER_PAGE;
			updateStatus();
			loadSummaries();
		}
		
		private function initFileFilter():void {
			var description:String = ALLOWED_IMPORT_FILE_EXTENSIONS.join(", ");
			fileFilter = new FileFilter(description, ALLOWED_IMPORT_FILE_EXTENSIONS.join("; "));
		}
		
		override internal function initEventListeners():void {
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			_view.paginationBar.addEventListener(PaginationBarEvent.PAGE_CHANGE, summaryPageChangeHandler);
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
			performSummariesLoad();
		}
		
		protected function performSummariesLoad():void {
			//to be implemented in sub-classes
		}
		
		protected function importButtonClickHandler(event:MouseEvent):void {
			if ( checkCanImport() ) {
				_fileReference.browse([fileFilter]);
			}
		}
		
		protected function checkCanImport():Boolean {
			return true;
		}
		
		protected function exportButtonClickHandler(event:MouseEvent):void {
			//TODO
		}
		
		protected function cancelClickHandler(event:MouseEvent):void {
			switch ( _view.currentState ) {
				case SpeciesImportView.STATE_UPLOADING:
					_fileReference.cancel();
					break;
				case SpeciesImportView.STATE_IMPORTING:
					performImportCancel();
					break;
			}
			_view.currentState = SpeciesImportView.STATE_DEFAULT;
		}
		
		private function performImportCancel():void {
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
		}
		
		protected function closePopUp():void {
			eventDispatcher.dispatchEvent(new UIEvent(UIEvent.CLOSE_SPECIES_IMPORT_POPUP));
		}
		
		protected function fileReferenceSelectHandler(event:Event):void {
			AlertUtil.showConfirm(MessageKeys.CONFIRM_IMPORT, null, 
				MessageKeys.CONFIRM_IMPORT_TITLE,
				startUpload);
		}
		
		protected function startUpload():void {
			updateViewForUploading();
			
			var url:String = ApplicationConstants.SPECIES_IMPORT_UPLOAD_URL;
			//workaround for firefox/chrome flahplayer bug
			//url +=";jsessionid=" + Application.sessionId;
			
			var request:URLRequest = new URLRequest(url);
			//request paramters
			request.method = URLRequestMethod.POST;
			var parameters:URLVariables = new URLVariables();
			//parameters.name = _fileReference.name;
			var ext:String = getExtension(_fileReference.name);
			parameters.name = IMPORT_FILE_NAME_PREFIX + ext;
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
			_view.currentState = SpeciesImportView.STATE_LOADING;
			performProcessStart();
			startProgressTimer();
		}
		
		protected function performProcessStart():void {
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			_view.currentState = SpeciesImportView.STATE_DEFAULT;
			AlertUtil.showError(MessageKeys.UPLOADING_FILE_ERROR, [event.text]);
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
						_view.currentState = SpeciesImportView.STATE_LOADING;
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
						AlertUtil.showError(MessageKeys.CANCELLED);
						resetView();
						break;
					default:
						resetView();
				}
			}
			_firstOpen = false;
		}
		
		protected function updateViewForUploading():void {
			_view.currentState = SpeciesImportView.STATE_UPLOADING;
			_view.progressTitleText = Message.get(MessageKeys.UPLOADING_FILE);
			_view.progressLabelText = "";
		}
		
		protected function updateViewForRunning():void {
			_view.currentState = SpeciesImportView.STATE_IMPORTING;
			_view.progressTitleText = Message.get(MessageKeys.IMPORTING);
			updateProgressBar(MessageKeys.IMPORTING_PROGRESS_LABEL);
		}
		
		protected function updateViewForError():void {
			if ( CollectionUtil.isEmpty(_state.errors) ) {
				_view.currentState = SpeciesImportView.STATE_DEFAULT;
				AlertUtil.showError(MessageKeys.ERROR, [_state.errorMessage]);
			} else {
				_view.currentState = SpeciesImportView.STATE_ERROR;
				_view.errorsDataGrid.dataProvider = _state.errors;
			}
		}
		
		protected function updateViewProcessComplete():void {
			_view.currentState = SpeciesImportView.STATE_DEFAULT;
			AlertUtil.showMessage(MessageKeys.COMPLETED, [_state.processed, _state.total]);
			loadSummaries();
		}
		
		protected function updateProgressBar(progressLabelResource:String):void {
			var progressText:String;
			if ( _state.total <= 0 ) {
				progressText = Message.get(MessageKeys.PROCESSING);
				_view.progressBar.setProgress(0, 0);
			} else {
				progressText = Message.get(progressLabelResource, [_state.processed, _state.total]);
				_view.progressBar.setProgress(_state.processed, _state.total);
			}
			_view.progressLabelText = progressText;
		}
		
		protected function resetView():void {
			_state = null;
			_view.currentState = SpeciesImportView.STATE_DEFAULT;
			stopProgressTimer();
		}
		
	}
}

class MessageKeys {
	public var SELECT_TAXONOMY:String = "speciesImport.selectTaxonomy";
	public var INVALID_TAXONOMY_NAME:String = "speciesImport.invalidTaxonomyName";
	public var DUPLICATE_TAXONOMY_NAME:String = "speciesImport.duplicateTaxonomyName";
	public var CONFIRM_DELETE_TAXONOMY:String = "speciesImport.confirmDeleteSeletectedTaxonomy";
	public static const IMPORTING_PROGRESS_LABEL:String = "speciesImport.importingProgressLabel";
	public static const COMPLETED:String = "speciesImport.completed";
	public static const IMPORTING:String = "speciesImport.importing";
	public static const PROCESSING:String = "speciesImport.processing";
	public static const CANCELLED:String = "speciesImport.cancelled";
	public static const UPLOADING_FILE:String = "speciesImport.uploadingFile";
	public static const ERROR:String = "speciesImport.error";
	public static const UPLOADING_FILE_ERROR:String = "speciesImport.file.error";
	public static const CONFIRM_IMPORT_TITLE:String = "speciesImport.confirmImport.title";
	public static const CONFIRM_IMPORT:String = "speciesImport.confirmImport.message";
	public var CONFIRM_CLOSE:String = "speciesImport.confirmClose.message";
	public static const CONFIRM_CLOSE_TITLE:String = "speciesImport.confirmClose.title";
	public static const SYNONYM:String = "speciesImport.summaryList.synonym";
}