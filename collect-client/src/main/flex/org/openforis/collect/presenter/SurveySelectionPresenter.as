package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.IList;
	import mx.collections.ItemResponder;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.NodeDefinitionSummary;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SchemaProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.model.SurveySummary;
	import org.openforis.collect.ui.view.SurveySelectionView;
	import org.openforis.collect.util.AlertUtil;
	
	import spark.events.IndexChangeEvent;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class SurveySelectionPresenter extends AbstractPresenter {

		private var _view:SurveySelectionView;
		
		public function SurveySelectionPresenter(view:SurveySelectionView) {
			this._view = view;
			super();
			
			_view.surveyDDL.dataProvider = Application.surveySummaries;
		}
		
		override internal function initEventListeners():void {
			super.initEventListeners();
			eventDispatcher.addEventListener(UIEvent.SHOW_SURVEY_SELECTION, showSurveySelectionHandler);
			eventDispatcher.addEventListener(UIEvent.SHOW_ROOT_ENTITY_SELECTION, showRootEntitySelectionHandler);
			_view.surveyDDL.addEventListener(IndexChangeEvent.CHANGE, surveySelectedHandler);
			_view.okButton.addEventListener(MouseEvent.CLICK, okButtonClickHandler);
		}
		
		protected function showSurveySelectionHandler(event:Event):void {
			_view.surveyDDL.selectedItem = null;
			if ( _view.surveyDDL.dataProvider != null && _view.surveyDDL.dataProvider.length == 1 ) {
				_view.surveyDDL.selectedIndex = 0;
			}
			_view.rootEntityDDL.dataProvider = null;
			_view.rootEntityDDL.selectedItem = null;
			_view.currentState = SurveySelectionView.DEFAULT_STATE;
		}
		
		protected function showRootEntitySelectionHandler(event:Event):void {
			var survey:SurveyProxy = Application.activeSurvey;
			var schema:SchemaProxy = survey.schema;
			_view.rootEntityDDL.dataProvider = schema.rootEntityDefinitions;
			_view.rootEntityDDL.selectedItem = null;
			_view.currentState = SurveySelectionView.ROOT_ENTITIES_LOADED_STATE;
		}
		
		protected function surveySelectedHandler(event:IndexChangeEvent):void {
			var survey:SurveySummary = _view.surveyDDL.selectedItem;
			if ( survey != null ) {
				var responder:IResponder = new AsyncResponder(getRootEntitiesSummariesResultHandler, faultHandler);
				ClientFactory.modelClient.getRootEntitiesSummaries(responder, survey.name);
				_view.currentState = SurveySelectionView.LOADING_ROOT_ENTITIES_STATE;
			}
		}
		
		protected function getRootEntitiesSummariesResultHandler(event:ResultEvent, token:Object = null):void {
			_view.rootEntityDDL.dataProvider = event.result as IList;
			_view.currentState = SurveySelectionView.ROOT_ENTITIES_LOADED_STATE;
			if ( _view.rootEntityDDL.dataProvider != null &&  _view.rootEntityDDL.dataProvider.length == 1 ) {
				_view.rootEntityDDL.selectedIndex = 0;
			}
		}
		
		protected function okButtonClickHandler(event:MouseEvent):void {
			var survey:SurveySummary = _view.surveyDDL.selectedItem;
			if ( survey != null ) {
				var rootEntitySummary:NodeDefinitionSummary = _view.rootEntityDDL.selectedItem;
				if ( rootEntitySummary != null ) {
					var name:String = survey.name;
					var responder:IResponder = new ItemResponder(setActiveSurveyResultHandler, faultHandler);
					ClientFactory.modelClient.setActiveSurvey(responder, name);
				} else {
					AlertUtil.showMessage("surveySelection.selectRootEntity");
				}
			} else {
				AlertUtil.showMessage("surveySelection.selectSurvey");
			}
		}
		
		protected function setActiveSurveyResultHandler(event:ResultEvent, token:Object = null):void {
			var survey:SurveyProxy = event.result as SurveyProxy;
			Application.activeSurvey = survey;
			survey.init();
			var selectedRootEntity:NodeDefinitionSummary	 = _view.rootEntityDDL.selectedItem;
			var schema:SchemaProxy = survey.schema;
			var rootEntityDef:EntityDefinitionProxy = EntityDefinitionProxy(schema.getDefinitionById(selectedRootEntity.id));
			Application.activeRootEntity = rootEntityDef;
			var uiEvent:UIEvent = new UIEvent(UIEvent.ROOT_ENTITY_SELECTED);
			uiEvent.obj = rootEntityDef;
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
	}
}