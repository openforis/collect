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
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class CollectPresenter extends AbstractPresenter {

		private static const KEEP_ALIVE_FREQUENCY:Number = 30000;
		
		private static const MOUSE_WHEEL_BUMP_DELTA:Number = 30;
		
		private const SURVEY_SELECTION_MENU_ITEM:String = Message.get("settings.selectSurvey");
		private const IMPORT_DATA_MENU_ITEM:String = Message.get("settings.admin.importData");
		private const USERS_MANAGEMENT_MENU_ITEM:String = Message.get("settings.admin.usersManagement");
		private const LOGOUT_MENU_ITEM:String = Message.get("settings.logout");
		private const ADMIN_SETTINGS_ITEMS:ArrayCollection = new ArrayCollection([IMPORT_DATA_MENU_ITEM, USERS_MANAGEMENT_MENU_ITEM]);
		
		private var _view:collect;
		private var _modelClient:ModelClient;
		private var _sessionClient:SessionClient;
		//private var _contextMenuPresenter:ContextMenuPresenter;
		
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
			var localeString:String = FlexGlobals.topLevelApplication.parameters.lang as String;
			if(localeString != null) {
				this._sessionClient.initSession(new AsyncResponder(initSessionResultHandler, faultHandler), localeString);
			}
		}
		
		override internal function initEventListeners():void {
			//mouse wheel handler to increment scroll step size
			FlexGlobals.topLevelApplication.systemManager.addEventListener(MouseEvent.MOUSE_WHEEL, mouseWheelHandler, true);
			eventDispatcher.addEventListener(UIEvent.SHOW_SURVEY_SELECTION, showSurveySelectionHandler);
			eventDispatcher.addEventListener(UIEvent.SURVEY_SELECTED, surveySelectedHandler);
			eventDispatcher.addEventListener(UIEvent.ROOT_ENTITY_SELECTED, rootEntitySelectedHandler);
			
			//_view.header.logoutButton.addEventListener(MouseEvent.CLICK, logoutButtonClickHandler);
			
			_view.header.settingsButton.addEventListener(MenuEvent.ITEM_CLICK, settingsItemClickHandler);
			
			CONFIG::debugging {
				_view.addEventListener(KeyboardEvent.KEY_DOWN, function(event:KeyboardEvent):void {
					//open FlexSpy popup pressing CTRL+i
					if(event.ctrlKey && event.charCode == 105)
						FlexSpy.show();
				});
			}
		}
		
		protected function updateSettingsPopUpMenu(includeSurveySelectionItem:Boolean = true):void {
			var result:ArrayCollection = new ArrayCollection();
			if ( includeSurveySelectionItem && canHaveSurveySelection() ) {
				result.addItem(SURVEY_SELECTION_MENU_ITEM);
			}
			if ( Application.user.hasEffectiveRole(UserProxy.ROLE_ADMIN) ) {
				result.addAll(ADMIN_SETTINGS_ITEMS);
			}
			result.addItem(LOGOUT_MENU_ITEM);
			_view.header.settingsButton.dataProvider = result;
		}
		
		protected function canHaveSurveySelection():Boolean {
			return Application.surveySummaries && Application.surveySummaries.length > 1 || 
				Application.activeSurvey != null && Application.activeSurvey.schema.rootEntityDefinitions.length > 0; 
		}
		
		protected function logoutButtonClickHandler(event:MouseEvent):void {
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
		
		internal function initSessionResultHandler(event:ResultEvent, token:Object = null):void {
			Application.user = event.result.user;
			Application.sessionId = event.result.sessionId;
			Application.locale = FlexGlobals.topLevelApplication.parameters.lang as String;
			
			getSurveySummaries();
			
			updateSettingsPopUpMenu();
		}
		
		internal function mouseWheelHandler(event:MouseEvent):void {
			//bump delta
			event.delta *= MOUSE_WHEEL_BUMP_DELTA;
		}
		
		protected function settingsItemClickHandler(event:MenuEvent):void {
			switch ( event.item ) {
				case SURVEY_SELECTION_MENU_ITEM:
					var uiEvent:UIEvent = new UIEvent(UIEvent.SHOW_SURVEY_SELECTION);
					eventDispatcher.dispatchEvent(uiEvent);
					break;
				case IMPORT_DATA_MENU_ITEM:
					PopUpUtil.createPopUp(DataImportPopUp, true);
					break;
				case USERS_MANAGEMENT_MENU_ITEM:
					PopUpUtil.createPopUp(UserManagementPopUp, true);
					break;
				case LOGOUT_MENU_ITEM:
					logoutButtonClickHandler(null);
					break;
			}
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
			var uiEvent:UIEvent;
			if ( summaries.length == 1) {
				var s:SurveySummary = summaries.getItemAt(0) as SurveySummary;
				uiEvent = new UIEvent(UIEvent.SURVEY_SELECTED);
				uiEvent.obj = s;
				eventDispatcher.dispatchEvent(uiEvent);
			} else {
				uiEvent = new UIEvent(UIEvent.SHOW_SURVEY_SELECTION);
				eventDispatcher.dispatchEvent(uiEvent);
			}
		}
		
		protected function showSurveySelectionHandler(event:UIEvent):void {
			updateSettingsPopUpMenu(false);
		}
		
		protected function surveySelectedHandler(event:UIEvent):void {
			var s:SurveySummary = event.obj as SurveySummary;
			var name:String = s.name;
			var responder:IResponder = new ItemResponder(setActiveSurveyResultHandler, faultHandler);
			_modelClient.setActiveSurvey(responder, name);			
		}
		
		protected function rootEntitySelectedHandler(event:UIEvent):void {
			updateSettingsPopUpMenu();
		}
		
		protected function setActiveSurvey(survey:SurveyProxy):void {
			Application.activeSurvey = survey;
			survey.init();
			var schema:SchemaProxy = survey.schema;
			var rootEntityDefinitions:ListCollectionView = schema.rootEntityDefinitions;
			var uiEvent:UIEvent;
			if ( rootEntityDefinitions.length == 1) {
				var rootEntityDef:EntityDefinitionProxy = rootEntityDefinitions.getItemAt(0) as EntityDefinitionProxy;
				uiEvent = new UIEvent(UIEvent.ROOT_ENTITY_SELECTED);
				uiEvent.obj = rootEntityDef;
				eventDispatcher.dispatchEvent(uiEvent);
			} else {
				uiEvent = new UIEvent(UIEvent.SHOW_ROOT_ENTITY_SELECTION);
				eventDispatcher.dispatchEvent(uiEvent);
			}
		}
		
		internal function setActiveSurveyResultHandler(event:ResultEvent, token:Object = null):void {
			var survey:SurveyProxy = event.result as SurveyProxy;
			setActiveSurvey(survey);
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
