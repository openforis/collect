package org.openforis.collect.event {
	
	import flash.events.Event;
	
	/**
	 * @author M. Togna
	 * @author S. Ricci 
	 * */
	public class UIEvent extends Event {
		
		//?
		public static const SHOW_SURVEY_SELECTION:String = "showSurveySelection";
		public static const SHOW_ROOT_ENTITY_SELECTION:String = "showRootEntitySelection";
		public static const SURVEY_SELECTED:String = "surveySelected";
		public static const ROOT_ENTITY_SELECTED:String = "rootEntitySelected";
		public static const BACK_TO_LIST:String = "backToList";
		
		public static const RECORD_SELECTED:String = "recordSelected";
		public static const RECORD_CREATED:String = "recordCreated";
		
		//List events		
		public static const LOAD_RECORD_SUMMARIES:String = "loadRecordSummaries"; 
		public static const RELOAD_RECORD_SUMMARIES:String = "reloadRecordSummaries"; 
		
		//Edit events
		public static const ACTIVE_RECORD_CHANGED:String = "activeRecordChanged";
		public static const BUILD_FORM:String = "buildForm";
		
		public static const ITEM_SELECT:String = "itemSelect";
		
		private var _obj:Object;
		private var _params:Array;
		
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