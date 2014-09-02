package org.openforis.collect.presenter
{
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	
	import org.openforis.collect.metamodel.proxy.CodeListItemProxy;
	import org.openforis.collect.ui.component.input.RadioButtonCodeInputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ArrayUtil;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.StringUtil;
	
	/**
	 * @author S. Ricci
	 */
	public class RadioButtonCodeInputFieldPresenter extends PreloadedCodeInputFieldPresenter {
		
		public function RadioButtonCodeInputFieldPresenter(view:RadioButtonCodeInputField) {
			super(view);
		}
		
		private function get view():RadioButtonCodeInputField {
			return RadioButtonCodeInputField(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			view.dataGroup.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			view.dataGroup.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			//view.radioButtonGroup.addEventListener(Event.CHANGE, changeHandler);
			view.addEventListener("apply", applyChangeHandler);
			view.dataGroup.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
		}
		
		protected function applyChangeHandler(event:Event):void {
			changeHandler(event);
			updateValue();
		}
		
		/*
		override protected function getSelectedItem():Object {
			var selectedValue:Object = view.radioButtonGroup.selectedValue;
			if ( StringUtil.isBlank(selectedValue) ) {
				return DropDownInputFieldPresenter.EMPTY_ITEM;
			} else {
				var selectedItem:Object = CollectionUtil.getItem(view.dataProvider, "code", selectedValue);
				if(selectedItem == null) {
					selectedItem = ArrayUtil.getItem([
						DropDownInputFieldPresenter.BLANK_ON_FORM_ITEM,
						DropDownInputFieldPresenter.DASH_ON_FORM_ITEM,
						DropDownInputFieldPresenter.ILLEGIBLE_ITEM
					], selectedValue, "shortCut");
				}
				return selectedItem;
			}
		}
		*/

	}
}