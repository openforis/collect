package org.openforis.collect.presenter {
	
	import flash.events.EventDispatcher;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	import mx.rpc.events.FaultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.ui.Images;
	import org.openforis.collect.ui.component.BlockingMessagePopUp;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.StringUtil;

	/**
	 * @author Mino Togna
	 * */
	internal class AbstractPresenter {

		private static var _serverOffLineMessage:String;
		
		public function AbstractPresenter() {
			initEventListeners();
		}
		
		internal static function get eventDispatcher():EventDispatcher {
			return EventDispatcherFactory.getEventDispatcher();
		}
		
		public static function faultHandler(event:FaultEvent, token:Object=null):void {
			var faultCode:String = event.fault.faultCode;
			switch(faultCode) {
				case "org.openforis.collect.web.session.InvalidSessionException":
					var u:URLRequest = new URLRequest(ApplicationConstants.URL +"login.htm?session_expired=1");
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
				case "org.openforis.collect.persistence.RecordUnlockedException":
					AlertUtil.showError("error.recordUnlocked");
					Application.activeRecord = null;
					var uiEvent:UIEvent = new UIEvent(UIEvent.BACK_TO_LIST);
					eventDispatcher.dispatchEvent(uiEvent);
					break;
				case "org.openforis.collect.manager.RecordPromoteException" :
					AlertUtil.showError('error.promoteException');
					break;
				case "org.openforis.collect.persistence.MissingRecordKeyException":
					AlertUtil.showError("error.missingRootEntityKeys");
					break;
				case "org.openforis.collect.manager.DatabaseVersionNotCompatibleException":
					showBlockingMessage("error.databaseVersionNotCompatible", event);
					break;
				case "Channel.Call.Failed":
					"Client.Error.MessageSend"
					"Client.Error.DeliveryInDoubt"
				default:
					showBlockingMessage("global.faultHandlerMsg", event);
			}
		}
		
		internal static function showBlockingMessage(messageKey:String, event:FaultEvent):void {
			if(! Application.serverOffline) {
				var message:String = Message.get(messageKey);
				var now:String = new Date().toString();
				var details:String = StringUtil.concat("\n\n", now, event.fault.faultCode, event.fault.faultDetail);
				BlockingMessagePopUp.show(Message.get("global.errorAlertTitle"), message, details, Images.ERROR);
			}
			Application.serverOffline = true;
			Application.activeRecord = null;
		}
		
		internal function initEventListeners():void {}
		
	}
}