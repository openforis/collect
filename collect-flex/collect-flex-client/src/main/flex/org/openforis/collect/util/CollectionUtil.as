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
			var oldIndex:int = removeItem(list, item);
			var index:int;
			if(oldIndex > newIndex) {
				index = newIndex;
			} else if(newIndex > 0) {
				index = newIndex - 1;
			} else {
				index = 0;
			}
			list.addItemAt(item, index);
		}
		
	}
}