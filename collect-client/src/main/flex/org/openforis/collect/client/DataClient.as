package org.openforis.collect.client {
	import mx.collections.IList;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.NodeChangeSetProxy;
	import org.openforis.collect.model.proxy.NodeUpdateRequestSetProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.model.proxy.RecordFilterProxy;
	
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
		private var _checkoutRecordOperation:Operation;
		private var _isLockingActiveRecordOperation:Operation;
		private var _promoteToCleansingOperation:Operation;
		private var _promoteToAnalysisOperation:Operation;
		private var _demoteToCleansingOperation:Operation;
		private var _demoteToEntryOperation:Operation;
		private var _clearActiveRecordOperation:Operation;
		private var _getCodeListItemsOperation:Operation;
		private var _findAssignableCodeListItemsOperation:Operation;
		private var _searchAutoCompleteValuesOperation:Operation;
		private var _assignOwnerOperation:Operation;
		private var _moveRecordsOperation:Operation;
		
		public function DataClient() {
			super("dataService");
			
			this._queueProcessor = new RemoteCallQueueProcessor(1, queueResultHandler);
			this._updateActiveRecordOperation = getOperation("updateActiveRecord");
			this._moveNodeOperation = getOperation("moveNode");
			this._saveActiveRecordOperation = getOperation("saveActiveRecord");
			this._createRecordOperation = getOperation("createRecord");
			this._deleteRecordOperation = getOperation("deleteRecord");
			this._loadRecordSummariesOperation = getOperation("loadRecordSummaries", CONCURRENCY_LAST);
			this._loadRecordOperation = getOperation("loadRecord");
			this._checkoutRecordOperation = getOperation("checkoutRecord");
			this._promoteToCleansingOperation = getOperation("promoteToCleansing");
			this._promoteToAnalysisOperation = getOperation("promoteToAnalysis");
			this._demoteToCleansingOperation = getOperation("demoteToCleansing");
			this._demoteToEntryOperation = getOperation("demoteToEntry");
			this._clearActiveRecordOperation = getOperation("clearActiveRecord");
			this._getCodeListItemsOperation = getOperation("getCodeListItems", CONCURRENCY_MULTIPLE);
			this._findAssignableCodeListItemsOperation = getOperation("findAssignableCodeListItems", CONCURRENCY_MULTIPLE);
			this._searchAutoCompleteValuesOperation = getOperation("searchAutoCompleteValues", CONCURRENCY_LAST, false);
			this._assignOwnerOperation = getOperation("assignOwner", CONCURRENCY_MULTIPLE, false);
			this._moveRecordsOperation = getOperation("moveRecords");
		}
		
		public function createNewRecord(responder:IResponder, rootEntityName:String, versionName:String, recordStep:CollectRecord$Step):void {
			var token:AsyncToken = this._createRecordOperation.send(rootEntityName, versionName, recordStep);
			token.addResponder(responder);
		}
		
		public function saveActiveRecord(resultHandler:Function = null, faultHandler:Function = null, token:Object = null):void {
			this._queueProcessor.appendOperation(token, resultHandler, faultHandler, _saveActiveRecordOperation);
		}
		
		public function deleteRecord(responder:IResponder, id:int):void {
			var token:AsyncToken = this._deleteRecordOperation.send(id);
			token.addResponder(responder);
		}
		
		public function loadRecordSummaries(responder:IResponder, filter:RecordFilterProxy, sortFields:IList=null, locale:String = null):void {
			var token:AsyncToken = this._loadRecordSummariesOperation.send(filter, sortFields, locale);
			token.addResponder(responder);
		}
		
		public function loadRecord(responder:IResponder, id:int, step:CollectRecord$Step = null):void {
			var stepNumber:Number = step == null ? NaN: Application.getRecordStepNumber(step);
			var token:AsyncToken = this._loadRecordOperation.send(id, stepNumber);
			token.addResponder(responder);
		}
		
		public function checkoutRecord(responder:IResponder, id:int, step:CollectRecord$Step = null, forceUnlock:Boolean = false):void {
			var stepNumber:Number = step == null ? NaN: Application.getRecordStepNumber(step);
			var token:AsyncToken = this._checkoutRecordOperation.send(id, stepNumber, forceUnlock);
			token.addResponder(responder);
		}
		
		public function clearActiveRecord(responder:IResponder = null):void {
			var token:AsyncToken = this._clearActiveRecordOperation.send();
			if (responder != null) {
				token.addResponder(responder);
			}
		}
		
		public function updateActiveRecord(requestSet:NodeUpdateRequestSetProxy, resultHandler:Function = null, faultHandler:Function = null):void {
			requestSet.autoSave = Application.autoSave;
			this._queueProcessor.appendOperation(requestSet, resultHandler, faultHandler, _updateActiveRecordOperation, requestSet);
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
		
		public function searchAutoCompleteValues(responder:IResponder, attributeId:int, fieldIdx:int, searchText:String):void {
			var token:AsyncToken = this._searchAutoCompleteValuesOperation.send(attributeId, fieldIdx, searchText);
			token.addResponder(responder);
		}
		
		public function assignOwner(recordId:int, ownerId:Number, responder:IResponder):void {
			var token:AsyncToken = this._assignOwnerOperation.send(recordId, ownerId);
			token.addResponder(responder);
		}

		public function moveRecords(rootEntity:String, fromStep:CollectRecord$Step, promote:Boolean, responder:IResponder = null):void {
			var token:AsyncToken = this._moveRecordsOperation.send(rootEntity, Application.getRecordStepNumber(fromStep), promote);
			if (responder != null) {
				token.addResponder(responder);
			}
		}
		
		protected function queueResultHandler(event:ResultEvent, token:Object = null):void {
			var lastCall:RemoteCallWrapper = _queueProcessor.lastCall;
			if(lastCall != null) {
				switch(lastCall.operation) {
					case _updateActiveRecordOperation:
						updateActiveRecordResultHandler(event, token as NodeUpdateRequestSetProxy);
						break;
				}
			}
		}
			
		protected function updateActiveRecordResultHandler(event:ResultEvent, token:NodeUpdateRequestSetProxy):void {
			var changeSet:NodeChangeSetProxy = NodeChangeSetProxy(event.result);
			Application.activeRecord.update(changeSet, token);
		}

	}
}