package org.openforis.collect.presenter {
	import org.openforis.collect.ui.component.input.NumericInputField;
	
	/**
	 * @author S. Ricci
	 * 
	 * */
	public class NumericInputFieldPresenter extends InputFieldPresenter {
		
		public function NumericInputFieldPresenter(view:NumericInputField) {
			super(view);
		}
		
		private function get view():NumericInputField {
			return NumericInputField(_view);
		}
		
		override protected function textToRequestValue():String {
			var text:String = view.text;
			if(text == ".") {
				return "";
			} else {
				return super.textToRequestValue();
			}
		}
		
	}
}