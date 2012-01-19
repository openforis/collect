package org.openforis.collect.util
{
	import mx.controls.Alert;
	import mx.events.CloseEvent;
	
	import org.openforis.collect.i18n.Message;
	
	/**
	 * @author S. Ricci
	 */
	public class ConfirmUtil {
		public static function showConfirm(messageResource:String, titleResource:String, yesHandler:Function, noHandler:Function = null):void {
			var message:String = Message.get(messageResource);
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