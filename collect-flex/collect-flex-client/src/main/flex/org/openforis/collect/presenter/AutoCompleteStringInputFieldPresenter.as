package org.openforis.collect.presenter
{
	import flash.display.DisplayObject;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.ui.Keyboard;
	
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.events.FlexMouseEvent;
	import mx.managers.PopUpManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataClient;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.ui.component.input.AutoCompleteStringInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TextAutoCompletePopUp;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class AutoCompleteStringInputFieldPresenter extends InputFieldPresenter {
		
		protected static const MIN_CHARS_TO_START_AUTOCOMPLETE:int = 2;
		
		protected static var popUp:TextAutoCompletePopUp;
		protected static var popUpOpened:Boolean = false;
		protected static var lastInputField:AutoCompleteStringInputField;
		
		private var _view:AutoCompleteStringInputField;
		private static var dataLoading:Boolean;
		
		public function AutoCompleteStringInputFieldPresenter(view:AutoCompleteStringInputField) {
			_view = view;
			super(view);
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			_view.applyChangesOnFocusOut = false;
			_view.addEventListener(InputFieldEvent.CHANGING, inputFieldChangingHandler);
			_view.addEventListener(FocusEvent.FOCUS_OUT, inputFieldFocusOutHandler);
			_view.textInput.addEventListener(KeyboardEvent.KEY_DOWN, inputFieldKeyDownHandler);
		}
		
		protected function inputFieldKeyDownHandler(event:KeyboardEvent):void {
			switch ( event.keyCode ) {
				case Keyboard.DOWN:
					if ( popUpOpened ) {
						popUp.list.setFocus();
						if ( CollectionUtil.isNotEmpty(popUp.list.dataProvider) ) {
							popUp.list.selectedIndex = 0;
						}
					}
					break;
				case Keyboard.ESCAPE:
					closePopUp();
					break;
				case Keyboard.TAB:
					if ( popUpOpened ) {
						var matchingResult:String = getMatchingResult();
						if ( matchingResult != null ) {
							performSelectValue(matchingResult);
						} else {
							popUp.list.setFocus();
							if ( CollectionUtil.isNotEmpty(popUp.list.dataProvider) ) {
								popUp.list.selectedIndex = 0;
							}
						}
					}
					break;
			}
		}
		
		protected static function getMatchingResult():String {
			var searchText:String = lastInputField.text;
			var dataProvider:IList = popUp.list.dataProvider;
			for each (var item:String in dataProvider) {
				if ( searchText.toUpperCase() == item.toUpperCase() ) {
					return item;
				}
			}
			return null;
		}
		protected function inputFieldFocusOutHandler(event:FocusEvent):void {
			var inputField:InputField = event.target.document;
			if ( inputField != null && inputField.changed ) {
				if ( ! popUpOpened ) {
					//inputField.presenter.undoLastChange();
					inputField.presenter.updateValue();
				}
			}
		}
		
		protected function inputFieldChangingHandler(event:InputFieldEvent):void {
			var text:String = (_view.textInput as TextInput).text;
			if ( text.length > MIN_CHARS_TO_START_AUTOCOMPLETE) {
				loadAutoCompleteData(text, _view);
			}
		}
		
		protected static function loadAutoCompleteData(text:String, inputField:AutoCompleteStringInputField):void {
			dataLoading = true;
			lastInputField = inputField;
			lastInputField.currentState = AutoCompleteStringInputField.STATE_LOADING;
			var client:DataClient = ClientFactory.dataClient;
			var searchText:String = lastInputField.text;
			var token:Object = {searchText: searchText};
			var responder:IResponder = new AsyncResponder(autoCompleteSearchResultHandler, searchFaultHandler, token);
			var attributeDefnId:int = lastInputField.attributeDefinition.id;
			var fieldIndex:int = lastInputField.fieldIndex;
			client.searchAutoCompleteValues(responder, attributeDefnId, fieldIndex, searchText);
		}
		
		protected static function showPopUp():void {
			var firstCreated:Boolean = false;
			if(popUp == null) {
				popUp = new TextAutoCompletePopUp();
				popUp.addEventListener(KeyboardEvent.KEY_DOWN, autoCompleteKeyDownHandler);
				popUp.addEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, autoCompleteMouseDownOutsideHandler);
				firstCreated = true;
			}
			if(! popUpOpened) {
				PopUpManager.addPopUp(popUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				
				PopUpUtil.alignToField(popUp, lastInputField, 
					PopUpUtil.POSITION_BELOW, 
					PopUpUtil.VERTICAL_ALIGN_BOTTOM, 
					PopUpUtil.HORIZONTAL_ALIGN_LEFT,
					false
				);
				
				popUpOpened = true;
				
				if ( firstCreated ) {
					popUp.addEventListener(UIEvent.ITEM_SELECT, itemSelectHandler);
				}
			}
		}
		
		protected static function itemSelectHandler(event:UIEvent = null):void {
			var selectedValue:String = popUp.list.selectedItem as String;
			performSelectValue(selectedValue);
		}
		
		protected static function performSelectValue(selectedValue:String):void {
			if ( selectedValue != null ) {
				lastInputField.text = selectedValue;
				lastInputField.presenter.updateValue();
			}
			closePopUp();
		}
		
		protected static function autoCompleteKeyDownHandler(event:KeyboardEvent):void {
			var keyCode:uint = event.keyCode;
			switch(keyCode) {
				case Keyboard.ENTER:
					itemSelectHandler();
					break;
				case Keyboard.ESCAPE:
					cancelAutoComplete();
					break;
			}
		}
		
		protected static function autoCompleteMouseDownOutsideHandler(event:FlexMouseEvent):void {
			cancelAutoComplete();
		}
		
		protected static function closePopUp():void {
			if ( popUpOpened ) {
				PopUpManager.removePopUp(popUp);
				popUpOpened = false;
				var textInput:TextInput = lastInputField.textInput as TextInput;
				textInput.setFocus();
			}
		}
		
		protected static function cancelAutoComplete():void {
			closePopUp();
		}
		
		protected static function autoCompleteSearchResultHandler(event:ResultEvent, token:Object):void {
			dataLoading = false;
			lastInputField.currentState = AutoCompleteStringInputField.STATE_DEFAULT;
			var data:IList = event.result as IList;
			if ( CollectionUtil.isEmpty(data) ) {
				closePopUp();
			} else {
				showPopUp();
				popUp.list.dataProvider = data;
			}
		}
		
		protected static function searchFaultHandler(event:FaultEvent, token:Object = null):void {
			dataLoading = false;
			faultHandler(event, token);
		}
	}
}