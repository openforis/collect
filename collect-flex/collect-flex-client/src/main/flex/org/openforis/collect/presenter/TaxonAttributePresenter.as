package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.KeyboardEvent;
	import flash.ui.Keyboard;
	
	import mx.collections.IList;
	import mx.controls.TextInput;
	import mx.core.FlexGlobals;
	import mx.events.FlexMouseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.SpeciesClient;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.TaxonInputFieldEvent;
	import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TaxonAttributeRenderer;
	import org.openforis.collect.ui.component.input.TaxonAutoCompletePopUp;
	import org.openforis.collect.util.PopUpUtil;

	CONFIG::debugging {
		import org.openforis.collect.util.itrace;
	}

	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TaxonAttributePresenter extends CompositeAttributePresenter {
		
		private static const MAX_RESULTS:int = 400;
		private static const SEARCH_BY_CODE:String = "byCode";
		private static const SEARCH_BY_SCIENTIFIC_NAME:String = "byScientificName";
		private static const SEARCH_BY_VERNACULAR_NAME:String = "byVernacularName";
		
		protected static var autoCompletePopUp:TaxonAutoCompletePopUp;
		protected static var autoCompletePopUpOpen:Boolean = false;
		protected static var autoCompleteSearchResponder:AsyncResponder;
		protected static var autoCompleteLastInputField:InputField;
		
		private var minCharsToStartAutoComplete:int = 2;
		
		private var _lastSelectedTaxon:TaxonOccurrenceProxy;
		
		public function TaxonAttributePresenter(view:TaxonAttributeRenderer) {
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			//id text input
			view.codeTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
			//scientific name text input
			view.scientificNameTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
			//vernacular name text input
			view.vernacularNameTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
			//language code text input
			view.languageCodeTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
			//language variety text input
			view.languageVarietyTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
		}
		
		private function get view():TaxonAttributeRenderer {
			return TaxonAttributeRenderer(_view);
		}
		
		protected function inputFieldChangingHandler(event:InputFieldEvent):void {
			var inputField:InputField = event.target as InputField;
			if(inputField == null) {
				return; //error
			}
			var text:String = mx.utils.StringUtil.trim(inputField.text);
			if(text.length < minCharsToStartAutoComplete) {
				return;
			}
			var searchType:String;
			switch(inputField) {
				case view.codeTextInput:
					searchType = SEARCH_BY_CODE;
					break;
				case view.scientificNameTextInput:
					searchType = SEARCH_BY_SCIENTIFIC_NAME;
					break;
				case view.vernacularNameTextInput:
					searchType = SEARCH_BY_VERNACULAR_NAME;
					break;
				default:
			}
			if(searchType != null) {
				showAutoCompletePopUp(searchType, inputField, view.codeTextInput);
			}
		}
		
		protected static function showAutoCompletePopUp(searchType:String, inputField:InputField, alignField:DisplayObject):void {
			if(autoCompletePopUp == null) {
				autoCompletePopUp = new TaxonAutoCompletePopUp();
				autoCompletePopUp.addEventListener(KeyboardEvent.KEY_DOWN, autoCompleteKeyDownHandler);
				autoCompletePopUp.addEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, autoCompleteMouseDownOutsideHandler);
				autoCompletePopUp.addEventListener(TaxonInputFieldEvent.TAXON_SELECT, taxonSelectHandler);
				autoCompleteSearchResponder = new AsyncResponder(autoCompleteSearchResultHandler, searchFaultHandler);
			}
			if(! autoCompletePopUpOpen) {
				PopUpManager.addPopUp(autoCompletePopUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				
				PopUpUtil.alignToField(autoCompletePopUp, alignField, 
					PopUpUtil.POSITION_BELOW, 
					PopUpUtil.VERTICAL_ALIGN_BOTTOM, 
					PopUpUtil.HORIZONTAL_ALIGN_LEFT,
					false
				);
				
				autoCompletePopUpOpen = true;
			}
			autoCompleteLastInputField = inputField;
			autoCompleteLastInputField.applyChangesOnFocusOut = false;
			loadAutoCompleteData(searchType, inputField);
		}
		
		protected static function loadAutoCompleteData(searchType:String, inputField:InputField):void {
			autoCompletePopUp.dataGrid.dataProvider = null;
			var client:SpeciesClient = ClientFactory.speciesClient;
			var searchText:String = inputField.text;
			switch(searchType) {
				case SEARCH_BY_CODE:
					client.findByCode(autoCompleteSearchResponder, searchText, MAX_RESULTS);
					break;
				case SEARCH_BY_SCIENTIFIC_NAME:
					client.findByScientificName(autoCompleteSearchResponder, searchText, MAX_RESULTS);
					break;
				case SEARCH_BY_VERNACULAR_NAME:
					//detect the use of qualifier
					//client.findByVernacularName(autoCompleteSearchResponder, searchText, MAX_RESULTS);					
					client.findByVernacularName(autoCompleteSearchResponder, searchText, MAX_RESULTS);	
					trace("yow.." + inputField.id);
					break;
				default:
			}
		}
		
		protected static function autoCompleteKeyDownHandler(event:KeyboardEvent):void {
			var keyCode:uint = event.keyCode;
			switch(keyCode) {
				case Keyboard.ENTER:
					taxonSelectHandler();
					break;
				case Keyboard.ESCAPE:
					cancelAutoComplete();
					break;
			}
		}
		
		protected static function autoCompleteMouseDownOutsideHandler(event:FlexMouseEvent):void {
			cancelAutoComplete();
		}
		
		protected static function closeAutoCompletePopUp():void {
			if(autoCompletePopUpOpen) {
				PopUpManager.removePopUp(autoCompletePopUp);
				autoCompletePopUpOpen = false;
				autoCompleteLastInputField.applyChangesOnFocusOut = true;
			}
		}
		
		protected static function cancelAutoComplete():void {
			var textInput:TextInput = autoCompleteLastInputField.textInput as TextInput;
			textInput.setFocus();
			InputFieldPresenter(autoCompleteLastInputField.presenter).undoLastChange();
			closeAutoCompletePopUp();
		}
		
		protected static function taxonSelectHandler(event:TaxonInputFieldEvent = null):void {
			var taxon:TaxonOccurrenceProxy;
			if(event != null) {
				taxon = event.taxon;
			} else {
				taxon = autoCompletePopUp.dataGrid.selectedItem as TaxonOccurrenceProxy;
			}
			if(taxon != null) {
				var renderer:TaxonAttributeRenderer = autoCompleteLastInputField.parentDocument as TaxonAttributeRenderer;
				var presenter:TaxonAttributePresenter = renderer.presenter as TaxonAttributePresenter;
				presenter.performSelectTaxon(taxon);
			}			
			closeAutoCompletePopUp();
		}
		
		public function performSelectTaxon(taxonOccurrence:TaxonOccurrenceProxy):void {
			_lastSelectedTaxon = taxonOccurrence;
			var req:UpdateRequest = new UpdateRequest();
			view.codeTextInput.text = taxonOccurrence.code;
			req.addOperation(view.codeTextInput.presenter.createUpdateValueOperation());
			
			view.scientificNameTextInput.text = taxonOccurrence.scientificName;
			req.addOperation(view.scientificNameTextInput.presenter.createUpdateValueOperation());
			
			view.vernacularNameTextInput.text = taxonOccurrence.vernacularName;
			req.addOperation(view.vernacularNameTextInput.presenter.createUpdateValueOperation());
			
			view.languageCodeTextInput.text = taxonOccurrence.languageCode;
			req.addOperation(view.languageCodeTextInput.presenter.createUpdateValueOperation());

			view.languageVarietyTextInput.text = taxonOccurrence.languageVariety;
			req.addOperation(view.languageVarietyTextInput.presenter.createUpdateValueOperation());
			
			ClientFactory.dataClient.updateActiveRecord(req, null, faultHandler);
		}
		
		protected static function autoCompleteSearchResultHandler(event:ResultEvent, token:Object):void {
			var data:IList = event.result as IList;
			autoCompletePopUp.dataGrid.dataProvider = data;
		}
		
		protected static function searchFaultHandler(event:FaultEvent, token:Object = null):void {
			faultHandler(event, token);
		}
	}
}
