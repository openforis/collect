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
	import org.openforis.collect.util.ApplicationConstants;
	import flash.events.Event;
	import mx.rpc.IResponder;
	import flash.net.URLRequest;
	import flash.net.navigateToURL;
	import org.openforis.collect.util.AlertUtil;
	import mx.events.MenuEvent;
	import org.openforis.collect.util.PopUpUtil;
	import org.openforis.collect.ui.component.DataImportPopUp;
	import org.openforis.collect.ui.component.user.UserManagementPopUp;
	import org.openforis.collect.ui.component.SurveySelectionPopUp;
	import mx.managers.PopUpManager;
	import flash.display.DisplayObject;
	import org.openforis.collect.util.StringUtil;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.ui.component.SpeciesImportPopUp;
	import mx.core.IFlexDisplayObject;
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class CollectPresenter extends AbstractPresenter {

		private static const KEEP_ALIVE_FREQUENCY:Number = 30000;
		
		private static const MOUSE_WHEEL_BUMP_DELTA:Number = 30;
		
		private var _view:collect;
		private var _modelClient:ModelClient;
		private var _sessionClient:SessionClient;
		//private var _contextMenuPresenter:ContextMenuPresenter;
		private var _speciedImportPopUp:SpeciesImportPopUp;
		
		private var _keepAliveTimer:Timer;
		
		public function CollectPresenter(view:collect) {
			this._view = view;
			this._modelClient = ClientFactory.modelClient;
			this._sessionClient = ClientFactory.sessionClient;

			super();

			_keepAliveTimer = new Timer(KEEP_ALIVE_FREQUENCY)
			_keepAliveTimer.addEventListener(TimerEvent.TIMER, sendKeepAliveMessage);
			_keepAliveTimer.start();

			//set language in session
			init();
		}
		
		internal function init():void {
			var params:Object = FlexGlobals.topLevelApplication.parameters;
			var preview:Boolean = params.preview == "true";
			var localeString:String = params.lang as String;
			if ( StringUtil.isEmpty(localeString) ) {
				AlertUtil.showError("global.error.invalidLocaleSpecified");
			} else if ( preview ) {
				Application.preview = true;
				var surveyId:int = int(params.surveyId);
				var rootEntityId:int = int(params.rootEntityId);
				var versionId:Number = Number(params.versionId);
				var token:Object = {surveyId: surveyId, rootEntityId: rootEntityId, versionId: versionId};
				var previewResp:IResponder = new AsyncResponder(initSessionForPreviewResultHandler, faultHandler, token);
				this._sessionClient.initSession(previewResp, localeString);
			} else {
				var responder:IResponder = new AsyncResponder(initSessionResultHandler, faultHandler);
				this._sessionClient.initSession(responder, localeString);
			}
		}
		
		override internal function initEventListeners():void {
			//mouse wheel handler to increment scroll step size
			FlexGlobals.topLevelApplication.systemManager.addEventListener(MouseEvent.MOUSE_WHEEL, mouseWheelHandler, true);
			eventDispatcher.addEventListener(UIEvent.LOGOUT_CLICK, logoutClickHandler);
			eventDispatcher.addEventListener(UIEvent.SHOW_LIST_OF_RECORDS, showListOfRecordsHandler);
			eventDispatcher.addEventListener(UIEvent.OPEN_SPECIES_IMPORT_POPUP, openSpeciesImportPopUpHandler);
			
			CONFIG::debugging {
				_view.addEventListener(KeyboardEvent.KEY_DOWN, function(event:KeyboardEvent):void {
					//open FlexSpy popup pressing CTRL+i
					if ( event.ctrlKey && event.charCode == 105 ) {
						FlexSpy.show();
					}
				});
			}
			
			_view.addEventListener(KeyboardEvent.KEY_DOWN, function(event:KeyboardEvent):void {
				//open species import popup pressing CTRL+SHIFT+s
				if ( event.ctrlKey && event.shiftKey && event.charCode == 83 ) {
					eventDispatcher.dispatchEvent(new UIEvent(UIEvent.OPEN_SPECIES_IMPORT_POPUP));
				}
			});
		}
		
		protected function canHaveSurveySelection():Boolean {
			return Application.surveySummaries && Application.surveySummaries.length > 1 || 
				Application.activeSurvey != null && Application.activeSurvey.schema.rootEntityDefinitions.length > 0; 
		}
		
		protected function logoutClickHandler(event:UIEvent):void {
			var messageKey:String;
			if ( Application.activeRecord != null && Application.activeRecord.updated ) {
				messageKey = "global.confirmLogoutRecordUpdated";
			} else {
				messageKey = "global.confirmLogout";
			}
			AlertUtil.showConfirm(messageKey, null, "global.confirmLogoutTitle", performLogout);
		}
		
		protected function performLogout():void {
			var responder:IResponder = new AsyncResponder(logoutResultHandler, faultHandler);
			_sessionClient.logout(responder);
		}
		
		internal function logoutResultHandler(event:ResultEvent, token:Object = null):void {
			Application.activeRecord = null;
			var u:URLRequest = new URLRequest(ApplicationConstants.URL +"login.htm");
			navigateToURL(u,"_self");
		}
		
		internal function initSessionCommonResultHandler(event:ResultEvent, token:Object = null):void {
			Application.user = event.result.user;
			Application.sessionId = event.result.sessionId;
			Application.locale = FlexGlobals.topLevelApplication.parameters.lang as String;
		}
		
		internal function initSessionResultHandler(event:ResultEvent, token:Object = null):void {
			initSessionCommonResultHandler(event, token);
			getSurveySummaries();
		}
		
		internal function initSessionForPreviewResultHandler(event:ResultEvent, token:Object = null):void {
			initSessionCommonResultHandler(event, token);
			var surveyId:int = token.surveyId;
			var responder:IResponder = new AsyncResponder(setActivePreviewSurveyResultHandler, faultHandler, token);
			ClientFactory.sessionClient.setActivePreviewSurvey(responder, surveyId);
		}
		
		protected function setActivePreviewSurveyResultHandler(event:ResultEvent, token:Object = null):void {
			var survey:SurveyProxy = event.result as SurveyProxy;
			Application.activeSurvey = survey;
			survey.init();
			var versionName:String = null;
			if (! isNaN(token.versionId) ) {
				var version:ModelVersionProxy = survey.getVersion(token.versionId);
				versionName = version.name;
			}
			var rootEntityId:int = token.rootEntityId;
			var schema:SchemaProxy = survey.schema;
			var rootEntityDef:EntityDefinitionProxy = EntityDefinitionProxy(schema.getDefinitionById(rootEntityId));
			Application.activeRootEntity = rootEntityDef;
			var newRecordResponder:IResponder = new AsyncResponder(createRecordResultHandler, faultHandler);
			ClientFactory.dataClient.createNewRecord(newRecordResponder, rootEntityDef.name, versionName);
		}
		
		protected function createRecordResultHandler(event:ResultEvent, token:Object = null):void {
			var record:RecordProxy = event.result as RecordProxy;
			record.survey = Application.activeSurvey;
			record.init();
			var uiEvent:UIEvent = new UIEvent(UIEvent.RECORD_CREATED);
			uiEvent.obj = record;
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		internal function mouseWheelHandler(event:MouseEvent):void {
			//bump delta
			event.delta *= MOUSE_WHEEL_BUMP_DELTA;
		}
		
		protected function showListOfRecordsHandler(event:UIEvent):void {
			if ( Application.activeSurvey != null && Application.activeRootEntity != null ) {
				var uiEvent:UIEvent = new UIEvent(UIEvent.ROOT_ENTITY_SELECTED);
				uiEvent.obj = Application.activeRootEntity;
				eventDispatcher.dispatchEvent(uiEvent);
			} else {
				openSurveySelectionPopUp();
			}
		}
		
		protected function openSurveySelectionPopUp(automaticallySelect:Boolean = true):void {
			var popUp:SurveySelectionPopUp = new SurveySelectionPopUp();
			popUp.visible = false;
			popUp.automaticallySelect = automaticallySelect;
			PopUpManager.addPopUp(popUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(popUp);
		}
		
		protected function openSpeciesImportPopUpHandler(event:Event = null):void {
			_speciedImportPopUp = SpeciesImportPopUp(PopUpUtil.createPopUp(SpeciesImportPopUp));
		}
		
		/**
		 * Get Survey Summaries
		 * 
		 * */
		internal function getSurveySummaries():void {
			var responder:IResponder = new ItemResponder(getSurveySummariesResultHandler, faultHandler);
			_modelClient.getSurveySummaries(responder);
		}
		
		internal function getSurveySummariesResultHandler(event:ResultEvent, token:Object = null):void {
			var summaries:IList =  event.result as IList;
			Application.surveySummaries = summaries;
			if ( Application.user.hasEffectiveRole(UserProxy.ROLE_ADMIN) ) {
				showHomePage();
			} else if ( CollectionUtil.isEmpty(Application.surveySummaries) ) {
				showErrorPage(Message.get("error.no_published_surveys_found"));
			} else {
				showHomePage();
				showListOfRecordsHandler(null);
			}
		}
		
		protected function showErrorPage(errorMessage:String):void {
			var uiEvent:UIEvent = new UIEvent(UIEvent.SHOW_ERROR_PAGE);
			uiEvent.obj = errorMessage;
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		protected function showHomePage():void {
			var uiEvent:UIEvent = new UIEvent(UIEvent.SHOW_HOME_PAGE);
			eventDispatcher.dispatchEvent(uiEvent);
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
