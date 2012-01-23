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
	import org.openforis.collect.model.Phase;
	import org.openforis.collect.model.proxy.RecordProxy;
	import org.openforis.collect.util.ModelClassInitializer;

	/**
	 * @author Mino Togna
	 * */
	public class Application {
		
		public static var SESSION_ID:String;

		private static var _surveySummaries:IList;
		
		private static var _activeSurvey:SurveyProxy;
		private static var _activeRecord:RecordProxy;
		private static var _activeRootEntity:EntityDefinitionProxy;
		private static var _activePhase:Phase;
		private static var _serverOffline:Boolean;
		
		private static var initialized:Boolean = false;
		internal static const CONTEXT_NAME:String = "collect";
		
		//TODO: are these necessay here??
		public static const FILE_UPLOAD_SERVLET_NAME:String = "upload";
		public static const FILE_DOWNLOAD_SERVLET_NAME:String = "download";
		public static const FILE_DELETE_SERVLET_NAME:String = "deleteFile";
		public static const EXPORT_DATA_SERVLET_NAME:String = "exportData";
		private static var _FILEUPLOAD_URL:String; 
		private static var _FILEDOWNLOAD_URL:String; 
		private static var _FILEDELETE_URL:String; 
		private static var _EXPORT_DATA_URL:String;
		
		private static var _HOST:String;
		private static var _PORT:uint;
		private static var _URL:String;
		
		{
			setUrl("http://localhost:8080/collect/collect.swf");
		}
		
		public function Application() {
		}
		
		public static function init():void {
			if(!initialized) {
				CursorManager.setBusyCursor();
				
				initExternalInterface();
				
				var url:String = FlexGlobals.topLevelApplication.url;
				setUrl(url);
				
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
		
		
		public static function get activeSurvey():SurveyProxy {
			return _activeSurvey;
		}

		public static function set activeSurvey(value:SurveyProxy):void {
			_activeSurvey = value;
		}

		public static function get activeRootEntity():EntityDefinitionProxy {
			return _activeRootEntity;
		}
		
		public static function set activeRootEntity(value:EntityDefinitionProxy):void {
			_activeRootEntity = value;
		}

		public static function get activeRecord():RecordProxy {
			return _activeRecord;
		}
		
		public static function set activeRecord(record:RecordProxy):void{
			_activeRecord = record;
		}
		
		[Bindable]
		public static function get activePhase():Phase {
			return _activePhase;
		}
		
		public static function set activePhase(value:Phase):void {
			_activePhase = value;
		}

		public static function get serverOffline():Boolean {
			return _serverOffline;
		}
		
		public static function set serverOffline(value:Boolean):void {
			_serverOffline = value;
		}

		public static function get FILEUPLOAD_URL():String {
			return _FILEUPLOAD_URL;
		}
		
		public static function get FILEDOWNLOAD_URL():String {
			return _FILEDOWNLOAD_URL;
		}
		
		public static function get FILEDELETE_URL():String {
			return _FILEDELETE_URL;
		}
		
		public static function get EXPORT_DATA_URL():String {
			return _EXPORT_DATA_URL;
		}
		
		public static function get HOST():String {
			return _HOST;
		}
		
		public static function get PORT():uint {
			return _PORT;
		}
		
		public static function get URL():String {
			return _URL;
		}
		
		internal static function setUrl(url:String):void {
			var protocol:String = URLUtil.getProtocol(url);
			
			_PORT = URLUtil.getPort(url);
			_HOST = URLUtil.getServerName(url); 
			
			if(_PORT == 0){
				_PORT = 80;
			}
			
			var applicationUrl:String = protocol + "://"+ _HOST + ":" + _PORT + "/" + CONTEXT_NAME + "/"; 
			_URL = applicationUrl;
			
			
			_FILEUPLOAD_URL = _URL + FILE_UPLOAD_SERVLET_NAME;
			_FILEDOWNLOAD_URL = _URL + FILE_DOWNLOAD_SERVLET_NAME;
			_FILEDELETE_URL = _URL + FILE_DELETE_SERVLET_NAME;
			_EXPORT_DATA_URL = _URL + EXPORT_DATA_SERVLET_NAME;
		}

		
	}
}