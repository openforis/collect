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

		//TODO: are these necessay here??
		/*
		public static const FILE_UPLOAD_SERVLET_NAME:String = "upload";
		public static const FILE_DOWNLOAD_SERVLET_NAME:String = "download";
		public static const FILE_DELETE_SERVLET_NAME:String = "deleteFile";
		*/
		public static const DOWNLOAD_EXPORTED_DATA_SERVLET_NAME:String = "downloadDataExport.htm";
		public static const DOWNLOAD_BACKUP_SERVLET_NAME:String = "downloadBackup.htm";
		
		private static var _FILEUPLOAD_URL:String; 
		private static var _FILEDOWNLOAD_URL:String; 
		private static var _FILEDELETE_URL:String; 
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
		
		public static function get FILEUPLOAD_URL():String {
			return _FILEUPLOAD_URL;
		}
		
		public static function get FILEDOWNLOAD_URL():String {
			return _FILEDOWNLOAD_URL;
		}
		
		public static function get FILEDELETE_URL():String {
			return _FILEDELETE_URL;
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
			
			/*
			_FILEUPLOAD_URL = _URL + FILE_UPLOAD_SERVLET_NAME;
			_FILEDOWNLOAD_URL = _URL + FILE_DOWNLOAD_SERVLET_NAME;
			_FILEDELETE_URL = _URL + FILE_DELETE_SERVLET_NAME;
			*/
			_DOWNLOAD_EXPORTED_DATA_URL = _URL + DOWNLOAD_EXPORTED_DATA_SERVLET_NAME;
			_DOWNLOAD_BACKUP_DATA_URL = _URL + DOWNLOAD_BACKUP_SERVLET_NAME;
		}

	}
}