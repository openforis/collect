package org.openforis.collect.presenter {
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
	import org.openforis.collect.metamodel.proxy.SchemaProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.model.SurveySummary;
	import org.openforis.collect.ui.component.SurveySelectionPopUp;
	import org.openforis.collect.util.AlertUtil;
	
	import spark.events.IndexChangeEvent;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class SurveySelectionPresenter extends PopUpPresenter {

		public function SurveySelectionPresenter(view:SurveySelectionPopUp) {
			super(view);
		}
		
		override public function init():void {
			super.init();
			
			var surveys:IList = Application.surveySummaries;
			view.surveyDDL.dataProvider = surveys;
			if ( view.automaticallySelect && surveys.length == 1 ) {
				var survey:SurveySummary = surveys.getItemAt(0) as SurveySummary;
				performSurveySelection(survey);
			} else {
				view.visible = true;
			}
		}
		
		private function get view():SurveySelectionPopUp {
			return SurveySelectionPopUp(_view);
		}
		
		override protected function initEventListeners():void {
			super.initEventListeners();
			view.surveyDDL.addEventListener(IndexChangeEvent.CHANGE, surveySelectedHandler);
			view.okButton.addEventListener(MouseEvent.CLICK, okButtonClickHandler);
		}
		
		protected function surveySelectedHandler(event:IndexChangeEvent):void {
			var survey:SurveySummary = view.surveyDDL.selectedItem;
			if ( survey != null ) {
				performSurveySelection(survey);
			}
		}
		
		protected function performSurveySelection(survey:SurveySummary):void {
			view.surveyDDL.selectedItem = survey;
			var responder:IResponder = new AsyncResponder(getRootEntitiesSummariesResultHandler, faultHandler);
			ClientFactory.modelClient.getRootEntitiesSummaries(responder, survey.name);
			view.currentState = SurveySelectionPopUp.LOADING_ROOT_ENTITIES_STATE;
		}
		
		protected function getRootEntitiesSummariesResultHandler(event:ResultEvent, token:Object = null):void {
			var rootEntities:IList = event.result as IList;
			view.rootEntityDDL.dataProvider = rootEntities;
			if ( rootEntities.length == 1 ) {
				view.currentState = SurveySelectionPopUp.SINGLE_ROOT_ENTITY_LOADED_STATE;
				view.rootEntityDDL.selectedIndex = 0;
				if ( Application.surveySummaries.length == 1 && view.automaticallySelect ) {
					okButtonClickHandler(null);
				} else {
					view.visible = true;
				}
			} else {
				view.currentState = SurveySelectionPopUp.MULTIPLE_ROOT_ENTITIES_LOADED_STATE;
			}
		}
		
		protected function okButtonClickHandler(event:MouseEvent):void {
			var survey:SurveySummary = view.surveyDDL.selectedItem;
			if ( survey != null ) {
				var rootEntitySummary:NodeDefinitionSummary = view.rootEntityDDL.selectedItem;
				if ( rootEntitySummary != null ) {
					var name:String = survey.name;
					var responder:IResponder = new ItemResponder(setActiveSurveyResultHandler, faultHandler);
					ClientFactory.sessionClient.setActiveSurvey(responder, name);
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
			var selectedRootEntity:NodeDefinitionSummary = view.rootEntityDDL.selectedItem;
			var schema:SchemaProxy = survey.schema;
			var rootEntityDef:EntityDefinitionProxy = EntityDefinitionProxy(schema.getDefinitionById(selectedRootEntity.id));
			Application.activeRootEntity = rootEntityDef;
			var uiEvent:UIEvent = new UIEvent(UIEvent.ROOT_ENTITY_SELECTED);
			uiEvent.obj = rootEntityDef;
			eventDispatcher.dispatchEvent(uiEvent);
			closeHandler();
		}
		
	}
}