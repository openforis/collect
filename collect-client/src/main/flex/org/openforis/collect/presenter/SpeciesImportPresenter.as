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
	
	import mx.collections.IList;
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
	import org.openforis.collect.metamodel.proxy.TaxonSummariesProxy;
	import org.openforis.collect.metamodel.proxy.TaxonSummaryProxy;
	import org.openforis.collect.model.proxy.TaxonomyProxy;
	import org.openforis.collect.remoting.service.speciesImport.proxy.SpeciesImportStatusProxy;
	import org.openforis.collect.ui.component.datagrid.PaginationBar;
	import org.openforis.collect.ui.component.speciesImport.TaxonomyEditPopUp;
	import org.openforis.collect.ui.view.SpeciesImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	
	import spark.components.gridClasses.GridColumn;
	import spark.events.IndexChangeEvent;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class SpeciesImportPresenter extends AbstractPresenter {
		
		private static const PROGRESS_UPDATE_DELAY:int = 2000;
		private static const VALID_NAME_REGEX:RegExp = /^[a-z][a-z0-9_]*$/;
		private static const IMPORT_FILE_NAME_PREFIX:Object = "species";
		private static const ALLOWED_IMPORT_FILE_EXTENSIONS:Array = ["*.csv"];
		private static const MAX_SUMMARIES_PER_PAGE:int = 20;
		private static const FIXED_SUMMARY_COLUMNS_LENGTH:int = 3;
		private static const VERANCULAR_NAMES_SEPARATOR:String = ", ";
		
		private var _view:SpeciesImportView;
		private var _fileReference:FileReference;
		private var _speciesImportClient:SpeciesImportClient;
		private var _speciesClient:SpeciesClient;
		private var _progressTimer:Timer;
		private var _state:SpeciesImportStatusProxy;
		
		private var _getStatusResponder:IResponder;
		private var _firstOpen:Boolean;
		private var _selectedTaxonomy:TaxonomyProxy;
		private var taxonomyEditPopUp:TaxonomyEditPopUp; 
		private var fileFilter:FileFilter;
		
		public function SpeciesImportPresenter(view:SpeciesImportView) {
			this._view = view;
			_firstOpen = true;
			_fileReference = new FileReference();
			initFileFilter();
			_speciesImportClient = ClientFactory.speciesImportClient;
			_speciesClient = ClientFactory.speciesClient;
			
			_getStatusResponder = new AsyncResponder(getStatusResultHandler, faultHandler);
			
			super();
			
			//try to see if there is a process still running
			_view.currentState = SpeciesImportView.STATE_LOADING;
			_view.paginationBar.maxRecordsPerPage = MAX_SUMMARIES_PER_PAGE;
			updateStatus();
			loadTaxonomies();
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
			
			_view.closeButton.addEventListener(MouseEvent.CLICK, closeButtonClickHandler);
			_view.checklistsDropDown.addEventListener(IndexChangeEvent.CHANGE, checklistChangeHandler);
			_view.newButton.addEventListener(MouseEvent.CLICK, newButtonClickHandler);
			_view.editButton.addEventListener(MouseEvent.CLICK, editButtonClickHandler);
			_view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
			_view.errorsOkButton.addEventListener(MouseEvent.CLICK, errorsOkButtonClickHandler);
			
			_view.importButton.addEventListener(MouseEvent.CLICK, importButtonClickHandler);
			//_view.exportButton.addEventListener(MouseEvent.CLICK, exportButtonClickHandler);
			
			_view.cancelImportButton.addEventListener(MouseEvent.CLICK, cancelClickHandler);
			
			_view.paginationBar.addEventListener(PaginationBarEvent.PAGE_CHANGE, summaryPageChangeHandler);
		}
		
		protected function summaryPageChangeHandler(event:PaginationBarEvent):void {
			loadSummaries(event.offset);
		}
		
		protected function loadTaxonomies():void {
			var responder:IResponder = new AsyncResponder(loadTaxonomiesResultHandler, faultHandler);
			_speciesClient.loadAllTaxonomies(responder);
		}
		
		protected function loadTaxonomiesResultHandler(event:ResultEvent, token:Object = null):void {
			var taxonomies:IList = event.result as IList;
			_view.checklistsDropDown.dataProvider = taxonomies;
			if ( _selectedTaxonomy != null && CollectionUtil.isNotEmpty(taxonomies) ) {
				selectTaxonomyInView(_selectedTaxonomy);
				loadSummaries();
			}
		}
		
		protected function selectTaxonomyInView(taxonomy:TaxonomyProxy):void {
			var taxonomies:IList = _view.checklistsDropDown.dataProvider;
			var item:TaxonomyProxy = CollectionUtil.getItem(taxonomies, "id", taxonomy.id);
			_view.checklistsDropDown.selectedItem = item;
			_selectedTaxonomy = item;
		}
		
		protected function checklistChangeHandler(event:IndexChangeEvent):void {
			_selectedTaxonomy = event.target.selectedItem;
			_view.paginationBar.showPage(1);
			loadSummaries();
		}
		
		protected function loadSummaries(offset:int = 0):void {
			if ( offset == 0 ) {
				_view.paginationBar.showPage(1);
			}
			_view.summaryDataGrid.dataProvider = null;
			
			_view.paginationBar.currentState = PaginationBar.LOADING_STATE;
			var taxonomyId:int = _selectedTaxonomy.id;
			var responder:IResponder = new AsyncResponder(loadTaxonSummariesResultHandler, faultHandler);
			_speciesClient.loadTaxonSummaries(responder, taxonomyId, offset, MAX_SUMMARIES_PER_PAGE);
		}
		
		protected function loadTaxonSummariesResultHandler(event:ResultEvent, token:Object = null):void {
			var result:TaxonSummariesProxy = TaxonSummariesProxy(event.result);
			var records:IList = result.summaries;
			updateSummaryColumns(result.vernacularNamesLanguageCodes);
			_view.summaryDataGrid.dataProvider = records;
			_view.paginationBar.totalRecords = result.totalCount;
		}
		
		protected function updateSummaryColumns(vernacularNamesLangCodes:IList):void {
			var columns:IList = _view.summaryDataGrid.columns;
			for (var i:int = columns.length - 1; i > FIXED_SUMMARY_COLUMNS_LENGTH - 1; i --) {
				columns.removeItemAt(i);
			}
			for each (var langCode:String in vernacularNamesLangCodes) {
				var col:GridColumn = new GridColumn();
				col.headerText = getLanguageCodeHeaderText(langCode);
				col.dataField = langCode;
				col.labelFunction = vernacularNamesLabelFunction;
				col.width = 100;
				columns.addItem(col);
			}
		}
		
		private function getLanguageCodeHeaderText(langCode:String):String {
			if ( StringUtil.isBlank(langCode) ) {
				return Message.get(MessageKeys.SYNONYM);
			} else {
				var languageLabel:String = Languages.getLanguageLabel(langCode);
				if ( languageLabel != null ) {
					return languageLabel + " (" + langCode + ")";
				} else {
					return langCode;
				}
			}
		}
		
		public function vernacularNamesLabelFunction(item:TaxonSummaryProxy, gridColumn:GridColumn):String {
			var names:IList = item.languageToVernacularNames.get(gridColumn.dataField);
			return StringUtil.concat(VERANCULAR_NAMES_SEPARATOR, names);
		}
		
		protected function newButtonClickHandler(event:MouseEvent):void	{
			openTaxonomyEditPopUp(true);
		}
		
		protected function openTaxonomyEditPopUp(newTaxonomy:Boolean = false):void {
			taxonomyEditPopUp = TaxonomyEditPopUp(PopUpUtil.createPopUp(TaxonomyEditPopUp));
			taxonomyEditPopUp.newTaxonomy = newTaxonomy;
			taxonomyEditPopUp.okButton.addEventListener(MouseEvent.CLICK, taxonomyEditOkClickHandler);
			taxonomyEditPopUp.cancelButton.addEventListener(MouseEvent.CLICK, taxonomyEditCancelClickHandler);
		}
		
		protected function closeTaxonomyEditPopUp():void {
			if ( taxonomyEditPopUp != null ) {
				PopUpManager.removePopUp(taxonomyEditPopUp);
			}
		}
		
		protected function editButtonClickHandler(event:MouseEvent):void {
			if ( checkTaxonomyIsSelected() ) {
				openTaxonomyEditPopUp(false);
				taxonomyEditPopUp.nameTextInput.text = _selectedTaxonomy.name;
			}
		}

		protected function deleteButtonClickHandler(event:MouseEvent):void	{
			if ( checkTaxonomyIsSelected() ) {
				AlertUtil.showConfirm(MessageKeys.CONFIRM_DELETE_TAXONOMY, null, null, performDeleteSelectedTaxonomy);
			}
		}
		
		protected function performDeleteSelectedTaxonomy():void {
			var responder:IResponder = new AsyncResponder(deleteTaxonomyResultHandler, faultHandler);
			_speciesClient.deleteTaxonomy(responder, _selectedTaxonomy);
		}
		
		protected function deleteTaxonomyResultHandler(event:ResultEvent, token:Object = null):void {
			_selectedTaxonomy = null;
			loadTaxonomies();
		}
		
		protected function taxonomyEditCancelClickHandler(event:MouseEvent):void {
			closeTaxonomyEditPopUp();
		}
		
		protected function taxonomyEditOkClickHandler(event:MouseEvent):void {
			var newTaxonomy:Boolean = taxonomyEditPopUp.newTaxonomy;
			var name:String = taxonomyEditPopUp.nameTextInput.text;
			name = StringUtil.trim(name);
			if ( checkValidTaxonomyName(name, newTaxonomy) ) {
				if ( newTaxonomy ) {
					_selectedTaxonomy = new TaxonomyProxy();
				} 
				_selectedTaxonomy.name = name;
				
				var responder:IResponder = new AsyncResponder(taxonomySaveResultHandler, faultHandler);
				_speciesClient.saveTaxonomy(responder, _selectedTaxonomy);
			}
		}
		
		protected function checkValidTaxonomyName(name:String, newTaxonomy:Boolean):Boolean {
			if ( VALID_NAME_REGEX.test(name) ) {
				return checkDuplicateTaxonomyName(name, newTaxonomy);
			} else {
				AlertUtil.showMessage(MessageKeys.INVALID_TAXONOMY_NAME);
				return false;
			}
		}
		
		protected function checkDuplicateTaxonomyName(name:String, newTaxonomy:Boolean):Boolean {
			var taxonomies:IList = _view.checklistsDropDown.dataProvider;
			var duplicateTaxonomy:TaxonomyProxy = CollectionUtil.getItem(taxonomies, "name", name);
			if ( duplicateTaxonomy != null && (newTaxonomy || duplicateTaxonomy != _selectedTaxonomy )) {
				AlertUtil.showError(MessageKeys.DUPLICATE_TAXONOMY_NAME);
				return false;
			} else {
				return true;
			}
		}
		
		protected function taxonomySaveResultHandler(event:ResultEvent, token:Object = null):void {
			var taxonomy:TaxonomyProxy = event.result as TaxonomyProxy;
			_selectedTaxonomy = taxonomy;
			closeTaxonomyEditPopUp();
			loadTaxonomies();
		}
		
		protected function checkTaxonomyIsSelected():Boolean {
			if ( _selectedTaxonomy != null ) {
				return true;
			} else {
				AlertUtil.showMessage(MessageKeys.SELECT_TAXONOMY);
				return false;
			}
		}
		
		protected function importButtonClickHandler(event:MouseEvent):void {
			if ( checkTaxonomyIsSelected() ) {
				_fileReference.browse([fileFilter]);
			}
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
					var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
					_speciesImportClient.cancel(responder);
					break;
			}
			_view.currentState = SpeciesImportView.STATE_DEFAULT;
		}
		
		protected function closeButtonClickHandler(event:MouseEvent):void {
			if ( _state == null || _state.step == ProcessStatus$Step.RUN ) {
				closePopUp();
			} else {
				AlertUtil.showConfirm(MessageKeys.CONFIRM_CLOSE, null, MessageKeys.CONFIRM_CLOSE_TITLE, performCancelThenClose);
			}
		}
		
		protected function performCancelThenClose():void {
			var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			_speciesImportClient.cancel(responder);
			
			function cancelResultHandler(event:ResultEvent, token:Object = null):void {
				closePopUp();
			}
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
			var responder:AsyncResponder = new AsyncResponder(startResultHandler, faultHandler);
			_speciesImportClient.start(responder, _selectedTaxonomy.name, true);
			startProgressTimer();
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
			_speciesImportClient.getStatus(_getStatusResponder);
		}
		
		protected function getStatusResultHandler(event:ResultEvent, token:Object = null):void {
			_state = event.result as SpeciesImportStatusProxy;
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
			_view.progressTitle.text = Message.get(MessageKeys.UPLOADING_FILE);
			_view.progressLabel.text = "";
		}
		
		protected function updateViewForRunning():void {
			_view.currentState = SpeciesImportView.STATE_IMPORTING;
			_view.progressTitle.text = Message.get(MessageKeys.IMPORTING);
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
			_view.progressLabel.text = progressText;
		}
		
		protected function resetView():void {
			_state = null;
			_view.currentState = SpeciesImportView.STATE_DEFAULT;
			stopProgressTimer();
		}
		
	}
}

class MessageKeys {
	public static const SELECT_TAXONOMY:String = "speciesImport.selectTaxonomy";
	public static const INVALID_TAXONOMY_NAME:String = "speciesImport.invalidTaxonomyName";
	public static const DUPLICATE_TAXONOMY_NAME:String = "speciesImport.duplicateTaxonomyName";
	public static const CONFIRM_DELETE_TAXONOMY:String = "speciesImport.confirmDeleteSeletectedTaxonomy";
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
	public static const CONFIRM_CLOSE:String = "speciesImport.confirmClose.message";
	public static const CONFIRM_CLOSE_TITLE:String = "speciesImport.confirmClose.title";
	public static const SYNONYM:String = "speciesImport.summaryList.synonym";
}