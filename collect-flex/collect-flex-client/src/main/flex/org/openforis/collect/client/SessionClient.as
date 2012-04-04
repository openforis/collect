package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.Application;
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class SessionClient extends AbstractClient {
		
		private var _getSessionStateOperation:Operation;
		private var _keepAliveOperation:Operation;
		private var _setLocaleOperation:Operation;
		
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
			var editing:Boolean = Application.isEditingRecord();
			var token:AsyncToken = this._keepAliveOperation.send(editing);
			token.addResponder(responder);
		}
		
		public function setLocale(responder:IResponder, localeString:String):void {
			var token:AsyncToken = this._setLocaleOperation.send(localeString);
			token.addResponder(responder);
		}
		
	}
}