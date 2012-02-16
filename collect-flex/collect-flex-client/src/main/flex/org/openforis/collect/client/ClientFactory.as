package org.openforis.collect.client {
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class ClientFactory {
		
		private static var _modelClient:ModelClient;
		private static var _sessionClient:SessionClient;
		private static var _dataClient:DataClient;
		private static var _taxonClient:TaxonClient;
		
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
		
		public static function get modelClient():ModelClient{
			if(_modelClient == null){
				_modelClient = new ModelClient();
			}
			return _modelClient;
		}
		
		public static function get taxonClient():TaxonClient{
			if(_taxonClient == null){
				_taxonClient = new TaxonClient();
			}
			return _taxonClient;
		}
		
	}
}