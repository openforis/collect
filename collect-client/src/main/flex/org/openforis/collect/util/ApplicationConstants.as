package org.openforis.collect.util {
	import mx.core.FlexGlobals;
	import mx.utils.StringUtil;
	import mx.utils.URLUtil;

	import org.openforis.collect.CollectCompleteInfo;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class ApplicationConstants {
		
		//compiler constants
		public static const DEBUGGING:Boolean = CONFIG::debugging;
		public static const VERSION:String = CONFIG::version;
		
		public static const DOWNLOAD_LOGO_URL:String = "downloadLogo.htm";
		
		private static const SPECIES_IMPORT_EXAMPLE_DOWNLOAD_SERVLET_NAME:String = "species/import/example.htm";
		private static const _SPECIES_EXPORT_URL:String = "species/export/{0}";
		
		private static const _SAMPLING_DESIGN_EXPORT_URL:String = "survey/{0}/sampling-point-data.csv";
		private static const SAMPLING_DESIGN_IMPORT_EXAMPLE_DOWNLOAD_SERVLET_NAME:String = "samplingdesign/import/example.htm";

		private static const CODE_LIST_IMPORT_EXAMPLE_DOWNLOAD_SERVLET_NAME:String = "codelist/import/example.htm";
		
		private static const FILE_UPLOAD_SERVLET_NAME:String = "uploadFile.htm";

		private static const RECORD_FILE_DOWNLOAD_SERVLET_NAME:String = "downloadRecordFile.htm";
		private static const RECORD_FILE_DELETE_SERVLET_NAME:String = "deleteRecordFile.htm";
		
		private static const DOWNLOAD_EXPORTED_DATA_SERVLET_NAME:String = "downloadDataExport.htm";
		private static const DOWNLOAD_BACKUP_SERVLET_NAME:String = "downloadBackup.htm";
		private static const VALIDATION_REPORT_SERVLET_NAME:String = "validationReport";
		
		private static const _DESIGNER_URL_PART:String = "designer.htm";
		private static const _MAP_VISUALIZER_URL_PART:String = "datamanager/map.html";
		private static const _DATA_CLEANSING_URL_PART:String = "datacleansing/main.html";
		
		public static const DATE_TIME_PATTERN:String = "dd-MM-yyyy HH:mm";
		public static const XML_DATE_TIME_PATTERN:String = "yyyy-MM-dd\'T\'HH:mm:ss";

		public static const VALID_INTERNAL_NAME_REGEX:RegExp = /^[a-z][a-z0-9_]*$/;
		
		private static var _RECORD_FILE_DOWNLOAD_URL:String; 
		private static var _RECORD_FILE_DELETE_URL:String; 
		private static var _SPECIES_IMPORT_EXAMPLE_DOWNLOAD_URL:String;
		private static var _SAMPLING_DESIGN_IMPORT_EXAMPLE_DOWNLOAD_URL:String;
		private static var _FILE_UPLOAD_URL:String;
		private static var _DOWNLOAD_EXPORTED_DATA_URL:String;
		private static var _DOWNLOAD_BACKUP_DATA_URL:String;
		private static var _VALIDATION_REPORT_URL:String;
		private static var _CODE_LIST_IMPORT_EXAMPLE_DOWNLOAD_URL:String;
		private static var _DESIGNER_URL:String;
		private static var _DATA_CLEANSING_URL:String;
		private static var _MAP_VISUALIZER_URL:String;
		
		private static var _HOST:String;
		private static var _PORT:uint;
		private static var _ROOT_URL:String;
		private static var _URL:String;

		private static var _collectInfo:CollectCompleteInfo;
		
		{
			setUrl("http://localhost:8080/collect/collect.swf");
		}
		
		public static function init():void {
			var url:String = FlexGlobals.topLevelApplication.url;
			setUrl(url);
		}
		
		public static function getSpeciesExportUrl(taxonomyId:int):String {
			return mx.utils.StringUtil.substitute(_SPECIES_EXPORT_URL, taxonomyId);
		}

		public static function getSamplingDesignExportUrl(surveyId:int, work:Boolean = false):String {
			var baseUrl:String = _SAMPLING_DESIGN_EXPORT_URL;
			return mx.utils.StringUtil.substitute(baseUrl, surveyId);
		}

		public static function getSurveyDataRestoreUrl():String {
			return _URL + "surveys/data/restore.json";
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
		
		public static function get VALIDATION_REPORT_URL():String {
			return _VALIDATION_REPORT_URL;
		}
		
		public static function get CODE_LIST_IMPORT_EXAMPLE_DOWNLOAD_URL():String {
			return _CODE_LIST_IMPORT_EXAMPLE_DOWNLOAD_URL;
		}
		
		public static function get SAMPLING_DESIGN_IMPORT_EXAMPLE_DOWNLOAD_URL():String {
			return _SAMPLING_DESIGN_IMPORT_EXAMPLE_DOWNLOAD_URL;
		}
		
		public static function get SPECIES_IMPORT_EXAMPLE_DOWNLOAD_URL():String {
			return _SPECIES_IMPORT_EXAMPLE_DOWNLOAD_URL;
		}
		
		public static function get FILE_UPLOAD_URL():String {
			return _FILE_UPLOAD_URL;
		}
		
		public static function get MAP_VISUALIZER_URL():String {
			return _MAP_VISUALIZER_URL;
		}
		
		public static function get DESIGNER_URL():String {
			return _DESIGNER_URL;
		}
		
		public static function get DATA_CLEANSING_URL():String {
			return _DATA_CLEANSING_URL;
		}
		
		public static function get HOST():String {
			return _HOST;
		}
		
		public static function get PORT():uint {
			return _PORT;
		}
		
		public static function get ROOT_URL():String {
			return _ROOT_URL;
		}
		
		public static function get URL():String {
			return _URL;
		}

		public static function get collectInfo():CollectCompleteInfo {
			return _collectInfo;
		}

		public static function set collectInfo(value:CollectCompleteInfo):void {
			_collectInfo = value;
		}

		internal static function setUrl(applicationUrl:String):void {
			var protocol:String = URLUtil.getProtocol(applicationUrl);
			var urlPort:uint = URLUtil.getPort(applicationUrl);
			_PORT = urlPort == 0 ? 80 : urlPort;
			_HOST = URLUtil.getServerName(applicationUrl); 
			var originalRootUrl:String = protocol + "://"+ _HOST + (urlPort > 0 ? (":" + urlPort): "") + "/";
			var contextNameLenght:int = applicationUrl.indexOf("/", originalRootUrl.length) - originalRootUrl.length;
			var contextName:String = applicationUrl.substr(originalRootUrl.length, contextNameLenght);
			_ROOT_URL = protocol + "://"+ _HOST + ":" + _PORT + "/";
			
			_URL = _ROOT_URL + contextName + "/";
			
			_FILE_UPLOAD_URL = _URL + FILE_UPLOAD_SERVLET_NAME;
			
			_RECORD_FILE_DOWNLOAD_URL = _URL + RECORD_FILE_DOWNLOAD_SERVLET_NAME;
			_RECORD_FILE_DELETE_URL = _URL + RECORD_FILE_DELETE_SERVLET_NAME;
			
			_DOWNLOAD_EXPORTED_DATA_URL = _URL + DOWNLOAD_EXPORTED_DATA_SERVLET_NAME;
			_DOWNLOAD_BACKUP_DATA_URL = _URL + DOWNLOAD_BACKUP_SERVLET_NAME;
			_VALIDATION_REPORT_URL = _URL + VALIDATION_REPORT_SERVLET_NAME;
			_SPECIES_IMPORT_EXAMPLE_DOWNLOAD_URL = URL + SPECIES_IMPORT_EXAMPLE_DOWNLOAD_SERVLET_NAME;
			_SAMPLING_DESIGN_IMPORT_EXAMPLE_DOWNLOAD_URL = URL + SAMPLING_DESIGN_IMPORT_EXAMPLE_DOWNLOAD_SERVLET_NAME;
			_CODE_LIST_IMPORT_EXAMPLE_DOWNLOAD_URL = URL + CODE_LIST_IMPORT_EXAMPLE_DOWNLOAD_SERVLET_NAME;
			_DESIGNER_URL = _URL + _DESIGNER_URL_PART;
			_DATA_CLEANSING_URL = _URL + _DATA_CLEANSING_URL_PART;
			_MAP_VISUALIZER_URL = _URL + _MAP_VISUALIZER_URL_PART;
		}

	}
}