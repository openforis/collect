package org.openforis.collect.presenter {
	
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	import mx.rpc.events.FaultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;

	/**
	 * @author Mino Togna
	 * */
	internal class AbstractPresenter {

		private static var _serverOffLineMessage:String;
		private var _broadcastEventListenerInitialized:Boolean;
		protected var _view:DisplayObject;
		
		public function AbstractPresenter(view:DisplayObject) {
			this._view = view;
		}
		
		public function init():void {
			initEventListeners();
			initBroadcastEventListeners();
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
				case "org.openforis.collect.persistence.RecordNotOwnedException":
					AlertUtil.showError("list.error.cannotEdit.differentOwner");
					break;
				case "org.openforis.collect.manager.RecordPromoteException" :
					AlertUtil.showError('error.promoteException');
					break;
				case "org.openforis.collect.persistence.MissingRecordKeyException":
					AlertUtil.showError("error.missingRootEntityKeys");
					break;
				case "org.openforis.collect.manager.DatabaseVersionNotCompatibleException":
					AlertUtil.showBlockingMessage("error.databaseVersionNotCompatible", event.fault);
					break;
				case "Channel.Call.Failed":
					"Client.Error.MessageSend"
					"Client.Error.DeliveryInDoubt"
				default:
					AlertUtil.showBlockingMessage("global.faultHandlerMsg", event.fault);
			}
		}
		
		protected static function preventDefaultHandler(event:Event):void {
			event.preventDefault();
		}
		
		protected static function preventDefaultHandlerAndPropagation(event:Event):void {
			event.preventDefault();
			event.stopImmediatePropagation();
		}
		
		protected function initEventListeners():void {
			if ( _view != null ) {
				_view.addEventListener(Event.ADDED_TO_STAGE, addedToStageHandler);
				_view.addEventListener(Event.REMOVED_FROM_STAGE, removedFromStageHandler);
			}
		}
		
		protected function addedToStageHandler(event:Event):void {
			if ( ! _broadcastEventListenerInitialized ) {
				initBroadcastEventListeners();
			}
		}
		
		protected function removedFromStageHandler(event:Event):void {
			removeBroadcastEventListeners();
		}
		
		protected function initBroadcastEventListeners():void {
			_broadcastEventListenerInitialized = true;
		}
		
		protected function removeBroadcastEventListeners():void {
			_broadcastEventListenerInitialized = false;
		};
	}
}