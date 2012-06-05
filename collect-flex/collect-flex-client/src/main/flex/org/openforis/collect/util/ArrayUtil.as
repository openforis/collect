package org.openforis.collect.util
{
	import mx.collections.ArrayCollection;
	import mx.collections.ICollectionView;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.collections.XMLListCollection;
	import mx.utils.ArrayUtil;
	
	/**
	 * 
	 * @author S. Ricci
	 * 
	 **/
	public class ArrayUtil
	{
		public static function getArray(array:Object):Array {
			if(array == null)
				return null;
			if(array is Array)
				return array as Array;
			if(array is ICollectionView) {
				var list:ICollectionView = array as ICollectionView;
				var result:Array = new Array(list.length);
				for(var index:int = 0; index < list.length; index ++) {
					result[index] = list[index];
				}
				return result;
			} else return null;
		}
		
		public static function isEmpty(array:Array):Boolean {
			return array == null || array.length == 0;
		}
		
		public static function isNotEmpty(array:Array):Boolean {
			return ! isEmpty(array);
		}
		
		public static function contains(array:Object, value:*):Boolean {
			if(array == null)
				return false;
			
			if(array is Array) {
				return (array as Array).indexOf(value) >= 0;
			}
			if(array is ICollectionView) {
				return (array as ICollectionView).contains(value);
			}
			return false;
		}
		
		public static function moveItem(array:Object, indexFrom:int, indexTo:int):int {
			var newIndex:int;
			if(array is IList) {
				newIndex = moveItemInList(array as IList, indexFrom, indexTo);
			} else if(array is Array) {
				newIndex = moveItemInArray(array as Array, indexFrom, indexTo);
			} else {
				throw new TypeError("moveItem: type of array object not supported.");
			}
			return newIndex;
		}
		
		private static function moveItemInList(list:IList, indexFrom:int, indexTo:int):int {
			if(indexFrom == indexTo) {
				//swap is useless, do nothing
				return indexFrom;
			}
			var item:Object = list.removeItemAt(indexFrom);

			var newIndex:Number;
			if(indexFrom < indexTo) {
				newIndex = indexTo - 1; //because of item removal before indexTo
			} else {
				newIndex = indexTo;
			}
			
			list.addItemAt(item, newIndex);
			
			return newIndex;
		}
		
		private static function moveItemInArray(array:Array, indexFrom:int, indexTo:int):int {
			var item:Object = array[indexFrom];
			var arrayPart1:Array = array.slice(0, indexFrom);
			//remove element in indexFrom
			array.splice(indexFrom, 1);
			array.splice(indexTo, 0, item);
			
			return indexTo;
		}
		
		public static function swapItems(array:Object, indexA:int, indexB:int):void {
			if(array is IList) {
				swapItemsInList(array as IList, indexA, indexB);
			} else if(array is Array) {
				swapItemsInArray(array as Array, indexA, indexB);
			} else {
				throw new TypeError("swapItems: type of array object not supported.");
			}
			
		}

		private static function swapItemsInList(list:IList, indexA:int, indexB:int):void {
			if(indexA == indexB) {
				//swap is useless, do nothing
				return;
			}
			var _indexA:int, _indexB:int;
				
			if(indexA > indexB) {
				_indexB = indexA;
				_indexA = indexB;
			} else {
				_indexA = indexA;
				_indexB = indexB;
			}
			var itemA:Object = list.removeItemAt(_indexA);
			var itemB:Object = list.removeItemAt(_indexB - 1);
			list.addItemAt(itemB, _indexA);
			list.addItemAt(itemA, _indexB);
		}
		
		private static function swapItemsInArray(array:Array, indexA:int, indexB:int):void {
			var itemA:Object = array[indexA];
			var itemB:Object = array[indexB];
			array[indexA] = array[indexB] = null;
			array[indexA] = itemB;
			array[indexB] = itemA;
		}
		
		/**
		 * Returns an array containing only the values of the property "propertyName" 
		 * of each item in the passed array
		 * 
		 * @param array Array or ICollectionView to iterate
		 * @param propertyName name of the property to include in the projection
		 * @return Array of values
		 **/
		public static function getProjection(array:Object, propertyName:String):Array {
			var arr:Array = getArray(array);
			if(arr != null) {
				var result:Array = [];
				for(var index:int = 0; index < arr.length; index ++) {
					var value:* = ObjectUtil.getValue(arr[index], propertyName);
					if(value != null) {
						result.push(value);
					}
				}
				return result;
			} else {
				return null;
			}
		}
		
		public static function removeItem(array:Array, item:Object):void {
			var result:Array;
			var indexOfItem:int = array.indexOf(item);
			if(indexOfItem >= 0) {
				array.splice(indexOfItem, 1);
			}
		}
		
		public static function getItem(array:Array, value:*, propertyName:String = null):* {
			for each(var item:* in array) {
				if(propertyName != null) {
					if(ObjectUtil.getValue(item, propertyName) == value) {
						return item;
					} else if(item == value) {
						return item;
					}
				}
			}
			return null;
		}
		
		public static function isIn(array:Array, value:*):Boolean {
			if(value == null) {
				return false;
			}
			for each(var item:* in array) {
				if(item == value) {
					return true;
				}
			}
			return false;
		}
		
		public static function isNotIn(array:Array, value:*):Boolean {
			return ! isIn(array, value);
		}
		
		/**
		 * Similar to the concat method of array but makes side effect on the source array
		 */
		public static function addAll(array:Array, values:Array):void {
			for each (var value:* in values) {
				array.push(value);
			}
			
		}
	}
}