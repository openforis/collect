package org.openforis.collect.util
{
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	
	import org.granite.collections.IMap;

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

		public static function getItemIndex(list:IList, propertyName:String, value:Object):int {
			for(var index:int = 0; index < list.length; index ++) {
				var item:Object = list.getItemAt(index);
				if(ObjectUtil.getValue(item, propertyName) == value) {
					return index;
				}
			}
			return -1;
		}
	
		public static function removeItem(list:IList, item:Object):int {
			var index:int = list.getItemIndex(item);
			if(index >= 0) {
				list.removeItemAt(index);
			}
			return index;
		}
		
		public static function moveItem(list:IList, item:Object, newIndex:int):void {
			removeItem(list, item);
			list.addItemAt(item, newIndex);
		}
		
		public static function contains(list:IList, item:Object):Boolean {
			var idx:int = list.getItemIndex(item);
			return idx >= 0;
		}
		
		public static function containsItemWith(list:IList, propertyName:String, value:*):Boolean {
			var item:Object = getItem(list, propertyName, value);
			return item != null;
		}
		
	}
}