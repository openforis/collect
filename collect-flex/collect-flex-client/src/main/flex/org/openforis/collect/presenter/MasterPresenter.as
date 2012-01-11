package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import mx.collections.IList;
	import mx.controls.Alert;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.model.RecordSummary;
	import org.openforis.collect.model.SurveySummary;
	import org.openforis.collect.ui.view.MasterView;

	public class MasterPresenter extends AbstractPresenter {
		
		private var _view:MasterView;
		
		public function MasterPresenter(view:MasterView) {
			this._view = view;
			super();
			
			_view.currentState = MasterView.LOADING_STATE;
			
			/*
			wait for surveys and sessionState loading, then dispatch APPLICATION_INITIALIZED event
			if more than one survey is found, then whow surveySelection view
			*/
			
			/*
			
			flow: loading -> surveySelection (optional) -> rootEntitySelection (optional) -> list -> edit
			
			*/
		}
		
		override internal function initEventListeners():void {
			//eventDispatcher.addEventListener(ApplicationEvent.APPLICATION_INITIALIZED, applicationInitializedHandler);
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			eventDispatcher.addEventListener(UIEvent.BACK_TO_LIST, backToListHandler);
			eventDispatcher.addEventListener(UIEvent.RECORD_SELECTED, recordSelectedHandler);
		}

		/*protected function applicationInitializedHandler(event:ApplicationEvent):void {
		}*/
		internal function recordSelectedHandler(uiEvent:UIEvent):void {
			var record:RecordSummary = uiEvent.obj as RecordSummary;
			Alert.show(record.id);
		}
		
		protected function rootEntitySelectedHandler(event:UIEvent):void {
			var entityDef:EntityDefinitionProxy = event.obj as EntityDefinitionProxy;
			
			
			_view.currentState = MasterView.LIST_STATE;
		}

		
		protected function newRecordCreatedHandler(event:UIEvent):void {
			_view.currentState = MasterView.DETAIL_STATE;
		}
		
		protected function backToListHandler(event:UIEvent):void {
			_view.currentState = MasterView.LIST_STATE;
			//load clusters...
		}
			
	}
}