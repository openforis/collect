package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.model.proxy.RecordProxy;
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class SessionClient extends AbstractClient {
		
		private var _keepAliveOperation:Operation;
		private var _initSessionOperation:Operation;
		
		public function SessionClient() {
			super("sessionService");
			
			this._keepAliveOperation = getOperation("keepAlive");
			this._initSessionOperation = getOperation("initSession");
		}
		
		public function keepAlive(responder:IResponder):void {
			var editing:Boolean = Application.isEditingRecord();
			var token:AsyncToken = this._keepAliveOperation.send(editing);
			token.addResponder(responder);
		}
		
		public function initSession(responder:IResponder, localeString:String):void {
			var token:AsyncToken = this._initSessionOperation.send(localeString);
			token.addResponder(responder);
		}
		
	}
}