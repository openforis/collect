package org.openforis.collect.util
{
	import mx.collections.IList;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 */
	public class CollectionUtil {
		
		public static function isEmpty(list:IList):Boolean {
			return list == null || list.length == 0;
		}
		
		public static function isNotEmpty(list:IList):Boolean {
			return ! isEmpty(list);
		}
		
		public static function addAll(list:IList, addList:IList):void {
			for each(var item:Object in addList) {
				list.addItem(item);
			}
		}
		
		public static function getItem(list:IList, propertyName:String, value:Object):Object {
			for each(var item:Object in list) {
				if(ObjectUtil.getValue(item, propertyName) == value) {
					return item;
				}
			}
			return null;
		}
		
	}
}