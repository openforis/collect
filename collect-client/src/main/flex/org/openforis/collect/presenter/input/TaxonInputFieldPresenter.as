package org.openforis.collect.presenter.input {
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.FocusEvent;
	
	import mx.collections.ArrayCollection;
	import mx.controls.TextInput;
	import mx.core.FlexGlobals;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.StringUtil;
	
	import org.openforis.collect.event.input.TaxonInputFieldEvent;
	import org.openforis.collect.idm.model.impl.AbstractValue;
	import org.openforis.collect.presenter.InputFieldPresenter;
	import org.openforis.collect.ui.component.detail.input.InputField;
	import org.openforis.collect.ui.component.detail.input.TaxonAutoCompletePopUp;
	import org.openforis.collect.ui.component.detail.input.TaxonInputField;
	import org.openforis.collect.util.PopUpUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TaxonInputFieldPresenter extends InputFieldPresenter {
		
		protected static var popUp:TaxonAutoCompletePopUp;
		protected static var popUpOpen:Boolean = false;
		protected static var responder:AsyncResponder;
		
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
			var subElementName:String = "id";
			switch(event.target) {
				case _taxonInputField.idTextInput:
					break;
				case _taxonInputField.scientificNameTextInput:
					break;
				case _taxonInputField.vernacularNameTextInput:
					break;
				case _taxonInputField.vernacularLangTextInput:
					break;
			}
			showAutocompletePopUp(subElementName, textInput);
		}
		
		protected static function showAutocompletePopUp(subElementName:String, textInput:TextInput, searchType:String = "contains"):void {
			if(popUp == null) {
				popUp = new TaxonAutoCompletePopUp();
				popUp.addEventListener(TaxonInputFieldEvent.TAXON_AUTOCOMPLETE_POPUP_CLOSE, popUpCloseHandler);
				popUp.addEventListener(TaxonInputFieldEvent.TAXON_SELECT, taxonSelectHandler);
				responder = new AsyncResponder(searchResultHandler, searchFaultHandler);
			}
			
			if(! popUpOpen) {
				PopUpManager.addPopUp(popUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				var alignField:DisplayObject = textInput;

				PopUpUtil.alignPopUpToField(popUp, alignField, 
					PopUpUtil.POSITION_BOTTOM, 
					PopUpUtil.VERTICAL_ALIGN_MIDDLE, 
					PopUpUtil.HORIZONTAL_ALIGN_LEFT);
				
				popUpOpen = true;
			}
			/*
			var taxonomy:String = inputField.attribute.taxonomy;
			var query:String = inputField.text;
			search(taxonomy, query, subElementName, searchType);
			*/
			searchResultHandler(null, null);
		}
		
		protected static function popUpCloseHandler(event:Event):void {
			PopUpManager.removePopUp(popUp);
		}
		
		protected static function taxonSelectHandler(event:Event):void {
			PopUpManager.removePopUp(popUp);
		}
		
		protected static function searchResultHandler(event:ResultEvent, token:Object):void {
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
			popUp.dataProvider = data;
		}
		
		protected static function searchFaultHandler(event:FaultEvent):void {
			
		}
	}
}
