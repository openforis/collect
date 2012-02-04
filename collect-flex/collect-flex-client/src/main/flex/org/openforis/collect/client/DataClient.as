package org.openforis.collect.client {
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.remoting.service.UpdateRequest;
	
/*	import org.openforis.collect.model.UpdateRequest;*/
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DataClient extends AbstractClient {
		
		private var _updateQueueProcessor:RemoteCallQueueProcessor;

		private var _updateOperation:Operation;
		private var _createNewRecordOperation:Operation;
		private var _deleteRecordOperation:Operation;
		private var _getRecordCountOperation:Operation;
		private var _getRecordSummariesOperation:Operation;
		private var _loadRecordOperation:Operation;
		private var _clearActiveRecordOperation:Operation;
		private var _findCodeListOperation:Operation;
		
		public function DataClient() {
			super("dataService");
			
			this._updateQueueProcessor = new RemoteCallQueueProcessor(3, faultHandler);
			this._updateOperation = getOperation("update");
			this._createNewRecordOperation = getOperation("createNewRecord");
			this._deleteRecordOperation = getOperation("deleteRecord");
			this._getRecordCountOperation = getOperation("getRecordCount");
			this._getRecordSummariesOperation = getOperation("getRecordSummaries");
			this._loadRecordOperation = getOperation("loadRecord");
			this._clearActiveRecordOperation = getOperation("clearActiveRecord");
			this._findCodeListOperation = getOperation("findCodeList");
		}
		
		public function getRecordCount(responder:IResponder):void {
			var token:AsyncToken = this._getRecordCountOperation.send();
			token.addResponder(responder);
		}
		
		public function createNewRecord(responder:IResponder, rootEntityName:String, versionName:String):void {
			var token:AsyncToken = this._createNewRecordOperation.send(rootEntityName, versionName);
			token.addResponder(responder);
		}
		
		public function deleteRecord(responder:IResponder, id:int):void {
			var token:AsyncToken = this._deleteRecordOperation.send(id);
			token.addResponder(responder);
		}
		
		public function getRecordSummaries(responder:IResponder, rootEntityName:String, offset:int, maxNumberOfRecords:int, orderByField:String=null, filter:String = null):void {
			var token:AsyncToken = this._getRecordSummariesOperation.send(rootEntityName, offset, maxNumberOfRecords, orderByField, filter);
			token.addResponder(responder);
		}
		
		public function loadRecord(responder:IResponder, id:int):void {
			var token:AsyncToken = this._loadRecordOperation.send(id);
			token.addResponder(responder);
		}
		
		public function clearActiveRecord(responder:IResponder):void {
			var token:AsyncToken = this._clearActiveRecordOperation.send();
			token.addResponder(responder);
		}
		
		public function update(responder:IResponder, request:UpdateRequest):void {
			this._updateQueueProcessor.appendOperation(responder, this._updateOperation, request);
		}
		
		public function findCodeList(responder:IResponder, parentEntityId:int, attribute:String):void {
			var token:AsyncToken = this._findCodeListOperation.send(parentEntityId, attribute);
			token.addResponder(responder);
		}
		
		protected function faultHandler(event:FaultEvent):void {
			Alert.show("Error\n\n" + event.fault.message);
		}
		
	}
}