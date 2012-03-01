package org.openforis.collect.presenter
{
	import org.openforis.collect.ui.component.input.RangeInputField;
	
	/**
	 * @author S. Ricci
	 */
	public class RangeInputFieldPresenter extends InputFieldPresenter {
		
		private var _view:RangeInputField;
		
		public function RangeInputFieldPresenter(inputField:RangeInputField) {
			_view = inputField;
			_view.fieldIndex = -1;
			super(inputField);
		}
		
	}
}