package org.openforis.collect.client {
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author E. Wibowo
	 * 
	 */
	public class LogoClient extends AbstractClient {		
		private var _loadLogo:Operation;

		public function LogoClient() {
			super("logoService");
			_loadLogo = getOperation("loadLogo", CONCURRENCY_LAST);
		}
		
		public function loadLogo(responder:IResponder, id:int):void {
			var token:AsyncToken = this._loadLogo.send(id);
			token.addResponder(responder);
		}
	}
}