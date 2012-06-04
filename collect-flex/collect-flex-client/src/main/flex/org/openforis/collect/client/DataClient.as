package org.openforis.collect.client {
	import mx.collections.IList;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.model.proxy.NodeProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DataClient extends AbstractClient {
		
		private var _queueProcessor:RemoteCallQueueProcessor;

		private var _updateActiveRecordOperation:Operation;
		private var _moveNodeOperation:Operation;
		private var _saveActiveRecordOperation:Operation;
		private var _createRecordOperation:Operation;
		private var _deleteRecordOperation:Operation;
		private var _loadRecordSummariesOperation:Operation;
		private var _loadRecordOperation:Operation;
		private var _isLockingActiveRecordOperation:Operation;
		private var _promoteToCleansingOperation:Operation;
		private var _promoteToAnalysisOperation:Operation;
		private var _demoteToCleansingOperation:Operation;
		private var _demoteToEntryOperation:Operation;
		private var _clearActiveRecordOperation:Operation;
		private var _getCodeListItemsOperation:Operation;
		private var _findAssignableCodeListItemsOperation:Operation;
		
		public function DataClient() {
			super("dataService");
			
			this._queueProcessor = new RemoteCallQueueProcessor(1, queueResultHandler);
			this._updateActiveRecordOperation = getOperation("updateActiveRecord");
			this._moveNodeOperation = getOperation("moveNode");
			this._saveActiveRecordOperation = getOperation("saveActiveRecord");
			this._createRecordOperation = getOperation("createRecord");
			this._deleteRecordOperation = getOperation("deleteRecord");
			this._loadRecordSummariesOperation = getOperation("loadRecordSummaries");
			this._loadRecordOperation = getOperation("loadRecord");
			this._promoteToCleansingOperation = getOperation("promoteToCleansing");
			this._promoteToAnalysisOperation = getOperation("promoteToAnalysis");
			this._demoteToCleansingOperation = getOperation("demoteToCleansing");
			this._demoteToEntryOperation = getOperation("demoteToEntry");
			this._clearActiveRecordOperation = getOperation("clearActiveRecord");
			this._getCodeListItemsOperation = getOperation("getCodeListItems", CONCURRENCY_MULTIPLE);
			this._findAssignableCodeListItemsOperation = getOperation("findAssignableCodeListItems", CONCURRENCY_MULTIPLE);
		}
		
		public function createNewRecord(responder:IResponder, rootEntityName:String, versionName:String):void {
			var token:AsyncToken = this._createRecordOperation.send(rootEntityName, versionName);
			token.addResponder(responder);
		}
		
		public function saveActiveRecord(resultHandler:Function = null, faultHandler:Function = null, token:Object = null):void {
			this._queueProcessor.appendOperation(token, resultHandler, faultHandler, _saveActiveRecordOperation);
		}
		
		public function deleteRecord(responder:IResponder, id:int):void {
			var token:AsyncToken = this._deleteRecordOperation.send(id);
			token.addResponder(responder);
		}
		
		public function loadRecordSummaries(responder:IResponder, rootEntityName:String, offset:int, maxNumberOfRecords:int, sortFields:IList=null, keyValues:Array = null):void {
			var token:AsyncToken = this._loadRecordSummariesOperation.send(rootEntityName, offset, maxNumberOfRecords, sortFields, keyValues);
			token.addResponder(responder);
		}
		
		public function loadRecord(responder:IResponder, id:int, step:CollectRecord$Step, forceUnlock:Boolean = false):void {
			var stepNumber:int = getRecordStepNumber(step);
			var token:AsyncToken = this._loadRecordOperation.send(id, stepNumber, forceUnlock);
			token.addResponder(responder);
		}
		
		public function clearActiveRecord(responder:IResponder):void {
			var token:AsyncToken = this._clearActiveRecordOperation.send();
			token.addResponder(responder);
		}
		
		public function updateActiveRecord(request:UpdateRequest, resultHandler:Function = null, faultHandler:Function = null):void {
			this._queueProcessor.appendOperation(request, resultHandler, faultHandler, _updateActiveRecordOperation, request);
		}
		
		public function moveNode(responder:IResponder, nodeId:int, index:int):void {
			var token:AsyncToken = this._moveNodeOperation.send(nodeId, index);
			token.addResponder(responder);
		}
		
		public function isLockingActiveRecord(responder:IResponder):void {
			var token:AsyncToken = this._isLockingActiveRecordOperation.send();
			token.addResponder(responder);
		}
		
		public function promoteToCleansing(responder:IResponder):void {
			var token:AsyncToken = this._promoteToCleansingOperation.send();
			token.addResponder(responder);
		}
		
		public function promoteToAnalysis(responder:IResponder):void {
			var token:AsyncToken = this._promoteToAnalysisOperation.send();
			token.addResponder(responder);
		}
		
		public function demoteToCleansing(responder:IResponder):void {
			var token:AsyncToken = this._demoteToCleansingOperation.send();
			token.addResponder(responder);
		}
		
		public function demoteToEntry(responder:IResponder):void {
			var token:AsyncToken = this._demoteToEntryOperation.send();
			token.addResponder(responder);
		}
		
		public function findAssignableCodeListItems(responder:IResponder, parentEntityId:int, attribute:String):AsyncToken {
			var token:AsyncToken = this._findAssignableCodeListItemsOperation.send(parentEntityId, attribute);
			token.addResponder(responder);
			return token;
		}
		
		public function getCodeListItems(responder:IResponder, parentEntityId:int, attribute:String, codes:Array):void {
			var token:AsyncToken = this._getCodeListItemsOperation.send(parentEntityId, attribute, codes);
			token.addResponder(responder);
		}
		
		protected function queueResultHandler(event:ResultEvent, token:Object = null):void {
			var lastCall:RemoteCallWrapper = _queueProcessor.lastCall;
			if(lastCall != null) {
				switch(lastCall.operation) {
					case _updateActiveRecordOperation:
						updateActiveRecordResultHandler(event, token as UpdateRequest);
						break;
				}
			}
		}
			
		protected function updateActiveRecordResultHandler(event:ResultEvent, token:UpdateRequest):void {
			var responses:IList = IList(event.result);
			Application.activeRecord.update(responses, token);
		}

		public static function getRecordStepNumber(step:CollectRecord$Step):int {
			switch(step) {
				case CollectRecord$Step.ENTRY:
					return 1;
				case CollectRecord$Step.CLEANSING:
					return 2;
				case CollectRecord$Step.ANALYSIS:
					return 3;
				default:
					return -1;
			}
		}
		
	}
}