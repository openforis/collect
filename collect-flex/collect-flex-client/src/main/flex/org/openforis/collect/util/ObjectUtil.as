package org.openforis.collect.util
{
	/**
	 * @author S. Ricci
	 */
	public class ObjectUtil
	{
		public static function getValue(item:Object, propertyName:String):* {
			if(item.hasOwnProperty(propertyName)) {
				return item[propertyName];
			} else {
				return null;
			}
		}
	}
}