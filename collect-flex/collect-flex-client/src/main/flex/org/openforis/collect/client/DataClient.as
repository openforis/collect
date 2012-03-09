package org.openforis.collect.client {
	import mx.collections.IList;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DataClient extends AbstractClient {
		
		private var _updateQueueProcessor:RemoteCallQueueProcessor;

		private var _updateActiveRecordOperation:Operation;
		private var _saveActiveRecordOperation:Operation;
		private var _createRecordOperation:Operation;
		private var _deleteRecordOperation:Operation;
		private var _getRecordSummariesOperation:Operation;
		private var _loadRecordOperation:Operation;
		private var _promoteActiveRecordOperation:Operation;
		private var _demoteActiveRecordOperation:Operation;
		private var _clearActiveRecordOperation:Operation;
		private var _getCodeListItemsOperation:Operation;
		private var _findAssignableCodeListItemsOperation:Operation;
		
		public function DataClient() {
			super("dataService");
			
			this._updateQueueProcessor = new RemoteCallQueueProcessor(1, updateResultHandler, updateFaultHandler);
			this._updateActiveRecordOperation = getOperation("updateActiveRecord");
			this._saveActiveRecordOperation = getOperation("saveActiveRecord");
			this._createRecordOperation = getOperation("createRecord");
			this._deleteRecordOperation = getOperation("deleteRecord");
			this._getRecordSummariesOperation = getOperation("getRecordSummaries");
			this._loadRecordOperation = getOperation("loadRecord");
			this._promoteActiveRecordOperation = getOperation("promoteActiveRecord");
			this._demoteActiveRecordOperation = getOperation("demoteActiveRecord");
			this._clearActiveRecordOperation = getOperation("clearActiveRecord");
			this._getCodeListItemsOperation = getOperation("getCodeListItems", CONCURRENCY_MULTIPLE);
			this._findAssignableCodeListItemsOperation = getOperation("findAssignableCodeListItems", CONCURRENCY_MULTIPLE);
		}
		
		public function createNewRecord(responder:IResponder, rootEntityName:String, versionName:String):void {
			var token:AsyncToken = this._createRecordOperation.send(rootEntityName, versionName);
			token.addResponder(responder);
		}
		
		public function saveActiveRecord(resultHandler:Function = null, faultHandler:Function = null, token:Object = null):void {
			this._updateQueueProcessor.appendOperation(token, resultHandler, faultHandler, _saveActiveRecordOperation);
			/*
			var token:AsyncToken = this._saveActiveRecordOperation.send();
			token.addResponder(responder);
			*/
		}
		
		public function deleteRecord(responder:IResponder, id:int):void {
			var token:AsyncToken = this._deleteRecordOperation.send(id);
			token.addResponder(responder);
		}
		
		public function getRecordSummaries(responder:IResponder, rootEntityName:String, offset:int, maxNumberOfRecords:int, orderByField:String=null, filter:String = null):void {
			var token:AsyncToken = this._getRecordSummariesOperation.send(rootEntityName, offset, maxNumberOfRecords, orderByField, filter);
			token.addResponder(responder);
		}
		
		public function loadRecord(responder:IResponder, id:int, step:CollectRecord$Step):void {
			var stepNumber:int = getRecordStepNumber(step);
			var token:AsyncToken = this._loadRecordOperation.send(id, stepNumber);
			token.addResponder(responder);
		}
		
		public function clearActiveRecord(responder:IResponder):void {
			var token:AsyncToken = this._clearActiveRecordOperation.send();
			token.addResponder(responder);
		}
		
		public function updateActiveRecord(request:UpdateRequest, token:UpdateRequestToken = null, 
										   resultHandler:Function = null, faultHandler:Function = null):void {
			this._updateQueueProcessor.appendOperation(token, resultHandler, faultHandler, _updateActiveRecordOperation, request);
		}
		
		public function promoteActiveRecord(responder:IResponder):void {
			var token:AsyncToken = this._promoteActiveRecordOperation.send();
			token.addResponder(responder);
		}
		
		public function demoteActiveRecord(responder:IResponder):void {
			var token:AsyncToken = this._demoteActiveRecordOperation.send();
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
		
		protected function updateResultHandler(event:ResultEvent, token:Object = null):void {
			var field:FieldProxy;
			if(token != null && token is UpdateRequestToken) {
				switch(UpdateRequestToken(token).type) {
					case UpdateRequestToken.TYPE_UPDATE_VALUE:
						//do not break, apply symbol to field
					case UpdateRequestToken.TYPE_UPDATE_SYMBOL:
						for each (field in token.updatedFields) {
							field.symbol = token.symbol;
						}
						break;
					case UpdateRequestToken.TYPE_UPDATE_REMARKS:
						for each (field in token.updatedFields) {
						field.remarks = token.remarks;
					}
						break;
				}
			}
			var responses:IList = IList(event.result);
			Application.activeRecord.update(responses);
			var appEvt:ApplicationEvent = new ApplicationEvent(ApplicationEvent.UPDATE_RESPONSE_RECEIVED);
			appEvt.result = responses;
			EventDispatcherFactory.getEventDispatcher().dispatchEvent(appEvt);
		}

		protected function updateFaultHandler(event:FaultEvent, token:Object = null):void {
			if(token != null && token is UpdateRequestToken) {
				var updateRequestToken:UpdateRequestToken = UpdateRequestToken(token);
				var inputField:InputField = token != null ? token.inputField: null;
				var fieldLabel:String = inputField != null ? inputField.attributeDefinition.getLabelText(): "";
				AlertUtil.showConfirm("global.confirmRetryUpdate", 
					[fieldLabel, event.fault.faultCode, event.fault.faultString], null, 
					retryUpdateHandler, doNotRetryUpdateHandler);
			} else {
				var operation:String = "";
				AlertUtil.showConfirm("global.confirmRetryOperation", 
					[operation, event.fault.faultCode, event.fault.faultString], null, 
					retryUpdateHandler, doNotRetryUpdateHandler);
			}
		}
		
		protected function retryUpdateHandler():void {
			_updateQueueProcessor.sendHeadRemoteCall();
		}
		
		protected function doNotRetryUpdateHandler():void {
			var call:RemoteCallWrapper = _updateQueueProcessor.removeHeadOperation();
			if(call != null && call.token != null && call.token is UpdateRequestToken) {
				var token:UpdateRequestToken = UpdateRequestToken(call.token);
				var inputField:InputField = token.inputField;
				if(inputField != null) {
					if(inputField.textInput != null) {
						inputField.textInput.setFocus();
					}
					UIUtil.ensureElementIsVisible(inputField);
				}
			}
		}
		
		private function getRecordStepNumber(step:CollectRecord$Step):int {
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