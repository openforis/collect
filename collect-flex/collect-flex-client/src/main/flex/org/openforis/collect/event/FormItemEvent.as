package org.openforis.collect.event
{
	import org.openforis.collect.ui.component.detail.CollectFormItem;
	import org.openforis.collect.ui.component.input.InputField;

	/**
	 * @author S. Ricci
	 */
	public class FormItemEvent extends UIEvent
	{
		public static const FORM_ITEM_MOUSE_OVER:String = "formItemMouseOver";
		public static const FORM_ITEM_MOUSE_OUT:String = "formItemMouseOut";
		
		private var _formItem:CollectFormItem;
		
		public function FormItemEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}

		public function get formItem():CollectFormItem {
			return _formItem;
		}

		public function set formItem(value:CollectFormItem):void {
			_formItem = value;
		}

	}
}