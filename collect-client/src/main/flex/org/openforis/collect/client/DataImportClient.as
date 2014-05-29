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
	public class DataImportClient extends AbstractClient {
		
		private var _getCurrentJobOperation:Operation;
		private var _startSummaryCreationOperation:Operation;
		private var _getSummaryOperation:Operation;
		private var _startImportOperation:Operation;
		private var _overwriteExistingRecordInConflict:Operation;
		private var _cancelOperation:Operation;
		
		public function DataImportClient() {
			super("dataImportService");
			
			_startSummaryCreationOperation = getOperation("startSummaryCreation");
			_getSummaryOperation = getOperation("getSummary");
			_startImportOperation = getOperation("startImport");
			_overwriteExistingRecordInConflict = getOperation("overwriteExistingRecordInConflict");
			_getCurrentJobOperation = getOperation("getCurrentJob", CONCURRENCY_LAST, false);
			_cancelOperation = getOperation("cancel");
		}
		
		public function getCurrentJob(responder:IResponder):void {
			var token:AsyncToken = this._getCurrentJobOperation.send();
			token.addResponder(responder);
		}
		
		public function getSummary(responder:IResponder):void {
			var token:AsyncToken = this._getSummaryOperation.send();
			token.addResponder(responder);
		}
		
		public function startSummaryCreation(responder:IResponder, selectedSurveyUri:String, filePath:String, overwriteAll:Boolean = false):void {
			var token:AsyncToken = this._startSummaryCreationOperation.send(selectedSurveyUri, filePath, overwriteAll);
			token.addResponder(responder);
		}
		
		public function startImport(responder:IResponder, entryIdsToImport:IList):void {
			var token:AsyncToken = this._startImportOperation.send(entryIdsToImport);
			token.addResponder(responder);
		}

		public function overwriteRecordInConflict(responder:IResponder, value:Boolean, overwriteAll:Boolean = false):void {
			var token:AsyncToken = this._overwriteExistingRecordInConflict.send(value, overwriteAll);
			token.addResponder(responder);
		}

		public function cancel(responder:IResponder):void {
			var token:AsyncToken = this._cancelOperation.send();
			token.addResponder(responder);
		}
		
	}
}