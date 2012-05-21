package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.FocusEvent;
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
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TaxonAttributeRenderer;
	import org.openforis.collect.ui.component.input.TaxonAutoCompletePopUp;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import mx.managers.IFocusManagerComponent;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TaxonAttributePresenter extends CompositeAttributePresenter {
		
		private static const MAX_RESULTS:int = 400;
		private static const MIN_CHARS_TO_START_AUTOCOMPLETE:int = 2;
		private static const SEARCH_BY_CODE:String = "byCode";
		private static const SEARCH_BY_SCIENTIFIC_NAME:String = "byScientificName";
		private static const SEARCH_BY_VERNACULAR_NAME:String = "byVernacularName";
		
		private static const UNKNOWN_ITEM:Object = {code: "UNK", scientificName: Message.get("edit.taxon.unknown")};
		private static const UNLISTED_ITEM:Object = {code: "UNL", scientificName: Message.get("edit.taxon.unlisted")};
		
		protected static var autoCompletePopUp:TaxonAutoCompletePopUp;
		protected static var autoCompletePopUpOpened:Boolean = false;
		protected static var autoCompleteSearchResponder:AsyncResponder;
		protected static var autoCompleteLastInputField:InputField;
		
		private var _lastSelectedTaxon:Object;
		private var _lastSearchType:String;
		
		public function TaxonAttributePresenter(view:TaxonAttributeRenderer) {
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			//id text input
			view.codeTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
			view.codeTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
			view.codeTextInput.textInput.addEventListener(KeyboardEvent.KEY_DOWN, inputFieldKeyDownHandler);
			//scientific name text input
			view.scientificNameTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
			view.scientificNameTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
			view.scientificNameTextInput.textInput.addEventListener(KeyboardEvent.KEY_DOWN, inputFieldKeyDownHandler);
			//vernacular name text input
			view.vernacularNameTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
			view.vernacularNameTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
			view.vernacularNameTextInput.textInput.addEventListener(KeyboardEvent.KEY_DOWN, inputFieldKeyDownHandler);
			//language code text input
			view.languageCodeTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
			//language variety text input
			view.languageVarietyTextInput.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
		}
		
		protected function inputFieldFocusOutHandler(event:FocusEvent):void {
			var inputField:InputField = event.target.document;
			if ( inputField != null && inputField.changed && inputField != view.codeTextInput && ! inputField.isEmpty() &&
					view.codeTextInput.isEmpty() && ! autoCompletePopUpOpened ) {
				view.codeTextInput.text = UNLISTED_ITEM.code;
				view.codeTextInput.presenter.updateValue();
			}
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
			if(text.length < MIN_CHARS_TO_START_AUTOCOMPLETE) {
				return;
			}
			switch(inputField) {
				case view.codeTextInput:
					_lastSearchType = SEARCH_BY_CODE;
					break;
				case view.scientificNameTextInput:
					_lastSearchType = SEARCH_BY_SCIENTIFIC_NAME;
					break;
				case view.vernacularNameTextInput:
					_lastSearchType = SEARCH_BY_VERNACULAR_NAME;
					break;
				default:
					_lastSearchType = null;
			}
			if(_lastSearchType != null) {
				showAutoCompletePopUp(_lastSearchType, inputField, view.codeTextInput);
			}
		}
		
		protected function inputFieldKeyDownHandler(event:KeyboardEvent):void {
			switch ( event.keyCode ) {
				case Keyboard.DOWN:
					if ( autoCompletePopUpOpened ) {
						autoCompletePopUp.dataGrid.setFocus();
						if ( CollectionUtil.isNotEmpty(autoCompletePopUp.dataGrid.dataProvider) ) {
							autoCompletePopUp.dataGrid.selectedIndex = 0;
						}
					}
					break;
				case Keyboard.ESCAPE:
					closeAutoCompletePopUp();
					break;
				case Keyboard.TAB:
					if ( autoCompletePopUpOpened ) {
						closeAutoCompletePopUp();
						/*var nextFocusManagerComponent:IFocusManagerComponent = _view.focusManager.getNextFocusManagerComponent(event.shiftKey);
						if ( nextFocusManagerComponent != null ) {
							nextFocusManagerComponent.setFocus();
						}*/
					}
					break;
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
			if(! autoCompletePopUpOpened) {
				PopUpManager.addPopUp(autoCompletePopUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				
				PopUpUtil.alignToField(autoCompletePopUp, alignField, 
					PopUpUtil.POSITION_BELOW, 
					PopUpUtil.VERTICAL_ALIGN_BOTTOM, 
					PopUpUtil.HORIZONTAL_ALIGN_LEFT,
					false
				);
				
				autoCompletePopUpOpened = true;
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
					client.findByVernacularName(autoCompleteSearchResponder, searchText, MAX_RESULTS);
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
			if ( autoCompletePopUpOpened ) {
				PopUpManager.removePopUp(autoCompletePopUp);
				autoCompletePopUpOpened = false;
				autoCompleteLastInputField.applyChangesOnFocusOut = true;
				var textInput:TextInput = autoCompleteLastInputField.textInput as TextInput;
				textInput.setFocus();
			}
		}
		
		protected static function cancelAutoComplete():void {
			closeAutoCompletePopUp();
		}
		
		protected static function taxonSelectHandler(event:TaxonInputFieldEvent = null):void {
			var taxon:Object;
			if(event != null) {
				taxon = event.taxon;
			} else {
				taxon = autoCompletePopUp.dataGrid.selectedItem;
			}
			if(taxon != null) {
				var renderer:TaxonAttributeRenderer = autoCompleteLastInputField.parentDocument as TaxonAttributeRenderer;
				var presenter:TaxonAttributePresenter = renderer.presenter as TaxonAttributePresenter;
				presenter.performSelectTaxon(taxon);
			}
			closeAutoCompletePopUp();
		}
		
		public function performSelectTaxon(taxonOccurrence:Object):void {
			_lastSelectedTaxon = taxonOccurrence;
			var req:UpdateRequest = new UpdateRequest();
			//update code
			view.codeTextInput.text = taxonOccurrence.code;
			req.addOperation(view.codeTextInput.presenter.createUpdateValueOperation());
			
			if ( ( taxonOccurrence != UNKNOWN_ITEM && taxonOccurrence != UNLISTED_ITEM )  || 
				_lastSearchType != SEARCH_BY_SCIENTIFIC_NAME && _lastSearchType != SEARCH_BY_VERNACULAR_NAME ) {
				//update scientific name
				view.scientificNameTextInput.text = taxonOccurrence.scientificName;
				//update vernacular name
				view.vernacularNameTextInput.text = taxonOccurrence.vernacularName;
				//update language code
				view.languageCodeTextInput.text = taxonOccurrence.languageCode;
				//update language variety
				view.languageVarietyTextInput.text = taxonOccurrence.languageVariety;
			}
			req.addOperation(view.scientificNameTextInput.presenter.createUpdateValueOperation());
			req.addOperation(view.vernacularNameTextInput.presenter.createUpdateValueOperation());
			req.addOperation(view.languageCodeTextInput.presenter.createUpdateValueOperation());
			req.addOperation(view.languageVarietyTextInput.presenter.createUpdateValueOperation());
			ClientFactory.dataClient.updateActiveRecord(req, null, faultHandler);
		}
		
		protected static function autoCompleteSearchResultHandler(event:ResultEvent, token:Object):void {
			var data:IList = event.result as IList;
			if ( CollectionUtil.isEmpty(data) ) {
				data.addItem(UNKNOWN_ITEM);
				data.addItem(UNLISTED_ITEM);
			}
			autoCompletePopUp.dataGrid.dataProvider = data;
		}
		
		protected static function searchFaultHandler(event:FaultEvent, token:Object = null):void {
			faultHandler(event, token);
		}
	}
}
