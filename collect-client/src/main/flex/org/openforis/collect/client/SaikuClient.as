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
	public class SaikuClient extends AbstractClient {
		
		private var _isSaikuAvailableOperation:Operation;
		private var _loadInfoOperation:Operation;
		private var _generateRdbOperation:Operation;

		public function SaikuClient() {
			super("saikuService");
			
			_isSaikuAvailableOperation = getOperation("isSaikuAvailable", CONCURRENCY_LAST, false);
			_loadInfoOperation = getOperation("loadInfo", CONCURRENCY_MULTIPLE, false);
			_generateRdbOperation = getOperation("generateRdb");
		}
		
		public function isSaikuAvailable(responder:IResponder):void {
			var token:AsyncToken = this._isSaikuAvailableOperation.send();
			token.addResponder(responder);
		}

		public function loadInfo(responder:IResponder, surveyName:String):void {
			var token:AsyncToken = this._loadInfoOperation.send(surveyName);
			token.addResponder(responder);
		}

		public function generateRdb(responder:IResponder, surveyName:String):void {
			var token:AsyncToken = this._generateRdbOperation.send(surveyName);
			token.addResponder(responder);
		}
	}
}