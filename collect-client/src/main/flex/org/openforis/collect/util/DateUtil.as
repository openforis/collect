package org.openforis.collect.util {
	import mx.utils.ObjectUtil;
	
	/**
	 * Utility class
	 */
	public class DateUtil {
		
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
		
	}
}