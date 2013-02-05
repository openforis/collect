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
	
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.SpeciesClient;
	import org.openforis.collect.client.SpeciesImportClient;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.manager.process.ProcessStatus$Step;
	import org.openforis.collect.manager.speciesImport.SpeciesImportStatus;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.ui.view.DataImportView;
	import org.openforis.collect.ui.view.SpeciesImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.idm.model.species.Taxonomy;
	
	import spark.events.IndexChangeEvent;

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
		private var _speciesClient:SpeciesClient;
		private var _progressTimer:Timer;
		private var _state:SpeciesImportStatus;
		
		private var _getStatusResponder:IResponder;
		private var _firstOpen:Boolean;
		private var _selectedTaxonomy:Taxonomy;
		
		public function SpeciesImportPresenter(view:SpeciesImportView) {
			this._view = view;
			_firstOpen = true;
			_fileReference = new FileReference();
			_speciesImportClient = ClientFactory.speciesImportClient;
			_speciesClient = ClientFactory.speciesClient;
			
			_getStatusResponder = new AsyncResponder(getStatusResultHandler, faultHandler);
			
			super();
			
			//try to see if there is a process still running
			_view.currentState = SpeciesImportView.STATE_LOADING;
			updateStatus();
			loadTaxonomies();
		}
		
		override internal function initEventListeners():void {
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			_view.checklistsDropDown.addEventListener(IndexChangeEvent.CHANGE, checklistChangeHandler);
			
			_view.newButton.addEventListener(MouseEvent.CLICK, newButtonClickHandler);
			_view.editButton.addEventListener(MouseEvent.CLICK, editButtonClickHandler);
			_view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
			
			_view.importButton.addEventListener(MouseEvent.CLICK, importButtonClickHandler);
			_view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			
			_view.cancelImportButton.addEventListener(MouseEvent.CLICK, cancelClickHandler);
		}
		
		protected function loadTaxonomies():void {
			var responder:IResponder = new AsyncResponder(loadTaxonomiesSuccessHandler, faultHandler);
			_speciesClient.loadAllTaxonomies(responder);
		}
		
		protected function loadTaxonomiesSuccessHandler(event:ResultEvent, token:Object = null):void {
			_view.checklistsDropDown.dataProvider = event.result as IList;
		}
		
		protected function checklistChangeHandler(event:IndexChangeEvent):void {
			_selectedTaxonomy = event.target.selectedItem;
			//reload taxonomy data
		}
		
		protected function newButtonClickHandler(event:MouseEvent):void	{
			
		}

		protected function editButtonClickHandler(event:MouseEvent):void	{
			
		}

		protected function deleteButtonClickHandler(event:MouseEvent):void	{
			
		}
		
		protected function importButtonClickHandler(event:MouseEvent):void {
			_fileReference.browse();
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
			AlertUtil.showConfirm("speciesImport.confirmImport.message", null, "speciesImport.confirmImport.title",
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
			_state = event.result as SpeciesImportStatus;
			updateView();
		}
		
		protected function startClickHandler(event:MouseEvent):void {
			var responder:AsyncResponder = new AsyncResponder(startResultHandler, faultHandler);
			var taxonomyName:String = _view.checklistsDropDown.selectedItem.name;
			_speciesImportClient.start(responder, taxonomyName);
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
			if(_state == null || _state.step == ProcessStatus$Step.INITED || 
				(_firstOpen && _state.step != ProcessStatus$Step.RUNNING) ) {
				resetView();
			} else {
				var step:ProcessStatus$Step = _state.step;
				switch ( step ) {
					case ProcessStatus$Step.INITED:
						resetView();
						break;
					case ProcessStatus$Step.PREPARING:
						_view.currentState = SpeciesImportView.STATE_LOADING;
						startProgressTimer();
						break;
					case ProcessStatus$Step.RUNNING:
						updateViewForImporting();
						startProgressTimer();
						break;
					case ProcessStatus$Step.COMPLETE:
						stopProgressTimer();
						updateViewImportCompleted();
						break;
					case ProcessStatus$Step.ERROR:
						//AlertUtil.showError("dataImport.error", [_state.errorMessage]);
						resetView();
						break;
					case ProcessStatus$Step.CANCELLED:
						AlertUtil.showError("speciesImport.cancelled");
						resetView();
						break;
					default:
						resetView();
				}
			}
			_firstOpen = false;
		}
		
		protected function updateViewImportCompleted():void {
			_view.currentState = SpeciesImportView.STATE_DEFAULT;
			//reload taxonomy
		}
		
		private function updateViewForUploading():void {
			_view.currentState = SpeciesImportView.STATE_UPLOADING;
			_view.progressTitle.text = Message.get("speciesImport.uploadingFile");
			_view.progressLabel.text = "";
		}

		private function updateViewForImporting():void {
			_view.currentState = SpeciesImportView.STATE_IMPORTING;
			_view.progressTitle.text = Message.get("speciesImport.importing");
			_view.progressBar.setProgress(_state.processedRows, _state.totalRows);
			updateProgressText("speciesImport.importingProgressLabel");
		}

		protected function updateProgressText(progressLabelResource:String):void {
			var progressText:String;
			if ( _state.totalRows == 0 ) {
				progressText = Message.get("speciesImport.processing");
			} else {
				progressText = Message.get(progressLabelResource, [_state.processedRows, _state.totalRows]);
			}
			_view.progressLabel.text = progressText;
		}
		
		protected function resetView():void {
			_state = null;
			_view.currentState = SpeciesImportView.STATE_DEFAULT;
			stopProgressTimer();
		}
		
	}
}