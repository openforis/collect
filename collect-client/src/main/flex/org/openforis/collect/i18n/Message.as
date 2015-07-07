package org.openforis.collect.i18n {
	import mx.resources.Locale;
	import mx.resources.ResourceManager;
	
	import org.openforis.collect.Application;
	import mx.resources.IResourceManager;

	/**
	 * @author Mino Togna
	 * @author S. Ricci
	 * */
	public class Message {
		
		public static const DEFAULT_LOCALE:String = "en_US";
		private static const MESSAGES_BUNDLE_NAME:String = "messages";
		
		private static const RESOURCE_MANAGER:IResourceManager = ResourceManager.getInstance();
		
		public static function get(resource:String, parameters:Array=null, bundle:String=MESSAGES_BUNDLE_NAME):String {
			var locale:String = Application.locale == null ? DEFAULT_LOCALE : Application.locale.toString();
			var message:String = RESOURCE_MANAGER.getString(bundle, resource, parameters, locale);
			if (message == null && locale != DEFAULT_LOCALE) {
				message = ResourceManager.getInstance().getString(bundle, resource, parameters, DEFAULT_LOCALE);
			}
			return message == null ? resource : message;
		} 
	}
}