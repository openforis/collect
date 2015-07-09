package org.openforis.collect.i18n {
	import mx.resources.IResourceManager;
	import mx.resources.Locale;
	import mx.resources.ResourceManager;
	import mx.utils.StringUtil;
	
	import org.openforis.collect.Application;

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
			var obj:Object = RESOURCE_MANAGER.getObject(bundle, resource, locale);
			if (obj == null && locale != DEFAULT_LOCALE) {
				obj = RESOURCE_MANAGER.getObject(bundle, resource, DEFAULT_LOCALE);
			}
			if (obj == null) {
				return resource;
			}
			var message:String = String(obj);
			if (parameters) {
				message = StringUtil.substitute(message, parameters);
			}
			return message;
		} 
		
	}
}