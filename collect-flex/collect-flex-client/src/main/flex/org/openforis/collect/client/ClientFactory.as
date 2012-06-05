package org.openforis.collect.client {
	
	/**
	 * 
	 * @author Mino Togna
	 * @author Stefano Ricci
	 * @author E. Wibowo
	 * */
	public class ClientFactory {
		
		private static var _backupClient:BackupClient;
		private static var _modelClient:ModelClient;
		private static var _sessionClient:SessionClient;
		private static var _dataClient:DataClient;
		private static var _dataExportClient:DataExportClient;
		private static var _speciesClient:SpeciesClient;
		private static var _logoClient:LogoClient;
		
		public function ClientFactory() {
		}
		
		public static function get sessionClient():SessionClient{
			if(_sessionClient == null){
				_sessionClient = new SessionClient();
			}
			return _sessionClient;
		}
		
		public static function get backupClient():BackupClient {
			if(_backupClient == null) {
				_backupClient = new BackupClient();
			}
			return _backupClient;
		}
		
		public static function get dataClient():DataClient{
			if(_dataClient == null){
				_dataClient = new DataClient();
			}
			return _dataClient;
		}

		public static function get dataExportClient():DataExportClient{
			if(_dataExportClient == null) {
				_dataExportClient = new DataExportClient();
			}
			return _dataExportClient;
		}

		public static function get modelClient():ModelClient{
			if(_modelClient == null){
				_modelClient = new ModelClient();
			}
			return _modelClient;
		}
		
		public static function get speciesClient():SpeciesClient{
			if(_speciesClient == null){
				_speciesClient = new SpeciesClient();
			}
			return _speciesClient;
		}
		
		public static function get logoClient():LogoClient{
			if(_logoClient == null){
				_logoClient = new LogoClient();
			}
			return _logoClient;
		}
		
	}
}