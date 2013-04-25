package org.openforis.collect.util
{
	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class MathUtil {
		
		public static function module(x:Number, n:Number):Number {
			var result:Number = x % n;
			if ( result < 0 ) {
				result += n;
			}
			return result;
		}
		
	}
}