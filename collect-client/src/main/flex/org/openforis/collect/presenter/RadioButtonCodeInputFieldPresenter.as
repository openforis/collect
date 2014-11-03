package org.openforis.collect.presenter
{
	import flash.events.FocusEvent;
	import flash.events.KeyboardEvent;
	
	import org.openforis.collect.ui.component.input.RadioButtonCodeInputField;
	
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
			view.dataGroup.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
		}
		
	}
}