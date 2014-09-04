package org.openforis.collect.presenter
{
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	
	import org.openforis.collect.ui.component.input.DropDownCodeInputField;
	
	/**
	 * @author S. Ricci
	 */
	public class DropDownCodeInputFieldPresenter extends PreloadedCodeInputFieldPresenter {
		
		public function DropDownCodeInputFieldPresenter(view:DropDownCodeInputField) {
			super(view);
		}
		
		private function get view():DropDownCodeInputField {
			return DropDownCodeInputField(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			
			view.dropDownList.addEventListener(FocusEvent.FOCUS_IN, focusInHandler);
			view.dropDownList.addEventListener(FocusEvent.FOCUS_OUT, focusOutHandler);
			view.dropDownList.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
		}
		
	}
}