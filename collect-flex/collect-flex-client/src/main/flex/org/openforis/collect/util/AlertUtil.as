package org.openforis.collect.util
{
	import mx.controls.Alert;
	import mx.events.CloseEvent;
	
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
		
		public static function showConfirm(messageResource:String, parameters:Array, titleResource:String, yesHandler:Function, noHandler:Function = null):void {
			var message:String = Message.get(messageResource, parameters);
			var title:String = Message.get(titleResource);
			
			Alert.show(message, title, Alert.YES|Alert.NO, null, closeHandler);
			
			function closeHandler(event:CloseEvent):void {
				if(event.detail == Alert.YES) {
					yesHandler();
				} else if(noHandler != null) {
					noHandler();
				}
			}
		}
			
	}
}