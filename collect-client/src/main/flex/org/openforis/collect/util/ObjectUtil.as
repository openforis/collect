package org.openforis.collect.util
{
	/**
	 * @author S. Ricci
	 */
	public class ObjectUtil
	{
		
		/**
		 * returns the value of a property (even if it is nested)
		 */
		public static function getValue(item:Object, propertyName:String):* {
			if(item != null) {
				var indexOfDot:int = propertyName.indexOf(".");
				if(indexOfDot > 0) {
					var mainProp:String = propertyName.substr(0, indexOfDot);
					if(item.hasOwnProperty(mainProp)) {
						var mainObj:Object = item[mainProp];
						var subProp:String = propertyName.substr(indexOfDot + 1);
						return getValue(mainObj, subProp);
					}
				} else if(item.hasOwnProperty(propertyName)) {
					return item[propertyName];
				}
			}
			return null;
		}
		
		public static function isNull(item:Object):Boolean {
			return (item is Number && isNaN(Number(item))) || item == null;
		}

		public static function isNotNull(item:Object):Boolean {
			return ! isNull(item);
		}
		
		public static function isNumber(value:Object):Boolean {
			return ! isNaN(Number(value));
		}
		
		public static function toNumber(item:Object):Number {
			var result:Number = Number(item);
			return result;
		}
		
		public static function defaultIfNull(value:Object, defaultValue:Object):* {
			return value == null ? defaultValue : value;
		}
	}
}