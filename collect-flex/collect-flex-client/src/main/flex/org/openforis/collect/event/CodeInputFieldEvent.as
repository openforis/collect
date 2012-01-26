package org.openforis.collect.event
{
	
	public class CodeInputFieldEvent extends InputFieldEvent
	{
		public static const OPEN_LIST_DIALOG_CLICK:String = "openListDialogClick";
		public static const CODE_LIST_DIALOG_APPLY_CLICK:String = "codeListDialogApplyClick";
		public static const CODE_LIST_DIALOG_CANCEL_CLICK:String = "codeListDialogCancelClick";
		
		public function CodeInputFieldEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}