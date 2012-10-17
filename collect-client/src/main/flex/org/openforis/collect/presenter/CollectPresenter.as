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
			eventDispatcher.addEventListener(UIEvent.SHOW_LIST_OF_RECORDS, showListOfRecordsHandler);
			
			CONFIG::debugging {
				_view.addEventListener(KeyboardEvent.KEY_DOWN, function(event:KeyboardEvent):void {
					//open FlexSpy popup pressing CTRL+i
					if ( event.ctrlKey && event.charCode == 105 ) {
						FlexSpy.show();
					}
				});
			}
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
		}
		
		internal function mouseWheelHandler(event:MouseEvent):void {
			//bump delta
			event.delta *= MOUSE_WHEEL_BUMP_DELTA;
		}
		
		protected function showListOfRecordsHandler(event:UIEvent):void {
			if ( Application.activeSurvey == null || Application.activeRootEntity == null ) {
				openSurveySelectionPopUp();
			} else {
				var uiEvent:UIEvent = new UIEvent(UIEvent.ROOT_ENTITY_SELECTED);
				uiEvent.obj = Application.activeRootEntity;
				eventDispatcher.dispatchEvent(uiEvent);
			}
		}
		
		protected function openSurveySelectionPopUp(automaticallySelect:Boolean = true):void {
			var popUp:SurveySelectionPopUp = new SurveySelectionPopUp();
			popUp.visible = false;
			popUp.automaticallySelect = automaticallySelect;
			PopUpManager.addPopUp(popUp, FlexGlobals.topLevelApplication as DisplayObject, true);
			PopUpManager.centerPopUp(popUp);
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
			if ( ! Application.user.hasEffectiveRole(UserProxy.ROLE_ADMIN) ) {
				showListOfRecordsHandler(null);
			} else {
				showHomePage();
			}
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
