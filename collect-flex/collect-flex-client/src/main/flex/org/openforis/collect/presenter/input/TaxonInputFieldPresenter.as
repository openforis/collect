package org.openforis.collect.presenter.input {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.TextInput;
	import mx.core.FlexGlobals;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	
	import org.openforis.collect.event.input.TaxonInputFieldEvent;
	import org.openforis.collect.idm.model.impl.AbstractValue;
	import org.openforis.collect.idm.model.impl.Taxon;
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.ui.component.detail.input.InputField;
	import org.openforis.collect.ui.component.detail.input.TaxonAutoCompletePopUp;
	import org.openforis.collect.ui.component.detail.input.TaxonInputField;
	import org.openforis.collect.ui.component.detail.input.TaxonSearchPopUp;
	import org.openforis.collect.util.PopUpUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TaxonInputFieldPresenter extends InputFieldPresenter {
		
		protected static var autoCompletePopUp:TaxonAutoCompletePopUp;
		protected static var autoCompletePopUpOpen:Boolean = false;
		protected static var searchPopUp:TaxonSearchPopUp;
		protected static var searchPopUpOpen:Boolean = false;
		protected static var autoCompleteSearchResponder:AsyncResponder;
		protected static var searchResponder:AsyncResponder;
		
		public var minCharsToStartAutoComplete:int = 2;
		
		private var _taxonInputField:TaxonInputField;
		
		public function TaxonInputFieldPresenter(inputField:TaxonInputField = null) {
			super();
			this.inputField = inputField;
		}
		
		override public function set inputField(value:InputField):void {
			super.inputField = value;
			
			_taxonInputField = value as TaxonInputField;
			
			if(_taxonInputField != null) {
				//add event listeners
				//id text input
				_taxonInputField.idTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
				_taxonInputField.idTextInput.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_taxonInputField.idTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				//scientific name text input
				_taxonInputField.scientificNameTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
				_taxonInputField.scientificNameTextInput.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_taxonInputField.scientificNameTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				//vernacular name text input
				_taxonInputField.vernacularNameTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
				_taxonInputField.vernacularNameTextInput.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_taxonInputField.vernacularNameTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				//vernacular lang text input
				_taxonInputField.vernacularLangTextInput.addEventListener(Event.CHANGE, textInputChangeHandler);
				_taxonInputField.vernacularLangTextInput.addEventListener(FocusEvent.FOCUS_IN, inputFieldFocusInHandler);
				_taxonInputField.vernacularLangTextInput.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
				//search icons
				_taxonInputField.idSearchImg.addEventListener(MouseEvent.CLICK, searchImageClickHandler);
				_taxonInputField.scientificNameSearchImg.addEventListener(MouseEvent.CLICK, searchImageClickHandler);
				_taxonInputField.vernacularNameSearchImg.addEventListener(MouseEvent.CLICK, searchImageClickHandler);
				_taxonInputField.vernacularLangSearchImg.addEventListener(MouseEvent.CLICK, searchImageClickHandler);
			}	
			
		}
		
		override public function set value(value:AbstractValue):void {
			_attributeValue = value;
			//this._inputField.attribute = attribute;
			this._taxonInputField.idTextInput.text = value.text1;
			this._taxonInputField.scientificNameTextInput.text = value.text2;
			this._taxonInputField.vernacularNameTextInput.text = value.text3;
			this._taxonInputField.vernacularLangTextInput.text = value.text4;
			/*
			this._inputField.error = value.error;
			this._inputField.warning = value.warning;
			*/
			this._inputField.remarks = value.remarks;
			this._inputField.approved = value.approved;
		}
		
		override public function createValue():AbstractValue {
			var value:AbstractValue = new AbstractValue();
			value.text1 = _taxonInputField.idTextInput.text;
			value.text2 = _taxonInputField.scientificNameTextInput.text;
			value.text3 = _taxonInputField.vernacularNameTextInput.text;
			value.text4 = _taxonInputField.vernacularLangTextInput.text;
			
			if(value != null) {
				//copy old informations
				value.remarks = value.remarks;
			}
			return value;
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
				case _taxonInputField.idTextInput:
					break;
				case _taxonInputField.scientificNameTextInput:
					break;
				case _taxonInputField.vernacularNameTextInput:
					break;
				case _taxonInputField.vernacularLangTextInput:
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
					id: mx.utils.StringUtil.substitute("00{0}", index + 1),
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
