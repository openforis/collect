package org.openforis.collect.client {
	import mx.controls.Alert;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * */
	public class TaxonClient extends AbstractClient {
		
		private var _searchByCodeOperation:Operation;
		private var _searchByScientificNameOperation:Operation;
		private var _searchByVernacularNameOperation:Operation;
		
		public function TaxonClient() {
			super("taxonService");
			
			_searchByCodeOperation = getOperation("searchByCode");
			_searchByScientificNameOperation = getOperation("searchByScientificName");
			_searchByVernacularNameOperation = getOperation("searchByVernacularName");
		}
		
		public function serachByCode(responder:IResponder, value:String):void {
			var token:AsyncToken = this._searchByCodeOperation.send(value);
			token.addResponder(responder);
		}
		
		public function serachByScientificName(responder:IResponder, value:String):void {
			var token:AsyncToken = this._searchByScientificNameOperation.send(value);
			token.addResponder(responder);
		}
		
		public function serachByVernacularName(responder:IResponder, value:String):void {
			var token:AsyncToken = this._searchByVernacularNameOperation.send(value);
			token.addResponder(responder);
		}

	}
}