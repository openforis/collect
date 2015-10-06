if (typeof OF == "undefined") {
	OF = {};
}

if (typeof OF.Collect == "undefined") {
	OF.Collect = function() {
		
	};
}

OF.Collect.COOKIE_KEY = "of.collect.cookie";

OF.Collect.Cookie = function() {
		
};

OF.Collect.getCookie = function() {
	var result = Cookies.get(OF.Collect.COOKIE_KEY);
	return result;
};

OF.Collect.initCookie = function() {
	var cookie = new OF.Collect.Cookie();
	Cookies.set(OF.Collect.COOKIE_KEY, cookie);
};