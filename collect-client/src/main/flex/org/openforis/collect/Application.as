package org.openforis.collect {
	
	import flash.external.ExternalInterface;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.managers.CursorManager;
	import mx.managers.ToolTipManager;
	import mx.resources.Locale;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.model.proxy.UserProxy;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.ModelClassInitializer;

	/**
	 * @author M. Togna
	 * @author S. Ricci
	 * */
	public class Application {
		
		private static const SET_PREVIEW_JS_FUNCTION:String = "OPENFORIS.setPreview";
		private static const SET_EDITING_RECORD_JS_FUNCTION:String = "OPENFORIS.setEditingRecord";
		
		private static var _user:UserProxy;
		
		private static var _surveySummaries:IList;
		
		private static var _sessionId:String;
		private static var _preview:Boolean;
		private static var _onlyOneRecordEdit:Boolean;
		private static var _activeSurvey:SurveyProxy;
		private static var _activeRecord:RecordProxy;
		private static var _activeRecordEditable:Boolean;
		private static var _activeRootEntity:EntityDefinitionProxy;
		private static var _activeStep:CollectRecord$Step;
		private static var _autoSave:Boolean;
		private static var _serverOffline:Boolean;
		private static var _locale:Locale;
		private static var _localeString:String;
		private static var _localeLanguageCode:String;
		private static var _extendedDetailView:Boolean = false;
		
		private static var initialized:Boolean = false;
		
		public static function init():void {
			if ( !initialized ) {
				CursorManager.setBusyCursor();
				
				initExternalInterface();
				
				ApplicationConstants.init();
				
				ToolTipManager.showDelay = 0;				
				ToolTipManager.hideDelay = 3000;
				
				ModelClassInitializer.init();
				
				initialized = true;
				CursorManager.removeBusyCursor();
			}
		}
		
		private static function initExternalInterface():void {
			if(ExternalInterface.available) {
				ExternalInterface.addCallback("isEditingRecord", isEditingRecord);
				ExternalInterface.addCallback("isPreview", isPreview);
				ExternalInterface.addCallback("getLeavingPageMessage", getLeavingPageMessage);
			}
		}
		
		public static function initLocale():void {
			var localeString:String = FlexGlobals.topLevelApplication.parameters.locale as String;
			var locale:Locale = new Locale(localeString);
			Application.locale = locale;
		}
		
		//called from External Interface (javascript)
		public static function isEditingRecord():Boolean {
			return ! (serverOffline || Application.activeRecord == null);
		}
		
		public static function isPreview():Boolean {
			return preview;
		}
		
		public static function getLeavingPageMessage():String {
			var message:String = Message.get("global.leavingPage");
			return message;
		}
		
		public static function getRecordStepNumber(step:CollectRecord$Step):int {
			switch(step) {
				case CollectRecord$Step.ENTRY:
					return 1;
				case CollectRecord$Step.CLEANSING:
					return 2;
				case CollectRecord$Step.ANALYSIS:
					return 3;
				default:
					return -1;
			}
		}
		
		public static function set surveySummaries(list:IList):void {
			_surveySummaries = list;
		}
		
		public static function get surveySummaries():IList {
			var list:ArrayCollection = new ArrayCollection();
			if ( _surveySummaries ) {
				list.addAll(_surveySummaries);
			}
			return list;
		}
		
		public static function get sessionId():String {
			return _sessionId;
		}
		
		public static function set sessionId(value:String):void {
			_sessionId = value;
		}
		
		[Bindable]
		public static function get preview():Boolean {
			return _preview;
		}
		
		public static function set preview(value:Boolean):void{
			_preview = value;
			if ( ExternalInterface.available ) {
				ExternalInterface.call(SET_PREVIEW_JS_FUNCTION, value);
			}
		}
		
		[Bindable]
		public static function get onlyOneRecordEdit():Boolean {
			return _onlyOneRecordEdit;
		}
		
		public static function set onlyOneRecordEdit(value:Boolean):void{
			_onlyOneRecordEdit = value;
		}
		
		[Bindable]
		public static function get activeSurvey():SurveyProxy {
			return _activeSurvey;
		}

		public static function set activeSurvey(value:SurveyProxy):void {
			_activeSurvey = value;
		}

		[Bindable]
		public static function get activeRootEntity():EntityDefinitionProxy {
			return _activeRootEntity;
		}
		
		public static function set activeRootEntity(value:EntityDefinitionProxy):void {
			_activeRootEntity = value;
		}

		[Bindable]
		public static function get user():UserProxy {
			return _user;
		}
		
		public static function set user(value:UserProxy):void {
			_user = value;
		}
		
		[Bindable]
		public static function get activeRecord():RecordProxy {
			return _activeRecord;
		}
		
		public static function set activeRecord(record:RecordProxy):void{
			_activeRecord = record;
			if ( ExternalInterface.available ) {
				ExternalInterface.call(SET_EDITING_RECORD_JS_FUNCTION, record != null);
			}
			
		}
		
		[Bindable]
		public static function get activeRecordEditable():Boolean {
			return _activeRecordEditable;
		}
		
		public static function set activeRecordEditable(value:Boolean):void{
			_activeRecordEditable = value;
		}
		
		[Bindable]
		public static function get activeStep():CollectRecord$Step {
			return _activeStep;
		}
		
		public static function set activeStep(value:CollectRecord$Step):void {
			_activeStep = value;
		}

		[Bindable]
		public static function get serverOffline():Boolean {
			return _serverOffline;
		}
		
		public static function set serverOffline(value:Boolean):void {
			_serverOffline = value;
		}
		
		[Bindable]
		public static function get autoSave():Boolean {
			return _autoSave;
		}
		
		public static function set autoSave(value:Boolean):void {
			_autoSave = value;
		}
		
		[Bindable]
		public static function get extendedDetailView():Boolean {
			return _extendedDetailView;
		}
		
		public static function set extendedDetailView(value:Boolean):void {
			_extendedDetailView = value;
		}
		
		[Bindable]
		public static function get locale():Locale {
			return _locale;
		}
		
		public static function set locale(value:Locale):void {
			_locale = value;
			if ( _locale == null ) {
				_localeString = null;
				_localeLanguageCode = null;
			} else {
				_localeString = value.toString();
				_localeLanguageCode = _locale.language;
			}
		}
		
		[Bindable(event="localeChange")]
		public static function get localeString():String {
			return _localeString;
		}
		
		[Bindable(event="localeChange")]
		public static function get localeLanguageCode():String {
			return _localeLanguageCode;
		}
		
	}
}