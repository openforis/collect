package org.openforis.collect.event {
	
	import flash.events.Event;
	
	/**
	 * @author M. Togna
	 * @author S. Ricci 
	 * */
	
	public class UIEvent extends Event {
		
		//?
		public static const SURVEY_SELECTED:String = "surveySelected";
		public static const ROOT_ENTITY_SELECTED:String = "rootEntitySelected";
		public static const NEW_RECORD_CREATED:String = "newRecordCreated";
		public static const BACK_TO_LIST:String = "backToList";
		
		private var _obj:Object;
		
		public function UIEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}
		
		public function get obj():Object {
			return _obj!=null?_obj : "";
		}
		
		public function set obj(value:Object):void {
			_obj = value;
		}
	}
}