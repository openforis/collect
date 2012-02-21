package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class SessionClient extends AbstractClient {
		
		private var _getSessionStateOperation:Operation;
		private var _keepAliveOperation:Operation;
		private var _setLocaleOperation:Operation;
		
		
		private var _testGetValueOperation:Operation;
		private var remoteCallQueueProcessor:RemoteCallQueueProcessor = new RemoteCallQueueProcessor();
		
		
		public function SessionClient() {
			super("sessionService");
			
			this._getSessionStateOperation = getOperation("getSessionState");
			this._keepAliveOperation = getOperation("keepAlive");
			this._setLocaleOperation = getOperation("setLocale");
		}
		
		public function getSessionState(responder:IResponder):void {
			var token:AsyncToken = this._getSessionStateOperation.send();
			token.addResponder(responder);
		}
		
		public function keepAlive(responder:IResponder):void {
			var token:AsyncToken = this._keepAliveOperation.send();
			token.addResponder(responder);
		}
		
		public function setLocale(responder:IResponder, localeString:String):void {
			var token:AsyncToken = this._setLocaleOperation.send(localeString);
			token.addResponder(responder);
		}
		
		// TEST
		public function testGetValue(responder:IResponder):void {
			/*
			var token:AsyncToken = this._testGetValue.send();
			token.addResponder(responder);
			*/
			
			remoteCallQueueProcessor.appendOperation(responder, this._testGetValueOperation);
		}
		
	}
}