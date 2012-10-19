package org.openforis.collect.i18n {
	import mx.resources.ResourceManager;

	/**
	 * @author Mino Togna
	 * @author S. Ricci
	 * */
	public class Message {
		
		public function Message() {
		}
		
		public static function get(resource:String, parameters:Array=null, bundle:String="messages"):String {
			var message:String = ResourceManager.getInstance().getString(bundle, resource, parameters);
			if(message != null) {
				return message;
			} else {
				return resource;
			}
		} 
	}
}