package org.openforis.collect.presenter {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.binding.utils.ChangeWatcher;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.TextInput;
	import mx.core.FlexGlobals;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	
	import org.openforis.collect.event.TaxonInputFieldEvent;
	import org.openforis.collect.model.proxy.AttributeProxy;
	import org.openforis.collect.model.proxy.TaxonProxy;
	import org.openforis.collect.ui.component.detail.TaxonAttributeFormItem;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TaxonAutoCompletePopUp;
	import org.openforis.collect.ui.component.input.TaxonInputField;
	import org.openforis.collect.ui.component.input.TaxonSearchPopUp;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TaxonAttributeFormItemPresenter extends FormItemPresenter {
		
		protected static var autoCompletePopUp:TaxonAutoCompletePopUp;
		protected static var autoCompletePopUpOpen:Boolean = false;
		protected static var searchPopUp:TaxonSearchPopUp;
		protected static var searchPopUpOpen:Boolean = false;
		protected static var autoCompleteSearchResponder:AsyncResponder;
		protected static var searchResponder:AsyncResponder;
		
		public var minCharsToStartAutoComplete:int = 2;
		
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
		
		protected function focusInHandler(event:FocusEvent):void {
			UIUtil.ensureElementIsVisible(event.target);
		}
		
		protected function focusOutHandler(event:FocusEvent):void {
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
					var taxon:TaxonProxy = TaxonProxy(value);
					var srs:Object = null;
					view.codeTextInput.text = String(taxon.code)
					view.scientificNameTextInput.text = String(taxon.scientificName);
					view.vernacularNameTextInput.text = String(taxon.vernacularName);
					view.vernacularLangTextInput.text = String(taxon.languageVariant);
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
			var subElementName:String = getSubElementName(textInput);
			showAutoCompletePopUp(subElementName, textInput);
		}
		
		protected function getSubElementName(textInput:TextInput):String {
			var subElementName:String = "id";
			switch(textInput) {
				case view.codeTextInput:
					break;
				case view.scientificNameTextInput:
					break;
				case view.vernacularNameTextInput:
					break;
				case view.vernacularLangTextInput:
					break;
			}
			return subElementName;
		}
		
		protected function searchImageClickHandler(event:Event):void {
			//TODO fill searchTextInput with inserted text
			showSearchPopUp();
		}
		
		protected static function showAutoCompletePopUp(subElementName:String, textInput:TextInput, searchType:String = "contains"):void {
			if(autoCompletePopUp == null) {
				autoCompletePopUp = new TaxonAutoCompletePopUp();
				autoCompletePopUp.addEventListener(TaxonInputFieldEvent.TAXON_SEARCH_POPUP_CLOSE, autoCompletePopUpCloseHandler);
				autoCompletePopUp.addEventListener(TaxonInputFieldEvent.TAXON_SELECT, taxonSelectHandler);
				autoCompleteSearchResponder = new AsyncResponder(autoCompleteSearchResultHandler, searchFaultHandler);
			}
			
			if(! autoCompletePopUpOpen) {
				PopUpManager.addPopUp(autoCompletePopUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				var alignField:DisplayObject = textInput;

				PopUpUtil.alignPopUpToField(autoCompletePopUp, alignField, 
					PopUpUtil.POSITION_BOTTOM, 
					PopUpUtil.VERTICAL_ALIGN_MIDDLE, 
					PopUpUtil.HORIZONTAL_ALIGN_LEFT);
				
				autoCompletePopUpOpen = true;
			}
			/*
			var taxonomy:String = inputField.attribute.taxonomy;
			var query:String = inputField.text;
			search(taxonomy, query, subElementName, searchType);
			*/
			autoCompleteSearchResultHandler(null, null);
		}
		
		protected static function showSearchPopUp():void {
			if(searchPopUp == null) {
				searchPopUp = new TaxonSearchPopUp();
				searchPopUp.addEventListener(TaxonInputFieldEvent.TAXON_SEARCH_POPUP_CLOSE, searchPopUpCloseHandler);
				searchPopUp.addEventListener(TaxonInputFieldEvent.TAXON_SELECT, taxonSelectHandler);
				searchPopUp.addEventListener(TaxonInputFieldEvent.TAXON_SEARCH_POPUP_SEARCH_TEXT_CHANGE, searchTextInputChangeHandler);
				searchResponder = new AsyncResponder(searchResultHandler, searchFaultHandler);
			}
			
			if(! searchPopUpOpen) {
				PopUpManager.addPopUp(searchPopUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				PopUpManager.centerPopUp(searchPopUp);
				
				searchPopUpOpen = true;
			}
		}
		
		protected static function searchPopUpCloseHandler(event:Event):void {
			PopUpManager.removePopUp(searchPopUp);
			searchPopUpOpen = false;
		}
		
		protected static function autoCompletePopUpCloseHandler(event:Event):void {
			PopUpManager.removePopUp(autoCompletePopUp);
			autoCompletePopUpOpen = false;
		}
		
		protected static function taxonSelectHandler(event:Event):void {
			//TODO apply changes to db...
			
			if(autoCompletePopUpOpen) {
				PopUpManager.removePopUp(autoCompletePopUp);
				autoCompletePopUpOpen = false;
			} else if(searchPopUpOpen) {
				PopUpManager.removePopUp(searchPopUp);
				searchPopUpOpen = false;
			}
		}
		
		protected static function searchTextInputChangeHandler(event:TaxonInputFieldEvent):void {
			var searchText:String = searchPopUp.searchTextInput.text;
			if(searchText.length > 2) {
				//TO DO start search
			}
		}
		
		protected static function searchResultHandler(event:ResultEvent, token:Object):void {
			var data:IList = IList(event.result);
			searchPopUp.speciesDataGrid.dataProvider = data;
		}
		
		protected static function autoCompleteSearchResultHandler(event:ResultEvent, token:Object):void {
			//test data
			var data:ArrayCollection = new ArrayCollection();
			for(var index:int = 0; index < 9; index ++) {
				data.addItem({
					code: mx.utils.StringUtil.substitute("00{0}", index + 1),
					scientificName: mx.utils.StringUtil.substitute("Plant n. 00{0}", index + 1),
					vernacularName: mx.utils.StringUtil.substitute("Vernacular Name for 00{0}", index + 1),
					vernacularLang: mx.utils.StringUtil.substitute("Vernacular Lang for 00{0}", index + 1)
				});
			}
			autoCompletePopUp.dataProvider = data;
		}
		
		protected static function searchFaultHandler(event:FaultEvent):void {
			//TO DO
		}
	}
}
