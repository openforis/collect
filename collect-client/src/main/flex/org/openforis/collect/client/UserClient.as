package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.model.proxy.UserProxy;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class UserClient extends AbstractClient {
		
		private var _loadAllOperation:Operation;
		private var _saveOperation:Operation;
		private var _deleteOperation:Operation;
		
		public function UserClient() {
			super("userService");
			
			_loadAllOperation = getOperation("loadAll");
			_saveOperation = getOperation("save");
			_deleteOperation = getOperation("delete");
		}
		
		public function loadAll(responder:IResponder):void {
			var token:AsyncToken = this._loadAllOperation.send();
			token.addResponder(responder);
		}
		
		public function save(responder:IResponder, user:UserProxy):void {
			var token:AsyncToken = this._saveOperation.send(user);
			token.addResponder(responder);
		}
		
		public function deleteUser(responder:IResponder, userId:int):void {
			var token:AsyncToken = this._deleteOperation.send(userId);
			token.addResponder(responder);
		}
		
	}
}