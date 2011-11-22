package org.openforis.collect.presenter {
	
	import flash.events.EventDispatcher;
	
	import mx.controls.Alert;
	import mx.rpc.events.FaultEvent;
	
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.ui.Images;
	import org.openforis.collect.ui.component.BlockingMessagePopUp;

	/**
	 * @author Mino Togna
	 * */
	internal class AbstractPresenter {

		private static var serverOffline:Boolean = false;
		private static var _serverOffLineMessage:String;
		
		public  function AbstractPresenter() {
			initEventListeners();	
		}
		
		internal static function get serverOffLineMessage():String {
			if(_serverOffLineMessage == null) {
				_serverOffLineMessage = Message.get("global.serverOffLineMsg");
			}
			return _serverOffLineMessage;
		}
		
		internal function get eventDispatcher():EventDispatcher {
			return EventDispatcherFactory.getEventDispatcher();
		}
		
		public static function faultResult(event:FaultEvent, token:Object=null):void {
			if(event.fault.faultCode == "Channel.Call.Failed"
				|| event.fault.faultCode == "Client.Error.MessageSend"
				|| event.fault.faultCode == "Client.Error.DeliveryInDoubt") {
				//server offline
				if(!serverOffline) {
					BlockingMessagePopUp.show(Message.get("global.serverOffLine"), serverOffLineMessage, Images.ERROR);
				}
				serverOffline = true;
			} else {
				Alert.show(Message.get("global.faultHandlerMsg")+"\n\n"+ event.toString());
			}
		}
		
		internal function initEventListeners():void {}
		
	}
}