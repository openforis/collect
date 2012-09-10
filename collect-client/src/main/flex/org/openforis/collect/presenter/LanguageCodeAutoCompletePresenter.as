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
	import org.openforis.collect.ui.component.input.AutoCompleteInputField;
	import org.openforis.collect.ui.component.input.AutoCompletePopUp;
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
	public class LanguageCodeAutoCompletePresenter extends AutoCompleteInputFieldPresenter {
		
		private var _view:LanguageCodeAutoComplete;
		
		private static var languages:ArrayCollection;
		{
			//init static vars
			languages = LanguageCodes.getLanguageCodes(Application.locale);
		}
		
		public function LanguageCodeAutoCompletePresenter(view:LanguageCodeAutoComplete) {
			this._view = view;
			super(view);
			minCharsToStartAutocomplete = 1;
			allowNotListedValues = false;
		}
		
		protected function languagesFilterFunction(item:LanguageItem):Boolean {
			var searchText:String = _view.text;
			if ( StringUtil.isNotBlank(searchText) ) {
				return StringUtil.startsWith(item.code, searchText, true) || StringUtil.startsWith(item.label, searchText, true);
			} else {
				return false;
			}
		}
		
		override protected function getMatchingResult():* {
			var searchText:String = _view.text;
			var list:IList = LanguageCodeAutoCompletePopUp(popUp).dataGrid.dataProvider;
			for each (var item:Object in list) {
				var compareToValue:String = item.code;
				if ( compareToValue != null && searchText.toUpperCase() == compareToValue.toUpperCase() ) {
					return item;
				}
			}
			return null;
		}
		
		override protected function loadAutoCompleteData():void {
			languages.filterFunction = languagesFilterFunction;
			languages.refresh();
			handleSearchResult(languages);
		}
		
		override protected function handleSearchResult(data:IList):void {
			if ( CollectionUtil.isEmpty(data) ) {
				closePopUp();
			} else {
				showPopUp();
				LanguageCodeAutoCompletePopUp(popUp).dataGrid.dataProvider = data;
			}
		}
		
		override protected function createPopUp():AutoCompletePopUp {
			var popUp:LanguageCodeAutoCompletePopUp = new LanguageCodeAutoCompletePopUp();
			return popUp;
		}
		
		override protected function performSelectValue(selectedValue:*):void {
			if ( selectedValue != null ) {
				_view.text = selectedValue.code;
				updateValue();
			}
			closePopUp();
		}
		
	}
}