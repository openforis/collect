package org.openforis.collect.event {
	
	import flash.events.Event;
	
	/**
	 * @author Mino Togna 
	 * */
	
	public class UIEvent extends Event {
		
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