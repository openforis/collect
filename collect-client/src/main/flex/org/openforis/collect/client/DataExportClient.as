package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DataExportClient extends AbstractClient {
		
		private var _getCurrentJobOperation:Operation;
		private var _exportOperation:Operation;
		private var _backupOperation:Operation;
		private var _fullExportOperation:Operation;
		private var _abortOperation:Operation;
		private var _sendBackupToRemoteCloneOperation:Operation;
		private var _getLastBackupInfoOperation:Operation;
		
		public function DataExportClient() {
			super("dataExportService");
			
			this._exportOperation = getOperation("export");
			this._backupOperation = getOperation("backup");
			this._fullExportOperation = getOperation("fullExport");
			this._getCurrentJobOperation = getOperation("getCurrentJob", CONCURRENCY_LAST, false);
			this._abortOperation = getOperation("abort");
			this._sendBackupToRemoteCloneOperation = getOperation("sendBackupToRemoteClone");
			this._getLastBackupInfoOperation = getOperation("getLastBackupInfo");
		}
		
		public function getCurrentJob(responder:IResponder):void {
			var token:AsyncToken = this._getCurrentJobOperation.send();
			token.addResponder(responder);
		}
		
		public function export(responder:IResponder, rootEntityName:String, stepNumber:int, entityId:Number = NaN, 
							   includeAllAncestorAttributes:Boolean = false, includeEnumeratedEntities:Boolean = false, 
							   includeCompositeAttributeMergedColumn:Boolean = false,
							   codeAttributeExpanded:Boolean = false,
							   onlyOwnedRecords:Boolean = false, rootEntityKeyValues:Array = null, 
							   includeKMLColumnForCoordinates:Boolean = false):void {
			var token:AsyncToken = this._exportOperation.send(rootEntityName, stepNumber, entityId, includeAllAncestorAttributes, 
					includeEnumeratedEntities, includeCompositeAttributeMergedColumn, codeAttributeExpanded, onlyOwnedRecords, rootEntityKeyValues,
					includeKMLColumnForCoordinates);
			token.addResponder(responder);
		}
		
		public function fullExport(responder:IResponder, includeRecordFiles:Boolean = false, onlyOwnedRecords:Boolean = false, rootEntityKeyValues:Array = null):void {
			var token:AsyncToken = this._fullExportOperation.send(includeRecordFiles, onlyOwnedRecords, rootEntityKeyValues);
			token.addResponder(responder);
		}
		
		public function backup(responder:IResponder, surveyName:String):void {
			var token:AsyncToken = this._backupOperation.send(surveyName);
			token.addResponder(responder);
		}
		
		public function abort(responder:IResponder):void {
			var token:AsyncToken = this._abortOperation.send();
			token.addResponder(responder);
		}
		
		public function sendBackupToRemoteClone(responder:IResponder, surveyName:String):void {
			var token:AsyncToken = this._sendBackupToRemoteCloneOperation.send(surveyName);
			token.addResponder(responder);
		}
		
		public function getLastBackupInfo(responder:IResponder, surveyName:String):void {
			var token:AsyncToken = this._getLastBackupInfoOperation.send(surveyName);
			token.addResponder(responder);
		}
		
	}
}