package org.openforis.collect.client {
	import mx.collections.IList;
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	import org.openforis.collect.io.parsing.CSVFileOptions;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class SpeciesImportClient extends AbstractClient {
		
		private var _getStatusOperation:Operation;
		private var _startOperation:Operation;
		private var _cancelOperation:Operation;
		
		public function SpeciesImportClient() {
			super("speciesImportService");
			
			_startOperation = getOperation("start");
			_getStatusOperation = getOperation("getStatus", CONCURRENCY_LAST, false);
			_cancelOperation = getOperation("cancel");
		}
		
		public function getStatus(responder:IResponder):void {
			var token:AsyncToken = this._getStatusOperation.send();
			token.addResponder(responder);
		}
		
		public function start(responder:IResponder, tempFileName:String, csvFileOptions:CSVFileOptions, taxonomyId:int, overwriteAll:Boolean):void {
			var token:AsyncToken = this._startOperation.send(tempFileName, csvFileOptions, taxonomyId, overwriteAll);
			token.addResponder(responder);
		}

		public function cancel(responder:IResponder):void {
			var token:AsyncToken = this._cancelOperation.send();
			token.addResponder(responder);
		}
		
	}
}