package org.openforis.collect.client {
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.model.UpdateRequest;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class DataClient extends AbstractClient {
		
		private var _updateQueueProcessor:RemoteCallQueueProcessor;

		private var _updateOperation:Operation;
		private var _newRecordOperation:Operation;
		
		public function DataClient() {
			super("dataService");
			
			this._updateQueueProcessor = new RemoteCallQueueProcessor(3, faultHandler);
			this._updateOperation = getOperation("update");
			this._newRecordOperation = getOperation("newRecord");
		}
		
		public function newRecord(responder:IResponder, newId:String, versionId:String):void {
			var token:AsyncToken = this._newRecordOperation.send(newId, versionId);
			token.addResponder(responder);
		}
		
		public function update(responder:IResponder, request:UpdateRequest):void {
			this._updateQueueProcessor.appendOperation(responder, this._updateOperation, request);
			/*
			var token:AsyncToken = this._updateOperation.send(request);
			token.addResponder(responder);
			*/
		}
		
		protected function faultHandler(event:FaultEvent):void {
			Alert.show("Error\n\n" + event.fault.message);
		}
		
	}
}