package org.openforis.collect.event.input
{
	import org.openforis.collect.event.InputFieldEvent;
	
	public class DateInputFieldEvent extends InputFieldEvent
	{
		public static const DATE_FIELD_OPEN:String = "dateFieldOpen";
		public static const DATE_FIELD_CHANGE:String = "dateFieldChange";
		
		public function DateInputFieldEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}