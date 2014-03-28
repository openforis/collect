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
	public class CodeListClient extends AbstractClient {
		
		var _isEditedSurveyCodeListEmptyOperation:Operation;
		
		public function CodeListClient() {
			super("codeListSessionService");
			_isEditedSurveyCodeListEmptyOperation = getOperation("isEditedSurveyCodeListEmpty");
		}
		
		public function isEditedSurveyCodeListEmpty(responder:IResponder, codeListId:int):void {
			var token:AsyncToken = this._isEditedSurveyCodeListEmptyOperation.send(codeListId);
			token.addResponder(responder);
		}
		
	}
}