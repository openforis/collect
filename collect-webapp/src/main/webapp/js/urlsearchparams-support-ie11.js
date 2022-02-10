/**
 * Adds support to URLSearchParams in Internet Explorer 11
 */ 
(function(w) {
	w.URLSearchParams = w.URLSearchParams || function(searchString) {
		var self = this;
		self.searchString = searchString;
		self.get = function(name) {
			var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(self.searchString);
			if (results == null) {
				return null;
			} else {
				return decodeURI(results[1]) || 0;
			}
		};
	}
})(window)