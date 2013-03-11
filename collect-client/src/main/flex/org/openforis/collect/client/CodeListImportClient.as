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
	public class CodeListImportClient extends AbstractReferenceDataImportClient {
		
		public function CodeListImportClient() {
			super("codeListImportService");
		}
		
		public function start(responder:IResponder, codeListId:int):void {
			var token:AsyncToken = this._startOperation.send(codeListId);
			token.addResponder(responder);
		}

	}
}