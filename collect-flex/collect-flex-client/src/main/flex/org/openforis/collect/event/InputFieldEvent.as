package org.openforis.collect.event
{
	import org.openforis.collect.ui.component.detail.input.InputField;

	public class InputFieldEvent extends UIEvent
	{
		public static const INPUT_FIELD_FOCUS_IN:String = "inputFieldFocusIn";
		public static const INPUT_FIELD_VALUE_CHANGE:String = "inputFieldValueChange";
		public static const INPUT_FIELD_FOCUS_OUT:String = "inputFieldFocusOut";
		public static const INPUT_FIELD_MOUSE_OVER:String = "inputFieldMouseOver";
		public static const INPUT_FIELD_MOUSE_OUT:String = "inputFieldMouseOut";
		
		public var inputField:InputField;
		
		public function InputFieldEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}