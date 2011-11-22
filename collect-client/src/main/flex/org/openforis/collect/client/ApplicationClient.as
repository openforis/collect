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
	public class ApplicationClient extends AbstractClient {
		
		private var _getSessionIdOperation:Operation;
		private var _keepAlive:Operation;
		
		public function ApplicationClient() {
			super();
			
			this._getSessionIdOperation = getOperation("getSessionId");
			this._keepAlive = getOperation("keepAlive");
		}
		
		public function getSessionId(responder:IResponder):void {
			var token:AsyncToken = this._getSessionIdOperation.send();
			token.addResponder(responder);
		}
		
		public function keepAlive(responder:IResponder):void {
			var token:AsyncToken = this._keepAlive.send();
			token.addResponder(responder);
		}
		
		public function sendTestInterface(name:String, surname:String, responder:IResponder):void {
			var test:Test = new TestImpl();
			test['name'] = name;
			test['surname'] = surname;
			
			var op:Operation = getOperation("test");
			op.send(test);
		}
	}
}