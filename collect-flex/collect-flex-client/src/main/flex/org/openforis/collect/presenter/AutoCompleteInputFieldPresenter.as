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
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.AutoCompleteInputField;
	import org.openforis.collect.ui.component.input.AutoCompletePopUp;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.TextInput;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class AutoCompleteInputFieldPresenter extends InputFieldPresenter {
		
		protected var _minCharsToStartAutocomplete:int;
		
		protected var popUp:AutoCompletePopUp;
		protected var popUpOpened:Boolean = false;
		
		private var _allowNotListedValues:Boolean;
		private var _view:AutoCompleteInputField;
		private var dataLoading:Boolean;
		
		public function AutoCompleteInputFieldPresenter(view:AutoCompleteInputField) {
			_view = view;
			_minCharsToStartAutocomplete = 2;
			_allowNotListedValues = true;
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
						popUp.listComponent.setFocus();
						if ( CollectionUtil.isNotEmpty(popUp.listComponent.dataProvider) ) {
							popUp.listComponent.selectedIndex = 0;
						}
					}
					break;
				case Keyboard.ESCAPE:
					closePopUp();
					break;
				case Keyboard.TAB:
					if ( popUpOpened ) {
						var matchingResult:Object = getMatchingResult();
						if ( matchingResult != null ) {
							performSelectValue(matchingResult);
						} else {
							popUp.listComponent.setFocus();
							if ( CollectionUtil.isNotEmpty(popUp.listComponent.dataProvider) ) {
								popUp.listComponent.selectedIndex = 0;
							}
						}
					}
					break;
			}
		}
		
		protected function getMatchingResult():* {
			var searchText:String = _view.text;
			var dataProvider:IList = popUp.listComponent.dataProvider;
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
					if ( allowNotListedValues || _view.isEmpty() || FieldProxy.isShortCutForReasonBlank(_view.text) ) {
						inputField.presenter.updateValue();
					} else {
						inputField.presenter.undoLastChange();
					}
				}
			}
		}
		
		protected function inputFieldChangingHandler(event:InputFieldEvent):void {
			var text:String = (_view.textInput as TextInput).text;
			if ( text.length > minCharsToStartAutocomplete ) {
				loadAutoCompleteData();
			} else {
				
			}
		}
		
		protected function loadAutoCompleteData():void {
			dataLoading = true;
			_view.currentState = AutoCompleteInputField.STATE_LOADING;
			var client:DataClient = ClientFactory.dataClient;
			var searchText:String = _view.text;
			var token:Object = {searchText: searchText};
			var responder:IResponder = new AsyncResponder(searchResultHandler, searchFaultHandler, token);
			var attributeDefnId:int = _view.attributeDefinition.id;
			var fieldIndex:int = _view.fieldIndex;
			client.searchAutoCompleteValues(responder, attributeDefnId, fieldIndex, searchText);
		}
		
		protected function showPopUp():void {
			var firstCreated:Boolean = false;
			if(popUp == null) {
				popUp = createPopUp();
				popUp.addEventListener(KeyboardEvent.KEY_DOWN, autoCompleteKeyDownHandler);
				popUp.addEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, autoCompleteMouseDownOutsideHandler);
				firstCreated = true;
			}
			if(! popUpOpened) {
				PopUpManager.addPopUp(popUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				
				PopUpUtil.alignToField(popUp, _view, 
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
		
		protected function createPopUp():AutoCompletePopUp {
			var popUp:AutoCompletePopUp = new AutoCompletePopUp();
			return popUp;
		}
		
		protected function itemSelectHandler(event:UIEvent = null):void {
			var selectedValue:Object = popUp.listComponent.selectedItem;
			performSelectValue(selectedValue);
		}
		
		protected function performSelectValue(selectedValue:*):void {
			if ( selectedValue != null ) {
				_view.text = selectedValue.toString();
				_view.presenter.updateValue();
			}
			closePopUp();
		}
		
		protected function autoCompleteKeyDownHandler(event:KeyboardEvent):void {
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
		
		protected function autoCompleteMouseDownOutsideHandler(event:FlexMouseEvent):void {
			cancelAutoComplete();
		}
		
		protected function closePopUp():void {
			if ( popUpOpened ) {
				PopUpManager.removePopUp(popUp);
				popUpOpened = false;
				var textInput:TextInput = _view.textInput as TextInput;
				textInput.setFocus();
			}
		}
		
		protected function cancelAutoComplete():void {
			closePopUp();
		}
		
		protected function searchResultHandler(event:ResultEvent, token:Object):void {
			var data:IList = event.result as IList;
			handleSearchResult(data);
		}
		
		protected function handleSearchResult(data:IList):void {
			dataLoading = false;
			_view.currentState = AutoCompleteInputField.STATE_DEFAULT;
			if ( CollectionUtil.isEmpty(data) ) {
				closePopUp();
			} else {
				showPopUp();
				popUp.listComponent.dataProvider = data;
			}
		}
		
		protected function searchFaultHandler(event:FaultEvent, token:Object = null):void {
			dataLoading = false;
			faultHandler(event, token);
		}
		
		public function get allowNotListedValues():Boolean {
			return _allowNotListedValues;
		}
		
		public function set allowNotListedValues(value:Boolean):void {
			_allowNotListedValues = value;
		}
		
		public function get minCharsToStartAutocomplete():int {
			return _minCharsToStartAutocomplete;
		}
		
		public function set minCharsToStartAutocomplete(value:int):void {
			_minCharsToStartAutocomplete = value;
		}
		
	}
}