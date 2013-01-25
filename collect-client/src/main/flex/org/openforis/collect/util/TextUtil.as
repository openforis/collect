package org.openforis.collect.util
{
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class TextUtil {
		
		private static const INDENTING_WHITESPACE_PATTERN:RegExp = /\n[^\S\n\r]+/g;
		
		public static function trimLeadingWhitespace(str:String):String {
			var result:String = str.replace(INDENTING_WHITESPACE_PATTERN, "\n");
			return result;
		}
	}
}