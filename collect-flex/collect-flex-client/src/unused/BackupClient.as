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
	public class BackupClient extends AbstractClient {
		
		private var _getStatusOperation:Operation;
		private var _backupOperation:Operation;
		private var _cancelOperation:Operation;
		
		public function BackupClient() {
			super("backupService");
			
			this._getStatusOperation = getOperation("getStatus", CONCURRENCY_LAST, false);
			this._backupOperation = getOperation("backup");
			this._cancelOperation = getOperation("cancel");
		}
		
		public function getStatus(responder:IResponder, rootEntityName:String):void {
			var token:AsyncToken = this._getStatusOperation.send(rootEntityName);
			token.addResponder(responder);
		}
		
		public function backup(responder:IResponder, rootEntityName:String, ids:IList = null, stepNumber:int = -1):void {
			var token:AsyncToken = this._backupOperation.send(rootEntityName, ids, stepNumber);
			token.addResponder(responder);
		}
		
		public function cancel(responder:IResponder, rootEntityName:String):void {
			var token:AsyncToken = this._cancelOperation.send(rootEntityName);
			token.addResponder(responder);
		}
		
	}
}