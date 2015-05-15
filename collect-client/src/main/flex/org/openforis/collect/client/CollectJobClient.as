package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class CollectJobClient extends AbstractClient {
		
		private var _getApplicationJobOperation:Operation;
		private var _abortApplicationJobOperation:Operation;
		private var _getSurveyJobOperation:Operation;
		private var _abortSurveyJobOperation:Operation;
		
		public function CollectJobClient() {
			super("collectJobService");
			
			this._getApplicationJobOperation = getOperation("getApplicationJob", CONCURRENCY_LAST, false);
			this._abortApplicationJobOperation = getOperation("abortApplicationJob");
			this._getSurveyJobOperation = getOperation("getSurveyJob", CONCURRENCY_LAST, false);
			this._abortSurveyJobOperation = getOperation("abortSurveyJob");
		}
		
		public function getApplicationJob(responder:IResponder):void {
			var token:AsyncToken = this._getApplicationJobOperation.send();
			token.addResponder(responder);
		}
		
		public function abortApplicationJob(responder:IResponder):void {
			var token:AsyncToken = this._abortApplicationJobOperation.send();
			token.addResponder(responder);
		}
		
		public function getSurveyJob(responder:IResponder, surveyId:int):void {
			var token:AsyncToken = this._getSurveyJobOperation.send(surveyId);
			token.addResponder(responder);
		}
		
		public function abortSurveyJob(responder:IResponder, surveyId):void {
			var token:AsyncToken = this._abortSurveyJobOperation.send(surveyId);
			token.addResponder(responder);
		}
		
	}
}