package org.openforis.collect.presenter
{
	import flash.display.DisplayObject;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	import flash.ui.Keyboard;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.TextInput;
	import mx.core.FlexGlobals;
	import mx.events.FlexMouseEvent;
	import mx.managers.PopUpManager;
	
	import org.granite.reflect.Field;
	import org.openforis.collect.Application;
	import org.openforis.collect.event.InputFieldEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.LanguageCodes;
	import org.openforis.collect.model.LanguageItem;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.LanguageCodeAutoComplete;
	import org.openforis.collect.ui.component.input.LanguageCodeAutoCompletePopUp;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.util.UIUtil;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class LanguageCodeAutoCompletePresenter extends InputFieldPresenter {
		
		private var _view:LanguageCodeAutoComplete;
		
		private static var autoCompletePopUp:LanguageCodeAutoCompletePopUp;
		private static var autoCompletePopUpOpened:Boolean;
		
		private static var languages:ArrayCollection;
		private static var autoCompleteLastInputField:LanguageCodeAutoComplete;
		private static var searchText:String;
		
		{
			//init static vars
			languages = LanguageCodes.getLanguageCodes(Application.locale);
			languages.filterFunction = languagesFilterFunction;
		}
		
		public function LanguageCodeAutoCompletePresenter(view:LanguageCodeAutoComplete) {
			this._view = view;

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
						var matchingResult:LanguageItem = getMatchingResult() as LanguageItem;
						if ( matchingResult != null ) {
							performSelectLanguage(matchingResult);
						} else {
							var text:String = autoCompleteLastInputField.text;
							if ( StringUtil.isBlank(text) || FieldProxy.isShortCutForReasonBlank(text) ) {
								autoCompleteLastInputField.presenter.updateValue();
							} else {
								autoCompleteLastInputField.presenter.undoLastChange();
							}
						}
						closeAutoCompletePopUp();
					}
					break;
			}
		}
		
		protected static function getMatchingResult():Object {
			var searchText:String = autoCompleteLastInputField.text;
			var list:IList = autoCompletePopUp.dataGrid.dataProvider;
			for each (var item:Object in list) {
				var compareToValue:String = item.code;
				if ( compareToValue != null && searchText.toUpperCase() == compareToValue.toUpperCase() ) {
					return item;
				}
			}
			return null;
		}
		
		protected function inputFieldFocusOutHandler(event:FocusEvent):void {
			var inputField:InputField = event.target.document;
			if ( inputField != null && inputField.changed ) {
				if ( ! autoCompletePopUpOpened && ! UIUtil.isFocusOnComponent(autoCompletePopUp) ) {
					inputField.presenter.undoLastChange();
				}
			}
		}
		
		protected function inputFieldChangingHandler(event:InputFieldEvent):void {
			var text:String = (_view.textInput as TextInput).text;
			showAutoCompletePopUp(text, _view);
		}
		
		protected static function languagesFilterFunction(item:LanguageItem):Boolean {
			if ( StringUtil.isNotBlank(searchText) ) {
				return StringUtil.startsWith(item.code, searchText, true) || StringUtil.startsWith(item.label, searchText, true);
			} else {
				return false;
			}
		}
		
		protected static function showAutoCompletePopUp(text:String, inputField:LanguageCodeAutoComplete):void {
			var firstCreated:Boolean = false;
			if(autoCompletePopUp == null) {
				autoCompletePopUp = new LanguageCodeAutoCompletePopUp();
				autoCompletePopUp.addEventListener(KeyboardEvent.KEY_DOWN, autoCompleteKeyDownHandler);
				autoCompletePopUp.addEventListener(FlexMouseEvent.MOUSE_DOWN_OUTSIDE, autoCompleteMouseDownOutsideHandler);
				firstCreated = true;
			}
			if(! autoCompletePopUpOpened) {
				PopUpManager.addPopUp(autoCompletePopUp, FlexGlobals.topLevelApplication as DisplayObject, false);
				
				PopUpUtil.alignToField(autoCompletePopUp, inputField, 
					PopUpUtil.POSITION_BELOW, 
					PopUpUtil.VERTICAL_ALIGN_BOTTOM, 
					PopUpUtil.HORIZONTAL_ALIGN_LEFT,
					false
				);
				
				autoCompletePopUpOpened = true;
				
				if ( firstCreated ) {
					autoCompletePopUp.addEventListener(UIEvent.ITEM_SELECT, languageSelectHandler);
				}
			}
			searchText = text;
			autoCompleteLastInputField = inputField;
			languages.refresh();
			autoCompletePopUp.dataGrid.dataProvider = languages;
		}
		
		protected static function languageSelectHandler(event:UIEvent = null):void {
			var selectedLanguage:LanguageItem = autoCompletePopUp.dataGrid.selectedItem as LanguageItem;
			performSelectLanguage(selectedLanguage);
		}
		
		protected static function performSelectLanguage(selectedLanguage:LanguageItem):void {
			if ( selectedLanguage != null ) {
				autoCompleteLastInputField.text = selectedLanguage.code;
				autoCompleteLastInputField.presenter.updateValue();
			}
			closeAutoCompletePopUp();
		}
		
		protected static function autoCompleteKeyDownHandler(event:KeyboardEvent):void {
			var keyCode:uint = event.keyCode;
			switch(keyCode) {
				case Keyboard.ENTER:
					languageSelectHandler();
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
				var textInput:TextInput = autoCompleteLastInputField.textInput as TextInput;
				textInput.setFocus();
			}
		}
		
		protected static function cancelAutoComplete():void {
			closeAutoCompletePopUp();
		}
		
	}
}