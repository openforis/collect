package org.openforis.collect.event
{
	import org.openforis.collect.model.FieldSymbol;
	import org.openforis.collect.ui.component.input.InputField;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class InputFieldEvent extends UIEvent {
		
		public static const FOCUS_IN:String = "inputFieldFocusIn";
		public static const CHANGING:String = "inputFieldChanging";
		public static const OPEN_REMARKS_POPUP:String = "inputFieldOpenRemarksPopup";
		public static const VISITED:String = "visited";
		
		private var _inputField:InputField;
		private var _symobl:FieldSymbol;
		
		public function InputFieldEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}

		public function get inputField():InputField {
			return _inputField;
		}

		public function set inputField(value:InputField):void {
			_inputField = value;
		}

	}
}