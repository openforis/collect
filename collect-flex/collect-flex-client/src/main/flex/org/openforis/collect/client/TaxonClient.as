package org.openforis.collect.client {
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TaxonClient extends AbstractClient {
		
		private var _getSurveysOperation:Operation;
		
		public function TaxonClient() {
			super("taxonService");
			
			//this._getSurveysOperation = getOperation("getSurveySummaries");
		}
		/*
		public function getSurveySummaries(responder:IResponder):void {
			var token:AsyncToken = this._getSurveysOperation.send();
			token.addResponder(responder);
		}
		
		*/
	}
}