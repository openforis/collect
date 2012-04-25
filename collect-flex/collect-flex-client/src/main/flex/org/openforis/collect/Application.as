package org.openforis.collect {
	
	import flash.external.ExternalInterface;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.FlexGlobals;
	import mx.managers.CursorManager;
	import mx.managers.ToolTipManager;
	import mx.utils.URLUtil;
	
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
		private static var _clientId:String;
		private static var _user:UserProxy;
		
		private static var _surveySummaries:IList;
		
		private static var _activeSurvey:SurveyProxy;
		private static var _activeRecord:RecordProxy;
		private static var _activeRootEntity:EntityDefinitionProxy;
		private static var _activeStep:CollectRecord$Step;
		private static var _serverOffline:Boolean;
		
		private static var initialized:Boolean = false;
		
		public function Application() {
		}
		
		public static function init():void {
			if ( !initialized ) {
				//generate client id
				clientId = Math.random().toString();
				
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
				ExternalInterface.addCallback("getLeavingPageMessage", getLeavingPageMessage);
			}
		}
		
		//called from External Interface (javascript)
		public static function isEditingRecord():Boolean {
			return ! serverOffline && Application.activeRecord != null;
		}
		
		public static function getLeavingPageMessage():String {
			var message:String = Message.get("global.leavingPage");
			return message;
		}
		
		public static function set surveySummaries(list:IList):void {
			_surveySummaries = list;
		}
		
		public static function get surveySummaries():IList {
			var list:ArrayCollection = new ArrayCollection();
			list.addAll(_surveySummaries);
			return list;
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
		public static function get clientId():String {
			return _clientId;
		}
		
		public static function set clientId(value:String):void {
			_clientId = value;
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


	}
}