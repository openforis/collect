package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	import test.Test;
	import test.TestImpl;
	import test._TestImpl;
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class SessionClient extends AbstractClient {
		
		private var _getSessionStateOperation:Operation;
		private var _keepAlive:Operation;
		
		public function SessionClient() {
			super();
			
			this._getSessionStateOperation = getOperation("getSessionState");
			this._keepAlive = getOperation("keepAlive");
		}
		
		public function getSessionState(responder:IResponder):void {
			var token:AsyncToken = this._getSessionStateOperation.send();
			token.addResponder(responder);
		}
		
		public function keepAlive(responder:IResponder):void {
			var token:AsyncToken = this._keepAlive.send();
			token.addResponder(responder);
		}
		
		
	}
}