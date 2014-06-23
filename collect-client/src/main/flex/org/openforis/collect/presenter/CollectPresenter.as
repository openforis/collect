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
	import org.openforis.collect.util.ObjectUtil;
	import mx.resources.Locale;
	import org.openforis.collect.ui.view.MasterView;
	import org.openforis.collect.ui.component.user.ChangePasswordPopUp;
	import mx.events.CloseEvent;
	import flash.events.UncaughtErrorEvent;
	import flash.events.ErrorEvent;
	import mx.rpc.Fault;
	
	/**
	 * 
	 * @author Mino Togna
	 * */
	public class CollectPresenter extends AbstractPresenter {

		private static const KEEP_ALIVE_FREQUENCY:Number = 30000;
		private static const KEEP_ALIVE_MAX_RETRY:int = 4;
		
		private static const MOUSE_WHEEL_BUMP_DELTA:Number = 30;
		
		private var _view:collect;
		private var _modelClient:ModelClient;
		private var _sessionClient:SessionClient;
		//private var _contextMenuPresenter:ContextMenuPresenter;
		private var _speciesImportPopUp:SpeciesImportPopUp;
		
		private var _keepAliveTimer:Timer;
		private var _keepAliveRetryTimes:int;
		
		public function CollectPresenter(view:collect) {
			this._view = view;
			this._modelClient = ClientFactory.modelClient;
			this._sessionClient = ClientFactory.sessionClient;

			super();
			
			_keepAliveRetryTimes = 0;
			_keepAliveTimer = new Timer(KEEP_ALIVE_FREQUENCY)
			_keepAliveTimer.addEventListener(TimerEvent.TIMER, sendKeepAliveMessage);
			_keepAliveTimer.start();
			
			//set language in session
			init();
		}
		
		override internal function initEventListeners():void {
			//mouse wheel handler to increment scroll step size
			FlexGlobals.topLevelApplication.systemManager.addEventListener(MouseEvent.MOUSE_WHEEL, mouseWheelHandler, true);
			eventDispatcher.addEventListener(UIEvent.LOGOUT_CLICK, logoutClickHandler);
			eventDispatcher.addEventListener(UIEvent.CHANGE_PASSWORD_CLICK, changePasswordClickHandler);
			eventDispatcher.addEventListener(UIEvent.SHOW_LIST_OF_RECORDS, showListOfRecordsHandler);
			eventDispatcher.addEventListener(UIEvent.OPEN_SPECIES_IMPORT_POPUP, openSpeciesImportPopUpHandler);
			eventDispatcher.addEventListener(UIEvent.CLOSE_SPECIES_IMPORT_POPUP, closeSpeciesImportPopUpHandler);
			eventDispatcher.addEventListener(UIEvent.TOGGLE_DETAIL_VIEW_SIZE, toggleDetailViewSizeHandler);
			eventDispatcher.addEventListener(UIEvent.CHECK_VIEW_SIZE, checkViewSizeHandler);
			
			//add uncaught error hanlder
			_view.loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorHandler);
			
			CONFIG::debugging {
				_view.addEventListener(KeyboardEvent.KEY_DOWN, function(event:KeyboardEvent):void {
					//open FlexSpy popup pressing CTRL+i
					if ( event.ctrlKey && event.charCode == 105 ) {
						FlexSpy.show();
					}
				});
			}
		}
		
		internal function init():void {
			var params:Object = FlexGlobals.topLevelApplication.parameters;
			var preview:Boolean = params.preview == "true";
			var edit:Boolean = params.edit == "true";
			var speciesImport:Boolean = params.species_import == "true";
			var codeListImport:Boolean = params.code_list_import == "true";
			var samplingDesignImport:Boolean = params.sampling_design_import == "true";
			var localeString:String = params.locale as String;
			if ( StringUtil.isEmpty(localeString) ) {
				AlertUtil.showError("global.error.invalidLocaleSpecified");
			} else if ( preview ) {
				initPreview(params, localeString);
			} else if ( edit ) {
				initEditRecord(params, localeString);
			} else if ( speciesImport ) {
				initSpeciesImport();
			} else if ( samplingDesignImport ) {
				initSamplingDesignImport();
			} else if ( codeListImport ) {
				initCodeListImport();
			} else {
				var responder:IResponder = new AsyncResponder(initSessionResultHandler, faultHandler);
				this._sessionClient.initSession(responder, localeString);
			}
		}
		
		protected function initPreview(params:Object, localeString:String):void {
			Application.preview = true;
			var surveyId:int = int(params.surveyId);
			var work:Boolean = params.work == "true";
			var rootEntityId:int = int(params.rootEntityId);
			var versionId:Number = Number(params.versionId);
			var token:Object = {surveyId: surveyId, work: work, rootEntityId: rootEntityId, versionId: versionId};
			var previewResp:IResponder = new AsyncResponder(initSessionForPreviewResultHandler, faultHandler, token);
			this._sessionClient.initSession(previewResp, localeString);
		}

		protected function initEditRecord(params:Object, localeString:String):void {
			Application.onlyOneRecordEdit = true;
			
			function initSessionForEditResultHandler(event:ResultEvent, token:Object = null):void {
				initSessionCommonResultHandler(event, token);
				var surveyId:int = token.surveyId;
				var responder:IResponder = new AsyncResponder(setActiveSurveyResultHandler, faultHandler, token);
				ClientFactory.sessionClient.setActiveSurveyById(responder, surveyId);
			}
			function setActiveSurveyResultHandler(event:ResultEvent, token:Object):void {
				//set active survey
				var survey:SurveyProxy = event.result as SurveyProxy;
				adjustLocaleForSurvey(survey);
				Application.activeSurvey = survey;
				survey.init();
				
				//set active root entity
				var schema:SchemaProxy = survey.schema;
				var rootEntityDefinitions:ListCollectionView = schema.rootEntityDefinitions;
				var mainRootEntityDef:EntityDefinitionProxy = EntityDefinitionProxy(schema.rootEntityDefinitions.getItemAt(0));
				Application.activeRootEntity = mainRootEntityDef;
				
				//dispatch edit record event
				var editEvent:UIEvent = new UIEvent(UIEvent.LOAD_RECORD_FOR_EDIT);
				editEvent.obj = {
					recordId: token.recordId
				};
				eventDispatcher.dispatchEvent(editEvent);
			}
			
			var token:Object = {
				surveyId: int(params.surveyId), 
				recordId: int(params.recordId)
			};
			var resp:IResponder = new AsyncResponder(initSessionForEditResultHandler, faultHandler, token);
			this._sessionClient.initSession(resp, localeString);
		}

		protected function initSpeciesImport():void {
			var token:ReferenceDataImportToken = new ReferenceDataImportToken();
			if ( fillRefereceDataImportToken(token) ) {
				token.uiEventName = UIEvent.SHOW_SPECIES_IMPORT;
				initReferenceDataImport(token);
			}
		}
		
		protected function initSamplingDesignImport():void {
			var token:ReferenceDataImportToken = new ReferenceDataImportToken();
			if ( fillRefereceDataImportToken(token) ) {
				token.uiEventName = UIEvent.SHOW_SAMPLING_DESIGN_IMPORT;
				initReferenceDataImport(token);
			}
		}

		protected function initCodeListImport():void {
			var token:CodeListImportToken = new CodeListImportToken();
			fillRefereceDataImportToken(token);
			token.uiEventName = UIEvent.SHOW_CODE_LIST_IMPORT;
			var params:Object = FlexGlobals.topLevelApplication.parameters;
			token.codeListId = int(params.code_list_id);
			if ( token.codeListId > 0 ) {
				initReferenceDataImport(token);
			} else {
				AlertUtil.showError("referenceDataImport.invalidParameters");
			}
		}
		
		protected function initReferenceDataImport(token:ReferenceDataImportToken):void {
			_view.currentState = collect.FULL_SCREEN_STATE;
			if ( token == null ) {
				AlertUtil.showError("referenceDataImport.saveSurveyBefore");
			} else {
				var sessionInitResponder:IResponder = new AsyncResponder(initSessionForReferenceDataImportResultHandler, faultHandler, token);
				this._sessionClient.initSession(sessionInitResponder, token.localeString);
			}
		}
		
		protected function fillRefereceDataImportToken(token:ReferenceDataImportToken):Boolean {
			var params:Object = FlexGlobals.topLevelApplication.parameters;
			var localeString:String = params.locale as String;
			var surveyId:int = int(params.surveyId);
			var work:Boolean = params.work == "true";
			if ( surveyId > 0 && params.work != "null" ) {
				token.localeString = localeString;
				token.surveyId = surveyId;
				token.work = work;
				return true;
			} else {
				AlertUtil.showError("referenceDataImport.invalidParameters");
				return false;
			}
		}
		
		protected function toggleDetailViewSizeHandler(event:UIEvent):void {
			Application.extendedDetailView = ! Application.extendedDetailView;
			checkViewSizeHandler(null);
		}
		
		protected function checkViewSizeHandler(event:UIEvent):void {
			if ( _view.masterView.currentState == MasterView.DETAIL_STATE && Application.extendedDetailView ) {
				_view.currentState = collect.ENLARGED_STATE;
			} else {
				_view.currentState = collect.DEFAULT_STATE;	
			}
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
		
		protected function changePasswordClickHandler(event:UIEvent):void {
			ChangePasswordPopUp(PopUpUtil.createPopUp(ChangePasswordPopUp));
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
			Application.initLocale();
		}
		
		internal function initSessionResultHandler(event:ResultEvent, token:Object = null):void {
			initSessionCommonResultHandler(event, token);
			getSurveySummaries();
		}
		
		internal function initSessionForPreviewResultHandler(event:ResultEvent, token:Object = null):void {
			initSessionCommonResultHandler(event, token);
			var surveyId:int = token.surveyId;
			var work:Boolean = token.work;
			var responder:IResponder = new AsyncResponder(setActivePreviewSurveyResultHandler, faultHandler, token);
			ClientFactory.sessionClient.setDesignerSurveyAsActive(responder, surveyId, work);
		}
		
		internal function initSessionForReferenceDataImportResultHandler(event:ResultEvent, token:ReferenceDataImportToken):void {
			initSessionCommonResultHandler(event, token);
			var surveyId:int = token.surveyId;
			var work:Boolean = token.work;
			var responder:IResponder = new AsyncResponder(setDesignerSurveyAsActiveResultHandler, faultHandler, token);
			ClientFactory.sessionClient.setDesignerSurveyAsActive(responder, surveyId, work);
			
			function setDesignerSurveyAsActiveResultHandler(event:ResultEvent, token:ReferenceDataImportToken):void {
				var survey:SurveyProxy = event.result as SurveyProxy;
				Application.activeSurvey = survey;
				var uiEvent:UIEvent = new UIEvent(token.uiEventName);
				uiEvent.obj = token;
				eventDispatcher.dispatchEvent(uiEvent);
			}
		}
		
		internal function showSamplingDesignImport(token:Object):void {
			var uiEvent:UIEvent = new UIEvent(UIEvent.SHOW_SAMPLING_DESIGN_IMPORT);
			uiEvent.obj = token;
			eventDispatcher.dispatchEvent(uiEvent);
		}
		
		protected function setActivePreviewSurveyResultHandler(event:ResultEvent, token:Object = null):void {
			var survey:SurveyProxy = event.result as SurveyProxy;
			adjustLocaleForSurvey(survey);
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
			var rootEntity:EntityDefinitionProxy = Application.activeSurvey == null ? null: Application.activeRootEntity;
			if ( rootEntity == null ) {
				openSurveySelectionPopUp();
			} else {
				var uiEvent:UIEvent = new UIEvent(UIEvent.ROOT_ENTITY_SELECTED);
				uiEvent.obj = rootEntity;
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
		
		protected function openSpeciesImportPopUpHandler(event:Event = null):void {
			_speciesImportPopUp = SpeciesImportPopUp(PopUpUtil.createPopUp(SpeciesImportPopUp));
		}
		
		protected function closeSpeciesImportPopUpHandler(event:Event = null):void {
			if ( _speciesImportPopUp != null ) {
				PopUpManager.removePopUp(_speciesImportPopUp);
			}
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
			_keepAliveRetryTimes = 0;
		}
		
		internal function keepAliveFaultHandler(event:FaultEvent, token:Object = null):void {
			if ( _keepAliveRetryTimes == KEEP_ALIVE_MAX_RETRY ) { 
				faultHandler(event, token);
				if(Application.serverOffline) {
					_keepAliveTimer.stop();
				}
			} else {
				_keepAliveRetryTimes ++;
			}
		}
		
		protected function adjustLocaleForSurvey(survey:SurveyProxy):void {
			var locale:Locale = Application.locale;
			var currentLangCode:String = locale.language;
			if ( ! survey.languages.contains(currentLangCode) ) {
				var surveyDefaultLocale:Locale = new Locale(survey.defaultLanguageCode);
				Application.locale = surveyDefaultLocale;
			}
		}
		
		private function uncaughtErrorHandler(event:UncaughtErrorEvent):void {
			var error:Error;
			if ( event.error is Error ) {
				error = event.error as Error;
			} else if ( event.error is ErrorEvent ) {
				var errorEvent:ErrorEvent = event.error as ErrorEvent;
				error = new Error(errorEvent.toString());
				error.name = "fault_event";
			} else {
				// a non-Error, non-ErrorEvent type was thrown and uncaught
				error = new Error(event.error);
				error.name = "unknown";
			}
			AlertUtil.showBlockingMessage("global.error.uncaught", error);
		}

	}
}

class ReferenceDataImportToken {
	private var _surveyId:int;
	private var _work:Boolean;
	private var _localeString:String;
	private var _uiEventName:String;
	
	public function get surveyId():int {
		return _surveyId;
	}

	public function set surveyId(value:int):void {
		_surveyId = value;
	}

	public function get work():Boolean {
		return _work;
	}

	public function set work(value:Boolean):void {
		_work = value;
	}

	public function get localeString():String {
		return _localeString;
	}

	public function set localeString(value:String):void {
		_localeString = value;
	}

	public function get uiEventName():String
	{
		return _uiEventName;
	}

	public function set uiEventName(value:String):void {
		_uiEventName = value;
	}

}

class CodeListImportToken extends ReferenceDataImportToken {
	private var _codeListId:int;
	
	public function get codeListId():int {
		return _codeListId;
	}

	public function set codeListId(value:int):void {
		_codeListId = value;
	}

}
