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
		private static const INFO_TITLE_RESOURCE:String = "global.infoAlertTitle";

		public static function showError(messageResource:String, messageParameters:Array = null, titleResource:String = null, titleParameters:Array = null):void {
			if(titleResource == null) {
				titleResource = ERROR_TITLE_RESOURCE;
			}
			showMsg(Images.ERROR, messageResource, messageParameters, titleResource, titleParameters);
		}
		
		public static function showMessage(messageResource:String, messageParameters:Array = null, titleResource:String = null, titleParameters:Array = null):void {
			if(titleResource == null) {
				titleResource = INFO_TITLE_RESOURCE;
			}
			showMsg(null, messageResource, messageParameters, titleResource, titleParameters);
		}
		
		private static function showMsg(icon:Class, messageResource:String, messageParameters:Array = null, titleResource:String = null, titleParameters:Array = null) {
			var message:String = Message.get(messageResource, messageParameters);
			var title:String = Message.get(titleResource, titleParameters);
			Alert.show(message, title, Alert.OK, null, null, icon);
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