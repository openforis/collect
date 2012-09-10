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
		
		private var _keepAliveOperation:Operation;
		private var _initSessionOperation:Operation;
		private var _logoutOperation:Operation;
		
		public function SessionClient() {
			super("sessionService");
			
			this._keepAliveOperation = getOperation("keepAlive", CONCURRENCY_LAST);
			this._initSessionOperation = getOperation("initSession");
			this._logoutOperation = getOperation("logout");
		}
		
		public function keepAlive(responder:IResponder):void {
			var editing:Boolean = Application.isEditingRecord();
			var token:AsyncToken = this._keepAliveOperation.send(editing);
			token.addResponder(responder);
		}
		
		public function cancelLastKeepAliveOperation():void {
			this._keepAliveOperation.cancel();
		}
		
		public function initSession(responder:IResponder, localeString:String):void {
			var token:AsyncToken = this._initSessionOperation.send(localeString);
			token.addResponder(responder);
		}
		
		public function logout(responder:IResponder):void {
			var token:AsyncToken = this._logoutOperation.send();
			token.addResponder(responder);
		}
		
	}
}