package org.openforis.collect.client {
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	
	import mx.collections.IList;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.EventDispatcherFactory;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.FieldProxy;
	import org.openforis.collect.remoting.service.UpdateRequest;
	import org.openforis.collect.ui.Images;
	import org.openforis.collect.ui.component.BlockingMessagePopUp;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.UIUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DataClient extends AbstractClient {
		
		private var _queueProcessor:RemoteCallQueueProcessor;

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
			
			this._queueProcessor = new RemoteCallQueueProcessor(1, queueResultHandler, queueFaultHandler);
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
			this._queueProcessor.appendOperation(token, resultHandler, faultHandler, _saveActiveRecordOperation);
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
			this._queueProcessor.appendOperation(token, resultHandler, faultHandler, _updateActiveRecordOperation, request);
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
		
		protected function queueResultHandler(event:ResultEvent, token:Object = null):void {
			var lastCall:RemoteCallWrapper = _queueProcessor.lastCall;
			if(lastCall != null) {
				switch(lastCall.operation) {
					case _updateActiveRecordOperation:
						updateActiveRecordResultHandler(event, token as UpdateRequestToken);
						break;
				}
			}
		}
			
		protected function updateActiveRecordResultHandler(event:ResultEvent, token:UpdateRequestToken):void {
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

		protected function queueFaultHandler(event:FaultEvent, token:Object = null):void {
			var faultCode:String = event.fault.faultCode;
			switch(faultCode) {
				case "org.openforis.collect.web.session.InvalidSessionException":
					var u:URLRequest = new URLRequest(Application.URL +"login.htm?session_expired=1");
					Application.activeRecord = null;
					navigateToURL(u,"_self");
					break;
				default:
					if(! Application.serverOffline) {
						var message:String = Message.get("global.faultHandlerMsg", [faultCode, event.fault.faultString]);
						BlockingMessagePopUp.show(Message.get("global.errorAlertTitle"), message, Images.ERROR);
					}
					Application.serverOffline = true;
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