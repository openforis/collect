package org.openforis.collect.client {
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.Operation;
	
	import org.openforis.collect.model.proxy.TaxonomyProxy;
	
	/**
	 * 
	 * @author S. Ricci
	 * @author E. Wibowo
	 * */
	public class SpeciesClient extends AbstractClient {
		
		private var _loadTaxonomiesBySurveyOperation:Operation;
		private var _loadTaxonSummariesOperation:Operation;
		private var _isTaxonomyInUseOperation:Operation;
		private var _saveTaxonomyOperation:Operation;
		private var _deleteTaxonomyOperation:Operation;
		private var _findByCodeOperation:Operation;
		private var _findByScientificNameOperation:Operation;
		private var _findByVernacularNameOperation:Operation;
		
		public function SpeciesClient() {
			super("speciesService");
			
			_loadTaxonomiesBySurveyOperation = getOperation("loadTaxonomiesBySurvey", CONCURRENCY_LAST);
			_loadTaxonSummariesOperation = getOperation("loadTaxonSummaries", CONCURRENCY_LAST);
			_isTaxonomyInUseOperation = getOperation("isTaxonomyInUse", CONCURRENCY_LAST);
			_saveTaxonomyOperation = getOperation("saveTaxonomy");
			_deleteTaxonomyOperation = getOperation("deleteTaxonomy");
			_findByCodeOperation = getOperation("findByCode", CONCURRENCY_LAST);
			_findByScientificNameOperation = getOperation("findByScientificName", CONCURRENCY_LAST);
			_findByVernacularNameOperation = getOperation("findByVernacularName", CONCURRENCY_LAST);
		}
		
		public function loadTaxonomiesBySurvey(responder:IResponder, surveyId:int, work:Boolean):void {
			var token:AsyncToken = this._loadTaxonomiesBySurveyOperation.send(surveyId, work);
			token.addResponder(responder);
		}
		
		public function loadTaxonSummaries(responder:IResponder, taxonomyId:int, offset:int = 1, maxRecords:int = 20):void {
			var token:AsyncToken = _loadTaxonSummariesOperation.send(taxonomyId, offset, maxRecords);
			token.addResponder(responder);
		}
		
		public function isTaxonomyInUse(responder:IResponder, taxonomyName:String):void {
			var token:AsyncToken = this._isTaxonomyInUseOperation.send(taxonomyName);
			token.addResponder(responder);
		}
		
		public function saveTaxonomy(responder:IResponder, taxonomy:TaxonomyProxy):void {
			var token:AsyncToken = this._saveTaxonomyOperation.send(taxonomy);
			token.addResponder(responder);
		}
		
		public function deleteTaxonomy(responder:IResponder, taxonomy:TaxonomyProxy):void {
			var token:AsyncToken = this._deleteTaxonomyOperation.send(taxonomy);
			token.addResponder(responder);
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