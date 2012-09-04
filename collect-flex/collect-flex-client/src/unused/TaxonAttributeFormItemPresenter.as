package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.ui.Keyboard;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.TextInput;
	import mx.core.FlexGlobals;
	import mx.core.UIComponent;
	import mx.events.FlexMouseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.TaxonClient;
	import org.openforis.collect.event.TaxonInputFieldEvent;
	import org.openforis.collect.metamodel.proxy.SpatialReferenceSystemProxy;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.TaxonOccurrenceProxy;
	import org.openforis.collect.model.proxy.TaxonProxy;
	import org.openforis.collect.model.proxy.TaxonVernacularNameProxy;
	import org.openforis.collect.ui.component.detail.TaxonAttributeFormItem;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TaxonAutoCompletePopUp;
	import org.openforis.collect.ui.component.input.TaxonInputField;
	import org.openforis.collect.ui.component.input.TaxonSearchPopUp;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TaxonAttributeFormItemPresenter extends AttributeFormItemPresenter {
		
		private static const MAX_RESULTS:int = 10;
		private static const SEARCH_BY_CODE:String = "byCode";
		private static const SEARCH_BY_SCIENTIFIC_NAME:String = "byScientificName";
		private static const SEARCH_BY_VERNACULAR_NAME:String = "byVernacularName";
		
		protected static var autoCompletePopUp:TaxonAutoCompletePopUp;
		protected static var autoCompletePopUpOpen:Boolean = false;
		protected static var autoCompleteSearchResponder:AsyncResponder;
		protected static var autoCompleteLastSearchTextInput:TextInput;
		
		private var minCharsToStartAutoComplete:int = 2;
		
		public function TaxonAttributeFormItemPresenter(view:TaxonAttributeFormItem = null) {
			super(view);
			updateView();
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			
			//id text input
			view.codeTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
			view.codeTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			view.codeTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			//scientific name text input
			view.scientificNameTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
			view.scientificNameTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			view.scientificNameTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			//vernacular name text input
			view.vernacularNameTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
			view.vernacularNameTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			view.vernacularNameTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			//vernacular lang text input
			view.vernacularLangTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
			view.vernacularLangTextInput.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			view.vernacularLangTextInput.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
		}
		
		private function get view():TaxonAttributeFormItem {
			return TaxonAttributeFormItem(_view);
		}
		
		override protected function updateView():void {
			var attribute:AttributeProxy = this.view.attribute;
			
			//reset view
			view.codeTextInput.text = null;
			view.scientificNameTextInput.text = null;
			view.vernacularNameTextInput.text = null;
			view.vernacularLangTextInput.text = null;
			
			if(attribute != null) {
				var value:Object = attribute.value;
				if(value is TaxonProxy) {
					var occurrence:TaxonOccurrenceProxy = TaxonOccurrenceProxy(value);
					var taxon:TaxonProxy = occurrence.taxon;
					var vernacularName:TaxonVernacularNameProxy = occurrence.vernacularName;
					if(taxon != null) {
						view.codeTextInput.text = taxon.code;
						view.scientificNameTextInput.text = taxon.scientificName;
					}
					if(vernacularName != null) {
						view.vernacularNameTextInput.text = vernacularName.vernacularName;
						view.vernacularLangTextInput.text = vernacularName.languageVariety;
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
				showAutoCompletePopUp(searchType, textInput, view.codeTextInput);
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
			autoCompleteLastSearchTextInput = textInput;
			loadAutoCompleteData(searchType, textInput);

			if(! autoCompletePopUpOpen) {
				PopUpManager.addPopUp(autoCompletePopUp, FlexGlobals.topLevelApplication as DisplayObject, false);

				PopUpUtil.alignPopUpToField(autoCompletePopUp, alignField, 
					PopUpUtil.POSITION_BOTTOM, 
					PopUpUtil.VERTICAL_ALIGN_MIDDLE, 
					PopUpUtil.HORIZONTAL_ALIGN_LEFT);
				
				autoCompletePopUpOpen = true;
			}
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
