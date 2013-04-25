package org.openforis.collect.presenter {
	
	import flash.events.MouseEvent;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.navigateToURL;
	
	import mx.collections.IList;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.SpeciesClient;
	import org.openforis.collect.client.SpeciesImportClient;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Languages;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.TaxonSummariesProxy;
	import org.openforis.collect.metamodel.proxy.TaxonSummaryProxy;
	import org.openforis.collect.model.proxy.TaxonomyProxy;
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
	public class SpeciesImportPresenter extends AbstractReferenceDataImportPresenter {
		
		private static const MAX_SUMMARIES_PER_PAGE:int = 20;
		private static const LATIN_LANGUAGE_CODE:String = "lat";
		private static const UPLOAD_FILE_NAME_PREFIX:String = "species";
		private static const VALID_NAME_REGEX:RegExp = /^[a-z][a-z0-9_]*$/;
		private static const FIXED_SUMMARY_COLUMNS_LENGTH:int = 3;
		private static const VERANCULAR_NAMES_SEPARATOR:String = ", ";
		
		private var _speciesImportClient:SpeciesImportClient;
		private var _speciesClient:SpeciesClient;
		private var _selectedTaxonomy:TaxonomyProxy;
		private var _taxonomyEditPopUp:TaxonomyEditPopUp; 
		
		public function SpeciesImportPresenter(view:SpeciesImportView) {
			_speciesImportClient = ClientFactory.speciesImportClient;
			_speciesClient = ClientFactory.speciesClient;
			super(view, new MessageKeys(), UPLOAD_FILE_NAME_PREFIX);
			view.importFileFormatInfo = Message.get(messageKeys.IMPORT_FILE_FORMAT_INFO);
		}
		
		private function get view():SpeciesImportView {
			return SpeciesImportView(_view);
		}
		
		private function get messageKeys():MessageKeys {
			return MessageKeys(_messageKeys);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			view.checklistsDropDown.addEventListener(IndexChangeEvent.CHANGE, checklistChangeHandler);
			view.newButton.addEventListener(MouseEvent.CLICK, newButtonClickHandler);
			view.editButton.addEventListener(MouseEvent.CLICK, editButtonClickHandler);
			view.deleteButton.addEventListener(MouseEvent.CLICK, deleteButtonClickHandler);
		}
		
		override protected function loadInitialData():void {
			updateStatus();
			loadTaxonomies();
		}
		
		override protected function browseFileToImport():void {
			super.browseFileToImport();
		}
		
		override protected function exportButtonClickHandler(event:MouseEvent):void {
			var request:URLRequest = getExportUrlRequest();
			if(request != null) {
				navigateToURL(request, "_new");
			}
		}
		
		protected function getExportUrlRequest():URLRequest {
			if ( _selectedTaxonomy == null ) {
				AlertUtil.showError(messageKeys.SELECT_TAXONOMY);
				return null;
			} else {
				var selectedTaxonomyId:Number = _selectedTaxonomy.id;
				var url:String = ApplicationConstants.getSpeciesExportUrl(selectedTaxonomyId);
				var request:URLRequest = new URLRequest(url);
				request.method = URLRequestMethod.GET;
				return request;
			}
		}
		
		protected function loadTaxonomies():void {
			var responder:IResponder = new AsyncResponder(loadTaxonomiesResultHandler, faultHandler);
			_speciesClient.loadTaxonomiesBySurvey(responder, view.surveyId, view.work);
		}
		
		protected function loadTaxonomiesResultHandler(event:ResultEvent, token:Object = null):void {
			var taxonomies:IList = event.result as IList;
			view.checklistsDropDown.dataProvider = taxonomies;
			if ( _selectedTaxonomy != null && CollectionUtil.isNotEmpty(taxonomies) ) {
				selectTaxonomyInView(_selectedTaxonomy);
				loadSummaries();
			}
		}
		
		protected function selectTaxonomyInView(taxonomy:TaxonomyProxy):void {
			var taxonomies:IList = view.checklistsDropDown.dataProvider;
			var item:TaxonomyProxy = CollectionUtil.getItem(taxonomies, "id", taxonomy.id);
			view.checklistsDropDown.selectedItem = item;
			_selectedTaxonomy = item;
		}
		
		protected function checklistChangeHandler(event:IndexChangeEvent):void {
			_selectedTaxonomy = event.target.selectedItem;
			_view.paginationBar.showPage(1);
			loadSummaries();
		}
		
		override protected function performSummariesLoad(offset:int = 0):void {
			var surveyId:int = view.surveyId;
			var taxonomyId:int = _selectedTaxonomy.id;
			var work:Boolean = view.work;
			var responder:IResponder = new AsyncResponder(loadSummariesResultHandler, faultHandler);
			_speciesClient.loadTaxonSummaries(responder, taxonomyId, offset, MAX_SUMMARIES_PER_PAGE);
		}
		
		override protected function loadSummariesResultHandler(event:ResultEvent, token:Object=null):void {
			var result:TaxonSummariesProxy = event.result as TaxonSummariesProxy;
			updateSummaryColumns(result.vernacularNamesLanguageCodes);
			_view.summaryDataGrid.dataProvider = result.summaries;
			_view.paginationBar.totalRecords = result.totalCount;
		}
		
		protected function updateSummaryColumns(vernacularNamesLangCodes:IList):void {
			var columns:IList = _view.summaryDataGrid.columns;
			for (var i:int = columns.length - 1; i > FIXED_SUMMARY_COLUMNS_LENGTH - 1; i --) {
				columns.removeItemAt(i);
			}
			sortVernacularLanguageCodes(vernacularNamesLangCodes);
			for each (var langCode:String in vernacularNamesLangCodes) {
				var col:GridColumn = new GridColumn();
				col.headerText = getLanguageCodeHeaderText(langCode);
				col.dataField = langCode;
				col.labelFunction = vernacularNamesLabelFunction;
				col.width = 100;
				columns.addItem(col);
			}
		}
		
		protected function sortVernacularLanguageCodes(vernacularNamesLangCodes:IList):void {
			if ( CollectionUtil.contains(vernacularNamesLangCodes, LATIN_LANGUAGE_CODE) ) {
				CollectionUtil.moveItem(vernacularNamesLangCodes, LATIN_LANGUAGE_CODE, 0);
			}
		}
		
		private function getLanguageCodeHeaderText(langCode:String):String {
			if ( StringUtil.isBlank(langCode) || langCode == LATIN_LANGUAGE_CODE ) {
				return Message.get(messageKeys.SYNONYM);
			} else {
				var languageLabel:String = Languages.getLanguageLabel(langCode);
				if ( languageLabel == null ) {
					return langCode;
				} else {
					return languageLabel + " (" + langCode + ")";
				}
			}
		}
		
		public function vernacularNamesLabelFunction(item:TaxonSummaryProxy, gridColumn:GridColumn):String {
			var names:IList = item.languageToVernacularNames.get(gridColumn.dataField);
			return StringUtil.concat(VERANCULAR_NAMES_SEPARATOR, names);
		}
		
		protected function newButtonClickHandler(event:MouseEvent):void	{
			if ( checkIsWork() ) {
				openTaxonomyEditPopUp(true);
			}
		}
		
		protected function openTaxonomyEditPopUp(newTaxonomy:Boolean = false):void {
			_taxonomyEditPopUp = TaxonomyEditPopUp(PopUpUtil.createPopUp(TaxonomyEditPopUp));
			_taxonomyEditPopUp.newTaxonomy = newTaxonomy;
			_taxonomyEditPopUp.okButton.addEventListener(MouseEvent.CLICK, taxonomyEditOkClickHandler);
			_taxonomyEditPopUp.cancelButton.addEventListener(MouseEvent.CLICK, taxonomyEditCancelClickHandler);
		}
		
		protected function closeTaxonomyEditPopUp():void {
			if ( _taxonomyEditPopUp != null ) {
				PopUpManager.removePopUp(_taxonomyEditPopUp);
			}
		}
		
		protected function editButtonClickHandler(event:MouseEvent):void {
			if ( checkTaxonomyIsSelected() && checkIsWork() ) {
				openTaxonomyEditPopUp(false);
				_taxonomyEditPopUp.nameTextInput.text = _selectedTaxonomy.name;
			}
		}

		protected function deleteButtonClickHandler(event:MouseEvent):void	{
			if ( checkTaxonomyIsSelected() && checkIsWork() ) {
				_speciesClient.isTaxonomyInUse(new AsyncResponder(checkTaxonomyIsInUseResultHandler, faultHandler), _selectedTaxonomy.name);
				
				function checkTaxonomyIsInUseResultHandler(event:ResultEvent, token:Object = null):void {
					var inUse:Boolean = event.result as Boolean;
					var messageKey:String = inUse ? messageKeys.CONFIRM_DELETE_IN_USE_TAXONOMY : messageKeys.CONFIRM_DELETE_TAXONOMY;
					AlertUtil.showConfirm(messageKey, null, null, performDeleteSelectedTaxonomy);
				}
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
			var newTaxonomy:Boolean = _taxonomyEditPopUp.newTaxonomy;
			var name:String = _taxonomyEditPopUp.nameTextInput.text;
			name = StringUtil.trim(name);
			if ( checkValidTaxonomyName(name, newTaxonomy) ) {
				if ( newTaxonomy ) {
					_selectedTaxonomy = new TaxonomyProxy();
				} 
				_selectedTaxonomy.surveyWorkId = view.surveyId;
				_selectedTaxonomy.name = name;
				
				var responder:IResponder = new AsyncResponder(taxonomySaveResultHandler, faultHandler);
				_speciesClient.saveTaxonomy(responder, _selectedTaxonomy);
			}
		}
		
		protected function checkValidTaxonomyName(name:String, newTaxonomy:Boolean):Boolean {
			if ( VALID_NAME_REGEX.test(name) ) {
				return checkDuplicateTaxonomyName(name, newTaxonomy);
			} else {
				AlertUtil.showMessage(messageKeys.INVALID_TAXONOMY_NAME);
				return false;
			}
		}
		
		protected function checkDuplicateTaxonomyName(name:String, newTaxonomy:Boolean):Boolean {
			var taxonomies:IList = view.checklistsDropDown.dataProvider;
			var duplicateTaxonomy:TaxonomyProxy = CollectionUtil.getItem(taxonomies, "name", name);
			if ( duplicateTaxonomy != null && (newTaxonomy || duplicateTaxonomy != _selectedTaxonomy )) {
				AlertUtil.showError(messageKeys.DUPLICATE_TAXONOMY_NAME);
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
		
		override protected function checkCanImport():Boolean {
			var result:Boolean = checkIsWork();
			if ( result ) {
				result = checkTaxonomyIsSelected();
			}
			return result;
		}
		
		protected function checkIsWork():Boolean {
			if ( view.work ) {
				return true;
			} else {
				AlertUtil.showMessage(messageKeys.SAVE_SURVEY_BEFORE_EDIT);
				return false;
			}
		}
		
		protected function checkTaxonomyIsSelected():Boolean {
			if ( _selectedTaxonomy != null ) {
				return true;
			} else {
				AlertUtil.showMessage(messageKeys.SELECT_TAXONOMY);
				return false;
			}
		}
		
		override protected function performImportCancel():void {
			var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			_speciesImportClient.cancel(responder);
		}
		
		override protected function performCancelThenClose():void {
			var responder:AsyncResponder = new AsyncResponder(cancelResultHandler, faultHandler);
			_speciesImportClient.cancel(responder);
			
			function cancelResultHandler(event:ResultEvent, token:Object = null):void {
				closePopUp();
			}
		}
		
		override protected function closePopUp():void {
			eventDispatcher.dispatchEvent(new UIEvent(UIEvent.CLOSE_SPECIES_IMPORT_POPUP));
		}
		
		override protected function performProcessStart():void {
			var responder:AsyncResponder = new AsyncResponder(startResultHandler, faultHandler);
			var taxonomyId:int = _selectedTaxonomy.id;
			var surveyId:int = view.surveyId;
			var work:Boolean = view.work;
			_speciesImportClient.start(responder, taxonomyId, true);
		}
		
		override protected function updateStatus():void {
			_speciesImportClient.getStatus(_getStatusResponder);
		}
		
	}
}
import org.openforis.collect.presenter.ReferenceDataImportMessageKeys;

class MessageKeys extends ReferenceDataImportMessageKeys {
	
	public function get SELECT_TAXONOMY():String {
		return "speciesImport.selectTaxonomy";
	}
	
	override public function get CONFIRM_IMPORT_TITLE():String {
		return "speciesImport.confirmImport.title";
	}
	
	override public function get CONFIRM_IMPORT():String {
		return "speciesImport.confirmImport.message";
	}
	
	public function get DUPLICATE_TAXONOMY_NAME():String {
		return "speciesImport.duplicateTaxonomyName";
	}

	public function get CONFIRM_DELETE_TAXONOMY():String {
		return "speciesImport.confirmDeleteSeletectedTaxonomy";
	}
	
	public function get CONFIRM_DELETE_IN_USE_TAXONOMY():String {
		return "speciesImport.confirmDeleteSeletectedInUseTaxonomy";
	}
	
	public function get INVALID_TAXONOMY_NAME():String {
		return "speciesImport.invalidTaxonomyName";
	}
	
	public function get SYNONYM():String {
		return "speciesImport.summaryList.synonym";
	}
	
	public function get SAVE_SURVEY_BEFORE_EDIT():String {
		return "speciesImport.saveSurveyBeforeEdit";
	}
	
	public function get IMPORT_FILE_FORMAT_INFO():String {
		return "speciesImport.importFileFormatInfo";
	}
}
