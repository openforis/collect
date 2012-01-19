package org.openforis.collect.util
{
	import mx.controls.Alert;
	import mx.events.CloseEvent;
	
	/**
	 * @author S. Ricci
	 */
	public class ConfirmUtil {
		public static function showConfirm(message:String, title:String, yesHandler:Function, noHandler:Function = null):void {
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