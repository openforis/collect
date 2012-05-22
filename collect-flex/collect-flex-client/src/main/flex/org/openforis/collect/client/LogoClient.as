package org.openforis.collect.client {
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author E. Wibowo
	 * */
	public class LogoClient extends AbstractClient {
		
		private var _getLogo:Operation;

		public function LogoClient() {
			super("logoService");
			_getLogo = getOperation("getLogo", CONCURRENCY_LAST);
		}
		
		public function getLogo(responder:IResponder, id:int):void {
			var token:AsyncToken = this._getLogo.send(id);
			token.addResponder(responder);
		}
	}
}