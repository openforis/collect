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
	import mx.controls.Alert;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataImportClient;
	import org.openforis.collect.client.SpeciesImportClient;
	import org.openforis.collect.event.DataImportEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.remoting.service.dataImport.DataImportState$MainStep;
	import org.openforis.collect.remoting.service.dataImport.DataImportState$SubStep;
	import org.openforis.collect.remoting.service.dataImport.DataImportStateProxy;
	import org.openforis.collect.remoting.service.dataImport.DataImportSummaryItemProxy;
	import org.openforis.collect.remoting.service.dataImport.DataImportSummaryProxy;
	import org.openforis.collect.remoting.service.dataImport.FileUnmarshallingErrorProxy;
	import org.openforis.collect.ui.component.DataImportNodeErrorsPopUp;
	import org.openforis.collect.ui.component.DataImportPopUp;
	import org.openforis.collect.ui.view.DataImportView;
	import org.openforis.collect.ui.view.SpeciesImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class SpeciesImportPresenter extends AbstractPresenter {
		
		private static const PROGRESS_UPDATE_DELAY:int = 2000;
		
		private var _view:SpeciesImportView;
		private var _fileReference:FileReference;
		private var _speciesImportClient:SpeciesImportClient;
		private var _progressTimer:Timer;
		private var _state:SpeciesImportStatus;
		
		private var _getStatusResponder:IResponder;
		private var _firstOpen:Boolean;
		
		public function SpeciesImportPresenter(view:SpeciesImportView) {
			this._view = view;
			_firstOpen = true;
			_fileReference = new FileReference();
			_speciesImportClient = ClientFactory.speciesImportClient;
			
			_getStatusResponder = new AsyncResponder(getStatusResultHandler, faultHandler);
			
			super();
			
			//try to see if there is a process still running
			_view.currentState = SpeciesImportView.STATE_LOADING;
			updateStatus();
		}
		
		override internal function initEventListeners():void {
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			_view.newButton.addEventListener(MouseEvent.CLICK, newButtonClickHandler);
			_view.editButton.addEventListener(MouseEvent.CLICK, editButtonClickHandler);
			_view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
			
			_view.importButton.addEventListener(MouseEvent.CLICK, importButtonClickHandler);
			_view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			
			_view.selectFileButton.addEventListener(MouseEvent.CLICK, selectFileClickHandler);
			_view.startButton.addEventListener(MouseEvent.CLICK, startClickHandler);
			_view.cancelSelectFileButton.addEventListener(MouseEvent.CLICK, cancelClickHandler);
			_view.cancelImportButton.addEventListener(MouseEvent.CLICK, cancelClickHandler);
		}
		
		protected function newButtonClickHandler(event:MouseEvent):void	{
			
		}

		protected function editButtonClickHandler(event:MouseEvent):void	{
			
		}

		protected function deleteButtonClickHandler(event:MouseEvent):void	{
			
		}
		
		protected function selectFileClickHandler(event:MouseEvent):void {
			_fileReference.browse();
		}
		
		protected function importButtonClickHandler(event:MouseEvent):void {
			
		}
		
		protected function exportButtonClickHandler(event:MouseEvent):void {
			
		}
		
		protected function cancelClickHandler(event:MouseEvent):void {
			switch ( _view.currentState ) {
				case SpeciesImportView.STATE_UPLOADING:
					_fileReference.cancel();
					break;
				case SpeciesImportView.STATE_IMPORTING:
					var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
					_speciesImportClient.cancel(responder);
					break;
			}
			_view.currentState = SpeciesImportView.STATE_DEFAULT;
		}
		
/*		protected function closeButtonClickHandler(event:MouseEvent):void {
			var popUp:SpeciesImportPopUp = _view.owner as SpeciesImportPopUp;
			PopUpManager.removePopUp(popUp);
		}
*/		
		protected function fileReferenceSelectHandler(event:Event):void {
			updateViewForUploading();
			
			var url:String = ApplicationConstants.SPECIES_IMPORT_UPLOAD_URL;
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
		
		protected function fileReferenceProgressHandler(event:ProgressEvent):void {
			_view.progressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			_view.currentState = DataImportView.STATE_LOADING;
			var responder:AsyncResponder = new AsyncResponder(startSummaryCreationResultHandler, faultHandler);
			var checklist:Object = _view.checklistsDropDown.selectedItem;
			if ( checklist != null ) {
				var taxonomyName:String = checklist.name;
				_speciesImportClient.start(responder, taxonomyName);
				startProgressTimer();
			}
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			_view.currentState = DataImportView.STATE_DEFAULT;
			AlertUtil.showError("speciesImport.file.error", [event.text]);
		}
		
		protected function startSummaryCreationResultHandler(event:ResultEvent, token:Object = null):void {
			_state = event.result as DataImportStateProxy;
			updateView();
		}
		
		protected function startClickHandler(event:MouseEvent):void {
			_speciesImportClient.start(responder, taxomyName);
			_view.progressBar.setProgress(0, 0);
			_view.currentState = SpeciesImportView.STATE_IMPORTING;
		}
		
		protected function startResultHandler(event:ResultEvent, token:Object = null):void {
			getStatusResultHandler(event, token);
		}
		
		protected function cancelResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}
		
		protected function updateStatus():void {
			_speciesImportClient.getStatus(_getStatusResponder);
		}
		
		protected function getStatusResultHandler(event:ResultEvent, token:Object = null):void {
			_state = event.result as SpeciesImportStatus;
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
		
		private function updateView():void {
			if(_state == null || _state.mainStep == DataImportState$MainStep.INITED || 
				(_firstOpen && _state.subStep != DataImportState$SubStep.RUNNING) ) {
				resetView();
			} else {
				var mainStep:DataImportState$MainStep = _state.mainStep;
				var subStep:DataImportState$SubStep = _state.subStep;
				switch ( subStep ) {
					case DataImportState$SubStep.INITED:
						switch ( mainStep ) {
							case DataImportState$MainStep.SUMMARY_CREATION:
								resetView();
								break;
							case DataImportState$MainStep.IMPORT:
								updateViewSummaryCompleted();
								break;
						}
						break;
					case DataImportState$SubStep.PREPARING:
						_view.currentState = DataImportView.STATE_LOADING;
						startProgressTimer();
						break;
					case DataImportState$SubStep.RUNNING:
						switch ( mainStep ) {
							case DataImportState$MainStep.SUMMARY_CREATION:
								updateViewForCreatingSummary();
								break;
							case DataImportState$MainStep.IMPORT:
								updateViewForImporting();
								break;
						}
						startProgressTimer();
						break;
					case DataImportState$SubStep.COMPLETE:
						stopProgressTimer();
						switch ( mainStep ) {
							case DataImportState$MainStep.SUMMARY_CREATION:
								updateViewSummaryCompleted();
								break;
							case DataImportState$MainStep.IMPORT:
								updateViewImportCompleted();
								break;
						}
						break;
					case DataImportState$SubStep.ERROR:
						AlertUtil.showError("dataImport.error", [_state.errorMessage]);
						resetView();
						break;
					case DataImportState$SubStep.CANCELLED:
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
			_view.progressLabel.text = "";
		}

		private function updateViewForImporting():void {
			_view.currentState = DataImportView.STATE_IMPORT_RUNNING;
			_view.progressTitle.text = Message.get("dataImport.importingRecords");
			_view.progressBar.setProgress(_state.count, _state.total);
			updateProgressText("dataImport.importingRecordsProgressLabel");
			updateErrorsTextArea()
		}

		private function updateViewForCreatingSummary():void {
			_view.currentState = DataImportView.STATE_SUMMARY_CREATIION_RUNNING;
			_view.progressTitle.text = Message.get("dataImport.creatingSummary");
			_view.progressBar.setProgress(_state.count, _state.total);
			updateProgressText("dataImport.creatingSummaryProgressLabel");
			updateErrorsTextArea()
		}
		
		protected function updateProgressText(progressLabelResource:String):void {
			var progressText:String;
			if ( _state.total == 0 ) {
				progressText = Message.get("dataImport.processing");
			} else {
				progressText = Message.get(progressLabelResource, [_state.count, _state.total]);
			}
			_view.progressLabel.text = progressText;
		}
		
		protected function updateErrorsTextArea():void {
			/*
			var result:String = "";
			if ( _state != null && _state.errors != null ) {
				var files:IList = _state.errors.keySet;
				for each (var fileName:String in files ) {
					var errorMessage:String = _state.errors.get(fileName);
					result += Message.get('dataImport.errorInFile', [fileName, errorMessage]);
				}
			}
			//_view.errorsTextArea.text = result;
			*/
		}
		
		protected function resetView():void {
			_state = null;
			_view.currentState = DataImportView.STATE_DEFAULT;
			stopProgressTimer();
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
	}
}