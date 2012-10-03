package org.openforis.collect.presenter {
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.collections.ItemResponder;
	import mx.collections.ListCollectionView;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
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
			var uiEvent:UIEvent = new UIEvent(UIEvent.SURVEY_SELECTED);
			uiEvent.obj = survey;
			eventDispatcher.dispatchEvent(uiEvent);
			_view.currentState = SurveySelectionView.LOADING_ROOT_ENTITIES_STATE;
		}
		
		protected function okButtonClickHandler(event:MouseEvent):void {
			var survey:SurveyProxy = Application.activeSurvey;
			if ( survey != null ) {
				var rootEntityDef:EntityDefinitionProxy = _view.rootEntityDDL.selectedItem;
				if ( rootEntityDef != null ) {
					Application.activeRootEntity = rootEntityDef;
					var uiEvent:UIEvent = new UIEvent(UIEvent.ROOT_ENTITY_SELECTED);
					uiEvent.obj = rootEntityDef;
					eventDispatcher.dispatchEvent(uiEvent);
				} else {
					AlertUtil.showMessage("surveySelect.selectRootEntity");
				}
			} else {
				AlertUtil.showMessage("surveySelect.selectSurvey");
			}
		}

	}
}