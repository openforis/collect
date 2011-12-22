package org.openforis.collect.presenter {
	/**
	 * 
	 * @author Mino Togna
	 * */
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.events.EventListenerRequest;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.idm.model.impl.EntityImpl;
	import org.openforis.collect.ui.component.MasterView;
	import org.openforis.idm.metamodel.Survey;
	import org.openforis.idm.metamodel.impl.SchemaImpl;

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
		}
		
		override internal function initEventListeners():void{
			eventDispatcher.addEventListener(ApplicationEvent.APPLICATION_INITIALIZED, applicationInitializedHandler);
			eventDispatcher.addEventListener(ApplicationEvent.SCHEMA_LOADED, schemaLoadedHandler);
			eventDispatcher.addEventListener(UIEvent.SURVEY_SELECTED, surveySelectedHandler);
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			eventDispatcher.addEventListener(UIEvent.NEW_RECORD_CREATED, newRecordCreatedHandler);
			eventDispatcher.addEventListener(UIEvent.BACK_TO_LIST, backToListHandler);
		}

		protected function applicationInitializedHandler(event:ApplicationEvent):void {
			if(Application.SURVEYS.length > 1) {
				_view.currentState = "surveySelection";
			} else {
				//select first survey
				selectSurvey(Application.SURVEYS[0]);
			}
		}
		
		protected function surveySelectedHandler(event:UIEvent):void {
			selectSurvey(event.obj as Survey);
		}
		
		protected function selectSurvey(survey:Survey):void {
			//TODO load root entities for the selected survey
			Application.selectedSurvey = survey;
			loadSchema();
		}
		
		protected function loadSchema():void {
			//TODO call metamodelclient...
			//_view.currentState = "loading";
			
			//test data
			/*
			var rootEntities:ArrayCollection = new ArrayCollection([
				{label: "Cluster", id: "cluster"},
				{label: "Plot", id: "plot"}
			]);
			*/
			var applicationEvent:ApplicationEvent = new ApplicationEvent(ApplicationEvent.SCHEMA_LOADED);
			applicationEvent.result = Application.selectedSurvey.schema;
			eventDispatcher.dispatchEvent(applicationEvent);
		}
		
		protected function schemaLoadedHandler(event:ApplicationEvent):void {
			var schema:SchemaImpl = event.result as SchemaImpl;
			if(schema != null) {
				var rootEntities:IList = schema.rootEntityDefinitions;
				if(rootEntities != null && rootEntities.length > 0) {
					if(rootEntities.length == 1) {
						//TODO load records for the unique root entity
						_view.currentState = "list";
					} else {
						_view.currentState = "rootEntitySelection";
					}
				} else {
					//TODO error, no root entities found
				}
			} else {
				//TODO error
			}
		}
		
		protected function rootEntitySelectedHandler(event:UIEvent):void {
			var rootEntity:EntityImpl = event.obj as EntityImpl;
			selectRootEntity(rootEntity);
		}
		
		protected function selectRootEntity(rootEntity:EntityImpl):void {
			Application.selectedRootEntity = rootEntity;
			//loadRecords();
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