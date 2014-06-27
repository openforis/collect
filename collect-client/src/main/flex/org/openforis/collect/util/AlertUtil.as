package org.openforis.collect.util
{
	import mx.controls.Alert;
	import mx.events.CloseEvent;
	import mx.rpc.Fault;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.ui.Images;
	import org.openforis.collect.ui.component.BlockingMessagePopUp;

	/**
	 * @author S. Ricci
	 */
	public class AlertUtil
	{
		private static const ERROR_TITLE_RESOURCE:String = "global.errorAlertTitle";
		private static const INFO_TITLE_RESOURCE:String = "global.infoAlertTitle";
		private static const CONFIRM_TITLE_RESOURCE:String = "global.confirmAlertTitle";

		public static function showError(messageResource:String, messageParameters:Array = null, titleResource:String = null, titleParameters:Array = null):void {
			if(titleResource == null) {
				titleResource = ERROR_TITLE_RESOURCE;
			}
			var alert:Alert = showMsg(Images.ERROR, messageResource, messageParameters, titleResource, titleParameters);
			alert.styleName = "error";
		}
		
		public static function showMessage(messageResource:String, messageParameters:Array = null, 
										   titleResource:String = null, titleParameters:Array = null):void {
			if(titleResource == null) {
				titleResource = INFO_TITLE_RESOURCE;
			}
			showMsg(null, messageResource, messageParameters, titleResource, titleParameters);
		}
		
		private static function showMsg(icon:Class, messageResource:String, messageParameters:Array = null, 
										titleResource:String = null, titleParameters:Array = null):Alert {
			var message:String = Message.get(messageResource, messageParameters);
			var title:String = Message.get(titleResource, titleParameters);
			var alert:Alert = Alert.show(message, title, Alert.OK, null, null, icon);
			return alert;
		}
		
		public static function showConfirm(messageResource:String, parameters:Array, titleResource:String, 
										   yesHandler:Function, yesArgs:Array = null, noHandler:Function = null):void {
			if(titleResource == null) {
				titleResource = CONFIRM_TITLE_RESOURCE;
			}
			var message:String = Message.get(messageResource, parameters);
			var title:String = Message.get(titleResource);
			
			Alert.show(message, title, Alert.YES|Alert.NO, null, closeHandler);
			
			function closeHandler(event:CloseEvent):void {
				if(event.detail == Alert.YES) {
					yesHandler.apply(null, yesArgs);
				} else if(noHandler != null) {
					noHandler();
				}
			}
		}
		
		public static function showBlockingMessage(messageKey:String, error:Error):void {
			if(! Application.serverOffline) {
				var message:String = Message.get(messageKey);
				var now:String = new Date().toString();
				var details:String = StringUtil.concat("\n\n", now, error.name, error.toString(), error.getStackTrace());
				BlockingMessagePopUp.show(Message.get("global.errorAlertTitle"), message, details, Images.ERROR);
			}
			Application.serverOffline = true;
			Application.activeRecord = null;
		}
		
	}
}