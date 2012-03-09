package org.openforis.collect.presenter {
	
	import flash.events.EventDispatcher;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	import mx.controls.Alert;
	import mx.rpc.events.FaultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.ui.Images;
	import org.openforis.collect.ui.component.BlockingMessagePopUp;
	import org.openforis.collect.util.AlertUtil;

	/**
	 * @author Mino Togna
	 * */
	internal class AbstractPresenter {

		private static var _serverOffLineMessage:String;
		
		public function AbstractPresenter() {
			initEventListeners();
		}
		
		internal function get eventDispatcher():EventDispatcher {
			return EventDispatcherFactory.getEventDispatcher();
		}
		
		public static function faultHandler(event:FaultEvent, token:Object=null):void {
			var faultCode:String = event.fault.faultCode;
			switch(faultCode) {
				case "org.openforis.collect.web.session.InvalidSessionException":
					var u:URLRequest = new URLRequest(Application.URL +"login.htm?session_expired=1");
					Application.activeRecord = null;
					navigateToURL(u,"_self");
					break;
				case "org.openforis.collect.persistence.AccessDeniedException":
					AlertUtil.showError('error.accessDenied');
					break;
				case "org.openforis.collect.persistence.MultipleEditException":
					AlertUtil.showError('error.multipleEdit');
					break;
				case "org.openforis.collect.persistence.RecordLockedException":
					AlertUtil.showError('error.recordLocked');
					break;
				case "Channel.Call.Failed":
					"Client.Error.MessageSend"
					"Client.Error.DeliveryInDoubt"
				default:
					Alert.show(Message.get("global.faultHandlerMsg")
						+"\n\n"+ event.fault.faultCode
						+"\n\n"+ event.fault.faultString
					);
			}
		}
		
		internal function initEventListeners():void {}
		
	}
}