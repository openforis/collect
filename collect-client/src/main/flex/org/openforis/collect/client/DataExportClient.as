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
	public class DataExportClient extends AbstractClient {
		
		private var _getCurrentJobOperation:Operation;
		private var _exportOperation:Operation;
		private var _fullExportOperation:Operation;
		private var _abortOperation:Operation;
		
		public function DataExportClient() {
			super("dataExportService");
			
			this._exportOperation = getOperation("export");
			this._fullExportOperation = getOperation("fullExport");
			this._getCurrentJobOperation = getOperation("getCurrentJob", CONCURRENCY_LAST, false);
			this._abortOperation = getOperation("abort");
		}
		
		public function getCurrentJob(responder:IResponder):void {
			var token:AsyncToken = this._getCurrentJobOperation.send();
			token.addResponder(responder);
		}
		
		public function export(responder:IResponder, rootEntityName:String, stepNumber:int, entityId:Number = NaN, 
							   includeAllAncestorAttributes:Boolean = false, onlyOwnedRecords:Boolean = false, rootEntityKeyValues:Array = null):void {
			var token:AsyncToken = this._exportOperation.send(rootEntityName, stepNumber, entityId, includeAllAncestorAttributes, onlyOwnedRecords, rootEntityKeyValues);
			token.addResponder(responder);
		}
		
		public function fullExport(responder:IResponder, includeRecordFiles:Boolean = false, onlyOwnedRecords:Boolean = false, rootEntityKeyValues:Array = null):void {
			var token:AsyncToken = this._fullExportOperation.send(includeRecordFiles, onlyOwnedRecords, rootEntityKeyValues);
			token.addResponder(responder);
		}
		
		public function abort(responder:IResponder):void {
			var token:AsyncToken = this._abortOperation.send();
			token.addResponder(responder);
		}
		
	}
}