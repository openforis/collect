package org.openforis.collect.client {
	import flash.utils.ByteArray;
	
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class RecordFileClient extends AbstractClient {
		
		private var _uploadOperation:Operation;
		
		public function RecordFileClient() {
			super("recordFileService");
			
			this._uploadOperation = getOperation("upload");
		}
		
		public function upload(responder:IResponder, data:ByteArray, originalFileName:String, nodeId:int):void {
			var token:AsyncToken = this._uploadOperation.send(data, originalFileName, nodeId);
			token.addResponder(responder);
		}
		
	}
}