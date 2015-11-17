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
		
		private static const COLLECT_DATA_FILTER:FileFilter = new FileFilter("Collect Data", "*.collect-data");
		private static const COLLECT_BACKUP_FILTER:FileFilter = new FileFilter("Collect Backup", "*.collect-backup");
		private static const COLLECT_EARTH_DATA_FILTER:FileFilter = new FileFilter("Collect Earth Data", "*.zip");
		private static const FILE_FILTERS:Array = [COLLECT_DATA_FILTER, COLLECT_BACKUP_FILTER, COLLECT_EARTH_DATA_FILTER];
		
		private var _fileReference:FileReference;
		private var _dataImportClient:DataImportClient;
		private var _progressTimer:Timer;
		private var _job:JobProxy;
		private var _summary:DataImportSummaryProxy;
		
		private var _getCurrentJobResponder:IResponder;
		private var _firstOpen:Boolean;
		private var _allConflictingRecordsSelected:Boolean;
		
		public function DataImportPresenter(view:DataImportView) {
			super(view);
			_firstOpen = true;
			_allConflictingRecordsSelected = false;
			
			_fileReference = new FileReference();
			_dataImportClient = ClientFactory.dataImportClient;
			
			_getCurrentJobResponder = new AsyncResponder(getCurrentJobResultHandler, faultHandler);
		}
		
		private function get view():DataImportView {
			return DataImportView(_view);
		}
		
		override public function init():void {
			super.init();
			//try to see if there is a process still running
			view.currentState = DataImportView.STATE_LOADING;
			updateState();
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(Event.COMPLETE, fileReferenceCompleteHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(Event.OPEN, fileReferenceOpenHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			view.uploadButton.addEventListener(MouseEvent.CLICK, uploadButtonClickHandler);
			view.startImportButton.addEventListener(MouseEvent.CLICK, startImportClickHandler);
			view.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
			
			view.conflictDataGrid.addEventListener(AdvancedDataGridEvent.HEADER_RELEASE, conflictDataGridHeaderReleaseHandler);
		}
		
		override protected function initBroadcastEventListeners():void {
			super.initBroadcastEventListeners();
			eventDispatcher.addEventListener(DataImportEvent.SHOW_IMPORT_WARNINGS, showImportWarningsPopUp);
			eventDispatcher.addEventListener(DataImportEvent.SHOW_SKIPPED_FILE_ERRORS, showSkippedFileErrorsPopUp);
			eventDispatcher.addEventListener(DataImportEvent.SELECT_ALL_CONFLICTING_RECORDS, selectAllConflictingRecords);
			eventDispatcher.addEventListener(DataImportEvent.CONFLICTING_RECORDS_SELECTION_CHANGE, conflictingRecordsSelectionChange);
			eventDispatcher.addEventListener(DataImportEvent.RECORDS_TO_IMPORT_SELECTION_CHANGE, recordsToImportSelectionChange);
		}
		
		override protected function removeBroadcastEventListeners():void {
			super.removeBroadcastEventListeners();
			eventDispatcher.removeEventListener(DataImportEvent.SHOW_IMPORT_WARNINGS, showImportWarningsPopUp);
			eventDispatcher.removeEventListener(DataImportEvent.SHOW_SKIPPED_FILE_ERRORS, showSkippedFileErrorsPopUp);
			eventDispatcher.removeEventListener(DataImportEvent.SELECT_ALL_CONFLICTING_RECORDS, selectAllConflictingRecords);
			eventDispatcher.removeEventListener(DataImportEvent.CONFLICTING_RECORDS_SELECTION_CHANGE, conflictingRecordsSelectionChange);
			eventDispatcher.removeEventListener(DataImportEvent.RECORDS_TO_IMPORT_SELECTION_CHANGE, recordsToImportSelectionChange);
		}
		
		protected function uploadButtonClickHandler(event:MouseEvent):void {
			_fileReference.browse(FILE_FILTERS);
		}
		
		protected function cancelButtonClickHandler(event:MouseEvent):void {
			if ( view.currentState == DataImportView.STATE_UPLOADING ) {
				//cancel uploading
				_fileReference.cancel();
			} else {
				//cancel import
				var responder:AsyncResponder = new AsyncResponder(cancelImportResultHandler, faultHandler);
				_dataImportClient.cancel(responder);
			}
			view.currentState = DataImportView.STATE_DEFAULT;
		}
		
		protected function fileReferenceSelectHandler(event:Event):void {
			updateViewForUploading();
			
			var url:String = ApplicationConstants.FILE_UPLOAD_URL;
			
			var request:URLRequest = new URLRequest(url);
			request.method = URLRequestMethod.POST;
			
			//request paramters
			request.data = new URLVariables();
			request.data.name = _fileReference.name;
			
			_fileReference.upload(request, "fileData");
		}
		
		protected function fileReferenceOpenHandler(event:Event):void {
		}
		
		protected function fileReferenceCompleteHandler(event:Event):void {
		}
		
		protected function fileReferenceProgressHandler(event:ProgressEvent):void {
			view.progressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			view.currentState = DataImportView.STATE_LOADING;
			var filePath:String = event.data;
			var responder:AsyncResponder = new AsyncResponder(startSummaryCreationResultHandler, faultHandler);
			var activeSurvey:SurveyProxy = Application.activeSurvey;
			var selectedSurveyUri:String = activeSurvey == null ? null: activeSurvey.uri;
			_dataImportClient.startSummaryCreation(responder, filePath, selectedSurveyUri);
			startProgressTimer();
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			view.currentState = DataImportView.STATE_DEFAULT;
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
				view.currentState = DataImportView.STATE_SUMMARY_CREATION_COMPLETE_NEW_SURVEY;
			} else {
				view.currentState = DataImportView.STATE_SUMMARY_CREATION_COMPLETE;
				view.surveyNameTextInput.text = _summary.surveyName;
			}
			var entryTotalRecords:int = _summary.totalPerStep.get(CollectRecord$Step.ENTRY);
			var cleansingTotalRecords:int = _summary.totalPerStep.get(CollectRecord$Step.CLEANSING);
			var analysisTotalRecords:int = _summary.totalPerStep.get(CollectRecord$Step.ANALYSIS);
			
			view.skippedFilesDataGrid.dataProvider = _summary.skippedFileErrors;
			//default selected
			setItemsSelected(_summary.recordsToImport);
			view.selectedRecordsToImportCount = _summary.recordsToImport.length;
			view.recordToImportDataGrid.dataProvider = _summary.recordsToImport;
			//default not selected
			_allConflictingRecordsSelected = false;
			view.selectedConflictingRecordsCount = 0;
			setItemsSelected(_summary.conflictingRecords, _allConflictingRecordsSelected);
			view.conflictDataGrid.dataProvider = _summary.conflictingRecords;
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
				var validateRecords:Boolean = view.validateRecordsCheckBox.selected;
				var responder:AsyncResponder = new AsyncResponder(startImportResultHandler, faultHandler);
				_dataImportClient.startImport(responder, entryIdsToImport, validateRecords);
				view.progressBar.setProgress(0, 0);
				view.currentState = DataImportView.STATE_IMPORT_RUNNING;
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
		
		protected function countSelectedItems(items:IList):int {
			if ( CollectionUtil.isEmpty(items) ) {
				return 0;
			} else {
				var count:int = 0;
				for each (var item:DataImportSummaryItemProxy in items) {
					if (item.selected ) {
						count ++;
					}
				}
				return count;
			}
		}
		
		protected function validateForm():Boolean {
			if ( view.currentState == DataImportView.STATE_SUMMARY_CREATION_COMPLETE_NEW_SURVEY ) {
				var surveyName:String = view.surveyNameTextInput.text;
				surveyName = StringUtil.trim(surveyName);
				view.surveyNameTextInput.text = surveyName;
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
			if ( _job == null || (_firstOpen && ! _job.running) ) {
				resetView();
			} else {
				view.currentState = DataImportView.STATE_LOADING;
				switch ( _job.status ) {
					case JobProxy$Status.PENDING:
						view.currentState = DataImportView.STATE_LOADING;
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
							var errors:ListCollectionView = DataRestoreJobProxy(_job).errors;
							updateViewImportCompleted(errors);
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
		
		protected function updateViewImportCompleted(errors:IList = null):void {
			view.currentState = DataImportView.STATE_IMPORT_COMPLETE;
			view.dataImportErrors = errors;
			
			//reload record summaries
			var uiEvent:UIEvent = new UIEvent(UIEvent.RELOAD_RECORD_SUMMARIES);
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		private function updateViewForUploading():void {
			view.currentState = DataImportView.STATE_UPLOADING;
			view.progressTitle.text = Message.get("dataImport.uploadingFile");
			//view.progressLabel.text = "";
		}

		private function updateViewForImporting():void {
			view.currentState = DataImportView.STATE_IMPORT_RUNNING;
			view.progressTitle.text = Message.get("dataImport.importingRecords");
			view.progressBar.setProgress(_job.progressPercent, 100);
			updateProgressText("dataImport.importingRecordsProgressLabel");
		}

		private function updateViewForCreatingSummary():void {
			view.currentState = DataImportView.STATE_SUMMARY_CREATIION_RUNNING;
			view.progressTitle.text = Message.get("dataImport.creatingSummary");
			view.progressBar.setProgress(_job.progressPercent, 100);
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
			view.progressLabel.text = progressText;
			*/
		}
		
		protected function resetView():void {
			stopProgressTimer();
			_job = null;
			view.currentState = DataImportView.STATE_DEFAULT;
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
			view.allConflictingRecordsSelected = _allConflictingRecordsSelected;
			view.selectedConflictingRecordsCount = _summary.conflictingRecords.length;
			setItemsSelected(_summary.conflictingRecords, _allConflictingRecordsSelected);
		}
		
		protected function conflictingRecordsSelectionChange(event:DataImportEvent):void {
			view.selectedConflictingRecordsCount = countSelectedItems(_summary.conflictingRecords);
			_allConflictingRecordsSelected = isAllItemsSelected(_summary.conflictingRecords);
			view.allConflictingRecordsSelected = _allConflictingRecordsSelected;
		}

		protected function recordsToImportSelectionChange(event:DataImportEvent):void {
			view.selectedRecordsToImportCount = countSelectedItems(_summary.recordsToImport);
		}
		
		protected function conflictDataGridHeaderReleaseHandler(event:AdvancedDataGridEvent):void {
			if ( event.triggerEvent.target is CheckBox) {
				event.preventDefault();
			}
		}

	}
}