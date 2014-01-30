package org.openforis.collect.util
{
	import flash.net.URLRequest;
	import flash.net.URLVariables;
	import flash.net.navigateToURL;

	/**
	 * @author S. Ricci
	 */
	public class NavigationUtil {
		
		public static function openInNewWindow(url:String, params:URLVariables = null):void {
			var req:URLRequest = new URLRequest(url);
			if ( params != null ) {
				req.data = params;
			}
			navigateToURL(req, "_new");
		}
	}
}