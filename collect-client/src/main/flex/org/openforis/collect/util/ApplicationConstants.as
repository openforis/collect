package org.openforis.collect.util {
	import mx.core.FlexGlobals;
	import mx.utils.StringUtil;
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
		
		public static var COUNTRY_LOGO_ID:int = 1;

		private static const DATA_IMPORT_UPLOAD_SERVLET_NAME:String = "uploadData.htm";
		
		private static const SPECIES_IMPORT_UPLOAD_SERVLET_NAME:String = "uploadFile.htm";
		private static const _SPECIES_EXPORT_URL:String = "species/export/{0}";
		
		private static const _SAMPLING_DESIGN_EXPORT_URL:String = "samplingdesign/export/{0}";
		private static const _SAMPLING_DESIGN_WORK_SURVEY_EXPORT_URL:String = "samplingdesign/export/work/{0}";
		
		private static const FILE_UPLOAD_SERVLET_NAME:String = "uploadFile.htm";

		private static const RECORD_FILE_UPLOAD_SERVLET_NAME:String = "uploadRecordFile.htm";
		private static const RECORD_FILE_DOWNLOAD_SERVLET_NAME:String = "downloadRecordFile.htm";
		private static const RECORD_FILE_DELETE_SERVLET_NAME:String = "deleteRecordFile.htm";
		
		private static const DOWNLOAD_EXPORTED_DATA_SERVLET_NAME:String = "downloadDataExport.htm";
		private static const DOWNLOAD_BACKUP_SERVLET_NAME:String = "downloadBackup.htm";
		private static const _DESIGNER_URL_PART:String = "designer.htm";
		
		public static const DATE_TIME_PATTERN:String = "dd-MM-yyyy HH:mm";
		
		private static var _DATA_IMPORT_UPLOAD_URL:String;
		private static var _RECORD_FILE_UPLOAD_URL:String; 
		private static var _RECORD_FILE_DOWNLOAD_URL:String; 
		private static var _RECORD_FILE_DELETE_URL:String; 
		private static var _SPECIES_IMPORT_UPLOAD_URL:String;
		private static var _FILE_UPLOAD_URL:String;
		private static var _DOWNLOAD_EXPORTED_DATA_URL:String;
		private static var _DOWNLOAD_BACKUP_DATA_URL:String;
		private static var _DESIGNER_URL:String;
		
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
		
		public static function get SPECIES_IMPORT_UPLOAD_URL():String {
			return _SPECIES_IMPORT_UPLOAD_URL;
		}
		
		public static function getSpeciesExportUrl(taxonomyId:int):String {
			return mx.utils.StringUtil.substitute(_SPECIES_EXPORT_URL, taxonomyId);
		}

		public static function getSamplingDesignExportUrl(surveyId:int, work:Boolean = false):String {
			var baseUrl:String = work ? _SAMPLING_DESIGN_WORK_SURVEY_EXPORT_URL: _SAMPLING_DESIGN_EXPORT_URL;
			return mx.utils.StringUtil.substitute(baseUrl, surveyId);
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
		
		public static function get FILE_UPLOAD_URL():String {
			return _FILE_UPLOAD_URL;
		}
		
		public static function get DESIGNER_URL():String {
			return _DESIGNER_URL;
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
			var urlPort:uint = URLUtil.getPort(url);
			if(urlPort == 0){
				_PORT = 80;
			} else {
				_PORT = urlPort;
			}
			_HOST = URLUtil.getServerName(url); 
			var originalRootUrl:String = protocol + "://"+ _HOST + (urlPort > 0 ? (":" + urlPort): "") + "/";
			var contextNameLenght:int = url.indexOf("/", originalRootUrl.length) - originalRootUrl.length;
			var contextName:String = url.substr(originalRootUrl.length, contextNameLenght);
			var rootUrl:String = protocol + "://"+ _HOST + ":" + _PORT + "/";
			var applicationUrl:String = rootUrl + contextName + "/";
			_URL = applicationUrl;
			
			_DATA_IMPORT_UPLOAD_URL = _URL + DATA_IMPORT_UPLOAD_SERVLET_NAME;
			_SPECIES_IMPORT_UPLOAD_URL = _URL + SPECIES_IMPORT_UPLOAD_SERVLET_NAME;
			_FILE_UPLOAD_URL = _URL + FILE_UPLOAD_SERVLET_NAME;
			
			_RECORD_FILE_UPLOAD_URL = _URL + RECORD_FILE_UPLOAD_SERVLET_NAME;
			_RECORD_FILE_DOWNLOAD_URL = _URL + RECORD_FILE_DOWNLOAD_SERVLET_NAME;
			_RECORD_FILE_DELETE_URL = _URL + RECORD_FILE_DELETE_SERVLET_NAME;
			
			_DOWNLOAD_EXPORTED_DATA_URL = _URL + DOWNLOAD_EXPORTED_DATA_SERVLET_NAME;
			_DOWNLOAD_BACKUP_DATA_URL = _URL + DOWNLOAD_BACKUP_SERVLET_NAME;
			_DESIGNER_URL = _URL + _DESIGNER_URL_PART;
		}

	}
}