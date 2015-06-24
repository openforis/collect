package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.model.Configuration$ConfigurationItem;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class ConfigurationClient extends AbstractClient {
		
		private var _loadConfigurationOperation:Operation;
		private var _updateUploadPathOperation:Operation;
		private var _updateIndexPathOperation:Operation;
		private var _updateConfigurationItemOperation:Operation;
		private var _testRemoteCloneUrlOperation:Operation;

		public function ConfigurationClient() {
			super("configurationService");
			_loadConfigurationOperation = getOperation("loadConfiguration", CONCURRENCY_MULTIPLE);
			_updateUploadPathOperation = getOperation("updateUploadPath");
			_updateIndexPathOperation = getOperation("updateIndexPath");
			_updateConfigurationItemOperation = getOperation("updateConfigurationItem", CONCURRENCY_MULTIPLE);
			_testRemoteCloneUrlOperation = getOperation("testRemoteCloneUrl", CONCURRENCY_LAST);
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
		
		public function updateConfigurationItem(responder:IResponder, item:Configuration$ConfigurationItem, value:String):void {
			var token:AsyncToken = this._updateConfigurationItemOperation.send(item.name, value);
			token.addResponder(responder);
		}
		
		public function testRemoteCloneUrl(responder:IResponder):void {
			var token:AsyncToken = this._testRemoteCloneUrlOperation.send();
			token.addResponder(responder);
		}
		
	}
}