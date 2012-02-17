package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.ui.Keyboard;
	
	import mx.collections.ArrayCollection;
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
	import org.openforis.collect.client.TaxonClient;
	import org.openforis.collect.event.TaxonInputFieldEvent;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
	import org.openforis.collect.model.proxy.TaxonProxy;
	import org.openforis.collect.model.proxy.TaxonVernacularNameProxy;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TaxonAutoCompletePopUp;
	import org.openforis.collect.ui.component.input.TaxonInputField;
	import org.openforis.collect.ui.component.input.TaxonSearchPopUp;
	import org.openforis.collect.util.PopUpUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TaxonInputFieldPresenter extends InputFieldPresenter {
		
		private static const MAX_RESULTS:int = 10;
		private static const SEARCH_BY_CODE:String = "byCode";
		private static const SEARCH_BY_SCIENTIFIC_NAME:String = "byScientificName";
		private static const SEARCH_BY_VERNACULAR_NAME:String = "byVernacularName";
		
		protected static var autoCompletePopUp:TaxonAutoCompletePopUp;
		protected static var autoCompletePopUpOpen:Boolean = false;
		protected static var autoCompleteSearchResponder:AsyncResponder;
		protected static var autoCompleteLastSearchTextInput:TextInput;
		
		private var minCharsToStartAutoComplete:int = 2;
		
		private var _view:TaxonInputField;
		
		public function TaxonInputFieldPresenter(view:TaxonInputField) {
			_view = view;
			
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			//id text input
			_view.codeTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
			_view.codeTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.codeTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			//scientific name text input
			_view.scientificNameTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
			_view.scientificNameTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.scientificNameTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			//vernacular name text input
			_view.vernacularNameTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
			_view.vernacularNameTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.vernacularNameTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			//vernacular lang text input
			_view.vernacularLangTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
			_view.vernacularLangTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			_view.vernacularLangTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
		}
		
		override protected function updateView():void {
			super.updateView();
			
			var attribute:AttributeProxy = _view.attribute;
			
			//reset view
			_view.codeTextInput.text = null;
			_view.scientificNameTextInput.text = null;
			_view.vernacularNameTextInput.text = null;
			_view.vernacularLangTextInput.text = null;
			
			if(attribute != null) {
				var value:Object = attribute.value;
				if(value is TaxonProxy) {
					var occurrence:TaxonOccurrenceProxy = TaxonOccurrenceProxy(value);
					var taxon:TaxonProxy = occurrence.taxon;
					var vernacularName:TaxonVernacularNameProxy = occurrence.vernacularName;
					if(taxon != null) {
						_view.codeTextInput.text = taxon.code;
						_view.scientificNameTextInput.text = taxon.scientificName;
					}
					if(vernacularName != null) {
						_view.vernacularNameTextInput.text = vernacularName.vernacularName;
						_view.vernacularLangTextInput.text = vernacularName.languageVariety;
					}
				}
			}
		}
		
		protected function textInputChangeHandler(event:Event):void {
			var textInput:TextInput = event.target as TextInput;
			if(textInput == null) {
				return; //error
			}
			var text:String = mx.utils.StringUtil.trim(textInput.text);
			if(text.length < minCharsToStartAutoComplete) {
				return;
			}
			var searchType:String;
			switch(textInput) {
				case _view.codeTextInput:
					searchType = SEARCH_BY_CODE;
					break;
				case _view.scientificNameTextInput:
					searchType = SEARCH_BY_SCIENTIFIC_NAME;
					break;
				case _view.vernacularNameTextInput:
					searchType = SEARCH_BY_VERNACULAR_NAME;
					break;
				default:
			}
			if(searchType != null) {
				showAutoCompletePopUp(searchType, textInput, _view.codeTextInput);
			}
		}
		
		protected static function showAutoCompletePopUp(searchType:String, textInput:TextInput, alignField:DisplayObject):void {
			if(autoCompletePopUp == null) {
				autoCompletePopUp = new TaxonAutoCompletePopUp();
				autoCompletePopUp.addEventListener(KeyboardEvent.KEY_DOWN, autoCompleteKeyDownHandler);
				autoCompletePopUp.addEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, autoCompleteMouseDownOutsideHandler);
				autoCompletePopUp.addEventListener(TaxonInputFieldEvent.TAXON_SELECT, taxonSelectHandler);
				autoCompleteSearchResponder = new AsyncResponder(autoCompleteSearchResultHandler, searchFaultHandler);
			}
			if(! autoCompletePopUpOpen) {
				PopUpManager.addPopUp(autoCompletePopUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				
				PopUpUtil.alignPopUpToField(autoCompletePopUp, alignField, 
					PopUpUtil.POSITION_BOTTOM, 
					PopUpUtil.VERTICAL_ALIGN_MIDDLE, 
					PopUpUtil.HORIZONTAL_ALIGN_LEFT);
				
				autoCompletePopUpOpen = true;
			}
			autoCompleteLastSearchTextInput = textInput;
			loadAutoCompleteData(searchType, textInput);
		}
		
		protected static function loadAutoCompleteData(searchType:String, textInput:TextInput):void {
			autoCompletePopUp.dataGrid.dataProvider = null;
			var client:TaxonClient = ClientFactory.taxonClient;
			var searchText:String = textInput.text;
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
					//TODO select taxon
					break;
				case Keyboard.ESCAPE:
					closeAutoCompletePopUp();
					autoCompleteLastSearchTextInput.setFocus();
					break;
			}
		}
		
		protected static function autoCompleteMouseDownOutsideHandler(event:FlexMouseEvent):void {
			closeAutoCompletePopUp();
		}
		
		protected static function closeAutoCompletePopUp():void {
			if(autoCompletePopUpOpen) {
				PopUpManager.removePopUp(autoCompletePopUp);
				autoCompletePopUpOpen = false;
			}
		}
		
		protected static function taxonSelectHandler(event:Event):void {
			//TODO apply changes to db...
			closeAutoCompletePopUp();
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
