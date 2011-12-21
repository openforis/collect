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
	public class MetaModelClient extends AbstractClient {
		
		private var _getSurveysOperation:Operation;
		private var _getSchemaOperation:Operation;
		
		public function MetaModelClient() {
			super("metaModelService");
			
			this._getSurveysOperation = getOperation("getSurveys");
			this._getSchemaOperation = getOperation("getSchema");
		}
		
		public function getSurveys(responder:IResponder):void {
			var token:AsyncToken = this._getSurveysOperation.send();
			token.addResponder(responder);
		}
		
		public function getSchema(responder:IResponder, surveyId:String):void {
			var token:AsyncToken = this._getSchemaOperation.send(surveyId);
			token.addResponder(responder);
		}
		
	}
}