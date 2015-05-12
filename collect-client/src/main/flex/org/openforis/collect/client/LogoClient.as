package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.model.Logo;
	import org.openforis.collect.model.LogoPosition;
	
	/**
	 * 
	 * @author E. Wibowo
	 * @author S. Ricci
	 * 
	 */
	public class LogoClient extends AbstractClient {		
		private var _loadLogoOperation:Operation;
		private var _deleteLogoOperation:Operation;
		private var _saveLogoOperation:Operation;

		public function LogoClient() {
			super("logoService");
			_loadLogoOperation = getOperation("loadLogo", CONCURRENCY_MULTIPLE);
			_deleteLogoOperation = getOperation("deleteLogo");
			_saveLogoOperation = getOperation("saveLogo");
		}
		
		public function loadLogo(responder:IResponder, position:LogoPosition):void {
			var token:AsyncToken = this._loadLogoOperation.send(position.name);
			token.addResponder(responder);
		}
		
		public function deleteLogo(responder:IResponder, position:LogoPosition):void {
			var token:AsyncToken = this._deleteLogoOperation.send(position.name);
			token.addResponder(responder);
		}

		public function saveLogo(responder:IResponder, logo:Logo):void {
			var token:AsyncToken = this._saveLogoOperation.send(logo);
			token.addResponder(responder);
		}
	}
}