package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class ConfigurationClient extends AbstractClient {
		
		private var _loadConfigurationOperation:Operation;
		private var _updateUploadPathOperation:Operation;
		private var _updateIndexPathOperation:Operation;

		public function ConfigurationClient() {
			super("logoService");
			_loadConfigurationOperation = getOperation("loadConfiguration", CONCURRENCY_MULTIPLE);
			_updateUploadPathOperation = getOperation("updateUploadPath");
			_updateIndexPathOperation = getOperation("updateIndexPath");
		}
		
		public function loadConfiguration(responder:IResponder):void {
			var token:AsyncToken = this._loadConfigurationOperation.send();
			token.addResponder(responder);
		}
		
		public function updateUploadPath(responder:IResponder, path:String):void {
			var token:AsyncToken = this._updateUploadPathOperation.send(path);
			token.addResponder(responder);
		}

		public function updateIndexPath(responder:IResponder, path:String):void {
			var token:AsyncToken = this._updateIndexPathOperation.send(path);
			token.addResponder(responder);
		}
		
	}
}