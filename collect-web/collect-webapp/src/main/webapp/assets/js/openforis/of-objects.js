OF.Objects = function() {};

OF.Objects.getProperty = function(item, propertyName) {
	return item.hasOwnProperty(propertyName) ? item[propertyName] : null;
};

OF.Objects.defaultIfNull = function(value, defaultValue) {
	return value == null || typeof value  == 'undefined' ? defaultValue: value; 
}
