package org.openforis.collect.client {
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.Application;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class ModelClient extends AbstractClient {
		
		private var _getSurveysOperation:Operation;
		private var _setActiveSurveyOperation:Operation;
		private var _getRootEntitiesSummariesOperation:Operation;
		
		public function ModelClient() {
			super("modelService");
			
			this._getSurveysOperation = getOperation("getSurveySummaries");
			this._setActiveSurveyOperation = getOperation("setActiveSurvey");
			this._getRootEntitiesSummariesOperation = getOperation("getRootEntitiesSummaries");
		}
		
		public function getSurveySummaries(responder:IResponder):void {
			var token:AsyncToken = this._getSurveysOperation.send();
			token.addResponder(responder);
		}
		
		public function setActiveSurvey(responder:IResponder, name:String):void {
			var token:AsyncToken = this._setActiveSurveyOperation.send(name);
			token.addResponder(responder);
		}
		
		public function getRootEntitiesSummaries(responder:IResponder, surveyName:String):void {
			var token:AsyncToken = this._getRootEntitiesSummariesOperation.send(surveyName);
			token.addResponder(responder);
		}
		
	}
}