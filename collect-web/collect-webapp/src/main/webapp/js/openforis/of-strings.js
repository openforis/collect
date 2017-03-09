OF.Strings = function() {};

OF.Strings.isBlank = function(value) {
	return value == null || value.trim().length == 0;
};

OF.Strings.isNotBlank = function(value) {
	return ! OF.Strings.isBlank(value);
};

OF.Strings.firstNotBlank = function() {
	var values = arguments;
	for(var i=0; i < values.length; i++) {
		var value = values[i];
		if (OF.Strings.isNotBlank(value)) {
			return value;
		}
	}
	return null;
};

OF.Strings.escapeHtml = function(str) {
	return $('<div>').text(str).html();
};

OF.Strings.leftPad = function(str, pad, length) {
	while (str.length < length) {
		str = pad + str;
	}
	return str;
}

OF.Strings.rightPad = function(str, pad, length) {
	while (str.length < length) {
		str = str + pad;
	}
	return str;
}

OF.Strings.format = function(format) {
    var args = Array.prototype.slice.call(arguments, 1);
	return format.replace(/{(\d+)}/g, function(match, number) {
		return typeof args[number] != 'undefined' ? args[number] : match;
	});
};

OF.Strings.hashCode = function(s) {
	var hash = 0;
	for (var i = 0; i < s.length; i++) {
		hash = s.charCodeAt(i) + ((hash << 5) - hash);
	}
	return hash;
}