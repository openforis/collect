package org.openforis.collect.presenter {
	import org.openforis.collect.ui.component.input.NumericInputField;
	
	/**
	 * @author S. Ricci
	 * 
	 * */
	public class NumericInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:NumericInputField;
		
		public function NumericInputFieldPresenter(view:NumericInputField) {
			_view = view;
			super(view);
		}
		
		override protected function textToRequestValue():String {
			var text:String = _view.text;
			if(text == ".") {
				return "";
			} else {
				return super.textToRequestValue();
			}
		}
		
	}
}