OF.Arrays = function() {};

/**
 * Removes the specified item from the specified array, if found
 */
OF.Arrays.removeItem = function(array, item) {
	var index = array.indexOf(item);
	if ( index >= 0 ) {
		array.splice(index, 1);
	}
};

/**
 * Returns true if the array contains the specified item 
 */
OF.Arrays.contains = function(array, item) {
	return array.indexOf(item) >= 0;
};

/**
 * Returns the item that has the specified property equals to the specified value 
 */
OF.Arrays.findItem = function(array, propertyName, value) {
	for(var i=0; i < array.length; i++) {
		var item = array[i];
		if ( item.hasOwnProperty(propertyName) ) {
			var itemValue = item[propertyName];
			if ( itemValue == value ) {
				return item;
			}
		}
	}
	return null;
};

/**
 * Returns a copy of the given array
 */
OF.Arrays.clone = function(array) {
	return array.slice(0);
};

/**
 * Changes the item position in the given array
 */
OF.Arrays.shiftItem = function ( array, item, toIndex ) {
	var oldIndex = array.indexOf(item);
	
	if ( oldIndex < 0 ) {
		throw new Error( "Item not found" );
	} else if ( toIndex < 0 && toIndex >= array.length ) {
		throw new Error( "Index out of bounds: " + toIndex + " (array length = " + array.length + ")" );
	} else {
		//remove item in the old position
		array.splice(oldIndex, 1);
		//add item in the new position
		array.splice(toIndex, 0, item);
	}
};

/**
 * Returns the head (first element) of the given array.
 */
OF.Arrays.head = function (array) {
	return array && array.length > 0 ? array[0] : null
}
