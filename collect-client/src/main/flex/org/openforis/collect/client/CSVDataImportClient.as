package org.openforis.collect.client {
	import mx.collections.IList;
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.model.CollectRecord$Step;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CSVDataImportClient extends AbstractImportProcessClient {
		
		public function CSVDataImportClient() {
			super("csvDataImportService");
		}
		
		public function start(responder:IResponder, parentEntityId:int, step:CollectRecord$Step = null, 
							  transactional:Boolean = true, validateRecords:Boolean = true):void {
			var token:AsyncToken = _startOperation.send(parentEntityId, step, transactional, validateRecords);
			token.addResponder(responder);
		}

	}
}