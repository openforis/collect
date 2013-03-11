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
	public class AbstractReferenceDataImportClient extends AbstractClient {
		
		protected var _getStatusOperation:Operation;
		protected var _startOperation:Operation;
		protected var _cancelOperation:Operation;
		
		public function AbstractReferenceDataImportClient(serviceName:String) {
			super(serviceName);
			
			_startOperation = getOperation("start");
			_getStatusOperation = getOperation("getStatus", CONCURRENCY_LAST, false);
			_cancelOperation = getOperation("cancel");
		}
		
		public function getStatus(responder:IResponder):void {
			var token:AsyncToken = this._getStatusOperation.send();
			token.addResponder(responder);
		}
		
		public function cancel(responder:IResponder):void {
			var token:AsyncToken = this._cancelOperation.send();
			token.addResponder(responder);
		}
		
	}
}