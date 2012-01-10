package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import mx.collections.IList;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.model.SurveySummary;
	import org.openforis.collect.ui.view.MasterView;

	public class MasterPresenter extends AbstractPresenter {
		
		private var _view:MasterView;
		
		public function MasterPresenter(view:MasterView) {
			this._view = view;
			super();
			
			_view.currentState = "loading";
			
			/*
			wait for surveys and sessionState loading, then dispatch APPLICATION_INITIALIZED event
			if more than one survey is found, then whow surveySelection view
			*/
			
			/*
			
			flow: loading -> surveySelection (optional) -> rootEntitySelection (optional) -> list -> edit
			
			*/
		}
		
		override internal function initEventListeners():void{
			//eventDispatcher.addEventListener(ApplicationEvent.APPLICATION_INITIALIZED, applicationInitializedHandler);
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			eventDispatcher.addEventListener(UIEvent.NEW_RECORD_CREATED, newRecordCreatedHandler);
			eventDispatcher.addEventListener(UIEvent.BACK_TO_LIST, backToListHandler);
		}

		/*protected function applicationInitializedHandler(event:ApplicationEvent):void {
		}*/
		
		protected function rootEntitySelectedHandler(event:UIEvent):void {
			var rootEntity:Object = event.obj as Object;
			selectRootEntity(rootEntity);
		}
		
		protected function selectRootEntity(rootEntity:Object):void {
	//		Application.selectedRootEntity = rootEntity;
			
			_view.currentState = "list";
			//TODO loadRecords();
		}
		
		protected function newRecordCreatedHandler(event:UIEvent):void {
			_view.currentState = "detail";
		}
		
		protected function backToListHandler(event:UIEvent):void {
			_view.currentState = "list";
			
			//load clusters...
		}
			
	}
}