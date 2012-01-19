package org.openforis.collect.util
{
	import mx.controls.Alert;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.ui.Images;

	/**
	 * @author S. Ricci
	 */
	public class AlertUtil
	{
		private static const ERROR_TITLE_RESOURCE:String = "global.errorAlertTitle";
		
		public static function showError(messageResource:String, parameters:Array = null, titleResource:String = null):void {
			if(titleResource == null) {
				titleResource = ERROR_TITLE_RESOURCE;
			}
			var message:String = Message.get(messageResource, parameters);
			var title:String = Message.get(titleResource, parameters);
			Alert.show(message, title, Alert.OK, null, null, Images.ERROR);
		}
		
		public static function showMessage(resource:String, title:String=""):void {
			var message:String = Message.get(resource);
			
			Alert.show(message, title);
		}
			
	}
}