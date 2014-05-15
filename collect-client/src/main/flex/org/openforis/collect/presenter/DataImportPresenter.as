package org.openforis.collect.presenter {
	
	import flash.events.DataEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.MouseEvent;
	import flash.events.ProgressEvent;
	import flash.events.TimerEvent;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.controls.CheckBox;
	import mx.events.AdvancedDataGridEvent;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataImportClient;
	import org.openforis.collect.event.DataImportEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.io.data.proxy.DataRestoreJobProxy;
	import org.openforis.collect.io.data.proxy.DataRestoreSummaryJobProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.remoting.service.dataimport.DataImportSummaryItemProxy;
	import org.openforis.collect.remoting.service.dataimport.DataImportSummaryProxy;
	import org.openforis.collect.remoting.service.dataimport.FileUnmarshallingErrorProxy;
	import org.openforis.collect.ui.component.DataImportNodeErrorsPopUp;
	import org.openforis.collect.ui.view.DataImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.concurrency.proxy.JobProxy;
	import org.openforis.concurrency.proxy.JobProxy$Status;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class DataImportPresenter extends AbstractPresenter {
		
		private static const PROGRESS_UPDATE_DELAY:int = 2000;
		
		private var _view:DataImportView;
		private var _fileReference:FileReference;
		private var _dataImportClient:DataImportClient;
		private var _progressTimer:Timer;
		private var _job:JobProxy;
		private var _summary:DataImportSummaryProxy;
		
		private var _getCurrentJobResponder:IResponder;
		private var _firstOpen:Boolean;
		private var _allConflictingRecordsSelected:Boolean;
		
		public function DataImportPresenter(view:DataImportView) {
			this._view = view;
			_firstOpen = true;
			_allConflictingRecordsSelected = false;
			
			_fileReference = new FileReference();
			_dataImportClient = ClientFactory.dataImportClient;
			
			_getCurrentJobResponder = new AsyncResponder(getCurrentJobResultHandler, faultHandler);
			
			super();
			
			//try to see if there is a process still running
			_view.currentState = DataImportView.STATE_LOADING;
			updateState();
		}
		
		override internal function initEventListeners():void {
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(Event.COMPLETE, fileReferenceCompleteHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(Event.OPEN, fileReferenceOpenHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			_view.uploadButton.addEventListener(MouseEvent.CLICK, uploadButtonClickHandler);
			_view.startImportButton.addEventListener(MouseEvent.CLICK, startImportClickHandler);
			_view.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
			
			_view.conflictDataGrid.addEventListener(AdvancedDataGridEvent.HEADER_RELEASE, conflictDataGridHeaderReleaseHandler);
			
			eventDispatcher.addEventListener(DataImportEvent.SHOW_IMPORT_WARNINGS, showImportWarningsPopUp);
			eventDispatcher.addEventListener(DataImportEvent.SHOW_SKIPPED_FILE_ERRORS, showSkippedFileErrorsPopUp);
			eventDispatcher.addEventListener(DataImportEvent.SELECT_ALL_CONFLICTING_RECORDS, selectAllConflictingRecords);
			eventDispatcher.addEventListener(DataImportEvent.CONFLICTING_RECORDS_SELECTION_CHANGE, conflictingRecordsSelectionChange);
		}
		
		protected function uploadButtonClickHandler(event:MouseEvent):void {
			_fileReference.browse();
		}
		
		protected function cancelButtonClickHandler(event:MouseEvent):void {
			if ( _view.currentState == DataImportView.STATE_UPLOADING ) {
				//cancel uploading
				_fileReference.cancel();
			} else {
				//cancel import
				var responder:AsyncResponder = new AsyncResponder(cancelImportResultHandler, faultHandler);
				_dataImportClient.cancel(responder);
			}
			_view.currentState = DataImportView.STATE_DEFAULT;
		}
		
		protected function fileReferenceSelectHandler(event:Event):void {
			updateViewForUploading();
			
			var url:String = ApplicationConstants.DATA_IMPORT_UPLOAD_URL;
			//workaround for firefox/chrome flahplayer bug
			//url +=";jsessionid=" + Application.sessionId;
			
			var request:URLRequest = new URLRequest(url);
			//request paramters
			request.method = URLRequestMethod.POST;
			var parameters:URLVariables = new URLVariables();
			parameters.name = _fileReference.name;
			parameters.sessionId = Application.sessionId;
			request.data = parameters;
			_fileReference.upload(request, "fileData");
		}
		
		protected function fileReferenceOpenHandler(event:Event):void {
			
		}
		
		protected function fileReferenceCompleteHandler(event:Event):void {
			
		}
		
		protected function fileReferenceProgressHandler(event:ProgressEvent):void {
			_view.progressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			_view.currentState = DataImportView.STATE_LOADING;
			var responder:AsyncResponder = new AsyncResponder(startSummaryCreationResultHandler, faultHandler);
			var activeSurvey:SurveyProxy = Application.activeSurvey;
			var selectedSurveyUri:String = activeSurvey == null ? null: activeSurvey.uri;
			_dataImportClient.startSummaryCreation(responder, selectedSurveyUri);
			startProgressTimer();
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			_view.currentState = DataImportView.STATE_DEFAULT;
			AlertUtil.showError("dataImport.file.error", [event.text]);
		}
		
		protected function startSummaryCreationResultHandler(event:ResultEvent, token:Object = null):void {
			_job = event.result as JobProxy;
			updateView();
		}
		
		protected function updateViewSummaryCompleted():void {
			var responder:AsyncResponder = new AsyncResponder(getSummaryResultHandler, faultHandler);
			_dataImportClient.getSummary(responder);
		}
		
		protected function getSummaryResultHandler(event:ResultEvent, token:Object = null):void {
			_summary = event.result as DataImportSummaryProxy;
			if ( _summary.surveyName == null ) {
				_view.currentState = DataImportView.STATE_SUMMARY_CREATION_COMPLETE_NEW_SURVEY;
			} else {
				_view.currentState = DataImportView.STATE_SUMMARY_CREATION_COMPLETE;
				_view.surveyNameTextInput.text = _summary.surveyName;
			}
			var entryTotalRecords:int = _summary.totalPerStep.get(CollectRecord$Step.ENTRY);
			var cleansingTotalRecords:int = _summary.totalPerStep.get(CollectRecord$Step.CLEANSING);
			var analysisTotalRecords:int = _summary.totalPerStep.get(CollectRecord$Step.ANALYSIS);
			
			_view.skippedFilesDataGrid.dataProvider = _summary.skippedFileErrors;
			//default selected
			setItemsSelected(_summary.recordsToImport);
			_view.recordToImportDataGrid.dataProvider = _summary.recordsToImport;
			//default not selected
			_allConflictingRecordsSelected = false;
			setItemsSelected(_summary.conflictingRecords, _allConflictingRecordsSelected);
			_view.conflictDataGrid.dataProvider = _summary.conflictingRecords;
		}
		
		protected function startImportClickHandler(event:MouseEvent):void {
			if ( validateForm() ) {
				var entryIdsToImport:ArrayCollection = new ArrayCollection();
				var tempSelectedItemIds:IList = getSelectedItemIds(_summary.recordsToImport);
				entryIdsToImport.addAll(tempSelectedItemIds);
				tempSelectedItemIds = getSelectedItemIds(_summary.conflictingRecords);
				entryIdsToImport.addAll(tempSelectedItemIds);
				if ( entryIdsToImport.length == 0 ) {
					AlertUtil.showError("dataImport.error.emptyImportSelection");
					return;
				}
				var responder:AsyncResponder = new AsyncResponder(startImportResultHandler, faultHandler);
				_dataImportClient.startImport(responder, entryIdsToImport);
				_view.progressBar.setProgress(0, 0);
				_view.currentState = DataImportView.STATE_IMPORT_RUNNING;
			}
		}
		
		protected function getSelectedItemIds(items:IList):IList {
			var result:IList = new ArrayCollection();
			for each (var item:DataImportSummaryItemProxy in items) {
				if ( item.selected ) {
					result.addItem(item.entryId);
				}
			}
			return result;
		}
		
		protected function setItemsSelected(items:IList, value:Boolean = true):void {
			ListCollectionView(items).disableAutoUpdate();
			for each (var item:DataImportSummaryItemProxy in items) {
				item.selected = value;
			}
			ListCollectionView(items).enableAutoUpdate();
		}
		
		protected function isAllItemsSelected(items:IList):Boolean {
			if ( CollectionUtil.isEmpty(items) ) {
				return false;
			} else {
				for each (var item:DataImportSummaryItemProxy in items) {
					if ( ! item.selected ) {
						return false;
					}
				}
				return true;
			}
		}
		
		protected function validateForm():Boolean {
			if ( _view.currentState == DataImportView.STATE_SUMMARY_CREATION_COMPLETE_NEW_SURVEY ) {
				var surveyName:String = _view.surveyNameTextInput.text;
				surveyName = StringUtil.trim(surveyName);
				_view.surveyNameTextInput.text = surveyName;
				if ( StringUtil.isBlank(surveyName) ) {
					AlertUtil.showError("dataImport.error.specifySurveyName");
					return false;
				}
			}
			return true;
		}
		
		protected function startImportResultHandler(event:ResultEvent, token:Object = null):void {
			_job = event.result as JobProxy;
			updateView();
		}
		
		protected function cancelImportResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}
		
		protected function updateState():void {
			_dataImportClient.getCurrentJob(_getCurrentJobResponder);
		}
		
		protected function getCurrentJobResultHandler(event:ResultEvent, token:Object = null):void {
			_job = event.result as JobProxy;
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
			updateState();
		}
		
		private function updateView():void {
			if ( _job == null || _job.pending || (_firstOpen && ! _job.running) ) {
				resetView();
			} else {
				_view.currentState = DataImportView.STATE_LOADING;
				switch ( _job.status ) {
					case JobProxy$Status.PENDING:
						_view.currentState = DataImportView.STATE_LOADING;
						startProgressTimer();
						break;
					case JobProxy$Status.RUNNING:
						if ( _job is DataRestoreSummaryJobProxy ) {
							updateViewForCreatingSummary();
						} else if ( _job is DataRestoreJobProxy ) {
							updateViewForImporting();
						}
						startProgressTimer();
						break;
					case JobProxy$Status.COMPLETED:
						stopProgressTimer();
						if ( _job is DataRestoreSummaryJobProxy ) {
							updateViewSummaryCompleted();
						} else if ( _job is DataRestoreJobProxy ) {
							updateViewImportCompleted();
						}
						break;
					case JobProxy$Status.FAILED:
						AlertUtil.showError("dataImport.error", [_job.errorMessage]);
						resetView();
						break;
					case JobProxy$Status.ABORTED:
						AlertUtil.showError("dataImport.cancelled");
						resetView();
						break;
					default:
						resetView();
				}
			}
			_firstOpen = false;
		}
		
		protected function updateViewImportCompleted():void {
			_view.currentState = DataImportView.STATE_IMPORT_COMPLETE;
			//reload record summaries
			var uiEvent:UIEvent = new UIEvent(UIEvent.RELOAD_RECORD_SUMMARIES);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		private function updateViewForUploading():void {
			_view.currentState = DataImportView.STATE_UPLOADING;
			_view.progressTitle.text = Message.get("dataImport.uploadingFile");
			//_view.progressLabel.text = "";
		}

		private function updateViewForImporting():void {
			_view.currentState = DataImportView.STATE_IMPORT_RUNNING;
			_view.progressTitle.text = Message.get("dataImport.importingRecords");
			_view.progressBar.setProgress(_job.progressPercent, 100);
			updateProgressText("dataImport.importingRecordsProgressLabel");
		}

		private function updateViewForCreatingSummary():void {
			_view.currentState = DataImportView.STATE_SUMMARY_CREATIION_RUNNING;
			_view.progressTitle.text = Message.get("dataImport.creatingSummary");
			_view.progressBar.setProgress(_job.progressPercent, 100);
			updateProgressText("dataImport.creatingSummaryProgressLabel");
		}
		
		protected function updateProgressText(progressLabelResource:String):void {
			/*
			var progressText:String;
			if ( _job.progressPercent == 0 ) {
				progressText = Message.get("dataImport.processing");
			} else {
				progressText = Message.get(progressLabelResource, [_job.progressPercent, 100]);
			}
			_view.progressLabel.text = progressText;
			*/
		}
		
		protected function resetView():void {
			stopProgressTimer();
			_job = null;
			_view.currentState = DataImportView.STATE_DEFAULT;
		}
		
		protected function showImportWarningsPopUp(event:DataImportEvent):void {
			var summaryItem:DataImportSummaryItemProxy = event.summaryItem;
			var warnings:IList = summaryItem.warnings;
			var popUp:DataImportNodeErrorsPopUp = DataImportNodeErrorsPopUp(PopUpUtil.createPopUp(DataImportNodeErrorsPopUp, false));
			var recordKey:String = summaryItem.key;
			popUp.title = Message.get("dataImport.warnings.title", [recordKey]);
			popUp.dataGrid.dataProvider = warnings;
		}
		
		protected function showSkippedFileErrorsPopUp(event:DataImportEvent):void {
			var fileErrorItem:FileUnmarshallingErrorProxy = event.fileUnmarshallingError;
			var errors:IList = fileErrorItem.errors;
			var popUp:DataImportNodeErrorsPopUp = DataImportNodeErrorsPopUp(PopUpUtil.createPopUp(DataImportNodeErrorsPopUp, false));
			var fileName:String = fileErrorItem.fileName;
			popUp.title = Message.get("dataImport.skippedFileErrors.title", [fileName]);
			popUp.showStep = false;
			popUp.dataGrid.dataProvider = errors;
		}
		
		protected function selectAllConflictingRecords(event:DataImportEvent):void {
			_allConflictingRecordsSelected = ! _allConflictingRecordsSelected;
			_view.allConflictingRecordsSelected = _allConflictingRecordsSelected;
			setItemsSelected(_summary.conflictingRecords, _allConflictingRecordsSelected);
		}
		
		protected function conflictingRecordsSelectionChange(event:DataImportEvent):void {
			_allConflictingRecordsSelected = isAllItemsSelected(_summary.conflictingRecords);
			_view.allConflictingRecordsSelected = _allConflictingRecordsSelected;
		}

		protected function conflictDataGridHeaderReleaseHandler(event:AdvancedDataGridEvent):void {
			if ( event.triggerEvent.target is CheckBox) {
				event.preventDefault();
			}
		}

	}
}