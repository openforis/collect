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
		private static const ERROR_TITLE:String = Message.get("global.errorAlertTitle");
		
		public static function showError(message:String, title:String = null):void {
			if(title == null) {
				title = ERROR_TITLE;
			}
			Alert.show(message, title, Alert.OK, null, null, Images.ERROR);
		}
		
		public static function showMessage(message:String, title:String=""):void {
			Alert.show(message, title);
		}
	}
}