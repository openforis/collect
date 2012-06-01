package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author E. Wibowo
	 * */
	public class SpeciesClient extends AbstractClient {
		
		private var _findByCodeOperation:Operation;
		private var _findByScientificNameOperation:Operation;
		private var _findByVernacularNameOperation:Operation;
		
		public function SpeciesClient() {
			super("speciesService");
			
			_findByCodeOperation = getOperation("findByCode", CONCURRENCY_LAST);
			_findByScientificNameOperation = getOperation("findByScientificName", CONCURRENCY_LAST);
			_findByVernacularNameOperation = getOperation("findByVernacularName", CONCURRENCY_LAST);
		}
		
		public function findByCode(responder:IResponder, taxonomy:String, value:String, maxResults:int):void {
			var token:AsyncToken = this._findByCodeOperation.send(taxonomy, value, maxResults);
			token.addResponder(responder);
		}
		
		public function findByScientificName(responder:IResponder, taxonomy:String, value:String, maxResults:int):void {
			var token:AsyncToken = this._findByScientificNameOperation.send(taxonomy, value, maxResults);
			token.addResponder(responder);
		}
		
		public function findByVernacularName(responder:IResponder, taxonomy:String, nodeId:int, value:String, maxResults:int):void {
			var token:AsyncToken = this._findByVernacularNameOperation.send(taxonomy, nodeId, value, maxResults);
			token.addResponder(responder);
		}

	}
}