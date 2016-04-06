package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class ModelClient extends AbstractClient {
		
		private var _getSurveysOperation:Operation;
		private var _getRootEntitiesSummariesOperation:Operation;
		private var _isActiveSurveyRecordsLockedOperation:Operation;
		
		public function ModelClient() {
			super("modelService");
			
			this._getSurveysOperation = getOperation("getSurveySummaries");
			this._getRootEntitiesSummariesOperation = getOperation("getRootEntitiesSummaries");
			this._isActiveSurveyRecordsLockedOperation = getOperation("isActiveSurveyRecordsLocked", CONCURRENCY_MULTIPLE);
		}
		
		public function getSurveySummaries(responder:IResponder):void {
			var token:AsyncToken = this._getSurveysOperation.send();
			token.addResponder(responder);
		}
		
		public function getRootEntitiesSummaries(responder:IResponder, surveyName:String):void {
			var token:AsyncToken = this._getRootEntitiesSummariesOperation.send(surveyName);
			token.addResponder(responder);
		}
		
		public function isActiveSurveyRecordsLocked(responder:IResponder):void {
			var token:AsyncToken = this._isActiveSurveyRecordsLockedOperation.send();
			token.addResponder(responder);
		}
		
	}
}