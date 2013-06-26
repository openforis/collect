package org.openforis.collect.util
{
	import mx.utils.StringUtil;

	/**
	 * @author S. Ricci
	 */
	public class StringUtil	{
		
		public static function trim(value:String, trimChar:String = null):String {
			if(trimChar == null) {
				return mx.utils.StringUtil.trim(value);
			} else {
				var temp:String = leftTrim(value, trimChar);
				temp = rightTrim(temp, trimChar);
				return temp;
			}
		}
		
		public static function trimToNull(value:String, trimChar:String = null):String {
			var result:String = trim(value, trimChar);
			return result == '' ? null: result;
		}

		public static function leftTrim(value:String, trimChar:String = null):String {
			if(trimChar == null)
				trimChar = " ";
			var i:int = 0;
			while(i < value.length && value.charAt(i) == trimChar) {
				i++;
			}
			return value.substr(i);
		}  
		
		public static function rightTrim(value:String, trimChar:String = null):String {
			if(trimChar == null)
				trimChar = " ";
			var i:int = value.length - 1;
			while(i >= 0 && value.charAt(i) == trimChar) {
				i--;
			}
			return value.substr(0, i + 1);
		}  

		public static function replaceAll(string:String, regExp:RegExp, replace:String=""):String {
			var newString:String = new String(string);
			while(newString.search(regExp)>0){
				newString = newString.replace(regExp,replace);
			}
			return newString;
		}
		
		public static function isWhitespace(value:String):Boolean {
			return value == " ";
		}
		
		public static function isEmpty(value:String):Boolean {
			return value == null || value == "";
		}
		
		public static function isBlank(value:String):Boolean {
			return mx.utils.StringUtil.trim(value) == "";
		}
		
		public static function isNotBlank(value:String):Boolean {
			return ! isBlank(value);
		}
		
		public static function isNotEmpty(value:String):Boolean {
			return ! isEmpty(value);
		}
		
		public static function nullToBlank(value:Object):String {
			if(value == null || (value is Number && isNaN(value as Number))) {
				return "";
			} else {
				return value.toString();
			}
		}
		
		/**
		 * Concats two or more strings separating them with a specified separator.
		 * Only not null and not blank values are included in the final string.
		 * 
		 * @param separator Separator to use in the concat
		 * @param args Strings to concat or Array of String items
		 * @return concatenated values
		 * 
		 **/
		public static function concat(separator:String, ... args):String {
			var parts:Array;
			if(args != null && args.length == 1 && args[0] is Array) {
				parts = args[0];
			} else {
				parts = args;
			}
			var resultParts:Array = [];
			for each(var value:String in parts) {
				if(isNotBlank(value)) {
					resultParts.push(value);
				}
			}
			return resultParts.join(separator);
		}
		
		public static function concatEvenNulls(separator:String, ... args):String {
			var parts:Array = [];
			for each(var value:String in args) {
				if(isNotBlank(value)) {
					parts.push(value);
				} else {
					parts.push("");
				}
			}
			return parts.join(separator);
		}
		
		public static function startsWith(string:String, startsWith:String, ignoreCase:Boolean = false):Boolean {
			if ( string == null && startsWith == null ) {
				return true;
			} else if ( string == null ) {
				return false;
			} else {
				if ( ignoreCase ) {
					string = string.toLowerCase();
					startsWith = startsWith.toLowerCase();
				}
				return string.indexOf(startsWith) == 0;
			}
		}
		
		public static function pad(str:String, length:int, pad:String):String {
			var result:String = str;
			while ( result.length < length ) {
				result = pad + result;
			}
			return result;
		}
		
		public static function zeroPad(number:Number, length:int = 2):String {
			if(! isNaN(number)) {
				return pad(number.toString(), length, "0");
			} else {
				return "";
			}
		}
	}
}