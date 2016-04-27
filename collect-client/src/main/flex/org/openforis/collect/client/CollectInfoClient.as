package org.openforis.collect.client {
	import mx.collections.IList;
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CollectInfoClient extends AbstractClient {
		
		private var _getCompleteInfoOperation:Operation;

		public function CollectInfoClient() {
			super("collectInfoService");
			
			_getCompleteInfoOperation = getOperation("getCompleteInfo", CONCURRENCY_MULTIPLE, false);
		}
		
		public function getCompleteInfo(responder:IResponder):void {
			var token:AsyncToken = this._getCompleteInfoOperation.send();
			token.addResponder(responder);
		}

	}
}