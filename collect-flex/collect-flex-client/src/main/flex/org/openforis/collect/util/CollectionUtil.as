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
		
		public static function getItem(list:IList, propertyName:String, value:Object):* {
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
		
		public static function contains(list:IList, item:Object, keyProperty:String = null):Boolean {
			if ( keyProperty == null ) {
				var idx:int = list.getItemIndex(item);
				return idx >= 0;
			} else {
				var keyValue:* = ObjectUtil.getValue(item, keyProperty);
				if ( keyValue != null ) {
					var item:Object = getItem(list, keyProperty, keyValue);
					return item != null;
				} else {
					return false;
				}
			}
		}
		
		public static function containsItemWith(list:IList, propertyName:String, value:*):Boolean {
			var item:Object = getItem(list, propertyName, value);
			return item != null;
		}
		
		public static function merge(oldCollection:IList, newCollection:IList, keyProperty:String = null):void {
			for each (var newItem:* in newCollection) {
				if ( ! contains(oldCollection, newItem, keyProperty) ) {
					oldCollection.addItem(newItem);
				}
			}
			for each (var oldItem:* in oldCollection) {
				if ( ! contains(newCollection, oldItem, keyProperty) ) {
					removeItem(oldCollection, oldItem);
				}
			}
			
		}
	}
}