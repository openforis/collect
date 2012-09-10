package org.openforis.collect.util {
	import mx.core.FlexGlobals;
	import mx.utils.URLUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class ApplicationConstants {
		
		//compiler constants
		public static const DEBUGGING:Boolean = CONFIG::debugging;
		public static const VERSION:String = CONFIG::version;
		
		internal static const CONTEXT_NAME:String = "collect";

		private static const DATA_IMPORT_UPLOAD_SERVLET_NAME:String = "uploadData.htm";
		
		private static const RECORD_FILE_UPLOAD_SERVLET_NAME:String = "uploadRecordFile.htm";
		private static const RECORD_FILE_DOWNLOAD_SERVLET_NAME:String = "downloadRecordFile.htm";
		private static const RECORD_FILE_DELETE_SERVLET_NAME:String = "deleteRecordFile.htm";
		
		private static const DOWNLOAD_EXPORTED_DATA_SERVLET_NAME:String = "downloadDataExport.htm";
		private static const DOWNLOAD_BACKUP_SERVLET_NAME:String = "downloadBackup.htm";
		
		public static const DATE_TIME_PATTERN:String = "dd-MM-yyyy HH:mm";
		
		private static var _DATA_IMPORT_UPLOAD_URL:String; 
		private static var _RECORD_FILE_UPLOAD_URL:String; 
		private static var _RECORD_FILE_DOWNLOAD_URL:String; 
		private static var _RECORD_FILE_DELETE_URL:String; 
		private static var _DOWNLOAD_EXPORTED_DATA_URL:String;
		private static var _DOWNLOAD_BACKUP_DATA_URL:String;
		
		private static var _HOST:String;
		private static var _PORT:uint;
		private static var _URL:String;
		
		{
			setUrl("http://localhost:8080/collect/collect.swf");
		}
		
		public static function init():void {
			var url:String = FlexGlobals.topLevelApplication.url;
			setUrl(url);
		}
		
		public static function get DATA_IMPORT_UPLOAD_URL():String {
			return _DATA_IMPORT_UPLOAD_URL;
		}
		
		public static function get RECORD_FILE_UPLOAD_URL():String {
			return _RECORD_FILE_UPLOAD_URL;
		}
		
		public static function get RECORD_FILE_DOWNLOAD_URL():String {
			return _RECORD_FILE_DOWNLOAD_URL;
		}
		
		public static function get RECORD_FILE_DELETE_URL():String {
			return _RECORD_FILE_DELETE_URL;
		}
		
		public static function get DOWNLOAD_EXPORTED_DATA_URL():String {
			return _DOWNLOAD_EXPORTED_DATA_URL;
		}
		
		public static function get DOWNLOAD_BACKUP_URL():String {
			return _DOWNLOAD_BACKUP_DATA_URL;
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
			
			_DATA_IMPORT_UPLOAD_URL = _URL + DATA_IMPORT_UPLOAD_SERVLET_NAME;
			
			_RECORD_FILE_UPLOAD_URL = _URL + RECORD_FILE_UPLOAD_SERVLET_NAME;
			_RECORD_FILE_DOWNLOAD_URL = _URL + RECORD_FILE_DOWNLOAD_SERVLET_NAME;
			_RECORD_FILE_DELETE_URL = _URL + RECORD_FILE_DELETE_SERVLET_NAME;
			
			_DOWNLOAD_EXPORTED_DATA_URL = _URL + DOWNLOAD_EXPORTED_DATA_SERVLET_NAME;
			_DOWNLOAD_BACKUP_DATA_URL = _URL + DOWNLOAD_BACKUP_SERVLET_NAME;
		}

	}
}