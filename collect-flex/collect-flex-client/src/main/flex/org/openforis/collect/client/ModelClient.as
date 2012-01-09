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
	public class ModelClient extends AbstractClient {
		
		private var _getSurveysOperation:Operation;
		private var _getSurveyOperation:Operation;
		private var _getSchemaOperation:Operation;
		
		public function ModelClient() {
			super("modelService");
			
			this._getSurveysOperation = getOperation("getSurveys");
			this._getSurveyOperation = getOperation("getSurvey");
			this._getSchemaOperation = getOperation("getSchema");
		}
		
		public function getSurveys(responder:IResponder):void {
			var token:AsyncToken = this._getSurveysOperation.send();
			token.addResponder(responder);
		}
		
		public function getSurvey(responder:IResponder, name:String):void {
			var token:AsyncToken = this._getSurveyOperation.send(name);
			token.addResponder(responder);
		}
		
		public function getSchema(responder:IResponder, surveyId:String):void {
			var token:AsyncToken = this._getSchemaOperation.send(surveyId);
			token.addResponder(responder);
		}
		
	}
}