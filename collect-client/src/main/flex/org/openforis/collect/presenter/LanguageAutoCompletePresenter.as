package org.openforis.collect.presenter
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.i18n.Languages;
	import org.openforis.collect.model.LanguageItem;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.ui.component.input.AutoCompletePopUp;
	import org.openforis.collect.ui.component.input.LanguageAutoComplete;
	import org.openforis.collect.ui.component.input.LanguageCodeAutoCompletePopUp;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class LanguageAutoCompletePresenter extends AutoCompleteInputFieldPresenter {
		
		private var _view:LanguageAutoComplete;
		
		private static var languages:ArrayCollection;
		{
			//init static vars
			languages = Languages.getLanguageCodes(Application.localeString);
		}
		
		public function LanguageAutoCompletePresenter(view:LanguageAutoComplete) {
			this._view = view;
			super(view);
			minCharsToStartAutocomplete = 1;
			allowNotListedValues = false;
		}
		
		override protected function textToRequestValue():String {
			var label:String = super.textToRequestValue();
			if ( StringUtil.isNotBlank(label) && ! FieldProxy.isShortCutForReasonBlank(label) ) {
				return Languages.getCode(label);
			} else {
				return label;
			}
		}
		
		override protected function getTextFromValue():String {
			var code:String = super.getTextFromValue();
			if ( StringUtil.isNotBlank(code) ) {
				var language:String = Languages.getLanguageLabel(code);
				if ( language != null ) {
					return language;
				} else {
					return code;
				}
			} else {
				return code;
			}
		}
		
		protected function languagesFilterFunction(item:LanguageItem):Boolean {
			var searchText:String = _view.text;
			if ( StringUtil.isNotBlank(searchText) ) {
				return StringUtil.startsWith(item.label, searchText, true);
			} else {
				return false;
			}
		}
		
		override protected function getMatchingResult():* {
			var searchText:String = _view.text;
			var list:IList = LanguageCodeAutoCompletePopUp(popUp).dataGrid.dataProvider;
			for each (var item:LanguageItem in list) {
				var compareToValue:String = item.label;
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
				_view.text = (selectedValue as LanguageItem).label;
				updateValue();
			}
			closePopUp();
		}
		
	}
}