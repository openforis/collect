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
		
		public var result:Object;
		
		public function ApplicationEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}
		
	}
}