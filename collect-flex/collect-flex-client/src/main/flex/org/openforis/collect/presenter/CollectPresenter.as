package org.openforis.collect.presenter {
	
	CONFIG::debugging {
		import com.flexspy.FlexSpy;
	}
	import flash.events.KeyboardEvent;
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.ItemResponder;
	import mx.collections.ListCollectionView;
	import mx.core.FlexGlobals;
	import mx.managers.SystemManager;
	import mx.rpc.AsyncResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.ModelClient;
	import org.openforis.collect.client.SessionClient;
	import org.openforis.collect.event.ApplicationEvent;
	import org.openforis.collect.event.UIEvent;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SchemaProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.model.SurveySummary;
	import org.openforis.collect.ui.view.ListView;
	import mx.rpc.events.FaultEvent;
	import org.openforis.collect.ui.component.BlockingMessagePopUp;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.ui.Images;
	import org.openforis.collect.model.proxy.UserProxy;
	import flash.events.MouseEvent;
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class CollectPresenter extends AbstractPresenter {

		private static const KEEP_ALIVE_FREQUENCY:Number = 30000;

		private var _view:collect;
		private var _modelClient:ModelClient;
		private var _sessionClient:SessionClient;
		//private var _contextMenuPresenter:ContextMenuPresenter;
		
		private var _keepAliveTimer:Timer;
		
		public function CollectPresenter(view:collect) {
			super();
			
			this._view = view;
			this._modelClient = ClientFactory.modelClient;
			this._sessionClient = ClientFactory.sessionClient;

			_keepAliveTimer = new Timer(KEEP_ALIVE_FREQUENCY)
			_keepAliveTimer.addEventListener(TimerEvent.TIMER, sendKeepAliveMessage);
			_keepAliveTimer.start();

			//init context menu presenter
			//this._contextMenuPresenter = new ContextMenuPresenter(view);
			
			//set language in session
			var localeString:String = FlexGlobals.topLevelApplication.parameters.lang as String;
			if(localeString != null) {
				this._sessionClient.initSession(new AsyncResponder(initSessionResultHandler, faultHandler), localeString);
			}
			
			CONFIG::debugging {
				view.addEventListener(KeyboardEvent.KEY_DOWN, function(event:KeyboardEvent):void {
					//open FlexSpy popup pressing CTRL+i
					if(event.ctrlKey && event.charCode == 105)
						FlexSpy.show();
				});
			}
		}
		
		override internal function initEventListeners():void {
			//mouse wheel handler to increment scroll step size
			FlexGlobals.topLevelApplication.systemManager.addEventListener(MouseEvent.MOUSE_WHEEL, mouseWheelHandler, true);
			eventDispatcher.addEventListener(UIEvent.SURVEY_SELECTED, surveySelectedHandler);
		}
		
		internal function initSessionResultHandler(event:ResultEvent, token:Object = null):void {
			Application.user = event.result as UserProxy;
			getSurveySummaries();
		}
		
		internal function mouseWheelHandler(event:MouseEvent):void {
			//bump delta
			event.delta *= 30;
		}
		
		/**
		 * Get Survey Summaries
		 * 
		 * */
		internal function getSurveySummaries():void {
			_modelClient.getSurveySummaries(new ItemResponder(getSurveySummariesResultHandler, faultHandler));
		}
		
		internal function getSurveySummariesResultHandler(event:ResultEvent, token:Object = null):void {
			var summaries:IList =  event.result as IList;
			Application.surveySummaries = summaries;
			
			if(summaries.length > 1){
				//TODO 
			} else {
				var s:SurveySummary = summaries.getItemAt(0) as SurveySummary;
				
				var uiEvent:UIEvent = new UIEvent(UIEvent.SURVEY_SELECTED);
				uiEvent.obj = s;
				eventDispatcher.dispatchEvent(uiEvent);
			}
		}
		
		/**
		 * 
		 * 
		 * */
		protected function surveySelectedHandler(event:UIEvent):void {
			var s:SurveySummary = event.obj as SurveySummary;
			var name:String = s.name;
			_modelClient.setActiveSurvey(new ItemResponder(setActiveSurveyResultHandler, faultHandler), name);			
		}
		
		internal function setActiveSurveyResultHandler(event:ResultEvent, token:Object = null):void {
			var survey:SurveyProxy = event.result as SurveyProxy;
			Application.activeSurvey = survey;
			var schema:SchemaProxy = survey.schema;
			schema.init();
			var rootEntityDefinitions:ListCollectionView = schema.rootEntityDefinitions;
			if(rootEntityDefinitions.length == 1){
				var rootEntityDef:EntityDefinitionProxy = rootEntityDefinitions.getItemAt(0) as EntityDefinitionProxy;
				var uiEvent:UIEvent = new UIEvent(UIEvent.ROOT_ENTITY_SELECTED);
				uiEvent.obj = rootEntityDef;
				eventDispatcher.dispatchEvent(uiEvent);
			} else {
				//TODO
				
				//REMOVE IT: TEMPORARY select of first root entity
				var listView:ListView = _view.masterView.listView;
				
				var rootEntityDef:EntityDefinitionProxy = rootEntityDefinitions.getItemAt(0) as EntityDefinitionProxy;
				Application.activeRootEntity = rootEntityDef;
				
				var uiEvent:UIEvent = new UIEvent(UIEvent.ROOT_ENTITY_SELECTED);
				uiEvent.obj = rootEntityDef;
				eventDispatcher.dispatchEvent(uiEvent);
			}
		}
		
		internal function sendKeepAliveMessage(event:TimerEvent):void {
			this._sessionClient.keepAlive(new ItemResponder(keepAliveResult, keepAliveFaultHandler));
		}
		
		internal function keepAliveResult(event:ResultEvent, token:Object = null):void {
			//keep alive heartbeat sent correctly
		}
		
		internal function keepAliveFaultHandler(event:FaultEvent, token:Object = null):void {
			faultHandler(event, token);
			if(Application.serverOffline) {
				_keepAliveTimer.stop();
			}
		}
	}
}
