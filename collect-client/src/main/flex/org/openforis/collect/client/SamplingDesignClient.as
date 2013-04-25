package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class SamplingDesignClient extends AbstractClient {
		
		private var _loadBySurveyWorkOperation:Operation;
		private var _loadBySurveyOperation:Operation;
		
		public function SamplingDesignClient() {
			super("samplingDesignService");
			
			_loadBySurveyOperation = getOperation("loadBySurvey");
			_loadBySurveyWorkOperation = getOperation("loadBySurveyWork");
		}
		
		public function loadBySurveyWork(responder:IResponder, surveyId:int, offset:int = 1, maxRecords:int = 20):void {
			var token:AsyncToken = this._loadBySurveyWorkOperation.send(surveyId, offset, maxRecords);
			token.addResponder(responder);
		}
		
		public function loadBySurvey(responder:IResponder, surveyId:int, offset:int = 1, maxRecords:int = 20):void {
			var token:AsyncToken = this._loadBySurveyOperation.send(surveyId, offset, maxRecords);
			token.addResponder(responder);
		}
		
	}
}