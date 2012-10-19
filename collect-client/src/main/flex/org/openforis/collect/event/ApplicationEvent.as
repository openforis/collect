package org.openforis.collect.event {
	
	import flash.events.Event;
	
	/**
	 * @author Mino Togna 
	 * */
	
	public class ApplicationEvent extends Event {
		
		public static const METAMODEL_LOADED:String = "metamodelLoaded";
		public static const SURVEYS_LOADED:String = "surveysLoaded";
		public static const SESSION_STATE_LOADED:String = "sessionStateLoaded";
		public static const APPLICATION_INITIALIZED:String = "applicationInitialized";
		public static const UPDATE_RESPONSE_RECEIVED:String = "updateResponseReceived";
		public static const RECORD_SAVED:String = "recordSaved";
		public static const RECORD_UNLOCKED:String = "recordUnlocked";
		public static const ASK_FOR_SUBMIT:String = "askForSubmit";
			
		private var _result:Object;
		private var _token:Object;
		
		public function ApplicationEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}
		
		public function get result():Object {
			return _result;
		}

		public function set result(value:Object):void {
			_result = value;
		}

		public function get token():Object {
			return _token;
		}

		public function set token(value:Object):void {
			_token = value;
		}


	}
}