package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.model.proxy.UserProxy;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class UserSessionClient extends AbstractClient {
		
		private var _changePasswordOperation:Operation;
		
		public function UserSessionClient() {
			super("userSessionService");
			
			_changePasswordOperation = getOperation("changePassword");
		}
		
		public function changePassword(responder:IResponder, oldPassword:String, newPassword:String):void {
			var token:AsyncToken = this._changePasswordOperation.send(oldPassword, newPassword);
			token.addResponder(responder);
		}
		
	}
}