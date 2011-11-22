package org.openforis.collect.client {
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class ClientFactory {
		
		private static var _applicationClient:ApplicationClient;
		
		public function ClientFactory() {
		}
		
		public static function get applicationClient():ApplicationClient{
			if(_applicationClient == null){
				_applicationClient = new ApplicationClient();
			}
			return _applicationClient;
		}
	}
}