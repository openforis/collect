package org.openforis.collect.client {
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class ClientFactory {
		
		private static var _sessionClient:SessionClient;
		
		public function ClientFactory() {
		}
		
		public static function get sessionClient():SessionClient{
			if(_sessionClient == null){
				_sessionClient = new SessionClient();
			}
			return _sessionClient;
		}
	}
}