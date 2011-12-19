package org.openforis.collect.presenter {
	
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ItemResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.SessionClient;
	import org.openforis.collect.idm.model.impl.AbstractValue;
	import org.openforis.collect.idm.model.impl.BooleanValueImpl;

	/**
	 * 
	 * @author Mino Togna
	 * */
	public class CollectPresenter extends AbstractPresenter {

		private static const KEEP_ALIVE_FREQUENCY:Number = 30000;

		private var _view:collect;
		private var _sessionClient:SessionClient;
		private var _contextMenuPresenter:ContextMenuPresenter;
		
		private var _keepAliveTimer:Timer;
		
		public function CollectPresenter(view:collect) {
			super();
			
			this._view = view;
			this._sessionClient = ClientFactory.sessionClient;
			/*
			_keepAliveTimer = new Timer(KEEP_ALIVE_FREQUENCY)
			_keepAliveTimer.addEventListener(TimerEvent.TIMER, sendKeepAliveMessage);
			_keepAliveTimer.start();
			
			this._sessionClient.getSessionState(new ItemResponder(getSessionStateResult, faultResult));
			*/
			this._contextMenuPresenter = new ContextMenuPresenter(view);
			
			//this._sessionClient.testGetValue(new ItemResponder(getValueResultHandler, faultHandler));
				
			function getValueResultHandler(event:ResultEvent, token:Object = null):void {
				var abstractValue:AbstractValue = event.result as AbstractValue;
				var booleanValue:BooleanValueImpl = event.result as BooleanValueImpl;
				trace(abstractValue.text1);
			}
		}
		
		override internal function initEventListeners():void {
			
		}
		internal function res(event:ResultEvent, token:Object = null):void {
			trace(event);
		}
		
		internal function getSessionStateResult(event:ResultEvent, token:Object = null):void {
			//Application.SESSION_ID = event.result as String;
			//TODO: Add sessionState to Application
		}
		
		internal function sendKeepAliveMessage(event:TimerEvent):void {
			this._sessionClient.keepAlive(new ItemResponder(keepAliveResult, faultHandler));
		}
		
		internal function keepAliveResult(event:ResultEvent, token:Object = null):void {
			//keep alive succesfully sent
			//trace("[Keep Alive response received] " + event);
		}
	}
}