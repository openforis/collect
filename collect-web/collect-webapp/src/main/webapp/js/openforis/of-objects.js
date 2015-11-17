OF.Objects = function() {};

OF.Objects.getProperty = function(item, propertyName) {
	return item.hasOwnProperty(propertyName) ? item[propertyName] : null;
};
