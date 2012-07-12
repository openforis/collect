package org.openforis.collect.event
{
	import flash.events.Event;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class ImageLoaderEvent extends Event {
		
		public static const IMAGE_CLICK:String = "imageClick";
		
		public function ImageLoaderEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false) {
			super(type, bubbles, cancelable);
		}
	}
}