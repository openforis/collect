package org.openforis.collect.event.input
{
	import org.openforis.collect.event.InputFieldEvent;
	
	public class RemarksPopUpEvent extends InputFieldEvent
	{
		public static const SAVE:String = "save";
		public static const CANCEL:String = "cancel";
		
		public function RemarksPopUpEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}