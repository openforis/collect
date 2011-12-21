package org.openforis.collect.client {
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class ClientFactory {
		
		private static var _metaModelClient:MetaModelClient;
		private static var _sessionClient:SessionClient;
		private static var _dataClient:DataClient;
		
		public function ClientFactory() {
		}
		
		public static function get sessionClient():SessionClient{
			if(_sessionClient == null){
				_sessionClient = new SessionClient();
			}
			return _sessionClient;
		}
		
		public static function get dataClient():DataClient{
			if(_dataClient == null){
				_dataClient = new DataClient();
			}
			return _dataClient;
		}
		
		public static function get metaModelClient():MetaModelClient{
			if(_metaModelClient == null){
				_metaModelClient = new MetaModelClient();
			}
			return _metaModelClient;
		}
	}
}