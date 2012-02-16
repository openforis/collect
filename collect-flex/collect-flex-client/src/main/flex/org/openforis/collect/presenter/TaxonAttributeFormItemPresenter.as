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
	import mx.events.FlexMouseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
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
				autoCompletePopUp.addEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, autoCompleteMouseDownOutsideHandler);
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
			
			//ClientFactory.taxonClient
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
		
		protected static function autoCompleteMouseDownOutsideHandler(event:FlexMouseEvent):void {
			closeAutoCompletePopUp();
		}
		
		protected static function closeAutoCompletePopUp():void {
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
			var data:IList = event.result as IList;
			autoCompletePopUp.dataGrid.dataProvider = data;
		}
		
		protected static function searchFaultHandler(event:FaultEvent):void {
			//TO DO
		}
	}
}
