package org.openforis.collect.util {
	import mx.utils.ObjectUtil;
	
	import spark.formatters.DateTimeFormatter;
	
	/**
	 * Utility class
	 */
	public class DateUtil {
		
		private static var _dateTimeFormatter:DateTimeFormatter;
		private static var _xmlDateTimeFormatter:DateTimeFormatter;
		
		//initialize static variables
		{
			_dateTimeFormatter = new DateTimeFormatter();
			_dateTimeFormatter.dateTimePattern = ApplicationConstants.DATE_TIME_PATTERN;
			_xmlDateTimeFormatter = new DateTimeFormatter();
			_xmlDateTimeFormatter.dateTimePattern = ApplicationConstants.XML_DATE_TIME_PATTERN;
		}
		
		public static function compareDates(date1:Date, date2:Date, ignoreTime:Boolean = true):int {
			if ( date1 == null && date2 == null ) {
				return 0;
			} else if ( date1 == null ) {
				return 1;
			} else if ( date2 == null ) {
				return -1;
			} else if ( ignoreTime ) {
				var c:int = mx.utils.ObjectUtil.numericCompare(date1.fullYear, date2.fullYear);
				if ( c != 0 ) {
					return c;
				} else {
					c = mx.utils.ObjectUtil.numericCompare(date1.month, date2.month);
					if ( c != 0 ) {
						return c;
					} else {
						c = mx.utils.ObjectUtil.numericCompare(date1.day, date2.day);
						return c;
					}
				}
			} else {
				return mx.utils.ObjectUtil.dateCompare(date1, date2);
			}
		}
		
		public static function format(date:Date):String {
			var result:String = _dateTimeFormatter.format(date);
			return result;
		}
		
		public static function formatToXML(date:Date):String {
			var result:String = _xmlDateTimeFormatter.format(date);
			return result;
		}
		
	}
}