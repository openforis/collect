package org.openforis.collect.client {
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DataImportClient extends AbstractClient {
		
		private var _getStateOperation:Operation;
		private var _initProcessOperation:Operation;
		private var _startImportOperation:Operation;
		private var _cancelOperation:Operation;
		
		public function DataImportClient() {
			super("dataImportService");
			
			this._initProcessOperation = getOperation("initProcess");
			this._startImportOperation = getOperation("startImport");
			this._getStateOperation = getOperation("getState", CONCURRENCY_LAST, false);
			this._cancelOperation = getOperation("cancel");
		}
		
		public function getState(responder:IResponder):void {
			var token:AsyncToken = this._getStateOperation.send();
			token.addResponder(responder);
		}
		
		public function startImport(responder:IResponder):void {
			var token:AsyncToken = this._startImportOperation.send();
			token.addResponder(responder);
		}

		public function initProcess(responder:IResponder, surveyName:String, rootEntityName:String):void {
			var token:AsyncToken = this._initProcessOperation.send(surveyName, rootEntityName);
			token.addResponder(responder);
		}
		
		public function cancel(responder:IResponder):void {
			var token:AsyncToken = this._cancelOperation.send();
			token.addResponder(responder);
		}
		
	}
}