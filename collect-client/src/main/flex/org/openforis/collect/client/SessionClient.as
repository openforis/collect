package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.Application;
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class SessionClient extends AbstractClient {
		
		private var _keepAliveOperation:Operation;
		private var _initSessionOperation:Operation;
		private var _logoutOperation:Operation;
		private var _setActiveSurveyOperation:Operation;
		private var _setDesignerSurveyAsActiveOperation:Operation;
		private var _setActivePreviewSurveyOperation:Operation;
		
		public function SessionClient() {
			super("sessionService");
			
			this._keepAliveOperation = getOperation("keepAlive", CONCURRENCY_LAST);
			this._initSessionOperation = getOperation("initSession");
			this._logoutOperation = getOperation("logout");
			this._setActiveSurveyOperation = getOperation("setActiveSurvey");
			this._setDesignerSurveyAsActiveOperation = getOperation("setDesignerSurveyAsActive");
			this._setActivePreviewSurveyOperation = getOperation("setActivePreviewSurvey");
		}
		
		public function keepAlive(responder:IResponder):void {
			var editing:Boolean = Application.isEditingRecord();
			var token:AsyncToken = this._keepAliveOperation.send(editing);
			token.addResponder(responder);
		}
		
		public function cancelLastKeepAliveOperation():void {
			this._keepAliveOperation.cancel();
		}
		
		public function initSession(responder:IResponder, localeString:String):void {
			var token:AsyncToken = this._initSessionOperation.send(localeString);
			token.addResponder(responder);
		}
		
		public function logout(responder:IResponder):void {
			var token:AsyncToken = this._logoutOperation.send();
			token.addResponder(responder);
		}
		
		public function setActiveSurvey(responder:IResponder, name:String):void {
			var token:AsyncToken = this._setActiveSurveyOperation.send(name);
			token.addResponder(responder);
		}
		
		public function setActiveSurveyById(responder:IResponder, surveyId:int):void {
			var token:AsyncToken = this._setActiveSurveyOperation.send(surveyId);
			token.addResponder(responder);
		}
		
		public function setDesignerSurveyAsActive(responder:IResponder, id:int, work:Boolean):void {
			var token:AsyncToken = this._setDesignerSurveyAsActiveOperation.send(id, work);
			token.addResponder(responder);
		}
		
		public function setActivePreviewSurvey(responder:IResponder, surveyId:int):void {
			var token:AsyncToken = this._setActivePreviewSurveyOperation.send(surveyId);
			token.addResponder(responder);
		}
		
	}
}