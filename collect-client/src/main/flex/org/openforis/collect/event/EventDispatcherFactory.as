package org.openforis.collect.event {
	
	import flash.events.EventDispatcher;
	/**
	 * EventDispatcherFactory
	 * 
	 * @author Mino Togna 
	 */
	public class EventDispatcherFactory {
		
		private static var _instance:EventDispatcher;
		
		public function EventDispatcherFactory() {
		}
		
		public static function getEventDispatcher():EventDispatcher {
			if (_instance == null) {
				_instance = new EventDispatcher();
			}
			return _instance;
		}
	}
}