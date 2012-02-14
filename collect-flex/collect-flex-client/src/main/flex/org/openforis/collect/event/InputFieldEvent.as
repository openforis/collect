package org.openforis.collect.event
{
	import org.openforis.collect.ui.component.input.InputField;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class InputFieldEvent extends UIEvent
	{
		public static const INPUT_FIELD_FOCUS_IN:String = "inputFieldFocusIn";
		public static const INPUT_FIELD_VALUE_CHANGE:String = "inputFieldValueChange";
		public static const INPUT_FIELD_FOCUS_OUT:String = "inputFieldFocusOut";
		public static const INPUT_FIELD_MOUSE_OVER:String = "inputFieldMouseOver";
		public static const INPUT_FIELD_MOUSE_OUT:String = "inputFieldMouseOut";
		
		private var _inputField:InputField;
		
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